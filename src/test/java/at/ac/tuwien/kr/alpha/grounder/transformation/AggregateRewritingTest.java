package at.ac.tuwien.kr.alpha.grounder.transformation;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm.ArithmeticOperator;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.test.util.RuleParser;
import at.ac.tuwien.kr.alpha.test.util.TestUtils;

public class AggregateRewritingTest {

	//@formatter:off
	public static final String LITERAL_SPLITTING_POS1_ASP = 
			"count_between :- "
				+ "X < #count{ N : thing2(N) } < Y, "
				+ "X = #count{ K : thing1(K) },"
				+ "Y = #count{ L : thing3(L) }.";
	public static final String LITERAL_SPLITTING_POS2_ASP = 
			"count_between :- "
				+ "X < #count{ N : thing2(N) } < Y, "
				+ "dom(X),"
				+ "dom1(Y),"
				+ "Y < #count{ L : thing3(L) } = Z.";
	public static final String LITERAL_SPLITTING_NEG1_ASP =
			"count_not_between :- "
			+ "dom(X), dom(Y),"
			+ "not X < #count{N : thing(N)} < Y.";
	public static final String LITERAL_SPLITTING_NEG2_ASP =
			"count_not_between :- "
			+ "dom(X), dom(Y), dom(U), dom(V),"
			+ "not X < #count{N : thing(N)} < Y,"
			+ "not U < #count{K : thing(K)} < V.";
	public static final String LITERAL_SPLITTING_NEG_POS_ASP =
			"count_between_and_not_between(U, V, X, Y) :- "
			+ "dom(X), dom(Y), dom(U), dom(V),"
			+ "U < #count{K : thing(K)} < V,"
			+ "not X < #count{N : thing(N)} < Y.";
	
	public static final String OPERATOR_NORMALIZATION_GT_POS_ASP = 
			"bla :- dom(X), X > #count{N : thing(N)}.";
	public static final String OPERATOR_NORMALIZATION_LT_POS_ASP =
			"bla :- dom(X), X < #count{N : thing(N)}.";
	public static final String OPERATOR_NORMALIZATION_NE_POS_ASP =
			"bla :- dom(X), X != #count{N : thing(N)}.";
	public static final String OPERATOR_NORMALIZATION_GE_POS_ASP =
			"bla :- dom(X), X >= #count{N : thing(N)}.";
	public static final String OPERATOR_NORMALIZATION_LT_NEG_ASP =
			"bla :- dom(X), not X < #count{N : thing(N)}.";
	public static final String OPERATOR_NORMALIZATION_NE_NEG_ASP =
			"bla :- dom(X), not X != #count{N : thing(N)}.";
	public static final String OPERATOR_NORMALIZATION_GT_NEG_ASP = 
			"bla :- dom(X), not X > #count{N : thing(N)}.";
	public static final String OPERATOR_NORMALIZATION_GE_NEG_ASP =
			"bla :- dom(X), not X >= #count{N : thing(N)}.";
	
	public static final String COUNT_EQ_ASP = 
			"thing(1..3)."
			+ "cnt_things(X) :- X = #count{N : thing(N)}.";
	//@formatter:on

	@Test
	public void literalSplittingSinglePositiveLiteral() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateLiteralSplitting();
		InputProgram input = alpha.readProgramString(LITERAL_SPLITTING_POS1_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule = RuleParser
				.parse("count_between :- X < #count{N : thing2(N)}, Y > #count{N : thing2(N)}, X = #count{ K : thing1(K) }, Y = #count{ L : thing3(L) }.");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule);
		Assert.assertEquals(1, rewritten.getRules().size());
	}

	@Test
	public void literalSplittingMultiplePositiveLiterals() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateLiteralSplitting();
		InputProgram input = alpha.readProgramString(LITERAL_SPLITTING_POS2_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule = RuleParser
				.parse("count_between :- X < #count{N : thing2(N)}, Y > #count{N : thing2(N)}, Y < #count{L : thing3(L)}, Z = #count{L : thing3(L)}, dom(X), dom1(Y).");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule);
		Assert.assertEquals(1, rewritten.getRules().size());
	}

	@Test
	public void literalSplittingSingleNegativeLiteral() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateLiteralSplitting();
		InputProgram input = alpha.readProgramString(LITERAL_SPLITTING_NEG1_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule1 = RuleParser
				.parse("count_not_between :- dom(X), dom(Y), not X < #count{N : thing(N)}.");
		BasicRule expectedRewrittenRule2 = RuleParser
				.parse("count_not_between :- dom(X), dom(Y), not Y > #count{N : thing(N)}.");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule1);
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule2);
		Assert.assertEquals(2, rewritten.getRules().size());
	}

	@Test
	public void literalSplittingMultipleNegativeLiterals() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateLiteralSplitting();
		InputProgram input = alpha.readProgramString(LITERAL_SPLITTING_NEG2_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule1 = RuleParser
				.parse("count_not_between :- dom(X), dom(Y), dom(U), dom(V), not X < #count{N : thing(N)}, not U < #count{K : thing(K)}.");
		BasicRule expectedRewrittenRule2 = RuleParser
				.parse("count_not_between :- dom(X), dom(Y), dom(U), dom(V), not X < #count{N : thing(N)}, not V > #count{K : thing(K)}.");
		BasicRule expectedRewrittenRule3 = RuleParser
				.parse("count_not_between :- dom(X), dom(Y), dom(U), dom(V), not Y > #count{N : thing(N)}, not U < #count{K : thing(K)}.");
		BasicRule expectedRewrittenRule4 = RuleParser
				.parse("count_not_between :- dom(X), dom(Y), dom(U), dom(V), not Y > #count{N : thing(N)}, not V > #count{K : thing(K)}.");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule1);
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule2);
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule3);
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule4);
		Assert.assertEquals(4, rewritten.getRules().size());
	}

	@Test
	public void literalSplittingNegativeAndPositiveLiteral() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateLiteralSplitting();
		InputProgram input = alpha.readProgramString(LITERAL_SPLITTING_NEG_POS_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule1 = RuleParser
				.parse("count_between_and_not_between(U, V, X, Y) :- dom(X), dom(Y), dom(U), dom(V), not X < #count{N : thing(N)}, U < #count{K : thing(K)}, V > #count{K : thing(K)}.");
		BasicRule expectedRewrittenRule2 = RuleParser
				.parse("count_between_and_not_between(U, V, X, Y) :- dom(X), dom(Y), dom(U), dom(V), not Y > #count{N : thing(N)}, U < #count{K : thing(K)}, V > #count{K : thing(K)}.");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule1);
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule2);
		Assert.assertEquals(2, rewritten.getRules().size());
	}

	/*
	 * <ul>
	 * <li><code>X < #aggr{...}</code> == <code>XP <= #aggr{...}, XP = X - 1</code></li>
	 * <li><code>X != #aggr{...}</code> == <code>not X = #aggr{...}</code></li>
	 * <li><code>X > #aggr{...}</code> == <code>not X <= #aggr{...}</code></li>
	 * <li><code>X >= #aggr{...}</code> == <code>not XP <= #aggr{...}, XP = X - 1</code></li>
	 * <li><code>not X < #aggr{...}</code> == <code>not XP <= #aggr{...}, XP = X - 1</code></li>
	 * <li><code>not X != #aggr{...}</code> == <code>X = #aggr{...}</code></li>
	 * <li><code>not X > #aggr{...}</code> == <code>X <= #aggr{...}</code></li>
	 * <li><code>not X >= #aggr{...}</code> == <code>XP <= #aggr{...}, XP = X - 1</code></li>
	 * </ul>
	 */

	@Test
	public void operatorNormalizationGTPositive() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_GT_POS_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule = RuleParser.parse("bla :- dom(X), not X <= #count{N : thing(N)}.");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule);
	}

	@Test
	public void operatorNormalizationLTPositive() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_LT_POS_ASP);
		BasicRule sourceRule = input.getRules().get(0);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule rewrittenRule = rewritten.getRules().get(0);
		assertOperatorNormalized(rewrittenRule, ComparisonOperator.LE, true);
		assertAggregateBoundDecremented(sourceRule, rewrittenRule);
	}

	@Test
	public void operatorNormalizationNEPositive() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_NE_POS_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule = RuleParser.parse("bla :- dom(X), not X = #count{N : thing(N)}.");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule);
	}

	@Test
	public void operatorNormalizationGEPositive() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_GE_POS_ASP);
		BasicRule sourceRule = input.getRules().get(0);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule rewrittenRule = rewritten.getRules().get(0);
		assertOperatorNormalized(rewrittenRule, ComparisonOperator.LE, false);
		assertAggregateBoundDecremented(sourceRule, rewrittenRule);
	}

	@Test
	public void operatorNormalizationLTNegative() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_LT_NEG_ASP);
		BasicRule sourceRule = input.getRules().get(0);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule rewrittenRule = rewritten.getRules().get(0);
		assertOperatorNormalized(rewrittenRule, ComparisonOperator.LE, false);
		assertAggregateBoundDecremented(sourceRule, rewrittenRule);
	}

	@Test
	public void operatorNormalizationNENegative() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_NE_NEG_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule = RuleParser.parse("bla :- dom(X), X = #count{N : thing(N)}.");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule);
	}

	@Test
	public void operatorNormalizationGTNegative() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_GT_NEG_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule = RuleParser.parse("bla :- dom(X), X <= #count{N : thing(N)}.");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule);
	}

	@Test
	public void operatorNormalizationGENegative() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_GE_NEG_ASP);
		BasicRule sourceRule = input.getRules().get(0);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule rewrittenRule = rewritten.getRules().get(0);
		assertOperatorNormalized(rewrittenRule, ComparisonOperator.LE, true);
		assertAggregateBoundDecremented(sourceRule, rewrittenRule);
	}

	@Test
	public void bindingAggregatesSmokeTest() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
		System.out.println(aggregateRewriting.apply(alpha.readProgramString(COUNT_EQ_ASP)));
	}

	private static void assertOperatorNormalized(BasicRule rewrittenRule, ComparisonOperator expectedRewrittenOperator,
			boolean expectedRewrittenLiteralPositive) {
		AggregateLiteral rewrittenAggregate = null;
		for (Literal lit : rewrittenRule.getBody()) {
			if (lit instanceof AggregateLiteral) {
				rewrittenAggregate = (AggregateLiteral) lit;
			}
		}
		Assert.assertNotNull(rewrittenAggregate);
		Assert.assertEquals(expectedRewrittenOperator, rewrittenAggregate.getAtom().getLowerBoundOperator());
		Assert.assertTrue(expectedRewrittenLiteralPositive == !rewrittenAggregate.isNegated());
	}

	private static void assertAggregateBoundDecremented(BasicRule sourceRule, BasicRule rewrittenRule) {
		AggregateLiteral sourceAggregate = null;
		for (Literal lit : sourceRule.getBody()) {
			if (lit instanceof AggregateLiteral) {
				sourceAggregate = (AggregateLiteral) lit;
			}
		}
		AggregateLiteral rewrittenAggregate = null;
		ComparisonLiteral addedComparisonLiteral = null;
		for (Literal lit : rewrittenRule.getBody()) {
			if (lit instanceof AggregateLiteral) {
				rewrittenAggregate = (AggregateLiteral) lit;
			} else if (lit instanceof ComparisonLiteral) {
				addedComparisonLiteral = (ComparisonLiteral) lit;
			}
		}
		Assert.assertNotNull(addedComparisonLiteral);
		Assert.assertEquals(addedComparisonLiteral.getAtom().getTerms().get(0), rewrittenAggregate.getAtom().getLowerBoundTerm());
		Term comparisonRightHandTerm = addedComparisonLiteral.getAtom().getTerms().get(1);
		Assert.assertTrue(comparisonRightHandTerm instanceof ArithmeticTerm);
		ArithmeticTerm decrementTerm = (ArithmeticTerm) comparisonRightHandTerm;
		Assert.assertEquals(ArithmeticOperator.MINUS, decrementTerm.getArithmeticOperator());
		Assert.assertEquals(ConstantTerm.getInstance(1), decrementTerm.getRight());
		Assert.assertEquals(sourceAggregate.getAtom().getLowerBoundTerm(), decrementTerm.getLeft());
	}

}
