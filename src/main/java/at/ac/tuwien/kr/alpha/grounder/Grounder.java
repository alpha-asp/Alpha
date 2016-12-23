package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.solver.Assignment;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface Grounder extends AtomTranslator {
	AnswerSet assignmentToAnswerSet(Iterable<Integer> trueAtoms);

	Map<Integer, NoGood> getNoGoods(ImmutableAssignment assignment);

	/**
	 *
	 * @return a pair (choiceOn, choiceOff) of two maps from atomIds to atomIds,
	 * choiceOn maps enabling atomIds to enabled atomIds to guess on, while
	 * choiceOff maps disabling atomIds to guessable atomIds.
	 */
	Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms();

	void updateAssignment(Iterator<Assignment.Entry> it);

	void forgetAssignment(int[] atomIds);

	// int[] getObsoleteAtomIds()

	/**
	 * Returns a list of currently known but unassigned.
	 * @param assignment the current assignment.
	 * @return a list of atoms not having assigned a truth value.
	 */
	List<Integer> getUnassignedAtoms(ImmutableAssignment assignment);

	/**
	 * Registers the given NoGood and returns the identifier of it.
	 * @param noGood
	 * @return
	 */
	int registerOutsideNoGood(NoGood noGood);

	/**
	 * Returns true whenever the atom is a valid choice point (i.e., it represents a rule body).
	 * @param atom
	 * @return
	 */
	boolean isAtomChoicePoint(int atom);
}
