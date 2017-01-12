package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.AtomTranslator;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.ReadableAssignment;
import at.ac.tuwien.kr.alpha.solver.Assignment;
import at.ac.tuwien.kr.alpha.solver.Choices;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface Grounder extends AtomTranslator {
	AnswerSet assignmentToAnswerSet(Iterable<Integer> trueAtoms);

	Map<Integer, NoGood> getNoGoods(ReadableAssignment assignment);

	/**
	 * @return a pair (choiceOn, choiceOff) of two maps from atomIds to atomIds,
	 * choiceOn maps enabling atomIds to enabled atomIds to guess on, while
	 * choiceOff maps disabling atomIds to guessable atomIds.
	 */
	Choices getChoices();

	void updateAssignment(Iterator<? extends Assignment.Entry> it);

	void forgetAssignment(int[] atomIds);

	// int[] getObsoleteAtomIds()

	/**
	 * Returns a list of currently known but unassigned.
	 * @param assignment the current assignment.
	 * @return a list of atoms not having assigned a truth value.
	 */
	List<Integer> getUnassignedAtoms(ReadableAssignment assignment);

	/**
	 * Registers the given NoGood and returns the identifier of it.
	 * @param noGood
	 * @return
	 */
	int registerOutsideNoGood(NoGood noGood);
}
