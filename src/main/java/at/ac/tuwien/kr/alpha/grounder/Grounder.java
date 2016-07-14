package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.AnswerSet;
import at.ac.tuwien.kr.alpha.AnswerSetFilter;
import at.ac.tuwien.kr.alpha.NoGood;

import java.util.Map;

public interface Grounder {
	AnswerSet assignmentToAnswerSet(AnswerSetFilter filter, int[] trueAtoms);

	Map<Integer, NoGood> getNoGoods();

	void updateAssignment(int[] atomIds, boolean[] truthValues);

	void forgetAssignment(int[] atomIds);
}