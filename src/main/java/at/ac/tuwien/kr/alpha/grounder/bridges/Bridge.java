package at.ac.tuwien.kr.alpha.grounder.bridges;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.rule.impl.InternalRule;
import at.ac.tuwien.kr.alpha.grounder.IntIdGenerator;

import java.util.Collection;

public interface Bridge {
	Collection<InternalRule> getRules(Assignment assignment, AtomStore atomStore, IntIdGenerator intIdGenerator);
}
