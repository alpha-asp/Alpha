package at.ac.tuwien.kr.alpha.grounder.bridges;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.rule.impl.NormalRule;
import at.ac.tuwien.kr.alpha.grounder.IntIdGenerator;

import java.util.Collection;

public interface Bridge {
	Collection<NormalRule> getRules(Assignment assignment, AtomStore atomStore, IntIdGenerator intIdGenerator);
}
