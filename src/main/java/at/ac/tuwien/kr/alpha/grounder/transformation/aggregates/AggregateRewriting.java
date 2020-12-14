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
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders.AbstractAggregateEncoder;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders.AggregateEncoderFactory;

/**
 * Rewrites {@link AggregateLiteral}s in programs to semantically equivalent, aggregate-free sub-programs.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public class AggregateRewriting extends ProgramTransformation<InputProgram, InputProgram> {

	private final AggregateEncoderFactory encoderFactory;

	private final AbstractAggregateEncoder countEqualsEncoder;
	private final AbstractAggregateEncoder countLessOrEqualSortingGridEncoder;
	private final AbstractAggregateEncoder sumEqualsEncoder;
	private final AbstractAggregateEncoder sumLessOrEqualEncoder;
	private final AbstractAggregateEncoder minEncoder;
	private final AbstractAggregateEncoder maxEncoder;

	/**
	 * Creates a new {@link AggregateRewriting} transformation.
	 * 
	 * @param useSortingCircuit if true, literals of form "X <= #count{...}" will be rewritten using a sorting grid-based
	 *                          encoding.
	 */
	public AggregateRewriting(boolean useSortingCircuit) {
		this.encoderFactory = new AggregateEncoderFactory(useSortingCircuit);
		this.countLessOrEqualSortingGridEncoder = this.encoderFactory.buildCountLessOrEqualEncoder();
		this.sumLessOrEqualEncoder = this.encoderFactory.buildSumLessOrEqualEncoder();
		this.sumEqualsEncoder = this.encoderFactory.buildSumEqualsEncoder();
		this.countEqualsEncoder = this.encoderFactory.buildCountEqualsEncoder();
		this.minEncoder = this.encoderFactory.buildMinEncoder();
		this.maxEncoder = this.encoderFactory.buildMaxEncoder();
	}

	/**
	 * Rewrites all {@link AggregateLiteral}s in the given program.
	 * The transformation workflow is split into a preprocessing- and an encoding phase.
	 * During preprocessing, all aggregate literals with two comparison operators are split into two aggregate literals with
	 * only a "lower bound" term and operator. After literal splitting, operators are normalized, i.e. all "count" and "sum"
	 * literals are rewritten to use either "<=" or "=" as comparison operator.
	 * 
	 * Rules containing {@link AggregateLiteral}s are registered in an {@link AggregateRewritingContext} during
	 * preprocessing. After preprocessing, for each rule in the context, aggregate literals in the rule body are substituted
	 * with normal literals of form "$id$_aggregate_result(...)". For each literal substituted this way, a set of rules
	 * deriving the result literal is added that is semantically equivalent to the replaced aggregate literal.
	 */
	@Override
	// Note: Ideally, all variables introduced by AggregateRewriting should be prefixed "_AGG" or something.
	public InputProgram apply(InputProgram inputProgram) {
		AggregateRewritingContext ctx = new AggregateRewritingContext();
		List<BasicRule> outputRules = new ArrayList<>();
		for (BasicRule inputRule : inputProgram.getRules()) {
			// Split literals with two operators.
			for (BasicRule splitRule : AggregateLiteralSplitting.split(inputRule)) {
				// Normalize operators on aggregate literals after splitting.
				BasicRule operatorNormalizedRule = AggregateOperatorNormalization.normalize(splitRule);
				boolean hasAggregate = ctx.registerRule(operatorNormalizedRule);
				// Only keep rules without aggregates. The ones with aggregates are registered in the context and taken care of later.
				if (!hasAggregate) {
					outputRules.add(operatorNormalizedRule);
				}
			}
		}
		// Substitute AggregateLiterals with generated result literals.
		outputRules.addAll(rewriteRulesWithAggregates(ctx));
		InputProgram.Builder resultBuilder = InputProgram.builder().addRules(outputRules).addFacts(inputProgram.getFacts())
				.addInlineDirectives(inputProgram.getInlineDirectives());
		// Add sub-programs deriving respective aggregate literals.
		for (ImmutablePair<AggregateFunctionSymbol, ComparisonOperator> func : ctx.getAggregateFunctionsToRewrite().keySet()) {
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
					return sumLessOrEqualEncoder;
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