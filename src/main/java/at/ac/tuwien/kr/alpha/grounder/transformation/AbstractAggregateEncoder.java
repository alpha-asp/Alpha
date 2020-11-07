package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.grounder.transformation.AggregateRewritingContext.AggregateInfo;

public abstract class AbstractAggregateEncoder {

	private final AggregateFunctionSymbol aggregateFunctionToEncode;

	protected AbstractAggregateEncoder(AggregateFunctionSymbol aggregateFunctionToEncode) {
		this.aggregateFunctionToEncode = aggregateFunctionToEncode;
	}

	// TODO rule builder and/or StringTemplate? leaning towards StringTemplate
	// TODO prefix ALL predicate names on which an aggregate result depends including the result itself with aggregate
	// id to ensure stratifiability of individual aggregate literal encodings
	public List<BasicRule> encodeAggregateLiterals(AggregateRewritingContext ctx) {
		Set<String> aggregatesToEncode = ctx.getAggregateFunctionsToRewrite().get(this.aggregateFunctionToEncode);
		List<BasicRule> aggregateEncodingRules = new ArrayList<>();
		for (String aggregateToEncode : aggregatesToEncode) {
			aggregateEncodingRules.addAll(encodeAggregateLiteral(ctx.getAggregateInfo(aggregateToEncode), ctx));
		}
		return aggregateEncodingRules;
	}

	public List<BasicRule> encodeAggregateLiteral(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		List<BasicRule> literalEncodingRules = new ArrayList<>();
		literalEncodingRules.addAll(encodeAggregateResult(aggregateToEncode, ctx));
		for (AggregateElement elementToEncode : aggregateToEncode.getLiteral().getAtom().getAggregateElements()) {
			literalEncodingRules.add(encodeAggregateElement(aggregateToEncode.getId(), elementToEncode));
		}
		return literalEncodingRules;
	}

	protected abstract List<BasicRule> encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx);

	protected abstract BasicRule encodeAggregateElement(String aggregateId, AggregateElement element);

}
