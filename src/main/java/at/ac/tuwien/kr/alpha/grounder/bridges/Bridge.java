package at.ac.tuwien.kr.alpha.grounder.bridges;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.AtomStore;
import at.ac.tuwien.kr.alpha.solver.Assignment;

import java.util.Collection;

public interface Bridge {
	Collection<NoGood> getNoGoods(Assignment assignment, AtomStore atomStore);
}
