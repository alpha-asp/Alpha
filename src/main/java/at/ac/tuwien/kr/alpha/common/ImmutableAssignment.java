package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

import java.util.Iterator;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

public interface ImmutableAssignment {
	/**
	 * Reports how many atoms are assigned to must-be-true currently. If this method returns
	 * zero, the assignment is guaranteed to be free of must-be-true values (i.e. it only
	 * contains assignments to either true or false).
	 * @return the count of must-be-true values in the asignment.
	 */
	int getMBTCount();

	/**
	 * Returns all atomIds that are assigned TRUE in the current assignment.
	 * @return a list of all true assigned atoms.
	 */
	Set<Integer> getTrueAssignments();

	Entry get(int atom);

	int getDecisionLevel();

	/**
	 * Returns the truth value assigned to an atom.
	 * @param atom the id of the atom.
	 * @return the truth value; null if atomId is not assigned.
	 */
	default ThriceTruth getTruth(int atom) {
		final Entry entry = get(atom);
		return entry == null ? null : entry.getTruth();
	}

	default boolean isAssigned(int atom) {
		return get(atom) != null;
	}

	default boolean containsRelaxed(int literal) {
		final Entry entry = get(atomOf(literal));
		return entry != null && isNegated(literal) == !entry.getTruth().toBoolean();
	}

	default boolean containsRelaxed(NoGood noGood, int index) {
		return containsRelaxed(noGood.getLiteral(index));
	}

	default boolean contains(int literal) {
		final Entry entry = get(atomOf(literal));
		return entry != null && (isNegated(literal) ? FALSE : TRUE).equals(entry.getTruth());
	}

	default boolean contains(NoGood noGood, int index) {
		return contains(noGood.getLiteral(index));
	}

	Iterator<OrdinaryAssignment> ordinaryIterator();

	interface Entry {
		ThriceTruth getTruth();
		int getDecisionLevel();
		Entry getPrevious();
		NoGood getImpliedBy();

		int getAtom();

		int getPropagationLevel();
	}
}
