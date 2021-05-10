package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;
import org.apache.commons.collections4.SetUtils;
import org.stringtemplate.v4.ST;

public class MinMaxEncoder extends AbstractAggregateEncoder {

	//@formatter:off
	private static final String MINMAX_ELEMENT_ORDERING =
			"$id$_element_tuple_less_than(ARGS, LESS, THAN) :- " +
			"	$id$_element_tuple(ARGS, LESS), $id$_element_tuple(ARGS, THAN), LESS < THAN.";
	
	/**
	 * If the literal to encode binds a variable (e.g. "X = #min{...}", "Y = #max{...}"), use this template for the result rule of the aggregate encoding.
	 */
	private static final ST BINDING_LITERAL_RESULT_RULE = Util.aspStringTemplate(
			"$aggregate_result$(ARGS, M) :- $id$_$agg_func$_element_tuple(ARGS, M).");
	
	private static final ST NONBINDING_LITERAL_RESULT_RULE = Util.aspStringTemplate(
			"$aggregate_result$($args$, $cmp_term$) :- $cmp_term$ $cmp_op$ AGG_VAL, $id$_$agg_func$_element_tuple($args$, AGG_VAL), $dependencies;separator=\", \"$.");
	
	private static final ST MAX_LITERAL_ENCODING = Util.aspStringTemplate(
			MINMAX_ELEMENT_ORDERING +
			"$id$_element_tuple_has_greater(ARGS, TPL) :- $id$_element_tuple_less_than(ARGS, TPL, _)." +
			"$id$_max_element_tuple(ARGS, MAX) :- " +
			"	$id$_element_tuple(ARGS, MAX), not $id$_element_tuple_has_greater(ARGS, MAX)."
			);
	
	private static final ST MIN_LITERAL_ENCODING = Util.aspStringTemplate(
			MINMAX_ELEMENT_ORDERING + 
			"$id$_element_tuple_has_smaller(ARGS, TPL) :- $id$_element_tuple_less_than(ARGS, _, TPL)." +
			"$id$_min_element_tuple(ARGS, MIN) :- " +
			"	$id$_element_tuple(ARGS, MIN), not $id$_element_tuple_has_smaller(ARGS, MIN)."
			);
	//@formatter:on

	private final ProgramParser parser = new ProgramParser();

	public MinMaxEncoder(AggregateFunctionSymbol func) {
		super(func, SetUtils.hashSet(ComparisonOperator.values()));
		if (!(func == AggregateFunctionSymbol.MAX || func == AggregateFunctionSymbol.MIN)) {
			throw new IllegalArgumentException("Encoder " + this.getClass().getSimpleName() + " can only encode min/max aggregates!");
		}
	}

	@Override
	protected InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode) {
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
		AggregateAtom atom = aggregateToEncode.getLiteral().getAtom();
		ComparisonOperator cmpOp = atom.getLowerBoundOperator();
		ST resultRuleTemplate = null;
		if (cmpOp == ComparisonOperator.EQ) {
			// aggregate to encode binds a variable, use appropriate result rule
			resultRuleTemplate = new ST(BINDING_LITERAL_RESULT_RULE);
		} else {
			// aggregate encoding needs to compare aggregate value with another variable
			resultRuleTemplate = new ST(NONBINDING_LITERAL_RESULT_RULE);
			resultRuleTemplate.add("args", aggregateToEncode.getAggregateArguments());
			// Note: here we could have a problem if the term is a weirdly named variable (e.g. "AGG_VAL").
			// Ideally, we'd use "internalized", i.e. somehow prefixed "_AGGR_..." etc
			resultRuleTemplate.add("cmp_term", atom.getLowerBoundTerm());
			resultRuleTemplate.add("cmp_op", cmpOp);
			resultRuleTemplate.add("dependencies", aggregateToEncode.getDependencies());

		}
		resultRuleTemplate.add("agg_func", atom.getAggregatefunction().toString().toLowerCase());
		resultRuleTemplate.add("id", id);
		resultRuleTemplate.add("aggregate_result", resultName);
		encodingTemplate.add("id", id);
		encodingTemplate.add("aggregate_result", resultName);
		return parser.parse(encodingTemplate.render() + resultRuleTemplate.render());
	}

	@Override
	protected Atom buildElementRuleHead(String aggregateId, AggregateElement element, Term aggregateArguments) {
		Predicate headPredicate = Predicate.getInstance(this.getElementTuplePredicateSymbol(aggregateId), 2);
		Term elementTerm = element.getElementTerms().get(0);
		return new BasicAtom(headPredicate, aggregateArguments, elementTerm);
	}

}
