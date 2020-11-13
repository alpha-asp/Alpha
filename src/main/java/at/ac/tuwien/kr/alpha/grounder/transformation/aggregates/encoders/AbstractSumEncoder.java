package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders;

import java.util.Set;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

/**
 * Abstract base class for aggregate encoders handling sum aggregates.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public abstract class AbstractSumEncoder extends AbstractAggregateEncoder {

	protected AbstractSumEncoder(Set<ComparisonOperator> acceptedOperators) {
		super(AggregateFunctionSymbol.SUM, acceptedOperators);
	}

	@Override
	protected abstract InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx);

	/**
	 * In contrast to encoders for other aggregate functions, the "element tuple" atom for sum encodings is ternary - apart
	 * from the aggregate arguments and the variable tuple identifying the aggregated element, it also holds the value to
	 * add to the result sum as its third argument.
	 */
	@Override
	protected Atom buildElementRuleHead(String aggregateId, AggregateElement element, AggregateRewritingContext ctx) {
		Predicate headPredicate = Predicate.getInstance(this.getElementTuplePredicateSymbol(aggregateId), 3);
		AggregateInfo aggregate = ctx.getAggregateInfo(aggregateId);
		Term aggregateArguments = aggregate.getAggregateArguments();
		FunctionTerm elementTuple = FunctionTerm.getInstance(AbstractAggregateEncoder.ELEMENT_TUPLE_FUNCTION_SYMBOL, element.getElementTerms());
		return new BasicAtom(headPredicate, aggregateArguments, elementTuple, element.getElementTerms().get(0));
	}

}