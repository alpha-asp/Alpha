package at.ac.tuwien.kr.alpha.grounder.bridges;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.ReadableAssignment;
import at.ac.tuwien.kr.alpha.grounder.AtomStore;

import java.util.Collection;

public interface Bridge {
	Collection<NoGood> getNoGoods(ReadableAssignment assignment, AtomStore atomStore);
}
