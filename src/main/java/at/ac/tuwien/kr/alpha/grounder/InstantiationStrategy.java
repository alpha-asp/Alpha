package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;

public interface InstantiationStrategy {

	boolean acceptSubstitutedLiteral(Literal lit, IndexedInstanceStorage knownInstances);

}
