package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;

public abstract class AbstractAggregateEncoder {

	private final AggregateFunctionSymbol aggregateFunctionToEncode;

	protected AbstractAggregateEncoder(AggregateFunctionSymbol aggregateFunctionToEncode) {
		this.aggregateFunctionToEncode = aggregateFunctionToEncode;
	}

	// TODO rule builder and/or StringTemplate? leaning towards StringTemplate
	// TODO prefix ALL predicate names on which an aggregate result depends including the result itself with aggregate
	// id to ensure stratifiability of individual aggregate literal encodings
	public List<BasicRule> encodeAggregateLiterals(AggregateRewritingContext ctx) {
		Set<AggregateLiteral> literalsToEncode = ctx.getAggregateFunctionsToRewrite().get(this.aggregateFunctionToEncode);
		List<BasicRule> aggregateEncodingRules = new ArrayList<>();
		for (AggregateLiteral literalToEncode : literalsToEncode) {
			aggregateEncodingRules.addAll(encodeAggregateResult(ctx.getAggregateId(literalToEncode)));
			for (AggregateElement elementToEncode : literalToEncode.getAtom().getAggregateElements()) {
				aggregateEncodingRules.add(encodeAggregateElement(ctx.getAggregateId(literalToEncode), elementToEncode));
			}
		}
		return aggregateEncodingRules;
	}

	protected abstract List<BasicRule> encodeAggregateResult(String aggregateId);

	protected abstract BasicRule encodeAggregateElement(String aggregateId, AggregateElement element);

}
