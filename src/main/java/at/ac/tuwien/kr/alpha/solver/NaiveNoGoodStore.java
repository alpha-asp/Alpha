package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static at.ac.tuwien.kr.alpha.common.Literals.*;
import static at.ac.tuwien.kr.alpha.common.NoGood.HEAD;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;

public class NaiveNoGoodStore implements NoGoodStore {
	private static final Logger LOGGER = LoggerFactory.getLogger(NaiveNoGoodStore.class);

	private HashMap<Integer, NoGood> delegate = new HashMap<>();
	private final WritableAssignment assignment;

	private boolean hasInferredAssignments;

	public NaiveNoGoodStore(WritableAssignment assignment) {
		this.assignment = assignment;
	}

	void clear() {
		assignment.clear();
		delegate.clear();
	}

	@Override
	public ConflictCause add(int id, NoGood noGood) {
		if (assignment.violates(noGood)) {
			return new ConflictCause(noGood);
		}

		delegate.put(id, noGood);
		return null;
	}

	@Override
	public ConflictCause propagate() {
		hasInferredAssignments = false;

		boolean any = false;
		boolean retry;

		do {
			retry = false;
			ConflictCause conflictCause;
			for (NoGood noGood : delegate.values()) {
				hasInferredAssignments = false;
				conflictCause = propagateWeakly(noGood);
				if (conflictCause != null) {
					return conflictCause;
				}
				if (hasInferredAssignments) {
					any = true;
					hasInferredAssignments = false;
					retry = true;
				}
			}
			for (NoGood noGood : delegate.values()) {
				hasInferredAssignments = false;
				conflictCause = propagateStrongly(noGood);
				if (conflictCause != null) {
					return conflictCause;
				}
				if (hasInferredAssignments) {
					any = true;
					hasInferredAssignments = false;
					retry = true;
				}
			}
		} while (retry);

		for (NoGood noGood : delegate.values()) {
			if (assignment.violates(noGood)) {
				return new ConflictCause(noGood);
			}
		}

		if (any) {
			hasInferredAssignments = true;
		}

		return null;
	}

	@Override
	public boolean didPropagate() {
		return hasInferredAssignments;
	}

	@Override
	public void backtrack() {
		assignment.backtrack();
	}

	/**
	 * Infer an assignment from a nogood if it is weakly unit.
	 *
	 * This method iterates over all literals in the given nogood
	 * in order to check whether it is weakly unit. If the nogood
	 * turns out to be unit, then an assignment is generated and
	 * {@code true} is returned. Otherwise, {@code false} is
	 * returned.
	 *
	 * @param noGood the nogood to analyze.
	 * @return {@code true} iff an assignment was inferred,
	 *         {@code false} otherwise.
	 */
	private ConflictCause propagateWeakly(NoGood noGood) {
		int index = -1;
		for (int i = 0; i < noGood.size(); i++) {
			final int literal = noGood.getLiteral(i);

			if (assignment.isAssigned(atomOf(literal))) {
				if (!assignment.isViolated(literal)) {
					// Literal is satisfied!
					return null;
				}
			} else if (index != -1) {
				// There is more than one unassigned literal!
				return null;
			} else {
				index = i;
			}
		}

		if (index == -1) {
			return null;
		}

		hasInferredAssignments = true;

		final int literal = noGood.getLiteral(index);
		return assignment.assign(atomOf(literal), isNegated(literal) ? MBT : FALSE, noGood);
	}

	/**
	 * Infer an assignment from a nogood if it is strongly unit.
	 *
	 * This method iterates over all literals in the given nogood
	 * in order to check whether it is strongly unit. If the nogood
	 * turns out to be unit, then an assignment for the head is
	 * generated and {@code true} is returned. Otherwise,
	 * {@code false} is returned.
	 *
	 * @param noGood the nogood to analyze.
	 * @return {@code true} iff an assignment was inferred,
	 *         {@code false} otherwise.
	 */
	private ConflictCause propagateStrongly(NoGood noGood) {
		if (!noGood.hasHead()) {
			return null;
		}

		final int head = noGood.getAtom(HEAD);

		if (assignment.getTruth(head) != MBT) {
			return null;
		}

		// Check that NoGood is violated except for the head
		// (i.e., without the head set it would be unit) and
		// that none of the true values are assigned MBT, but
		// instead are all TRUE.
		for (int i = 1; i < noGood.size(); i++) {
			final int literal = noGood.getLiteral(i);

			if (!assignment.isViolated(literal)) {
				return null;
			}

			// Skip if positive literal is assigned MBT.
			if (isPositive(literal) && assignment.getTruth(atomOf(literal)) != TRUE) {
				return null;
			}
		}

		hasInferredAssignments = true;

		return assignment.assign(head, TRUE, noGood);
	}
}
