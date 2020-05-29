package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

public interface InstantiationStrategy {

	boolean acceptSubstitutedLiteral(Literal lit, InstanceStorageView knownInstances);

	List<Substitution> getAcceptedSubstitutions(Literal lit, Substitution partialSubstitution, InstanceStorageView knownInstances);

}
