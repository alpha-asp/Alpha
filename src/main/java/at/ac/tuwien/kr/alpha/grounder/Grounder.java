package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Predicate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public interface Grounder {
	AnswerSet assignmentToAnswerSet(java.util.function.Predicate<Predicate> filter, int[] trueAtoms);

	Map<Integer, NoGood> getNoGoods();

	/**
	 *
	 * @return a pair (choiceOn, choiceOff) of two maps from atomIds to atomIds,
	 * choiceOn maps enabling atomIds to enabled atomIds to guess on, while
	 * choiceOff maps disabling atomIds to guessable atomIds.
	 */
	Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms();

	void updateAssignment(int[] atomIds, boolean[] truthValues);

	void forgetAssignment(int[] atomIds);

	// int[] getObsoleteAtomIds()
}
