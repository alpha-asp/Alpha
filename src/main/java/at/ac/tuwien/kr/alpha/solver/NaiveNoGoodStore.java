package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;
import static at.ac.tuwien.kr.alpha.common.Literals.isPositive;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

public class NaiveNoGoodStore implements NoGoodStore {
	private static final Logger LOGGER = LoggerFactory.getLogger(NaiveNoGoodStore.class);

	private HashMap<Integer, NoGood> delegate = new HashMap<>();
	private final WritableAssignment assignment;

	public NaiveNoGoodStore(WritableAssignment assignment) {
		this.assignment = assignment;
	}

	@Override
	public void clear() {
		assignment.clear();
		delegate.clear();
	}

	@Override
	public ConflictCause add(int id, NoGood noGood) {
		if (assignment.violates(noGood)) {
			return new ConflictCause(noGood, null);
		}

		delegate.put(id, noGood);
		return null;
	}

	@Override
	public NoGood getViolatedNoGood() {
		// Check each NoGood, if it is violated
		for (NoGood noGood : delegate.values()) {
			if (assignment.violates(noGood)) {
				LOGGER.trace("Violated NoGood: {}", noGood);
				return noGood;
			}
		}
		return null;
	}

	@Override
	public boolean propagate() {
		boolean retry = false;
		boolean result = false;
		do {
			retry = false;
			if (doUnitPropagation()) {
				result = true;
				retry = true;
			}
			if (doMBTPropagation()) {
				result = true;
				retry = true;
			}
		} while (retry);
		return result;
	}

	@Override
	public void backtrack() {
		assignment.backtrack();
	}

	@Override
	public void enableInternalChecks() {

	}

	@Override
	public int size() {
		return delegate.size();
	}


	@Override
	public Iterator<NoGood> iterator() {
		return delegate.values().iterator();
	}

	private boolean doMBTPropagation() {
		boolean result = false;
		boolean didPropagate = true;
		while (didPropagate) {
			didPropagate = false;
			for (Map.Entry<Integer, NoGood> noGoodEntry : delegate.entrySet()) {
				if (propagateMBT(noGoodEntry.getValue())) {
					didPropagate = true;
					result = true;
				}
			}
		}
		return result;
	}

	private boolean doUnitPropagation() {
		boolean result = false;
		// Check each NoGood if it is unit (naive algorithm)
		for (NoGood noGood : delegate.values()) {
			int implied = unitPropagate(noGood);
			if (implied == -1) {	// NoGood is not unit, skip.
				continue;
			}
			int impliedLiteral = noGood.getLiteral(implied);
			int impliedAtomId = atomOf(impliedLiteral);
			boolean impliedTruthValue = isNegated(impliedLiteral);
			if (assignment.isAssigned(impliedAtomId)) {	// Skip if value already was assigned.
				continue;
			}
			assignment.assign(impliedAtomId, impliedTruthValue ? MBT : FALSE, noGood);
			result = true;
		}
		return result;
	}

	/**
	 * Returns position of implied literal if input NoGood is unit.
	 * @param noGood
	 * @return -1 if NoGood is not unit.
	 */
	private int unitPropagate(NoGood noGood) {
		int lastUnassignedPosition = -1;
		for (int i = 0; i < noGood.size(); i++) {
			int literal = noGood.getLiteral(i);
			if (assignment.isAssigned(atomOf(literal))) {
				if (!assignment.isViolated(literal)) {
					// The NoGood is satisfied, hence it cannot be unit.
					return -1;
				}
			} else if (lastUnassignedPosition != -1) {
				// NoGood is not unit, if there is not exactly one unassigned literal
				return -1;
			} else {
				lastUnassignedPosition = i;
			}
		}
		return lastUnassignedPosition;
	}

	private boolean propagateMBT(NoGood noGood) {
		// The MBT propagation checks whether the head-indicated literal is MBT
		// and the remaining literals are violated
		// and none of them are MBT,
		// then the head literal is set from MBT to true.

		if (!noGood.hasHead()) {
			return false;
		}

		int headAtom = noGood.getAtom(noGood.getHead());

		// Check whether head is assigned MBT.
		if (!assignment.isMBT(headAtom)) {
			return false;
		}

		// Check that NoGood is violated except for the head (i.e., without the head set it would be unit)
		// and that none of the true values is MBT.
		for (int i = 0; i < noGood.size(); i++) {
			if (noGood.getHead() == i) {
				continue;
			}
			int literal = noGood.getLiteral(i);
			if (!(assignment.isAssigned(atomOf(literal)) && assignment.isViolated(literal))) {
				return false;
			}

			// Skip if positive literal is assigned MBT.
			if (isPositive(literal) && assignment.getTruth(atomOf(literal)) == MBT) {
				return false;
			}
		}

		// Set truth value from MBT to true.
		assignment.assign(headAtom, TRUE, noGood);
		return true;
	}
}
