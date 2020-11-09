package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.grounder.transformation.AggregateRewritingContext.AggregateInfo;

public abstract class AbstractAggregateEncoder {

	private final AggregateFunctionSymbol aggregateFunctionToEncode;
	private final Set<ComparisonOperator> acceptedOperators;

	protected AbstractAggregateEncoder(AggregateFunctionSymbol aggregateFunctionToEncode, Set<ComparisonOperator> acceptedOperators) {
		this.aggregateFunctionToEncode = aggregateFunctionToEncode;
		this.acceptedOperators = acceptedOperators;
	}

	// TODO rule builder and/or StringTemplate? leaning towards StringTemplate
	// TODO prefix ALL predicate names on which an aggregate result depends including the result itself with aggregate
	// id to ensure stratifiability of individual aggregate literal encodings
	public List<BasicRule> encodeAggregateLiterals(AggregateRewritingContext ctx, Set<String> aggregateIdsToEncode) {
		List<BasicRule> aggregateEncodingRules = new ArrayList<>();
		for (String aggregateId : aggregateIdsToEncode) {
			aggregateEncodingRules.addAll(encodeAggregateLiteral(ctx.getAggregateInfo(aggregateId), ctx));
		}
		return aggregateEncodingRules;
	}

	public List<BasicRule> encodeAggregateLiteral(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		AggregateLiteral literalToEncode = aggregateToEncode.getLiteral();
		if (literalToEncode.getAtom().getAggregatefunction() != this.aggregateFunctionToEncode) {
			throw new IllegalArgumentException(
					"Encoder " + this.getClass().getSimpleName() + " cannot encode aggregate function " + literalToEncode.getAtom().getAggregatefunction());
		}
		if (!this.acceptedOperators.contains(literalToEncode.getAtom().getLowerBoundOperator())) {
			throw new IllegalArgumentException("Encoder " + this.getClass().getSimpleName() + " cannot encode aggregate function "
					+ literalToEncode.getAtom().getAggregatefunction() + " with operator " + literalToEncode.getAtom().getLowerBoundOperator());
		}
		List<BasicRule> literalEncodingRules = new ArrayList<>();
		List<BasicRule> resultEncoding = encodeAggregateResult(aggregateToEncode, ctx);
		for (BasicRule rule : resultEncoding) {
			literalEncodingRules.add(PredicateInternalizer.makePrefixedPredicatesInternal(rule, aggregateToEncode.getId()));
		}
		for (AggregateElement elementToEncode : literalToEncode.getAtom().getAggregateElements()) {
			literalEncodingRules.add(PredicateInternalizer.makePrefixedPredicatesInternal(encodeAggregateElement(aggregateToEncode.getId(), elementToEncode),
					aggregateToEncode.getId()));
		}

		return literalEncodingRules;
	}

	protected abstract List<BasicRule> encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx);

	protected abstract BasicRule encodeAggregateElement(String aggregateId, AggregateElement element);

}
