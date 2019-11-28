package at.ac.tuwien.kr.alpha.grounder.transformation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.Alpha;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.impl.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.NormalProgram;
import at.ac.tuwien.kr.alpha.test.util.TestUtils;

@RunWith(Parameterized.class)
public class PartialEvaluationRegressionTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartialEvaluationRegressionTest.class);

	private static final String STRATIFIED_NEG_ASP = 
			"base(X) :- req(X), not incomp(X).\n"
			+ "depend_base(X, Y) :- base(X), base(Y).\n"
			+ "dep_b_hlp(X) :- depend_base(X, _).\n"
			+ "fallback_base(X) :- base(X), not dep_b_hlp(X).\n"
			+ "depend_further(X) :- depend_base(_, X).\n"
			+ "depend_further(X) :- fallback_base(X).";
	
	private static final String BASIC_TEST_ASP = "a. b:- a.";
	private static final String BASIC_MULTI_INSTANCE_ASP = "p(a). p(b). q(X) :- p(X).";
	private static final String BASIC_NEGATION_ASP = "p(a). q(b). p(c). q(d). r(c). s(X, Y) :- p(X), q(Y), not r(X).";
	private static final String PART_STRATIFIED_ASP = "p(a). q(a). p(b). m(c). n(d).\n" + "r(X) :- p(X), q(X).\n" + "s(X, Y, Z) :- r(X), m(Y), n(Z).\n"
			+ "t(X, Y) :- p(X), q(X), p(Y), not q(Y).\n" + "either(X) :- t(X, _), not or(X).\n" + "or(X) :- t(X, _), not either(X).";
	private static final String POSITIVE_RECURSION_ASP = "num(0).\n" + "max_num(10).\n" + "num(S) :- num(N), S = N + 1, S <= M, max_num(M).";
	private static final String EMPTY_PROG_ASP = "";
	private static final String FACTS_ONLY_ASP = "a. b. c. p(a). q(b, c). r(c, c, a). s(b).";
	private static final String STRATIFIED_NO_FACTS_ASP = STRATIFIED_NEG_ASP;
	private static final String STRATIFIED_W_FACTS_ASP = "req(a). req(b). incomp(b).\n" + STRATIFIED_NEG_ASP;
	private static final String EQUALITY_ASP = "equal :- 1 = 1.";
	private static final String EQUALITY_WITH_VAR_ASP = "a(1). a(2). a(3). b(X) :- a(X), X = 1. c(X) :- a(X), X = 2. d(X) :- X = 3, a(X).";
	

	private static final ImmutablePair<Consumer<InternalProgram>, Consumer<Set<AnswerSet>>> BASIC_VERIFIERS = new ImmutablePair<>(
			PartialEvaluationRegressionTest::verifyProgramBasic, PartialEvaluationRegressionTest::verifyAnswerSetsBasic);
	private static final ImmutablePair<Consumer<InternalProgram>, Consumer<Set<AnswerSet>>> BASIC_MULTI_INSTANCE_VERIFIERS = new ImmutablePair<>(
			PartialEvaluationRegressionTest::verifyProgramBasicMultiInstance, PartialEvaluationRegressionTest::verifyAnswerSetsBasicMultiInstance);
	private static final ImmutablePair<Consumer<InternalProgram>, Consumer<Set<AnswerSet>>> BASIC_NEGATION_VERIFIERS = new ImmutablePair<>(
			PartialEvaluationRegressionTest::verifyProgramBasicNegation, PartialEvaluationRegressionTest::verifyAnswerSetsBasicNegation);
	private static final ImmutablePair<Consumer<InternalProgram>, Consumer<Set<AnswerSet>>> PART_STRATIFIED_VERIFIERS = new ImmutablePair<>(
			PartialEvaluationRegressionTest::verifyProgramPartStratified, PartialEvaluationRegressionTest::verifyAnswerSetsPartStratified);
	private static final ImmutablePair<Consumer<InternalProgram>, Consumer<Set<AnswerSet>>> POSITIVE_RECURSIVE_VERIFIERS = new ImmutablePair<>(
			PartialEvaluationRegressionTest::verifyProgramPositiveRecursive, PartialEvaluationRegressionTest::verifyAnswerSetsPositiveRecursive);
	private static final ImmutablePair<Consumer<InternalProgram>, Consumer<Set<AnswerSet>>> EMPTY_PROG_VERIFIERS = new ImmutablePair<>(
			PartialEvaluationRegressionTest::verifyProgramEmptyProg, PartialEvaluationRegressionTest::verifyAnswerSetsEmptyProg);
	private static final ImmutablePair<Consumer<InternalProgram>, Consumer<Set<AnswerSet>>> FACTS_ONLY_VERIFIERS = new ImmutablePair<>(
			PartialEvaluationRegressionTest::verifyProgramFactsOnly, PartialEvaluationRegressionTest::verifyAnswerSetsFactsOnly);
	private static final ImmutablePair<Consumer<InternalProgram>, Consumer<Set<AnswerSet>>> STRATIFIED_NO_FACTS_VERIFIERS = new ImmutablePair<>(
			PartialEvaluationRegressionTest::verifyProgramStratNoFacts, PartialEvaluationRegressionTest::verifyAnswerSetsStratNoFacts);
	private static final ImmutablePair<Consumer<InternalProgram>, Consumer<Set<AnswerSet>>> STRATIFIED_W_FACTS_VERIFIERS = new ImmutablePair<>(
			PartialEvaluationRegressionTest::verifyProgramStratWithFacts, PartialEvaluationRegressionTest::verifyAnswerSetsStratWithFacts);
	private static final ImmutablePair<Consumer<InternalProgram>, Consumer<Set<AnswerSet>>> EQUALITY_VERIFIERS = new ImmutablePair<>(
			PartialEvaluationRegressionTest::verifyProgramEquality, PartialEvaluationRegressionTest::verifyAnswerSetsEquality);
	private static final ImmutablePair<Consumer<InternalProgram>, Consumer<Set<AnswerSet>>> EQUALITY_WITH_VAR_VERIFIERS = new ImmutablePair<>(
			PartialEvaluationRegressionTest::verifyProgramEqualityWithVar, PartialEvaluationRegressionTest::verifyAnswerSetsEqualityWithVar);
	
	
	@Parameters(name = "Run {index}: aspString={0}, programVerifier={1}, answerSetsVerifier={2}")
	public static Iterable<Object[]> params() {
		List<ImmutablePair<String, ImmutablePair<Consumer<InternalProgram>, Consumer<Set<AnswerSet>>>>> testCases = new ArrayList<>();
		List<Object[]> paramList = new ArrayList<>();
		testCases.add(new ImmutablePair<>(BASIC_TEST_ASP, BASIC_VERIFIERS));
		testCases.add(new ImmutablePair<>(BASIC_MULTI_INSTANCE_ASP, BASIC_MULTI_INSTANCE_VERIFIERS));
		testCases.add(new ImmutablePair<>(BASIC_NEGATION_ASP, BASIC_NEGATION_VERIFIERS));
		testCases.add(new ImmutablePair<>(PART_STRATIFIED_ASP, PART_STRATIFIED_VERIFIERS));
		testCases.add(new ImmutablePair<>(POSITIVE_RECURSION_ASP, POSITIVE_RECURSIVE_VERIFIERS));
		testCases.add(new ImmutablePair<>(EMPTY_PROG_ASP, EMPTY_PROG_VERIFIERS));
		testCases.add(new ImmutablePair<>(FACTS_ONLY_ASP, FACTS_ONLY_VERIFIERS));
		testCases.add(new ImmutablePair<>(STRATIFIED_NO_FACTS_ASP, STRATIFIED_NO_FACTS_VERIFIERS));
		testCases.add(new ImmutablePair<>(STRATIFIED_W_FACTS_ASP, STRATIFIED_W_FACTS_VERIFIERS));
		testCases.add(new ImmutablePair<>(EQUALITY_ASP, EQUALITY_VERIFIERS));
		testCases.add(new ImmutablePair<>(EQUALITY_WITH_VAR_ASP, EQUALITY_WITH_VAR_VERIFIERS));

		testCases.forEach((pair) -> paramList.add(new Object[] {pair.left, pair.right.left, pair.right.right }));
		return paramList;
	}

	private String aspString;
	private Consumer<InternalProgram> programVerifier;
	private Consumer<Set<AnswerSet>> answerSetsVerifier;

	public PartialEvaluationRegressionTest(String aspString, Consumer<InternalProgram> programVerifier, Consumer<Set<AnswerSet>> answerSetsVerifier) {
		this.aspString = aspString;
		this.programVerifier = programVerifier;
		this.answerSetsVerifier = answerSetsVerifier;
	}

	@Test
	public void runTest() {
		String aspStr = this.aspString;
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new PartialEvaluation().apply(analyzed);
		this.programVerifier.accept(evaluated);
		Set<AnswerSet> answerSets = system.solve(evaluated).collect(Collectors.toSet());
		this.answerSetsVerifier.accept(answerSets);
	}

	private static void verifyProgramBasic(InternalProgram evaluated) {
		TestUtils.assertFactsContainedInProgram(evaluated, BasicAtom.newInstance("a"), BasicAtom.newInstance("b"));
		Assert.assertEquals(2, evaluated.getFacts().size());
		Assert.assertTrue(evaluated.getRules().size() == 0);
	}

	private static void verifyAnswerSetsBasic(Set<AnswerSet> answerSets) {
		TestUtils.assertAnswerSetsEqual("a, b", answerSets);
	}

	private static void verifyProgramBasicMultiInstance(InternalProgram evaluated) {
		TestUtils.assertFactsContainedInProgram(evaluated, BasicAtom.newInstance("q", "a"), BasicAtom.newInstance("q", "b"));
		Assert.assertTrue(evaluated.getRules().size() == 0);
	}

	private static void verifyAnswerSetsBasicMultiInstance(Set<AnswerSet> answerSets) {
		TestUtils.assertAnswerSetsEqual("p(a), p(b), q(a), q(b)", answerSets);
	}

	private static void verifyProgramBasicNegation(InternalProgram evaluated) {
		TestUtils.assertFactsContainedInProgram(evaluated, BasicAtom.newInstance("s", "a", "b"), BasicAtom.newInstance("s", "a", "d"));
		Assert.assertEquals(7, evaluated.getFacts().size());
		Assert.assertEquals(0, evaluated.getRules().size());
	}

	private static void verifyAnswerSetsBasicNegation(Set<AnswerSet> answerSets) {
		TestUtils.assertAnswerSetsEqual("p(a), q(b), p(c), q(d), r(c), s(a,b), s(a,d)", answerSets);
	}

	private static void verifyProgramPartStratified(InternalProgram evaluated) {
		TestUtils.assertFactsContainedInProgram(evaluated, BasicAtom.newInstance("p", "a"), BasicAtom.newInstance("q", "a"), BasicAtom.newInstance("p", "b"),
				BasicAtom.newInstance("m", "c"), BasicAtom.newInstance("n", "d"), BasicAtom.newInstance("r", "a"), BasicAtom.newInstance("s", "a", "c", "d"),
				BasicAtom.newInstance("t", "a", "b"));
		LOGGER.debug("part stratified evaluated prog is:\n{}", evaluated.toString());
		Assert.assertEquals(2, evaluated.getRules().size());
	}

	private static void verifyAnswerSetsPartStratified(Set<AnswerSet> answerSets) {
		TestUtils.assertAnswerSetsEqual(new String[] {"p(a), q(a), p(b), m(c), n(d), r(a), s(a,c,d), t(a,b), either(a)",
				"p(a), q(a), p(b), m(c), n(d), r(a), s(a,c,d), t(a,b), or(a)" }, answerSets);
	}

	private static void verifyProgramPositiveRecursive(InternalProgram evaluated) {
		TestUtils.assertFactsContainedInProgram(evaluated, BasicAtom.newInstance("max_num", "10"), BasicAtom.newInstance("num", "0"),
				BasicAtom.newInstance("num", "1"), BasicAtom.newInstance("num", "2"), BasicAtom.newInstance("num", "3"), BasicAtom.newInstance("num", "4"),
				BasicAtom.newInstance("num", "5"), BasicAtom.newInstance("num", "6"), BasicAtom.newInstance("num", "7"), BasicAtom.newInstance("num", "8"),
				BasicAtom.newInstance("num", "9"), BasicAtom.newInstance("num", "10"));
		LOGGER.debug("Recursive program evaluated is:\n{}", evaluated.toString());
		Assert.assertEquals(0, evaluated.getRules().size());
	}

	private static void verifyAnswerSetsPositiveRecursive(Set<AnswerSet> answerSets) {
		TestUtils.assertAnswerSetsEqual("max_num(10), num(0), num(1), num(2), num(3), num(4), num(5), num(6), num(7), num(8), num(9), num(10)", answerSets);
	}

	private static void verifyProgramEmptyProg(InternalProgram evaluated) {
		Assert.assertTrue(evaluated.getRules().isEmpty());
		Assert.assertTrue(evaluated.getRulesById().isEmpty());
		Assert.assertTrue(evaluated.getPredicateDefiningRules().isEmpty());
		Assert.assertTrue(evaluated.getFacts().isEmpty());
		Assert.assertTrue(evaluated.getFactsByPredicate().isEmpty());
	}

	private static void verifyAnswerSetsEmptyProg(Set<AnswerSet> answerSets) {
		Assert.assertEquals(1, answerSets.size());
		Assert.assertTrue(answerSets.iterator().next().isEmpty());
	}

	private static void verifyProgramFactsOnly(InternalProgram evaluated) {
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("a")));
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("b")));
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("c")));
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("p", "a")));
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("q", "b", "c")));
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("r", "c", "c", "a")));
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("s", "b")));
	}

	private static void verifyAnswerSetsFactsOnly(Set<AnswerSet> answerSets) {
		TestUtils.assertAnswerSetsEqual("a, b, c, p(a), q(b,c), r(c,c,a), s(b)", answerSets);
	}

	private static void verifyProgramStratNoFacts(InternalProgram evaluated) {
		Assert.assertTrue(evaluated.getFacts().isEmpty());
	}
	
	private static void verifyAnswerSetsStratNoFacts(Set<AnswerSet> answerSets) {
		Assert.assertEquals(1, answerSets.size());
		Assert.assertTrue(answerSets.iterator().next().isEmpty());
	}
	
	private static void verifyProgramStratWithFacts(InternalProgram evaluated) {
		// rules should all be taken care of at this point
		Assert.assertTrue(evaluated.getRules().isEmpty());
		Assert.assertTrue(evaluated.getRulesById().isEmpty());
		Assert.assertTrue(evaluated.getPredicateDefiningRules().isEmpty());
		
		// facts should be the full answer set
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("req", "a")));
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("req", "b")));
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("incomp", "b")));
		
		// below facts from stratified evaluation
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("base", "a")));
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("depend_base", "a", "a")));
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("dep_b_hlp", "a")));
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("depend_further", "a")));
	}
	
	private static void verifyAnswerSetsStratWithFacts(Set<AnswerSet> answerSets) {
		TestUtils.assertAnswerSetsEqual("req(a), req(b), incomp(b), base(a), depend_base(a,a), dep_b_hlp(a), depend_further(a)", answerSets);
	}
	
	private static void verifyProgramEquality(InternalProgram evaluated) {
		Assert.assertEquals(0, evaluated.getRules().size());
		Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("equal")));
	}
	
	private static void verifyAnswerSetsEquality(Set<AnswerSet> answerSets) {
		TestUtils.assertAnswerSetsEqual("equal", answerSets);
	}
	
	private static void verifyProgramEqualityWithVar(InternalProgram evaluated) {
		Assert.assertEquals(0, evaluated.getRules().size());
		// TODO convenience method for atoms with numeric terms
		//Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("b", "1")));
		//Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("c", "2")));
		//Assert.assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("d", "3")));
	}
	
	private static void verifyAnswerSetsEqualityWithVar(Set<AnswerSet> answerSets) {
		TestUtils.assertAnswerSetsEqual("a(1), a(2), a(3), b(1), c(2), d(3)", answerSets);
	}
	
	// TODO more arithmetics and builtins (equality, etc.)

}
