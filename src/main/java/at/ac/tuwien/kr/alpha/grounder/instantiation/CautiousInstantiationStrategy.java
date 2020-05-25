package at.ac.tuwien.kr.alpha.grounder.instantiation;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.IndexedInstanceStorage;
import at.ac.tuwien.kr.alpha.grounder.Instance;

public class CautiousInstantiationStrategy implements InstantiationStrategy {

	/**
	 * Accepts a substituted literal lit iff:
	 * - if lit is positive, knownInstances contains the instance represented by lit
	 * - if lit is negative, knownInstances does NOT contain the instance
	 * represented by lit
	 */
	@Override
	public boolean acceptSubstitutedLiteral(Literal lit, IndexedInstanceStorage knownInstances) {
		Instance refInstance = new Instance(lit.getTerms());
		boolean instanceKnown = knownInstances.containsInstance(refInstance);
		return lit.isNegated() ? (!instanceKnown) : instanceKnown;
	}

}
