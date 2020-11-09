package at.ac.tuwien.kr.alpha.grounder.transformation;

import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.grounder.transformation.AggregateRewritingContext.AggregateInfo;

public abstract class AbstractAggregateEncoder {

	private final AggregateFunctionSymbol aggregateFunctionToEncode;
	private final Set<ComparisonOperator> acceptedOperators;

	protected AbstractAggregateEncoder(AggregateFunctionSymbol aggregateFunctionToEncode, Set<ComparisonOperator> acceptedOperators) {
		this.aggregateFunctionToEncode = aggregateFunctionToEncode;
		this.acceptedOperators = acceptedOperators;
	}

	public InputProgram encodeAggregateLiterals(AggregateRewritingContext ctx, Set<String> aggregateIdsToEncode) {
		InputProgram.Builder programBuilder = InputProgram.builder();
		for (String aggregateId : aggregateIdsToEncode) {
			programBuilder.accumulate(encodeAggregateLiteral(ctx.getAggregateInfo(aggregateId), ctx));
		}
		return programBuilder.build();
	}

	public InputProgram encodeAggregateLiteral(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		AggregateLiteral literalToEncode = aggregateToEncode.getLiteral();
		if (literalToEncode.getAtom().getAggregatefunction() != this.aggregateFunctionToEncode) {
			throw new IllegalArgumentException(
					"Encoder " + this.getClass().getSimpleName() + " cannot encode aggregate function " + literalToEncode.getAtom().getAggregatefunction());
		}
		if (!this.acceptedOperators.contains(literalToEncode.getAtom().getLowerBoundOperator())) {
			throw new IllegalArgumentException("Encoder " + this.getClass().getSimpleName() + " cannot encode aggregate function "
					+ literalToEncode.getAtom().getAggregatefunction() + " with operator " + literalToEncode.getAtom().getLowerBoundOperator());
		}
		String aggregateId = aggregateToEncode.getId();
		InputProgram literalEncoding = PredicateInternalizer.makePrefixedPredicatesInternal(encodeAggregateResult(aggregateToEncode, ctx), aggregateId);
		// InputProgram literalEncoding = encodeAggregateResult(aggregateToEncode, ctx);
		List<BasicRule> elementEncodingRules = new ArrayList<>();
		for (AggregateElement elementToEncode : literalToEncode.getAtom().getAggregateElements()) {
			elementEncodingRules.add(PredicateInternalizer.makePrefixedPredicatesInternal(encodeAggregateElement(aggregateId, elementToEncode),
					aggregateId));
//			elementEncodingRules.add(encodeAggregateElement(aggregateId, elementToEncode));
		}
		return new InputProgram(ListUtils.union(literalEncoding.getRules(), elementEncodingRules), literalEncoding.getFacts(), new InlineDirectives());
	}

	protected abstract InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx);

	protected abstract BasicRule encodeAggregateElement(String aggregateId, AggregateElement element);

}
