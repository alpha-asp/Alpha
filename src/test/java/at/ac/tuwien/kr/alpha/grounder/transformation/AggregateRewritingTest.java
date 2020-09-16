package at.ac.tuwien.kr.alpha.grounder.transformation;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
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
	//@formatter:on

	@Test
	public void literalSplittingSinglePositiveLiteral() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
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
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
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
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
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
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
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
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
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

}
