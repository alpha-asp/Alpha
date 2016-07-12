package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.AnswerSet;
import at.ac.tuwien.kr.alpha.NoGood;

import java.util.Collection;

public interface Grounder {
	Collection<NoGood> getNoGoods(int[] ids, boolean[] truthValues);
	AnswerSet translate(int[] trueAtoms);
}
