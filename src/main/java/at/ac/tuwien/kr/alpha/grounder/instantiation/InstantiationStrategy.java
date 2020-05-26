package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.IndexedInstanceStorage;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

public interface InstantiationStrategy {

	boolean acceptSubstitutedLiteral(Literal lit, IndexedInstanceStorage knownInstances);

	List<Substitution> getAcceptedSubstitutions(Literal lit, Substitution partialSubstitution, IndexedInstanceStorage knownInstances);

}
