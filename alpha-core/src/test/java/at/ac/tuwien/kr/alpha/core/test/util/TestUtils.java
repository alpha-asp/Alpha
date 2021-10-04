package at.ac.tuwien.kr.alpha.core.test.util;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.function.Executable;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.Solver;
import at.ac.tuwien.kr.alpha.api.config.GrounderHeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.api.config.Heuristic;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.Program;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.ProgramTransformation;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.core.common.NoGood;
import at.ac.tuwien.kr.alpha.core.grounder.Grounder;
import at.ac.tuwien.kr.alpha.core.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.core.parser.aspcore2.ASPCore2ProgramParser;
import at.ac.tuwien.kr.alpha.core.programs.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.core.programs.InternalProgram;
import at.ac.tuwien.kr.alpha.core.programs.transformation.NormalizeProgramTransformation;
import at.ac.tuwien.kr.alpha.core.programs.transformation.StratifiedEvaluation;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.AggregateRewriting;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders.AggregateEncoderFactory;
import at.ac.tuwien.kr.alpha.core.solver.RegressionTestConfig;
import at.ac.tuwien.kr.alpha.core.solver.SolverFactory;

public class TestUtils {

	public static void fillAtomStore(AtomStore atomStore, int numberOfAtomsToFill) {
		Predicate predA = Predicates.getPredicate("a", 1);
		for (int i = 0; i < numberOfAtomsToFill; i++) {
			atomStore.putIfAbsent(Atoms.newBasicAtom(predA, Terms.newConstant(i)));
		}
	}

	public static Atom atom(String predicateName, String... termStrings) {
		Term[] terms = new Term[termStrings.length];
		for (int i = 0; i < termStrings.length; i++) {
			String termString = termStrings[i];
			if (StringUtils.isAllUpperCase(termString.substring(0, 1))) {
				terms[i] = Terms.newVariable(termString);
			} else {
				terms[i] = Terms.newConstant(termString);
			}
		}
		return Atoms.newBasicAtom(Predicates.getPredicate(predicateName, terms.length), terms);
	}

	public static Atom atom(String predicateName, int... termInts) {
		Term[] terms = new Term[termInts.length];
		for (int i = 0; i < termInts.length; i++) {
			terms[i] = Terms.newConstant(termInts[i]);
		}
		return Atoms.newBasicAtom(Predicates.getPredicate(predicateName, terms.length), terms);
	}

	public static void printNoGoods(AtomStore atomStore, Collection<NoGood> noGoods) {
		System.out.println(noGoods.stream().map(atomStore::noGoodToString).collect(Collectors.toSet()));
	}

	public static void assertAnswerSetsEqual(Set<AnswerSet> expected, Set<AnswerSet> actual) {
		if (expected == null) {
			if (actual != null) {
				throw new AssertionError("Expected answer sets are null, but actual are not!");
			}
		}
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			Set<AnswerSet> expectedMinusActual = new LinkedHashSet<>(expected);
			expectedMinusActual.removeAll(actual);
			Set<AnswerSet> actualMinusExpected = new LinkedHashSet<>(actual);
			actualMinusExpected.removeAll(expected);
			String setDiffs = "Expected and actual answer sets do not agree, differences are:\nExpected \\ Actual:\n" + expectedMinusActual
					+ "\nActual \\ Expected:\n" + actualMinusExpected;
			throw new AssertionError(setDiffs + e.getMessage(), e);
		}
	}

	public static void assertAnswerSetsEqual(String[] expected, Set<AnswerSet> actual) {
		if (expected.length == 0) {
			TestUtils.assertAnswerSetsEqual(emptySet(), actual);
			return;
		}
		StringJoiner joiner = new StringJoiner("} {", "{", "}");
		Arrays.stream(expected).forEach(joiner::add);
		TestUtils.assertAnswerSetsEqual(AnswerSetsParser.parse(joiner.toString()), actual);
	}

	public static void assertAnswerSetsEqual(String expectedAnswerSet, Set<AnswerSet> actual) {
		TestUtils.assertAnswerSetsEqual(AnswerSetsParser.parse("{ " + expectedAnswerSet + " }"), actual);
	}

	public static void assertAnswerSetsEqualWithBase(String base, String[] expectedAnswerSets, Set<AnswerSet> actual) {
		base = base.trim();
		if (!base.endsWith(",")) {
			base += ", ";
		}

		for (int i = 0; i < expectedAnswerSets.length; i++) {
			expectedAnswerSets[i] = base + expectedAnswerSets[i];
			// Remove trailing ",".
			expectedAnswerSets[i] = expectedAnswerSets[i].trim();
			if (expectedAnswerSets[i].endsWith(",")) {
				expectedAnswerSets[i] = expectedAnswerSets[i].substring(0, expectedAnswerSets[i].length() - 1);
			}
		}
		TestUtils.assertAnswerSetsEqual(expectedAnswerSets, actual);
	}

	public static void assertFactsContainedInProgram(Program<?> prog, Atom... facts) {
		for (Atom fact : facts) {
			Assert.assertTrue(prog.getFacts().contains(fact));
		}
	}

	public static BasicAtom basicAtomWithStringTerms(String predicate, String... terms) {
		Predicate pred = Predicates.getPredicate(predicate, terms.length);
		List<Term> trms = new ArrayList<>();
		for (String str : terms) {
			trms.add(Terms.newConstant(str));
		}
		return Atoms.newBasicAtom(pred, trms);
	}

	public static BasicAtom basicAtomWithSymbolicTerms(String predicate, String... constantSymbols) {
		Predicate pred = Predicates.getPredicate(predicate, constantSymbols.length);
		List<Term> trms = new ArrayList<>();
		for (String str : constantSymbols) {
			trms.add(Terms.newSymbolicConstant(str));
		}
		return Atoms.newBasicAtom(pred, trms);
	}

	private static Solver buildSolverFromSystemConfig(InputProgram prog, SystemConfig cfg) {
		NormalProgram normalProg = new NormalizeProgramTransformation(cfg.getAggregateRewritingConfig()).apply(prog);
		InternalProgram preprocessed = cfg.isEvaluateStratifiedPart() ? new StratifiedEvaluation().apply(AnalyzedProgram.analyzeNormalProgram(normalProg))
				: InternalProgram.fromNormalProgram(normalProg);
		AtomStore atomStore = new AtomStoreImpl();
		return new SolverFactory()
				.createSolver(new GrounderFactory(new GrounderHeuristicsConfiguration(), cfg.isDebugInternalChecks())
						.createGrounder(preprocessed, atomStore), atomStore);
	}

	public static Solver buildSolverForRegressionTest(InputProgram prog, RegressionTestConfig cfg) {
		return buildSolverFromSystemConfig(prog, cfg.toSystemConfig());
	}

	public static Solver buildSolverForRegressionTest(String prog, RegressionTestConfig cfg) {
		return buildSolverFromSystemConfig(new ASPCore2ProgramParser().parse(prog), cfg.toSystemConfig());
	}

	public static Solver buildSolverForRegressionTest(AtomStore atomStore, Grounder grounder, RegressionTestConfig cfg) {
		return new SolverFactory().createSolver(grounder, atomStore); // TODO
	}

	public static Set<AnswerSet> collectRegressionTestAnswerSets(InputProgram prog, RegressionTestConfig cfg) {
		return buildSolverForRegressionTest(prog, cfg).collectSet();
	}

	public static Set<AnswerSet> collectRegressionTestAnswerSets(String aspstr, RegressionTestConfig cfg) {
		InputProgram prog = new ASPCore2ProgramParser().parse(aspstr);
		return collectRegressionTestAnswerSets(prog, cfg);
	}

	public static void assertRegressionTestAnswerSet(RegressionTestConfig cfg, String program, String answerSet) {
		Set<AnswerSet> actualAnswerSets = collectRegressionTestAnswerSets(program, cfg);
		TestUtils.assertAnswerSetsEqual(answerSet, actualAnswerSets);
	}

	public static void assertRegressionTestAnswerSets(RegressionTestConfig cfg, String program, String... answerSets) {
		Set<AnswerSet> actualAnswerSets = collectRegressionTestAnswerSets(program, cfg);
		TestUtils.assertAnswerSetsEqual(answerSets, actualAnswerSets);
	}

	public static void assertRegressionTestAnswerSetsWithBase(RegressionTestConfig cfg, String program, String base, String... answerSets) {
		Set<AnswerSet> actualAnswerSets = collectRegressionTestAnswerSets(program, cfg);
		TestUtils.assertAnswerSetsEqualWithBase(base, answerSets, actualAnswerSets);
	}

	public static void runWithTimeout(RegressionTestConfig cfg, long baseTimeout, long timeoutFactor, Executable action) {
		long timeout = cfg.isDebugChecks() ? timeoutFactor * baseTimeout : baseTimeout;
		assertTimeoutPreemptively(Duration.ofMillis(timeout), action);
	}

	public static void ignoreTestForNaiveSolver(RegressionTestConfig cfg) {
		Assumptions.assumeFalse(cfg.getSolverName().equals("naive"));
	}

	public static void ignoreTestForNonDefaultDomainIndependentHeuristics(RegressionTestConfig cfg) {
		Assumptions.assumeTrue(cfg.getBranchingHeuristic() == Heuristic.VSIDS);
	}

	public static void ignoreTestForSimplifiedSumAggregates(RegressionTestConfig cfg) {
		Assumptions.assumeTrue(cfg.isSupportNegativeSumElements());
	}

	/**
	 * Convenience method for use in tests that parses and normalizes an ASP program represented as a string.
	 * 
	 * @param asp the program to parse
	 * @return a normalized program representation of the given asp string
	 */
	public static NormalProgram parseAndNormalizeWithDefaultConfig(String asp) {
		ProgramParser parser = new ASPCore2ProgramParser();
		InputProgram input = parser.parse(asp);

		// AggregateEncoderFactory depends on parser factory since stringtemplate-based aggregate encoders need to use the same parser that's used
		// for input programs.
		AggregateEncoderFactory aggregateEncoderFactory = new AggregateEncoderFactory(() -> parser,
				true, true);

		// Factory for aggregate rewriting (depends on encoders provided by above factory).
		Supplier<AggregateRewriting> aggregateRewritingFactory = () -> new AggregateRewriting(aggregateEncoderFactory.newCountEqualsEncoder(),
				aggregateEncoderFactory.newCountLessOrEqualEncoder(), aggregateEncoderFactory.newSumEqualsEncoder(),
				aggregateEncoderFactory.newSumLessOrEqualEncoder(), aggregateEncoderFactory.newMinEncoder(), aggregateEncoderFactory.newMaxEncoder());
		NormalizeProgramTransformation normalize = new NormalizeProgramTransformation(
				aggregateRewritingFactory);
		return normalize.apply(input);
	}

}
