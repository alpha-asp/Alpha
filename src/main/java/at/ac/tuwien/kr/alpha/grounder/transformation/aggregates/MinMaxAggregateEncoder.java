package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates;

import org.apache.commons.collections4.SetUtils;
import org.stringtemplate.v4.ST;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

public class MinMaxAggregateEncoder extends AbstractAggregateEncoder {

	//@formatter:off
	private static final String MINMAX_ELEMENT_ORDERING =
			"$id$_element_tuple_less_than(ARGS, LESS, THAN) :- " +
			"	$id$_element_tuple(ARGS, LESS), $id$_element_tuple(ARGS, THAN), LESS < THAN.";
	
	private static final ST MAX_LITERAL_ENCODING = Util.aspStringTemplate(
			MINMAX_ELEMENT_ORDERING +
			"$id$_element_tuple_has_greater(ARGS, TPL) :- $id$_element_tuple_less_than(ARGS, TPL, _)." +
			"$id$_max_element_tuple(ARGS, MAX) :- " +
			"	$id$_element_tuple(ARGS, MAX), not $id$_element_tuple_has_greater(ARGS, MAX)." +
			"$aggregate_result$(ARGS, MAX) :- $id$_max_element_tuple(ARGS, MAX)."
			);
	
	private static final ST MIN_LITERAL_ENCODING = Util.aspStringTemplate(
			MINMAX_ELEMENT_ORDERING + 
			"$id$_element_tuple_has_smaller(ARGS, TPL) :- $id$_element_tuple_less_than(ARGS, _, TPL)." +
			"$id$_min_element_tuple(ARGS, MIN) :- " +
			"	$id$_element_tuple(ARGS, MIN), not $id$_element_tuple_has_smaller(ARGS, MIN)." +
			"$aggregate_result$(ARGS, MIN) :- $id$_min_element_tuple(ARGS, MIN)."
			);
	//@formatter:on

	private final ProgramParser parser = new ProgramParser();

	public MinMaxAggregateEncoder(AggregateFunctionSymbol func) {
		super(func, SetUtils.hashSet(ComparisonOperator.values()));
		if (!(func == AggregateFunctionSymbol.MAX || func == AggregateFunctionSymbol.MIN)) {
			throw new IllegalArgumentException("Encoder " + this.getClass().getSimpleName() + " can only encode min/max aggregates!");
		}
	}

	@Override
	protected InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		ST encodingTemplate = null;
		if (this.getAggregateFunctionToEncode() == AggregateFunctionSymbol.MAX) {
			encodingTemplate = new ST(MAX_LITERAL_ENCODING);
		} else if (this.getAggregateFunctionToEncode() == AggregateFunctionSymbol.MIN) {
			encodingTemplate = new ST(MIN_LITERAL_ENCODING);
		} else {
			// Note that this should definitely not happen due to the check in the constructor!
			throw new UnsupportedOperationException("Cannot encode anything other than min/max aggregates!");
		}
		String id = aggregateToEncode.getId();
		String resultName = aggregateToEncode.getOutputAtom().getPredicate().getName();
		encodingTemplate.add("id", id);
		encodingTemplate.add("aggregate_result", resultName);
		return parser.parse(encodingTemplate.render());
	}

	@Override
	protected Atom buildElementRuleHead(String aggregateId, AggregateElement element, AggregateRewritingContext ctx) {
		Predicate headPredicate = Predicate.getInstance(this.getElementTuplePredicateSymbol(aggregateId), 2);
		AggregateInfo aggregate = ctx.getAggregateInfo(aggregateId);
		Term aggregateArguments = aggregate.getAggregateArguments();
		Term elementTerm = element.getElementTerms().get(0);
		return new BasicAtom(headPredicate, aggregateArguments, elementTerm);
	}

}
