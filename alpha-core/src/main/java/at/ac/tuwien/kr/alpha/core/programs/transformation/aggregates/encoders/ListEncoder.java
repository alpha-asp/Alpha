package at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders;

import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.util.Util;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.AggregateRewritingContext;
import org.stringtemplate.v4.ST;

import java.util.Set;

public class ListEncoder extends AbstractAggregateEncoder {

	private static final ST LIST_AGGREGATION = Util.aspStringTemplate(
			// First, establish ordering of elements (which we need to establish the order within the list)
			"$id$_element_greater(ARGS, N, K) :- $id$_element(ARGS, N), $id$_element(ARGS, K), N > K. " +
			"$id$_element_not_successor(ARGS, N, K) :- $id$_element_greater(ARGS, N, I), $id$_element_greater(ARGS, I, K). " +
			"$id$_element_successor(ARGS, N, K) :- $id$_element_greater(ARGS, N, K), not $id$_element_not_successor(ARGS, N, K). " +
			"$id$_element_has_successor(ARGS, N) :- $id$_element_successor(ARGS, _, N). " +
			// Now build the list as a recursively nested function term
			"$id$_lst_element(ARGS, IDX, lst(N, lst_empty)) :- $id$_element(ARGS, N), not $id$_element_has_successor(ARGS, N), IDX = 0. " +
			"$id$_lst_element(ARGS, IDX, lst(N, lst(K, TAIL))) :- $id$_element(ARGS, N), $id$_element_successor(ARGS, K, N), $id$_lst_element(ARGS, PREV_IDX, lst(K, TAIL)), IDX = PREV_IDX + 1. " +
			"$id$_has_next_element(ARGS, IDX) :- $id$_lst_element(ARGS, IDX, _), NEXT_IDX = IDX + 1, $id$_lst_element(ARGS, NEXT_IDX, _). " +
			"$aggregate_result$(ARGS, LIST) :- $id$_lst_element(ARGS, IDX, LIST), not $id$_has_next_element(ARGS, IDX).");

	private final ProgramParser parser;

	protected ListEncoder(ProgramParser parser) {
		super(AggregateAtom.AggregateFunctionSymbol.LIST, Set.of(ComparisonOperators.EQ));
		this.parser = parser;
	}

	@Override
	protected InputProgram encodeAggregateResult(AggregateRewritingContext.AggregateInfo aggregateToEncode) {
		ST encodingTemplate = new ST(LIST_AGGREGATION);
		encodingTemplate.add("id", aggregateToEncode.getId());
		encodingTemplate.add("aggregate_result", aggregateToEncode.getOutputAtom().getPredicate().getName());
		return parser.parse(encodingTemplate.render());
	}

	@Override
	protected BasicAtom buildElementRuleHead(String aggregateId, AggregateAtom.AggregateElement element, Term aggregateArguments) {
		Predicate headPredicate = Predicates.getPredicate(this.getElementTuplePredicateSymbol(aggregateId), 2);
		if (element.getElementTerms().size() != 1) {
			throw new IllegalArgumentException("List elements may only consist of one term.");
		}
		Term value = element.getElementTerms().get(0);
		return Atoms.newBasicAtom(headPredicate, aggregateArguments, value);
	}

	@Override
	protected String getElementTuplePredicateSymbol(String aggregateId) {
		return aggregateId + "_element";
	}
}
