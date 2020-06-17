package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

public abstract class AbstractInstantiationStrategy implements InstantiationStrategy {

	public List<Substitution> getAcceptedSubstitutions(Literal lit, Substitution partialSubstitution, InstanceStorageView knownInstances) {
		
		return null;
	}

	// TODO contract: only call on ground literals
	// (ensure externally or check with abstract strategy and template pattern)
	protected abstract ThriceTruth acceptSubstitutedLiteral(Literal lit, InstanceStorageView knownInstances);

}
