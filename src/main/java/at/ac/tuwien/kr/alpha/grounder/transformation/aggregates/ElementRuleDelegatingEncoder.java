package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates;

import java.util.Collections;

import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

// TODO do this via decorator pattern on top of any encoder!
public class ElementRuleDelegatingEncoder extends AbstractAggregateEncoder {

	private final AbstractAggregateEncoder delegate;
	private final String delegatedToId;

	ElementRuleDelegatingEncoder(String delegatedToId, AbstractAggregateEncoder delegate) {
		super(delegate.getAggregateFunctionToEncode(), delegate.getAcceptedOperators());
		this.delegate = delegate;
		this.delegatedToId = delegatedToId;
	}

	@Override
	protected InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		return delegate.encodeAggregateResult(aggregateToEncode, ctx);
	}

	@Override
	protected BasicRule encodeAggregateElement(String aggregateId, AggregateElement element, AggregateRewritingContext ctx) {
		Atom headAtom = this.buildElementRuleHead(aggregateId, element, ctx);
		return new BasicRule(new NormalHead(headAtom), Collections.singletonList(delegate.buildElementRuleHead(this.delegatedToId, element, ctx).toLiteral()));
	}

}
