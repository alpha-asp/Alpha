package at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.commons.util.Util;

/**
 * Aggregate encoder handling sum aggregates.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public final class SumEncoder extends StringtemplateBasedAggregateEncoder {

	private static final STGroup AGGREGATE_ENCODINGS = Util.loadStringTemplateGroup(
			SumEncoder.class.getResource("/stringtemplates/aggregate-encodings.stg"));

	private static final ST SUM_LE_TEMPLATE = AGGREGATE_ENCODINGS.getInstanceOf("sum_le");
	private static final ST SUM_EQ_TEMPLATE = AGGREGATE_ENCODINGS.getInstanceOf("sum_eq");

	private static final ST NON_NEG_ELEMENTS_SUM_LE_TEMPLATE = AGGREGATE_ENCODINGS.getInstanceOf("sum_le_no_negative_elements");
	private static final ST NON_NEG_ELEMENTS_SUM_EQ_TEMPLATE = AGGREGATE_ENCODINGS.getInstanceOf("sum_eq_no_negative_elements");

	private SumEncoder(ProgramParser parser, ComparisonOperator acceptedOperator, ST encodingTemplate) {
		super(parser, AggregateFunctionSymbol.SUM, acceptedOperator, encodingTemplate);
	}

	static SumEncoder buildSumLessOrEqualEncoder(ProgramParser parser, boolean supportNegativeIntegers) {
		return new SumEncoder(parser, ComparisonOperators.LE, supportNegativeIntegers ? SUM_LE_TEMPLATE : NON_NEG_ELEMENTS_SUM_LE_TEMPLATE);
	}

	public static SumEncoder buildSumEqualsEncoder(ProgramParser parser, boolean supportNegativeIntegers) {
		return new SumEncoder(parser, ComparisonOperators.EQ, supportNegativeIntegers ? SUM_EQ_TEMPLATE : NON_NEG_ELEMENTS_SUM_EQ_TEMPLATE);
	}

	/**
	 * In contrast to encoders for other aggregate functions, the "element tuple" atom for sum encodings is ternary - apart
	 * from the aggregate arguments and the variable tuple identifying the aggregated element, it also holds the value to
	 * add to the result sum as its third argument.
	 */
	@Override
	protected BasicAtom buildElementRuleHead(String aggregateId, AggregateElement element, Term aggregateArguments) {
		Predicate headPredicate = Predicates.getPredicate(this.getElementTuplePredicateSymbol(aggregateId), 3);
		FunctionTerm elementTuple = Terms.newFunctionTerm(AbstractAggregateEncoder.ELEMENT_TUPLE_FUNCTION_SYMBOL, element.getElementTerms());
		return Atoms.newBasicAtom(headPredicate, aggregateArguments, elementTuple, element.getElementTerms().get(0));
	}

}