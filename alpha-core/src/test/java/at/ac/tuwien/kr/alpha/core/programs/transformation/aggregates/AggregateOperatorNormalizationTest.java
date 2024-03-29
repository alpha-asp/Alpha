package at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.literals.AggregateLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.programs.terms.ArithmeticOperator;
import at.ac.tuwien.kr.alpha.api.programs.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import at.ac.tuwien.kr.alpha.core.test.util.RuleParser;

public class AggregateOperatorNormalizationTest {

	//@formatter:off
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
	/**
	 * Operator normalization must also make sure that literals with only a right-hand term
	 * are normalized to left-hand term only (and then operator-normalized if necessary) 
	 * @see <a href="https://github.com/alpha-asp/alpha/issues/311">#311</a> 
	 */
	public static final String OPERATOR_NORMALIZATION_RIGHT_OP_ONLY =
			"bla :- dom(X), #count{N : thing(N)} < X.";
	public static final String OPERATOR_NORMALIZATION_RIGHT_OP_EQ =
			"bla :- #count{X : thing(X)} = Y.";
	// Check an AggregateFuntction that needs just literal flipping, but no rewriting
	public static final String OPERATOR_NORMALIZATION_RIGHT_OP_NO_REWRITE = 
			"bla :- #max{X : thing(X)} >= 4.";
	//@formatter:on

	@Test
	public void gtPositive() {
		Rule<Head> inputRule = RuleParser.parse(OPERATOR_NORMALIZATION_GT_POS_ASP);
		Rule<Head> rewritten = AggregateOperatorNormalization.normalize(inputRule);
		Rule<Head> expectedRewrittenRule = RuleParser.parse("bla :- dom(X), not X <= #count{N : thing(N)}.");
		assertEquals(expectedRewrittenRule, rewritten);
	}

	@Test
	public void ltPositive() {
		Rule<Head> inputRule = RuleParser.parse(OPERATOR_NORMALIZATION_LT_POS_ASP);
		Rule<Head> rewritten = AggregateOperatorNormalization.normalize(inputRule);
		assertOperatorNormalized(rewritten, ComparisonOperators.LE, true);
		assertAggregateBoundIncremented(inputRule, rewritten);
	}

	@Test
	public void nePositive() {
		Rule<Head> inputRule = RuleParser.parse(OPERATOR_NORMALIZATION_NE_POS_ASP);
		Rule<Head> rewritten = AggregateOperatorNormalization.normalize(inputRule);
		Rule<Head> expectedRewrittenRule = RuleParser.parse("bla :- dom(X), not X = #count{N : thing(N)}.");
		assertEquals(expectedRewrittenRule, rewritten);
	}

	@Test
	public void gePositive() {
		Rule<Head> inputRule = RuleParser.parse(OPERATOR_NORMALIZATION_GE_POS_ASP);
		Rule<Head> rewritten = AggregateOperatorNormalization.normalize(inputRule);
		assertOperatorNormalized(rewritten, ComparisonOperators.LE, false);
		assertAggregateBoundIncremented(inputRule, rewritten);
	}

	@Test
	public void ltNegative() {
		Rule<Head> inputRule = RuleParser.parse(OPERATOR_NORMALIZATION_LT_NEG_ASP);
		Rule<Head> rewritten = AggregateOperatorNormalization.normalize(inputRule);
		assertOperatorNormalized(rewritten, ComparisonOperators.LE, false);
		assertAggregateBoundIncremented(inputRule, rewritten);
	}

	@Test
	public void neNegative() {
		Rule<Head> inputRule = RuleParser.parse(OPERATOR_NORMALIZATION_NE_NEG_ASP);
		Rule<Head> rewritten = AggregateOperatorNormalization.normalize(inputRule);
		Rule<Head> expectedRewrittenRule = RuleParser.parse("bla :- dom(X), X = #count{N : thing(N)}.");
		assertEquals(expectedRewrittenRule, rewritten);
	}

	@Test
	public void gtNegative() {
		Rule<Head> inputRule = RuleParser.parse(OPERATOR_NORMALIZATION_GT_NEG_ASP);
		Rule<Head> rewritten = AggregateOperatorNormalization.normalize(inputRule);
		Rule<Head> expectedRewrittenRule = RuleParser.parse("bla :- dom(X), X <= #count{N : thing(N)}.");
		assertEquals(expectedRewrittenRule, rewritten);
	}

	@Test
	public void geNegative() {
		Rule<Head> inputRule = RuleParser.parse(OPERATOR_NORMALIZATION_GE_NEG_ASP);
		Rule<Head> rewritten = AggregateOperatorNormalization.normalize(inputRule);
		assertOperatorNormalized(rewritten, ComparisonOperators.LE, true);
		assertAggregateBoundIncremented(inputRule, rewritten);
	}

	@Test
	public void normalizeRightHandComparison() {
		Rule<Head> inputRule = RuleParser.parse(OPERATOR_NORMALIZATION_RIGHT_OP_ONLY);
		Rule<Head> rewritten = AggregateOperatorNormalization.normalize(inputRule);
		// literal gets flipped to "X > #count{N : thing(N)}",
		// then rewritten to "not X <= #count{N : thing(N)}"
		assertOperatorNormalized(rewritten, ComparisonOperators.LE, false);
	}

	@Test
	public void normalizeRightHandComparisonEq() {
		Rule<Head> inputRule = RuleParser.parse(OPERATOR_NORMALIZATION_RIGHT_OP_EQ);
		Rule<Head> rewritten = AggregateOperatorNormalization.normalize(inputRule);
		assertEquals(RuleParser.parse("bla :- Y = #count{X : thing(X)}."), rewritten);
	}

	@Test
	public void normalizeRightHandComparisonNoRewrite() {
		Rule<Head> inputRule = RuleParser.parse(OPERATOR_NORMALIZATION_RIGHT_OP_NO_REWRITE);
		Rule<Head> rewritten = AggregateOperatorNormalization.normalize(inputRule);
		assertEquals(RuleParser.parse("bla :- 4 <= #max{X : thing(X)}."), rewritten);
	}

	private static void assertOperatorNormalized(Rule<Head> rewrittenRule, ComparisonOperator expectedRewrittenOperator,
			boolean expectedRewrittenLiteralPositive) {
		AggregateLiteral rewrittenAggregate = null;
		for (Literal lit : rewrittenRule.getBody()) {
			if (lit instanceof AggregateLiteral) {
				rewrittenAggregate = (AggregateLiteral) lit;
			}
		}
		assertNotNull(rewrittenAggregate);
		assertEquals(expectedRewrittenOperator, rewrittenAggregate.getAtom().getLowerBoundOperator());
		assertTrue(expectedRewrittenLiteralPositive == !rewrittenAggregate.isNegated());
	}

	private static void assertAggregateBoundIncremented(Rule<Head> sourceRule, Rule<Head> rewrittenRule) {
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
		assertNotNull(addedComparisonLiteral);
		assertEquals(addedComparisonLiteral.getAtom().getTerms().get(0), rewrittenAggregate.getAtom().getLowerBoundTerm());
		Term comparisonRightHandTerm = addedComparisonLiteral.getAtom().getTerms().get(1);
		assertTrue(comparisonRightHandTerm instanceof ArithmeticTerm);
		ArithmeticTerm incrementTerm = (ArithmeticTerm) comparisonRightHandTerm;
		assertEquals(ArithmeticOperator.PLUS, incrementTerm.getOperator());
		assertEquals(Terms.newConstant(1), incrementTerm.getRightOperand());
		Term sourceBound = sourceAggregate.getAtom().getLowerBoundTerm() != null ? sourceAggregate.getAtom().getLowerBoundTerm()
				: sourceAggregate.getAtom().getUpperBoundTerm();
		assertEquals(sourceBound, incrementTerm.getLeftOperand());
	}

}
