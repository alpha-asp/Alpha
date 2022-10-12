package at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.collections4.ListUtils;
import org.stringtemplate.v4.ST;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.programs.InlineDirectivesImpl;
import at.ac.tuwien.kr.alpha.commons.programs.Programs;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.rules.Rules;
import at.ac.tuwien.kr.alpha.commons.programs.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;
import at.ac.tuwien.kr.alpha.core.programs.transformation.EnumerationRewriting;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

/**
 * Abstract base class for aggregate encoders making use of stringtemplates in their rewriting workflow.
 * The base implementation encodes the core (i.e. aggregate element-independent) part of an aggregate literal's encoding
 * by way of rendering a stringtemplate that is expected to have the arguments: "id" (the aggregate id to encode),
 * "result_predicate" (the symbol of the predicate by which the respective predicate got replaced in it's source rule),
 * "element_tuple" (the predicate name for tuples to aggregate over) and "bound" (the predicate name of the bound atom
 * to use in the encoding).
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public abstract class StringtemplateBasedAggregateEncoder extends AbstractAggregateEncoder {

	private final ProgramParser parser = new ProgramParserImpl();
	private final ST encodingTemplate;
	private final boolean needsBoundRule;

	protected StringtemplateBasedAggregateEncoder(AggregateFunctionSymbol aggregateFunctionToEncode, ComparisonOperator acceptedOperator, ST encodingTemplate) {
		super(aggregateFunctionToEncode, Collections.singleton(acceptedOperator));
		this.encodingTemplate = encodingTemplate;
		if (acceptedOperator.equals(ComparisonOperators.EQ)) {
			this.needsBoundRule = false;
		} else if (acceptedOperator.equals(ComparisonOperators.LE)) {
			this.needsBoundRule = true;
		} else {
			throw new IllegalArgumentException("This encoder is incompatible with comparison operator: " + acceptedOperator);
		}
	}

	@Override
	protected ASPCore2Program encodeAggregateResult(AggregateInfo aggregateToEncode) {
		String aggregateId = aggregateToEncode.getId();

		/*
		 * Create a rule deriving a "bound" value for the core aggregate encoding.
		 * The bound is (in case of encodings for "<=" comparisons) the value that should be tested for being a lower bound, or
		 * else zero.
		 */
		Rule<Head> boundRule = null;
		if (this.needsBoundRule) {
			boundRule = this.buildBoundRule(aggregateToEncode);
		} else {
			/*
			 * Even if we don't have to create a bound rule because the aggregate encoding generates its own candidate values,
			 * we still generate a rule deriving zero as a bound, so that sums and counts over empty sets correctly return 0.
			 */
			boundRule = this.buildZeroBoundRule(aggregateToEncode);
		}

		// Generate encoding
		ST coreEncodingTemplate = new ST(this.encodingTemplate);
		coreEncodingTemplate.add("result_predicate", aggregateToEncode.getOutputAtom().getPredicate().getName());
		coreEncodingTemplate.add("id", aggregateId);
		coreEncodingTemplate.add("element_tuple", this.getElementTuplePredicateSymbol(aggregateId));
		coreEncodingTemplate.add("bound", this.getBoundPredicateName(aggregateId));
		String coreEncodingAsp = coreEncodingTemplate.render();

		// Create the basic program
		ASPCore2Program coreEncoding = new EnumerationRewriting().apply(parser.parse(coreEncodingAsp));

		// Add the programatically created bound rule and return
		return Programs.newASPCore2Program(ListUtils.union(coreEncoding.getRules(), Collections.singletonList(boundRule)), coreEncoding.getFacts(),
				new InlineDirectivesImpl());
	}

	private String getBoundPredicateName(String aggregateId) {
		return aggregateId + "_bound";
	}

	private Rule<Head> buildZeroBoundRule(AggregateInfo aggregateToEncode) {
		BasicAtom bound = Atoms.newBasicAtom(Predicates.getPredicate(getBoundPredicateName(aggregateToEncode.getId()), 2),
				aggregateToEncode.getAggregateArguments(), Terms.newConstant(0));
		return Rules.newRule(Heads.newNormalHead(bound), new ArrayList<>(aggregateToEncode.getDependencies()));
	}

	private Rule<Head> buildBoundRule(AggregateInfo aggregateToEncode) {
		BasicAtom bound = Atoms.newBasicAtom(Predicates.getPredicate(getBoundPredicateName(aggregateToEncode.getId()), 2),
				aggregateToEncode.getAggregateArguments(), aggregateToEncode.getLiteral().getAtom().getLowerBoundTerm());
		return Rules.newRule(Heads.newNormalHead(bound), new ArrayList<>(aggregateToEncode.getDependencies()));
	}

}
