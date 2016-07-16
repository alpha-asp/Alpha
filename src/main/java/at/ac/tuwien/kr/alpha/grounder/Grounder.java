package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.AnswerSet;
import at.ac.tuwien.kr.alpha.NoGood;

import java.util.Map;
import java.util.function.Predicate;

public interface Grounder {
	AnswerSet assignmentToAnswerSet(Predicate<GrounderPredicate> filter, int[] trueAtoms);

	Map<Integer, NoGood> getNoGoods();

	void updateAssignment(int[] atomIds, boolean[] truthValues);

	void forgetAssignment(int[] atomIds);
}