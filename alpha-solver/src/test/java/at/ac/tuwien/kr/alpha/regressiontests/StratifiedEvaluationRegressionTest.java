package at.ac.tuwien.kr.alpha.regressiontests;

import static at.ac.tuwien.kr.alpha.test.AlphaAssertions.assertAnswerSetsEqual;
import static at.ac.tuwien.kr.alpha.test.AlphaAssertions.assertFactsContainedInProgram;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.DebugSolvingContext;
import at.ac.tuwien.kr.alpha.api.Solver;
import at.ac.tuwien.kr.alpha.api.impl.AlphaFactory;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;

// TODO This is a functional test and should not be run with standard unit tests
public class StratifiedEvaluationRegressionTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(StratifiedEvaluationRegressionTest.class);

	//@formatter:off
	private static final String STRATIFIED_NEG_ASP = "base(X) :- req(X), not incomp(X).\n"
			+ "depend_base(X, Y) :- base(X), base(Y).\n"
			+ "dep_b_hlp(X) :- depend_base(X, _).\n"
			+ "fallback_base(X) :- base(X), not dep_b_hlp(X).\n"
			+ "depend_further(X) :- depend_base(_, X).\n"
			+ "depend_further(X) :- fallback_base(X).";

	private static final String BASIC_TEST_ASP = "a. b:- a.";
	private static final String BASIC_MULTI_INSTANCE_ASP = "p(a). p(b). q(X) :- p(X).";
	private static final String BASIC_NEGATION_ASP = "p(a). q(b). p(c). q(d). r(c). s(X, Y) :- p(X), q(Y), not r(X).";
	private static final String PART_STRATIFIED_ASP = 
			"p(a). q(a). p(b). m(c). n(d).\n" + 
			"r(X) :- p(X), q(X).\n" + 
			"s(X, Y, Z) :- r(X), m(Y), n(Z).\n" + 
			"t(X, Y) :- p(X), q(X), p(Y), not q(Y).\n" + 
			"either(X) :- t(X, _), not or(X).\n" + 
			"or(X) :- t(X, _), not either(X).";
	private static final String POSITIVE_RECURSION_ASP = 
			"num(0).\n" + 
			"max_num(10).\n" + 
			"num(S) :- num(N), S = N + 1, S <= M, max_num(M).";
	private static final String EMPTY_PROG_ASP = "";
	private static final String FACTS_ONLY_ASP = "a. b. c. p(a). q(b, c). r(c, c, a). s(b).";
	private static final String STRATIFIED_NO_FACTS_ASP = STRATIFIED_NEG_ASP;
	private static final String STRATIFIED_W_FACTS_ASP = "req(a). req(b). incomp(b).\n" + STRATIFIED_NEG_ASP;
	private static final String EQUALITY_ASP = "equal :- 1 = 1.";
	private static final String EQUALITY_WITH_VAR_ASP = "a(1). a(2). a(3). b(X) :- a(X), X = 1. c(X) :- a(X), X = 2. d(X) :- X = 3, a(X).";

	private static final ImmutablePair<Consumer<NormalProgram>, Consumer<Set<AnswerSet>>> BASIC_VERIFIERS = new ImmutablePair<>(
			StratifiedEvaluationRegressionTest::verifyProgramBasic, StratifiedEvaluationRegressionTest::verifyAnswerSetsBasic);
	private static final ImmutablePair<Consumer<NormalProgram>, Consumer<Set<AnswerSet>>> BASIC_MULTI_INSTANCE_VERIFIERS = new ImmutablePair<>(
			StratifiedEvaluationRegressionTest::verifyProgramBasicMultiInstance, StratifiedEvaluationRegressionTest::verifyAnswerSetsBasicMultiInstance);
	private static final ImmutablePair<Consumer<NormalProgram>, Consumer<Set<AnswerSet>>> BASIC_NEGATION_VERIFIERS = new ImmutablePair<>(
			StratifiedEvaluationRegressionTest::verifyProgramBasicNegation, StratifiedEvaluationRegressionTest::verifyAnswerSetsBasicNegation);
	private static final ImmutablePair<Consumer<NormalProgram>, Consumer<Set<AnswerSet>>> PART_STRATIFIED_VERIFIERS = new ImmutablePair<>(
			StratifiedEvaluationRegressionTest::verifyProgramPartStratified, StratifiedEvaluationRegressionTest::verifyAnswerSetsPartStratified);
	private static final ImmutablePair<Consumer<NormalProgram>, Consumer<Set<AnswerSet>>> POSITIVE_RECURSIVE_VERIFIERS = new ImmutablePair<>(
			StratifiedEvaluationRegressionTest::verifyProgramPositiveRecursive, StratifiedEvaluationRegressionTest::verifyAnswerSetsPositiveRecursive);
	private static final ImmutablePair<Consumer<NormalProgram>, Consumer<Set<AnswerSet>>> EMPTY_PROG_VERIFIERS = new ImmutablePair<>(
			StratifiedEvaluationRegressionTest::verifyProgramEmptyProg, StratifiedEvaluationRegressionTest::verifyAnswerSetsEmptyProg);
	private static final ImmutablePair<Consumer<NormalProgram>, Consumer<Set<AnswerSet>>> FACTS_ONLY_VERIFIERS = new ImmutablePair<>(
			StratifiedEvaluationRegressionTest::verifyProgramFactsOnly, StratifiedEvaluationRegressionTest::verifyAnswerSetsFactsOnly);
	private static final ImmutablePair<Consumer<NormalProgram>, Consumer<Set<AnswerSet>>> STRATIFIED_NO_FACTS_VERIFIERS = new ImmutablePair<>(
			StratifiedEvaluationRegressionTest::verifyProgramStratNoFacts, StratifiedEvaluationRegressionTest::verifyAnswerSetsStratNoFacts);
	private static final ImmutablePair<Consumer<NormalProgram>, Consumer<Set<AnswerSet>>> STRATIFIED_W_FACTS_VERIFIERS = new ImmutablePair<>(
			StratifiedEvaluationRegressionTest::verifyProgramStratWithFacts, StratifiedEvaluationRegressionTest::verifyAnswerSetsStratWithFacts);
	private static final ImmutablePair<Consumer<NormalProgram>, Consumer<Set<AnswerSet>>> EQUALITY_VERIFIERS = new ImmutablePair<>(
			StratifiedEvaluationRegressionTest::verifyProgramEquality, StratifiedEvaluationRegressionTest::verifyAnswerSetsEquality);
	private static final ImmutablePair<Consumer<NormalProgram>, Consumer<Set<AnswerSet>>> EQUALITY_WITH_VAR_VERIFIERS = new ImmutablePair<>(
			StratifiedEvaluationRegressionTest::verifyProgramEqualityWithVar, StratifiedEvaluationRegressionTest::verifyAnswerSetsEqualityWithVar);

	public static List<Arguments> params() {
		List<ImmutablePair<String, ImmutablePair<Consumer<NormalProgram>, Consumer<Set<AnswerSet>>>>> testCases = new ArrayList<>();
		List<Arguments> paramList = new ArrayList<>();
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

		testCases.forEach((pair) -> paramList.add(Arguments.of(pair.left, pair.right.left, pair.right.right)));
		return paramList;
	}
	//@formatter:on

	@ParameterizedTest
	@MethodSource("at.ac.tuwien.kr.alpha.regressiontests.StratifiedEvaluationRegressionTest#params")
	public void runTest(String aspString, Consumer<NormalProgram> programVerifier,
			Consumer<Set<AnswerSet>> resultVerifier) {
		LOGGER.debug("Testing ASP String {}", aspString);
		// Parse and pre-evaluate program
		// Alpha instance with default config, stratified evaluation enabled
		Alpha alpha = AlphaFactory.newAlpha();
		InputProgram input = alpha.readProgramString(aspString);
		DebugSolvingContext dbgInfo = alpha.prepareDebugSolve(input);
		NormalProgram evaluated = dbgInfo.getPreprocessedProgram();
		// Verify stratified evaluation result
		programVerifier.accept(evaluated);
		// Solve remaining program
		Solver solver = dbgInfo.getSolver();
		Set<AnswerSet> answerSets = solver.collectSet();
		resultVerifier.accept(answerSets);
	}

	private static void verifyProgramBasic(NormalProgram evaluated) {
		assertFactsContainedInProgram(evaluated, Atoms.newBasicAtom(Predicates.getPredicate("a", 0)),
				Atoms.newBasicAtom(Predicates.getPredicate("b", 0)));
		assertEquals(2, evaluated.getFacts().size());
		assertTrue(evaluated.getRules().size() == 0);
	}

	private static void verifyAnswerSetsBasic(Set<AnswerSet> answerSets) {
		assertAnswerSetsEqual("a, b", answerSets);
	}

	private static void verifyProgramBasicMultiInstance(NormalProgram evaluated) {
		assertFactsContainedInProgram(evaluated,
				Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newSymbolicConstant("a")),
				Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newSymbolicConstant("b")));
		assertTrue(evaluated.getRules().size() == 0);
	}

	private static void verifyAnswerSetsBasicMultiInstance(Set<AnswerSet> answerSets) {
		assertAnswerSetsEqual("p(a), p(b), q(a), q(b)", answerSets);
	}

	private static void verifyProgramBasicNegation(NormalProgram evaluated) {
		assertFactsContainedInProgram(evaluated,
				Atoms.newBasicAtom(Predicates.getPredicate("s", 2), Terms.newSymbolicConstant("a"),
						Terms.newSymbolicConstant("b")),
				Atoms.newBasicAtom(Predicates.getPredicate("s", 2), Terms.newSymbolicConstant("a"),
						Terms.newSymbolicConstant("d")));
		assertEquals(7, evaluated.getFacts().size());
		assertEquals(0, evaluated.getRules().size());
	}

	private static void verifyAnswerSetsBasicNegation(Set<AnswerSet> answerSets) {
		assertAnswerSetsEqual("p(a), q(b), p(c), q(d), r(c), s(a,b), s(a,d)", answerSets);
	}

	private static void verifyProgramPartStratified(NormalProgram evaluated) {
		LOGGER.debug("part stratified evaluated prog is:\n{}", evaluated.toString());
		assertFactsContainedInProgram(evaluated,
				Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newSymbolicConstant("a")),
				Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newSymbolicConstant("a")),
				Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newSymbolicConstant("b")),
				Atoms.newBasicAtom(Predicates.getPredicate("m", 1), Terms.newSymbolicConstant("c")),
				Atoms.newBasicAtom(Predicates.getPredicate("n", 1), Terms.newSymbolicConstant("d")),
				Atoms.newBasicAtom(Predicates.getPredicate("r", 1), Terms.newSymbolicConstant("a")),
				Atoms.newBasicAtom(Predicates.getPredicate("s", 3), Terms.newSymbolicConstant("a"),
						Terms.newSymbolicConstant("c"), Terms.newSymbolicConstant("d")),
				Atoms.newBasicAtom(Predicates.getPredicate("t", 2), Terms.newSymbolicConstant("a"),
						Terms.newSymbolicConstant("b")));
		assertEquals(2, evaluated.getRules().size());
	}

	private static void verifyAnswerSetsPartStratified(Set<AnswerSet> answerSets) {
		assertAnswerSetsEqual(new String[] {"p(a), q(a), p(b), m(c), n(d), r(a), s(a,c,d), t(a,b), either(a)",
				"p(a), q(a), p(b), m(c), n(d), r(a), s(a,c,d), t(a,b), or(a)" }, answerSets);
	}

	private static void verifyProgramPositiveRecursive(NormalProgram evaluated) {
		Predicate num = Predicates.getPredicate("num", 1);
		assertFactsContainedInProgram(evaluated,
				Atoms.newBasicAtom(Predicates.getPredicate("max_num", 1), Terms.newConstant(10)),
				Atoms.newBasicAtom(num, Terms.newConstant(0)), Atoms.newBasicAtom(num, Terms.newConstant(1)),
				Atoms.newBasicAtom(num, Terms.newConstant(2)), Atoms.newBasicAtom(num, Terms.newConstant(3)),
				Atoms.newBasicAtom(num, Terms.newConstant(4)), Atoms.newBasicAtom(num, Terms.newConstant(5)),
				Atoms.newBasicAtom(num, Terms.newConstant(6)), Atoms.newBasicAtom(num, Terms.newConstant(7)),
				Atoms.newBasicAtom(num, Terms.newConstant(8)), Atoms.newBasicAtom(num, Terms.newConstant(9)),
				Atoms.newBasicAtom(num, Terms.newConstant(10)));
		LOGGER.debug("Recursive program evaluated is:\n{}", evaluated.toString());
		assertEquals(0, evaluated.getRules().size());
	}

	private static void verifyAnswerSetsPositiveRecursive(Set<AnswerSet> answerSets) {
		assertAnswerSetsEqual(
				"max_num(10), num(0), num(1), num(2), num(3), num(4), num(5), num(6), num(7), num(8), num(9), num(10)",
				answerSets);
	}

	private static void verifyProgramEmptyProg(NormalProgram evaluated) {
		assertTrue(evaluated.getRules().isEmpty());
		assertTrue(evaluated.getFacts().isEmpty());
	}

	private static void verifyAnswerSetsEmptyProg(Set<AnswerSet> answerSets) {
		assertEquals(1, answerSets.size());
		assertTrue(answerSets.iterator().next().isEmpty());
	}

	private static void verifyProgramFactsOnly(NormalProgram evaluated) {
		assertTrue(evaluated.getFacts().contains(Atoms.newBasicAtom(Predicates.getPredicate("a", 0))));
		assertTrue(evaluated.getFacts().contains(Atoms.newBasicAtom(Predicates.getPredicate("b", 0))));
		assertTrue(evaluated.getFacts().contains(Atoms.newBasicAtom(Predicates.getPredicate("c", 0))));
		assertTrue(evaluated.getFacts()
				.contains(Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newSymbolicConstant("a"))));
		assertTrue(evaluated.getFacts().contains(Atoms.newBasicAtom(Predicates.getPredicate("q", 2),
				Terms.newSymbolicConstant("b"), Terms.newSymbolicConstant("c"))));
		assertTrue(evaluated.getFacts().contains(Atoms.newBasicAtom(Predicates.getPredicate("r", 3),
				Terms.newSymbolicConstant("c"), Terms.newSymbolicConstant("c"), Terms.newSymbolicConstant("a"))));
		assertTrue(evaluated.getFacts()
				.contains(Atoms.newBasicAtom(Predicates.getPredicate("s", 1), Terms.newSymbolicConstant("b"))));
	}

	private static void verifyAnswerSetsFactsOnly(Set<AnswerSet> answerSets) {
		assertAnswerSetsEqual("a, b, c, p(a), q(b,c), r(c,c,a), s(b)", answerSets);
	}

	private static void verifyProgramStratNoFacts(NormalProgram evaluated) {
		assertTrue(evaluated.getFacts().isEmpty());
	}

	private static void verifyAnswerSetsStratNoFacts(Set<AnswerSet> answerSets) {
		assertEquals(1, answerSets.size());
		assertTrue(answerSets.iterator().next().isEmpty());
	}

	private static void verifyProgramStratWithFacts(NormalProgram evaluated) {
		// rules should all be taken care of at this point
		assertTrue(evaluated.getRules().isEmpty());

		// facts should be the full answer set
		assertTrue(evaluated.getFacts()
				.contains(Atoms.newBasicAtom(Predicates.getPredicate("req", 1), Terms.newSymbolicConstant("a"))));
		assertTrue(evaluated.getFacts()
				.contains(Atoms.newBasicAtom(Predicates.getPredicate("req", 1), Terms.newSymbolicConstant("b"))));
		assertTrue(evaluated.getFacts()
				.contains(Atoms.newBasicAtom(Predicates.getPredicate("incomp", 1), Terms.newSymbolicConstant("b"))));

		// below facts from stratified evaluation
		assertTrue(evaluated.getFacts()
				.contains(Atoms.newBasicAtom(Predicates.getPredicate("base", 1), Terms.newSymbolicConstant("a"))));
		assertTrue(evaluated.getFacts().contains(Atoms.newBasicAtom(Predicates.getPredicate("depend_base", 2),
				Terms.newSymbolicConstant("a"), Terms.newSymbolicConstant("a"))));
		assertTrue(evaluated.getFacts()
				.contains(Atoms.newBasicAtom(Predicates.getPredicate("dep_b_hlp", 1), Terms.newSymbolicConstant("a"))));
		assertTrue(evaluated.getFacts().contains(
				Atoms.newBasicAtom(Predicates.getPredicate("depend_further", 1), Terms.newSymbolicConstant("a"))));
	}

	private static void verifyAnswerSetsStratWithFacts(Set<AnswerSet> answerSets) {
		assertAnswerSetsEqual("req(a), req(b), incomp(b), base(a), depend_base(a,a), dep_b_hlp(a), depend_further(a)",
				answerSets);
	}

	private static void verifyProgramEquality(NormalProgram evaluated) {
		assertEquals(0, evaluated.getRules().size());
		assertTrue(evaluated.getFacts().contains(Atoms.newBasicAtom(Predicates.getPredicate("equal", 0))));
	}

	private static void verifyAnswerSetsEquality(Set<AnswerSet> answerSets) {
		assertAnswerSetsEqual("equal", answerSets);
	}

	private static void verifyProgramEqualityWithVar(NormalProgram evaluated) {
		assertEquals(0, evaluated.getRules().size());
		assertTrue(evaluated.getFacts()
				.contains(Atoms.newBasicAtom(Predicates.getPredicate("a", 1), Terms.newConstant(1))));
		assertTrue(evaluated.getFacts()
				.contains(Atoms.newBasicAtom(Predicates.getPredicate("c", 1), Terms.newConstant(2))));
		assertTrue(evaluated.getFacts()
				.contains(Atoms.newBasicAtom(Predicates.getPredicate("d", 1), Terms.newConstant(3))));
	}

	private static void verifyAnswerSetsEqualityWithVar(Set<AnswerSet> answerSets) {
		assertAnswerSetsEqual("a(1), a(2), a(3), b(1), c(2), d(3)", answerSets);
	}

}