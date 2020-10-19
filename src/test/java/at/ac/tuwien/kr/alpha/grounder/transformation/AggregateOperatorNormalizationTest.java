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
	//@formatter:on

	@Test
	public void gtPositive() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateOperatorNormalization();
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_GT_POS_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule = RuleParser.parse("bla :- dom(X), not X <= #count{N : thing(N)}.");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule);
	}

	@Test
	public void ltPositive() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateOperatorNormalization();
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_LT_POS_ASP);
		BasicRule sourceRule = input.getRules().get(0);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule rewrittenRule = rewritten.getRules().get(0);
		assertOperatorNormalized(rewrittenRule, ComparisonOperator.LE, true);
		assertAggregateBoundDecremented(sourceRule, rewrittenRule);
	}

	@Test
	public void nePositive() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateOperatorNormalization();
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_NE_POS_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule = RuleParser.parse("bla :- dom(X), not X = #count{N : thing(N)}.");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule);
	}

	@Test
	public void gePositive() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateOperatorNormalization();
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_GE_POS_ASP);
		BasicRule sourceRule = input.getRules().get(0);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule rewrittenRule = rewritten.getRules().get(0);
		assertOperatorNormalized(rewrittenRule, ComparisonOperator.LE, false);
		assertAggregateBoundDecremented(sourceRule, rewrittenRule);
	}

	@Test
	public void ltNegative() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateOperatorNormalization();
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_LT_NEG_ASP);
		BasicRule sourceRule = input.getRules().get(0);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule rewrittenRule = rewritten.getRules().get(0);
		assertOperatorNormalized(rewrittenRule, ComparisonOperator.LE, false);
		assertAggregateBoundDecremented(sourceRule, rewrittenRule);
	}

	@Test
	public void neNegative() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateOperatorNormalization();
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_NE_NEG_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule = RuleParser.parse("bla :- dom(X), X = #count{N : thing(N)}.");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule);
	}

	@Test
	public void gtNegative() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateOperatorNormalization();
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_GT_NEG_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule = RuleParser.parse("bla :- dom(X), X <= #count{N : thing(N)}.");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule);
	}

	@Test
	public void geNegative() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateOperatorNormalization();
		InputProgram input = alpha.readProgramString(OPERATOR_NORMALIZATION_GE_NEG_ASP);
		BasicRule sourceRule = input.getRules().get(0);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule rewrittenRule = rewritten.getRules().get(0);
		assertOperatorNormalized(rewrittenRule, ComparisonOperator.LE, true);
		assertAggregateBoundDecremented(sourceRule, rewrittenRule);
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
