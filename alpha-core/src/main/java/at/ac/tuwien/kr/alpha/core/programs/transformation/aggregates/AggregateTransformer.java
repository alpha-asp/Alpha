package at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.api.programs.literals.AggregateLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.programs.Programs;
import at.ac.tuwien.kr.alpha.commons.programs.Programs.ASPCore2ProgramBuilder;
import at.ac.tuwien.kr.alpha.commons.programs.literals.Literals;
import at.ac.tuwien.kr.alpha.commons.programs.rules.Rules;
import at.ac.tuwien.kr.alpha.core.programs.transformation.ProgramTransformation;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.AggregateRewritingContext.AggregateInfo;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders.AbstractAggregateEncoder;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders.CountEncoder;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders.MinMaxEncoder;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders.SumEncoder;

/**
 * Rewrites {@link AggregateLiteral}s in programs to semantically equivalent, aggregate-free sub-programs.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public class AggregateRewriting extends ProgramTransformation<ASPCore2Program, ASPCore2Program> {

	private final AbstractAggregateEncoder countEqualsEncoder;
	private final AbstractAggregateEncoder countLessOrEqualEncoder;
	private final AbstractAggregateEncoder sumEqualsEncoder;
	private final AbstractAggregateEncoder sumLessOrEqualEncoder;
	private final AbstractAggregateEncoder minEncoder;
	private final AbstractAggregateEncoder maxEncoder;

	/**
	 * Creates a new {@link AggregateRewriting} transformation.
	 * 
	 * @param useSortingCircuit       if true, literals of form "X <= #count{...}" will be rewritten using a sorting
	 *                                grid-based
	 *                                encoding.
	 * @param supportNegativeIntegers if true, use ASP encodings for "#sum{...}" literals that support summation over all
	 *                                (including negative) integers. Note that these encodings are less performant than
	 *                                their simpler counterparts that only support positive integers (eused when flag set to false)
	 */
	public AggregateRewriting(boolean useSortingCircuit, boolean supportNegativeIntegers) {
		this.countLessOrEqualEncoder = CountEncoder.buildCountLessOrEqualEncoder(useSortingCircuit);
		this.sumLessOrEqualEncoder = SumEncoder.buildSumLessOrEqualEncoder(supportNegativeIntegers);
		this.sumEqualsEncoder = SumEncoder.buildSumEqualsEncoder(supportNegativeIntegers);
		this.countEqualsEncoder = CountEncoder.buildCountEqualsEncoder();
		this.minEncoder = new MinMaxEncoder(AggregateFunctionSymbol.MIN);
		this.maxEncoder = new MinMaxEncoder(AggregateFunctionSymbol.MAX);
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
	public ASPCore2Program apply(ASPCore2Program inputProgram) {
		AggregateRewritingContext ctx = new AggregateRewritingContext();
		List<Rule<Head>> outputRules = new ArrayList<>();
		for (Rule<Head> inputRule : inputProgram.getRules()) {
			// Split literals with two operators.
			for (Rule<Head> splitRule : AggregateLiteralSplitting.split(inputRule)) {
				// Normalize operators on aggregate literals after splitting.
				Rule<Head> operatorNormalizedRule = AggregateOperatorNormalization.normalize(splitRule);
				boolean hasAggregate = ctx.registerRule(operatorNormalizedRule);
				// Only keep rules without aggregates. The ones with aggregates are registered in the context and taken care of later.
				if (!hasAggregate) {
					outputRules.add(operatorNormalizedRule);
				}
			}
		}
		// Substitute AggregateLiterals with generated result literals.
		outputRules.addAll(rewriteRulesWithAggregates(ctx));
		ASPCore2ProgramBuilder resultBuilder = Programs.builder().addRules(outputRules).addFacts(inputProgram.getFacts())
				.addInlineDirectives(inputProgram.getInlineDirectives());
		// Add sub-programs deriving respective aggregate literals.
		for (Map.Entry<ImmutablePair<AggregateFunctionSymbol, ComparisonOperator>, Set<AggregateInfo>> aggToRewrite : ctx.getAggregateFunctionsToRewrite()
				.entrySet()) {
			ImmutablePair<AggregateFunctionSymbol, ComparisonOperator> func = aggToRewrite.getKey();
			AbstractAggregateEncoder encoder = getEncoderForAggregateFunction(func.left, func.right);
			resultBuilder.accumulate(encoder.encodeAggregateLiterals(aggToRewrite.getValue()));
		}
		return resultBuilder.build();
	}

	private AbstractAggregateEncoder getEncoderForAggregateFunction(AggregateFunctionSymbol function, ComparisonOperator operator) {
		switch (function) {
			case COUNT:
				if (operator.equals(ComparisonOperators.EQ)) {
					return countEqualsEncoder;
				} else if (operator.equals(ComparisonOperators.LE)) {
					return countLessOrEqualEncoder;
				} else {
					throw new UnsupportedOperationException("No fitting encoder for aggregate function " + function + "and operator " + operator + "!");
				}
			case MIN:
				return minEncoder;
			case MAX:
				return maxEncoder;
			case SUM:
				if (operator.equals(ComparisonOperators.EQ)) {
					return sumEqualsEncoder;
				} else if (operator.equals(ComparisonOperators.LE)) {
					return sumLessOrEqualEncoder;
				} else {
					throw new UnsupportedOperationException("No fitting encoder for aggregate function " + function + "and operator " + operator + "!");
				}
			default:
				throw new UnsupportedOperationException("Unsupported aggregate function/comparison operator: " + function + ", " + operator);
		}
	}

	/**
	 * Transforms (restricted) aggregate literals of format "VAR OP #AGG_FN{...}" into literals of format
	 * "<result_predicate>(ARGS, VAR)" where ARGS is a function term wrapping the aggregate's global variables.
	 *
	 * @param ctx the {@link AggregateRewritingContext} containing information about all aggregates.
	 * @return for each rule, its rewritten version where aggregates are replaced with output atoms of the encoding.
	 */
	private static List<Rule<Head>> rewriteRulesWithAggregates(AggregateRewritingContext ctx) {
		List<Rule<Head>> rewrittenRules = new ArrayList<>();
		for (Rule<Head> rule : ctx.getRulesWithAggregates()) {
			List<Literal> rewrittenBody = new ArrayList<>();
			for (Literal lit : rule.getBody()) {
				if (lit instanceof AggregateLiteral) {
					AggregateInfo aggregateInfo = ctx.getAggregateInfo((AggregateLiteral) lit);
					rewrittenBody.add(Literals.fromAtom(aggregateInfo.getOutputAtom(), !lit.isNegated()));
				} else {
					rewrittenBody.add(lit);
				}
			}
			rewrittenRules.add(Rules.newRule(rule.getHead(), rewrittenBody));
		}
		return rewrittenRules;
	}
}
