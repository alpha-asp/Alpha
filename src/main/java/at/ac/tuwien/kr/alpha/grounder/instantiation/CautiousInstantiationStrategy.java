package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.List;
import java.util.stream.Collectors;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

public class CautiousInstantiationStrategy implements InstantiationStrategy {

	/**
	 * Accepts a substituted literal lit iff:
	 * - if lit is positive, knownInstances contains the instance represented by lit
	 * - if lit is negative, knownInstances does NOT contain the instance
	 * represented by lit
	 */
	@Override
	public boolean acceptSubstitutedLiteral(Literal lit, InstanceStorageView knownInstances) {
		Instance refInstance = new Instance(lit.getTerms());
		boolean instanceKnown = knownInstances.containsInstance(refInstance);
		return lit.isNegated() ? (!instanceKnown) : instanceKnown;
	}

	@Override
	public List<Substitution> getAcceptedSubstitutions(Literal lit, Substitution partialSubstitution, InstanceStorageView knownInstances) {
		if (lit.isNegated()) {
			throw new UnsupportedOperationException("Cannot extend substitution for negated literal - literal should be ground already!");
		}
		List<Instance> instances = knownInstances.getInstancesFromPartiallyGroundAtom(lit.getAtom());
		// could do this in a loop, but let's see what's faster...
		List<Substitution> extendedSubstitutions = instances.stream().parallel()
				.map((instance) -> Substitution.unify(lit.getAtom(), instance, new Substitution(partialSubstitution)))
				.filter((unified) -> unified != null)
				.collect(Collectors.toList());
		return extendedSubstitutions;
	}

}
