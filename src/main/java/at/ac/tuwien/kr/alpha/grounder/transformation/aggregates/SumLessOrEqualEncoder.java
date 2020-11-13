package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.stringtemplate.v4.ST;

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
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

public class SumLessOrEqualEncoder extends AbstractAggregateEncoder {

	//@formatter:off
	private static final ST SUM_LE_ENCODING = Util.aspStringTemplate(
			"#enumeration_predicate_is $enumeration$."
			+ "$id$_input_number_with_first(ARGS, IDX, VAL) :- $element_tuple$(ARGS, TPL, VAL), $enumeration$(ARGS, TPL, IDX)."
			+ "$id$_interesting_number(ARGS, 1..I1) :- $id$_input_number_with_first(ARGS, I, _), I1 = I - 1."
			+ "$id$_prefix_subset_sum(ARGS, 0, 0) :- $id$_input_number_with_first(ARGS, _, _)."
			+ "$id$_prefix_subset_sum(ARGS, I, S) :- $id$_prefix_subset_sum(ARGS, I1, S), I1 = I - 1, $id$_interesting_number(ARGS, I)."
			+ "$id$_prefix_subset_sum(ARGS, I, SF) :- $id$_prefix_subset_sum(ARGS, I1, S), I1 = I - 1, SF = S + F, "
			+ "		$id$_input_number_with_first(ARGS, I, F), bound(ARGS, K), SF < K."
			+ "$aggregate_result$(ARGS, K) :- $id$_bound(ARGS, K), K <= 0."
			+ "$aggregate_result$(ARGS, K) :- $id$_prefix_subset_sum(ARGS, I1, S), I1 = I - 1, $id$_input_number_with_first(ARGS, I, F), "
			+ "		$id$_bound(ARGS, K), K <= S + F.");
	//@formatter:on

	private final ProgramParser parser = new ProgramParser();

	public SumLessOrEqualEncoder() {
		super(AggregateFunctionSymbol.SUM, SetUtils.hashSet(ComparisonOperator.LE));
	}

	@Override
	protected InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		String aggregateId = aggregateToEncode.getId();
		AggregateLiteral lit = aggregateToEncode.getLiteral();
		ST encodingTemplate = new ST(SUM_LE_ENCODING);
		encodingTemplate.add("aggregate_result", aggregateToEncode.getOutputAtom().getPredicate().getName());
		encodingTemplate.add("id", aggregateId);
		encodingTemplate.add("enumeration", aggregateId + "_enum");
		encodingTemplate.add("element_tuple", this.getElementTuplePredicateSymbol(aggregateId));
		String resultEncodingAsp = encodingTemplate.render();
		InputProgram resultEncoding = new EnumerationRewriting().apply(parser.parse(resultEncodingAsp));
		BasicAtom sumBound = new BasicAtom(Predicate.getInstance(aggregateId + "_bound", 2),
				aggregateToEncode.getAggregateArguments(), lit.getAtom().getLowerBoundTerm());
		BasicRule sumBoundRule = new BasicRule(new NormalHead(sumBound), new ArrayList<>(ctx.getDependencies(aggregateId)));
		return new InputProgram(ListUtils.union(resultEncoding.getRules(), Collections.singletonList(sumBoundRule)), resultEncoding.getFacts(),
				new InlineDirectives());
	}

	@Override
	protected Atom buildElementRuleHead(String aggregateId, AggregateElement element, AggregateRewritingContext ctx) {
		Predicate headPredicate = Predicate.getInstance(this.getElementTuplePredicateSymbol(aggregateId), 3);
		AggregateInfo aggregate = ctx.getAggregateInfo(aggregateId);
		Term aggregateArguments = aggregate.getAggregateArguments();
		FunctionTerm elementTuple = FunctionTerm.getInstance(AbstractAggregateEncoder.ELEMENT_TUPLE_FUNCTION_SYMBOL, element.getElementTerms());
		return new BasicAtom(headPredicate, aggregateArguments, element.getElementTerms().get(0), elementTuple);
	}

}
