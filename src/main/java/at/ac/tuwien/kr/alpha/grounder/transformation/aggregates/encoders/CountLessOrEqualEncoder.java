package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders;

import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

public class CountLessOrEqualEncoder extends CandidateEnumeratingAggregateEncoder {

	private static final ST SORTING_GRID_TEMPLATE = Util.loadStringTemplateGroup(
			CountLessOrEqualEncoder.class.getResource("/stringtemplates/aggregate-encodings.stg")).getInstanceOf("count_le_sorting_grid");

	public CountLessOrEqualEncoder() {
		super(SORTING_GRID_TEMPLATE, AggregateFunctionSymbol.COUNT, ComparisonOperator.LE);
	}

	@Override
	protected BasicRule createCandidateRule(AggregateInfo aggregateToEncode) {
		BasicRule retVal;
		if (this.isEmbeddedEncoder()) {
			retVal = this.createInputNumberRuleFromAtom(aggregateToEncode, this.getCandidateSource());
		} else {
			retVal = this.createDefaultInputNumberRule(aggregateToEncode);
		}
		return retVal;
	}

	// "$aggregate_id$_sorting_network_input_number(ARGS, I) :- $element_tuple$(ARGS, X), $enumeration$(ARGS, X, I).");
	private BasicRule createDefaultInputNumberRule(AggregateInfo aggregateToEncode) {
		String aggregateId = aggregateToEncode.getId();
		VariableTerm aggregateArgumentsVar = VariableTerm.getInstance("ARGS");
		VariableTerm indexVar = VariableTerm.getInstance("IDX");
		VariableTerm elementTupleVar = VariableTerm.getInstance("TPL");

		BasicAtom headAtom = this.createInputNumberAtom(aggregateId, aggregateArgumentsVar, indexVar);
		BasicAtom elementTupleAtom = new BasicAtom(
				Predicate.getInstance(this.getElementTuplePredicateSymbol(aggregateToEncode.getId()), 2),
				aggregateArgumentsVar, elementTupleVar);

		List<Term> enumTerms = new ArrayList<>();
		enumTerms.add(aggregateArgumentsVar);
		enumTerms.add(elementTupleVar);
		enumTerms.add(indexVar);
		BasicAtom enumerationAtom = new EnumerationAtom(enumTerms);

		return BasicRule.getInstance(new NormalHead(headAtom), elementTupleAtom.toLiteral(), enumerationAtom.toLiteral());
	}

	private BasicRule createInputNumberRuleFromAtom(AggregateInfo aggregateToEncode, BasicAtom sourceAtom) {
		String aggregateId = aggregateToEncode.getId();
		return BasicRule.getInstance(
				new NormalHead(this.createInputNumberAtom(aggregateId, aggregateToEncode.getAggregateArguments(), sourceAtom.getTerms().get(1))),
				sourceAtom.toLiteral());
	}

	private BasicAtom createInputNumberAtom(String aggregateId, Term argsTerm, Term idxTerm) {
		return new BasicAtom(Predicate.getInstance(this.getCandidatePredicateSymbol(aggregateId), 2), argsTerm, idxTerm);
	}

}
