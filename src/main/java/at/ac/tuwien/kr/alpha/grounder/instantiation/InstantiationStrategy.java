package at.ac.tuwien.kr.alpha.grounder.instantiation;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.IndexedInstanceStorage;

public interface InstantiationStrategy {

	boolean acceptSubstitutedLiteral(Literal lit, IndexedInstanceStorage knownInstances);

}
