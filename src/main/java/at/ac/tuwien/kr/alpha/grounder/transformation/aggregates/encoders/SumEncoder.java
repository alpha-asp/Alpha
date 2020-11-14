package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders;

import org.apache.commons.collections4.ListUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import java.util.ArrayList;
import java.util.Collections;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.EnumerationRewriting;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

/**
 * Abstract base class for aggregate encoders handling sum aggregates.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public final class SumEncoder extends AbstractAggregateEncoder {

	private static final STGroup AGGREGATE_ENCODINGS = Util.loadStringTemplateGroup(
			SumEncoder.class.getResource("/stringtemplates/aggregate-encodings.stg"));

	private static final ST SUM_LE_TEMPLATE = AGGREGATE_ENCODINGS.getInstanceOf("sum_le");
	private static final ST SUM_EQ_TEMPLATE = AGGREGATE_ENCODINGS.getInstanceOf("sum_eq");

	private final ProgramParser parser = new ProgramParser();
	private final ST encodingTemplate;
	private final boolean needsBoundRule;

	protected SumEncoder(ComparisonOperator acceptedOperator, ST encodingTemplate, boolean needsBoundRule) {
		super(AggregateFunctionSymbol.SUM, Collections.singleton(acceptedOperator));
		this.encodingTemplate = encodingTemplate;
		this.needsBoundRule = needsBoundRule;
	}

	public static SumEncoder buildSumLessOrEqualEncoder() {
		return new SumEncoder(ComparisonOperator.LE, SUM_LE_TEMPLATE, true);
	}

	public static SumEncoder buildSumEqualsEncoder() {
		return new SumEncoder(ComparisonOperator.EQ, SUM_EQ_TEMPLATE, false);
	}

	@Override
	protected InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		String aggregateId = aggregateToEncode.getId();
		AggregateLiteral lit = aggregateToEncode.getLiteral();

		// Generate encoding
		ST coreEncodingTemplate = new ST(this.encodingTemplate);
		coreEncodingTemplate.add("result_predicate", aggregateToEncode.getOutputAtom().getPredicate().getName());
		coreEncodingTemplate.add("id", aggregateId);
		coreEncodingTemplate.add("element_tuple", this.getElementTuplePredicateSymbol(aggregateId));
		String coreEncodingAsp = coreEncodingTemplate.render();

		// Create the basic program
		InputProgram coreEncoding = new EnumerationRewriting().apply(parser.parse(coreEncodingAsp));

		if (this.needsBoundRule) {
			BasicAtom bound = new BasicAtom(Predicate.getInstance(aggregateId + "_bound", 2),
					aggregateToEncode.getAggregateArguments(), lit.getAtom().getLowerBoundTerm());
			BasicRule boundRule = new BasicRule(new NormalHead(bound), new ArrayList<>(ctx.getDependencies(aggregateId)));
			// Combine core encoding and bound rule
			return new InputProgram(ListUtils.union(coreEncoding.getRules(), Collections.singletonList(boundRule)), coreEncoding.getFacts(),
					new InlineDirectives());
		} else {
			return coreEncoding;
		}
	}

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