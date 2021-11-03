package at.ac.tuwien.kr.alpha.api.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.AggregateLiteralSplitting;

// TODO this is a functional test that wants to be a unit test
public class AggregateLiteralSplittingTest {

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
	public void singlePositiveLiteral() {
		Rule<Head> inputRule = RuleParser.parse(LITERAL_SPLITTING_POS1_ASP);
		Rule<Head> expectedRewrittenRule = RuleParser
				.parse("count_between :- X < #count{N : thing2(N)}, Y > #count{N : thing2(N)}, X = #count{ K : thing1(K) }, Y = #count{ L : thing3(L) }.");
		List<Rule<Head>> rewritten = AggregateLiteralSplitting.split(inputRule);
		assertEquals(1, rewritten.size());
		assertEquals(expectedRewrittenRule, rewritten.get(0));
	}

	@Test
	public void multiplePositiveLiterals() {
		Rule<Head> inputRule = RuleParser.parse(LITERAL_SPLITTING_POS2_ASP);
		Rule<Head> expectedRewrittenRule = RuleParser
				.parse("count_between :- X < #count{N : thing2(N)}, Y > #count{N : thing2(N)}, Y < #count{L : thing3(L)}, Z = #count{L : thing3(L)}, dom(X), dom1(Y).");
		List<Rule<Head>> rewritten = AggregateLiteralSplitting.split(inputRule);
		assertEquals(1, rewritten.size());
		assertEquals(expectedRewrittenRule, rewritten.get(0));
	}

	@Test
	public void singleNegativeLiteral() {
		Rule<Head> inputRule = RuleParser.parse(LITERAL_SPLITTING_NEG1_ASP);
		Rule<Head> expectedRewrittenRule1 = RuleParser
				.parse("count_not_between :- dom(X), dom(Y), not X < #count{N : thing(N)}.");
		Rule<Head> expectedRewrittenRule2 = RuleParser
				.parse("count_not_between :- dom(X), dom(Y), not Y > #count{N : thing(N)}.");
		List<Rule<Head>> rewritten = AggregateLiteralSplitting.split(inputRule);
		assertEquals(2, rewritten.size());
		assertTrue(rewritten.contains(expectedRewrittenRule1));
		assertTrue(rewritten.contains(expectedRewrittenRule2));
	}

	@Test
	public void multipleNegativeLiterals() {
		Rule<Head> inputRule = RuleParser.parse(LITERAL_SPLITTING_NEG2_ASP);
		Rule<Head> expectedRewrittenRule1 = RuleParser
				.parse("count_not_between :- dom(X), dom(Y), dom(U), dom(V), not X < #count{N : thing(N)}, not U < #count{K : thing(K)}.");
		Rule<Head> expectedRewrittenRule2 = RuleParser
				.parse("count_not_between :- dom(X), dom(Y), dom(U), dom(V), not X < #count{N : thing(N)}, not V > #count{K : thing(K)}.");
		Rule<Head> expectedRewrittenRule3 = RuleParser
				.parse("count_not_between :- dom(X), dom(Y), dom(U), dom(V), not Y > #count{N : thing(N)}, not U < #count{K : thing(K)}.");
		Rule<Head> expectedRewrittenRule4 = RuleParser
				.parse("count_not_between :- dom(X), dom(Y), dom(U), dom(V), not Y > #count{N : thing(N)}, not V > #count{K : thing(K)}.");
		List<Rule<Head>> rewritten = AggregateLiteralSplitting.split(inputRule);
		assertEquals(4, rewritten.size());
		assertTrue(rewritten.contains(expectedRewrittenRule1));
		assertTrue(rewritten.contains(expectedRewrittenRule2));
		assertTrue(rewritten.contains(expectedRewrittenRule3));
		assertTrue(rewritten.contains(expectedRewrittenRule4));
	}

	@Test
	public void negativeAndPositiveLiteral() {
		Rule<Head> inputRule = RuleParser.parse(LITERAL_SPLITTING_NEG_POS_ASP);
		Rule<Head> expectedRewrittenRule1 = RuleParser
				.parse("count_between_and_not_between(U, V, X, Y) :- dom(X), dom(Y), dom(U), dom(V), not X < #count{N : thing(N)}, U < #count{K : thing(K)}, V > #count{K : thing(K)}.");
		Rule<Head> expectedRewrittenRule2 = RuleParser
				.parse("count_between_and_not_between(U, V, X, Y) :- dom(X), dom(Y), dom(U), dom(V), not Y > #count{N : thing(N)}, U < #count{K : thing(K)}, V > #count{K : thing(K)}.");
		List<Rule<Head>> rewritten = AggregateLiteralSplitting.split(inputRule);
		assertEquals(2, rewritten.size());
		assertTrue(rewritten.contains(expectedRewrittenRule1));
		assertTrue(rewritten.contains(expectedRewrittenRule2));
	}

}
