package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.grounder.transformation.ProgramTransformation;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

public class AggregateRewriting extends ProgramTransformation<InputProgram, InputProgram> {

	private final AggregateRewritingConfig config;

	private final AbstractAggregateEncoder countEqualsEncoder = new CountEqualsAggregateEncoder();
	private final AbstractAggregateEncoder countLessOrEqualSortingGridEncoder = new CountLessOrEqualSortingGridEncoder();
	private final AbstractAggregateEncoder sumEqualsEncoder = new SumEqualsAggregateEncoder();
	private final AbstractAggregateEncoder sumLessOrEqualsEncoder = new SumLessOrEqualEncoder();
	private final AbstractAggregateEncoder minEncoder = new MinMaxAggregateEncoder(AggregateFunctionSymbol.MIN);
	private final AbstractAggregateEncoder maxEncoder = new MinMaxAggregateEncoder(AggregateFunctionSymbol.MAX);

	public AggregateRewriting(AggregateRewritingConfig config) {
		this.config = config;
	}

	/**
	 * Transformation steps:
	 * - Preprocessing: build a "symbol table", assigning an ID to each distinct aggregate literal
	 * - Bounds normalization: everything to "left-associative" expressions with one operator
	 * - Operator normalization: everything to expressions of form "RESULT LEQ #agg{...}"
	 * - Cardinality normalization: rewrite #count expressions
	 * - Sum normalization: rewrite #sum expressions
	 */
	@Override
	// TODO prefix all variables generated in aggregate processing with some internal "_AGG" or something
	public InputProgram apply(InputProgram inputProgram) {
		AggregateRewritingContext ctx = new AggregateRewritingContext();
		List<BasicRule> outputRules = new ArrayList<>();
		for (BasicRule inputRule : inputProgram.getRules()) {
			for (BasicRule splitRule : AggregateLiteralSplitting.split(inputRule)) {
				BasicRule operatorNormalizedRule = AggregateOperatorNormalization.normalize(splitRule);
				boolean hasAggregate = ctx.registerRule(operatorNormalizedRule);
				// Only keep rules without aggregates. The ones with aggregates are registered in the context and taken care of later
				if (!hasAggregate) {
					outputRules.add(operatorNormalizedRule);
				}
			}
		}
		outputRules.addAll(rewriteRulesWithAggregates(ctx));
		InputProgram.Builder resultBuilder = InputProgram.builder().addRules(outputRules).addFacts(inputProgram.getFacts())
				.addInlineDirectives(inputProgram.getInlineDirectives());
		for (ImmutablePair<AggregateFunctionSymbol, ComparisonOperator> func : ctx.getAggregateFunctionsToRewrite().keySet()) {
			// aggregateEncodingRules.addAll(encodeAggregateFunction(func, ctx.getAggregateFunctionsToRewrite().get(func), ctx));
			AbstractAggregateEncoder encoder = getEncoderForAggregateFunction(func.left, func.right);
			resultBuilder.accumulate(encoder.encodeAggregateLiterals(ctx, ctx.getAggregateFunctionsToRewrite().get(func)));
		}
		return resultBuilder.build();
	}

	private AbstractAggregateEncoder getEncoderForAggregateFunction(AggregateFunctionSymbol function, ComparisonOperator operator) {
		switch (function) {
			case COUNT:
				if (operator == ComparisonOperator.EQ) {
					return countEqualsEncoder;
				} else if (operator == ComparisonOperator.LE) {
					return countLessOrEqualSortingGridEncoder;
				} else {
					throw new UnsupportedOperationException("No fitting encoder for aggregate function " + function + "and operator " + operator + "!");
				}
			case MIN:
				return minEncoder;
			case MAX:
				return maxEncoder;
			case SUM:
				if (operator == ComparisonOperator.EQ) {
					return sumEqualsEncoder;
				} else if (operator == ComparisonOperator.LE) {
					return sumLessOrEqualsEncoder;
				} else {
					throw new UnsupportedOperationException("No fitting encoder for aggregate function " + function + "and operator " + operator + "!");
				}
			default:
				throw new UnsupportedOperationException("Unsupported aggregate function/comparison operator: " + function + ", " + operator);
		}
	}

	// Transforms (restricted) aggregate literals of format "VAR OP #AGG_FN{...}" into literals of format
	// "<result_predicate>(ARGS, VAR)" where ARGS is a function term wrapping the aggregate's global variables.
	private static List<BasicRule> rewriteRulesWithAggregates(AggregateRewritingContext ctx) {
		List<BasicRule> rewrittenRules = new ArrayList<>();
		for (BasicRule rule : ctx.getRulesToRewrite()) {
			List<Literal> rewrittenBody = new ArrayList<>();
			Map<AggregateLiteral, String> aggregatesInRule = ctx.getAggregatesInRule(rule);
			for (Literal lit : rule.getBody()) {
				if (lit instanceof AggregateLiteral) {
					String aggregateId = aggregatesInRule.get((AggregateLiteral) lit);
					AggregateInfo aggregateInfo = ctx.getAggregateInfo(aggregateId);
					rewrittenBody.add(new BasicLiteral(aggregateInfo.getOutputAtom(), !lit.isNegated()));
				} else {
					rewrittenBody.add(lit);
				}
			}
			rewrittenRules.add(new BasicRule(rule.getHead(), rewrittenBody));
		}
		return rewrittenRules;
	}

}
