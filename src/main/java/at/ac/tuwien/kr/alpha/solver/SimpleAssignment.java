package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;

import java.util.Set;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;

public interface SimpleAssignment {
	boolean isAssigned(int atom);
	ThriceTruth getTruth(int atom);

	default boolean isViolated(int literal) {
		final int atom = atomOf(literal);
		final ThriceTruth truth = getTruth(atom);

		// For unassigned atoms, any literal is not violated.
		if (truth == null) {
			return false;
		}

		return isNegated(literal) != truth.toBoolean();
	}

	default boolean violates(NoGood noGood) {
		// Check each NoGood, if it is violated
		for (Integer noGoodLiteral : noGood) {
			if (!isAssigned(atomOf(noGoodLiteral)) || !isViolated(noGoodLiteral)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns all atomIds that are assigned TRUE in the current assignment.
	 * @return a list of all true assigned atoms.
	 */
	Set<Integer> getTrueAssignments();

	interface Entry {
		int getAtom();
		ThriceTruth getTruth();
	}
}
