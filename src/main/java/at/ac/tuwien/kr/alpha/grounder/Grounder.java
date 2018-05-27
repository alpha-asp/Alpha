package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;
import java.util.Map;

public interface Grounder {
	/**
	 * Translates an answer-set represented by true atom IDs into its logical representation.
	 * @param trueAtoms
	 * @return
	 */
	AnswerSet assignmentToAnswerSet(Iterable<Integer> trueAtoms);

	/**
	 * Applies lazy grounding and returns all newly derived (fully ground) NoGoods.
	 * @return a mapping of nogood IDs to NoGoods.
	 */
	Map<Integer, NoGood> getNoGoods(Assignment assignment);

	/**
	 * Return choice points and their enablers and disablers.
	 * Must be preceeded by a call to getNoGoods().
	 * @return a pair (choiceOn, choiceOff) of two maps from atomIds to atomIds,
	 * choiceOn maps atoms (choice points) to their enabling atoms
	 * and choiceOff maps atoms (choice points) to their disabling atoms.
	 */
	Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms();

	void updateAssignment(Iterator<Assignment.Entry> it);

	void forgetAssignment(int[] atomIds);

	/**
	 * Registers the given NoGood and returns the identifier of it.
	 * @param noGood
	 * @return
	 */
	int register(NoGood noGood);
}
