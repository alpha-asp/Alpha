package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.solver.SimpleAssignment;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.WatchedNoGood;

import java.util.Iterator;
import java.util.Queue;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

public interface Assignment extends SimpleAssignment {
	Entry get(int atom);

	/**
	 * In case that assign fails (i.e., it returns false) the NoGood violated by the assignment can be obtained by this method.
	 * The returned value is arbitrary if the previous assign did not fail.
	 * @return
	 */
	NoGood getNoGoodViolatedByAssign();

	Assignment.Entry getGuessViolatedByAssign();

	void growForMaxAtomId(int maxAtomId);

	/**
	 * Returns an iterator over all new assignments. New assignments are only returned once.
	 * getNewAssignmentsIterator and getNewAssignmentsIterator2 are independent of each other (i.e., each has its own backing collection).
	 * @return
	 */
	Iterator<Entry> getNewAssignmentsIterator();

	/**
	 * Helper for debugging: prints the given NoGood with its assignment inlined.
	 * @param noGood the nogood to print.
	 * @return a string of the nogood with the current assignment (inlined).
	 */
	default String printNoGoodAssignment(NoGood noGood) {
		StringBuilder sb = new StringBuilder();

		sb.append("{ ");

		for (int i = 0; i < noGood.size(); i++) {
			sb.append(noGood.getLiteral(i));
			sb.append("=");
			sb.append(this.get(atomOf(noGood.getLiteral(i))));
			sb.append(" ");
		}

		sb.append("}");

		if (noGood.hasHead()) {
			sb.append("[");
			sb.append(noGood.getHead());
			sb.append("]");
		}

		if (noGood instanceof WatchedNoGood) {
			WatchedNoGood watchedNoGood = (WatchedNoGood) noGood;
			sb.append("{ " + watchedNoGood.getPointer(0) + " " + watchedNoGood.getPointer(1) + " " + watchedNoGood.getAlphaPointer() + " }");
		}

		return sb.toString();
	}

	Queue<? extends Entry> getAssignmentsToProcess();


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

	default boolean contains(int literal) {
		final Entry entry = get(atomOf(literal));
		return entry != null && (isNegated(literal) ? FALSE : TRUE).equals(entry.getTruth());
	}

	default boolean containsWeakComplement(int literal) {
		final Entry entry = get(atomOf(literal));
		return entry != null && isNegated(literal) == !entry.getTruth().toBoolean();
	}

	default boolean containsWeakComplement(NoGood noGood, int index) {
		return containsWeakComplement(noGood.getLiteral(index));
	}

	default boolean contains(NoGood noGood, int index) {
		return contains(noGood.getLiteral(index));
	}


	/**
	 * Determines if the given {@code noGood} is violated in the current assignment.
	 * @param noGood
	 * @return {@code true} iff all literals in {@code noGood} evaluate to true in the current assignment.
	 */
	default boolean isViolated(NoGood noGood) {
		for (Integer literal : noGood) {
			if (!contains(literal)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines if the given {@code noGood} is undefined in the current assignment.
	 *
	 * @param noGood
	 * @return {@code true} iff at least one literal in {@code noGood} is unassigned.
	 */
	default boolean isUndefined(NoGood noGood) {
		for (Integer literal : noGood) {
			if (!isAssigned(atomOf(literal))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns an iterator over all new assignments and additionally all backtracked reassignments at lower decision level).
	 * These assignments are only returned once.
	 * @return
	 */
	Iterator<Entry> getNewAssignmentsForChoice();

	interface Entry extends SimpleAssignment.Entry {
		ThriceTruth getTruth();
		int getDecisionLevel();
		Entry getPrevious();
		NoGood getImpliedBy();

		int getAtom();

		int getPropagationLevel();
		boolean isReassignAtLowerDecisionLevel();

		/**
		 * Returns the literal corresponding to this assignment
		 * @return atomId if this entry is TRUE/MBT and -atomId if entry is FALSE.
		 */
		default int getLiteral() {
			return getTruth().toBoolean() ? getAtom() : -getAtom();
		}

		/**
		 * Returns the weakly assigned decision level.
		 * @return the decision level of a previous MBT if it exists, otherwise the decision level of this entry.
		 */
		default int getWeakDecisionLevel() {
			return getPrevious() != null ? getPrevious().getDecisionLevel() : getDecisionLevel();
		}

		/**
		 * Returns the strongly assigned decision level.
		 * @return the decision level of this entry if it is TRUE/FALSE and Integer.MAX_VALUE otherwise.
		 */
		default int getStrongDecisionLevel() {
			return getTruth().isMBT() ? Integer.MAX_VALUE : getDecisionLevel();
		}
	}
}
