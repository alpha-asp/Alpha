package at.ac.tuwien.kr.alpha.grounder.bridges;

import at.ac.tuwien.kr.alpha.grounder.IntIdGenerator;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import java.util.List;

import java.util.Collection;

public interface Bridge {
	Collection<NonGroundRule> getRules(List<Atom> trueAtoms, IntIdGenerator intIdGenerator);
}
