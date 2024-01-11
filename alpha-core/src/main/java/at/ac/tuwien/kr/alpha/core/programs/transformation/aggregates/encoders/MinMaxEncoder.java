package at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.stringtemplate.v4.ST;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.literals.Literals;
import at.ac.tuwien.kr.alpha.commons.rules.Rules;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.commons.util.Util;
import at.ac.tuwien.kr.alpha.core.programs.InputProgramImpl;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

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

	private final ProgramParser parser;

	MinMaxEncoder(ProgramParser parser, AggregateFunctionSymbol func) {
		super(func, SetUtils.hashSet(ComparisonOperators.operators()));
		if (!(func == AggregateFunctionSymbol.MAX || func == AggregateFunctionSymbol.MIN)) {
			throw new IllegalArgumentException("Encoder " + this.getClass().getSimpleName() + " can only encode min/max aggregates!");
		}
		this.parser = parser;
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
		encodingTemplate.add("id", id);
		encodingTemplate.add("aggregate_result", resultName);
		if (cmpOp.equals(ComparisonOperators.EQ)) {
			// Aggregate to encode binds a variable, use appropriate result rule.
			ST resultRuleTemplate = new ST(BINDING_LITERAL_RESULT_RULE);
			resultRuleTemplate.add("agg_func", atom.getAggregateFunction().toString().toLowerCase());
			resultRuleTemplate.add("id", id);
			resultRuleTemplate.add("aggregate_result", resultName);
			return parser.parse(encodingTemplate.render() + resultRuleTemplate.render());
		} else {
			/*
			 * Aggregate encoding needs to compare aggregate value with another variable.
			 * Note that this should also use a string template for the result rule. However,
			 * since we need to compared to a (user-supplied) variable, we have to use a definitely
			 * non-conflicting variable name for the aggregate value, i.e. something prefixed with "_".
			 * Since the ProgramParser doesn't accept this, we need to build the result rule
			 * programmatically as a workaround.
			 *
			 * Result rule stringtemplate for reference:
			 * $aggregate_result$($args$, $cmp_term$) :-
			 * $cmp_term$ $cmp_op$ AGG_VAL,
			 * $id$_$agg_func$_element_tuple($args$, AGG_VAL),
			 * $dependencies;separator=\", \"$."
			 */
			NormalHead resultRuleHead = Heads.newNormalHead(
					Atoms.newBasicAtom(Predicates.getPredicate(resultName, 2), aggregateToEncode.getAggregateArguments(), atom.getLowerBoundTerm()));
			Set<Literal> resultRuleBody = new LinkedHashSet<>();
			VariableTerm aggregateValue = Terms.newVariable("_AGG_VAL");
			ComparisonLiteral aggregateValueComparison = Literals.fromAtom(Atoms.newComparisonAtom(atom.getLowerBoundTerm(), aggregateValue, cmpOp), true);
			Literal aggregateResult = Atoms.newBasicAtom(Predicates.getPredicate(
					id + "_" + atom.getAggregateFunction().toString().toLowerCase() + "_element_tuple", 2),
					aggregateToEncode.getAggregateArguments(), aggregateValue).toLiteral();
			resultRuleBody.add(aggregateResult);
			resultRuleBody.add(aggregateValueComparison);
			resultRuleBody.addAll(aggregateToEncode.getDependencies());
			InputProgramImpl.Builder bld = InputProgramImpl.builder(parser.parse(encodingTemplate.render()));
			Rule<Head> resultRule = Rules.newRule(resultRuleHead, resultRuleBody);
			bld.addRule(resultRule);
			return bld.build();
		}

	}

	@Override
	protected BasicAtom buildElementRuleHead(String aggregateId, AggregateElement element, Term aggregateArguments) {
		Predicate headPredicate = Predicates.getPredicate(this.getElementTuplePredicateSymbol(aggregateId), 2);
		Term elementTerm = element.getElementTerms().get(0);
		return Atoms.newBasicAtom(headPredicate, aggregateArguments, elementTerm);
	}

}
