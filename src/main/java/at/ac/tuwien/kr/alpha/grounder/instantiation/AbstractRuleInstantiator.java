package at.ac.tuwien.kr.alpha.grounder.instantiation;

import at.ac.tuwien.kr.alpha.common.rule.InternalRule;

public abstract class AbstractRuleInstantiator {
	
	private LiteralInstantiator literalInstantiator;
	private LiteralInstantiationStrategy instantiationStrategy;
	
	public BindingResult instantiateRule(InternalRule rule) {
		return null;
	}

}
