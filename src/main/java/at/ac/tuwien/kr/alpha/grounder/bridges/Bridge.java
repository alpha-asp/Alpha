package at.ac.tuwien.kr.alpha.grounder.bridges;

import at.ac.tuwien.kr.alpha.grounder.AtomStore;
import at.ac.tuwien.kr.alpha.grounder.IntIdGenerator;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.solver.WritableAssignment;

import java.util.Collection;

public interface Bridge {
	Collection<NonGroundRule> getRules(WritableAssignment assignment, AtomStore atomStore, IntIdGenerator intIdGenerator);
}
