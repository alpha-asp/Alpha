package at.ac.tuwien.kr.alpha.core.test.util;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.function.Executable;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.Solver;
import at.ac.tuwien.kr.alpha.api.config.Heuristic;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.Program;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.WeightedAnswerSet;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.core.common.NoGood;
import at.ac.tuwien.kr.alpha.core.grounder.Grounder;
import at.ac.tuwien.kr.alpha.core.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;
import at.ac.tuwien.kr.alpha.core.programs.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.core.programs.InternalProgram;
import at.ac.tuwien.kr.alpha.core.programs.transformation.NormalizeProgramTransformation;
import at.ac.tuwien.kr.alpha.core.programs.transformation.StratifiedEvaluation;
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
			assertEquals(expected, actual);
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
			assertTrue(prog.getFacts().contains(fact));
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

	private static Solver buildSolverFromSystemConfig(ASPCore2Program prog, SystemConfig cfg) {
		AtomStore atomStore = new AtomStoreImpl();
		NormalProgram normalProg = new NormalizeProgramTransformation(cfg.getAggregateRewritingConfig()).apply(prog);
		InternalProgram preprocessed = cfg.isEvaluateStratifiedPart() ? new StratifiedEvaluation().apply(AnalyzedProgram.analyzeNormalProgram(normalProg))
				: InternalProgram.fromNormalProgram(normalProg);
		return SolverFactory.getInstance(cfg, atomStore, GrounderFactory.getInstance(cfg.getGrounderName(), preprocessed, atomStore, cfg.isDebugInternalChecks()));
	}

	public static Solver buildSolverForRegressionTest(ASPCore2Program prog, RegressionTestConfig cfg) {
		return buildSolverFromSystemConfig(prog, cfg.toSystemConfig());
	}

	public static Solver buildSolverForOptimizationRegressionTest(ASPCore2Program prog, RegressionTestConfig cfg) {
		SystemConfig systemConfig = cfg.toSystemConfig();
		systemConfig.setAnswerSetOptimizationEnabled(true);
		return buildSolverFromSystemConfig(prog, systemConfig);
	}

	public static Set<AnswerSet> collectRegressionTestOptimalAnswerSets(String prog, RegressionTestConfig config) {
		return buildSolverForOptimizationRegressionTest(new ProgramParserImpl().parse(prog), config).collectSet();
	}
	public static Solver buildSolverForRegressionTest(String prog, RegressionTestConfig cfg) {
		return buildSolverFromSystemConfig(new ProgramParserImpl().parse(prog), cfg.toSystemConfig());
	}

	public static Solver buildSolverForRegressionTest(AtomStore atomStore, Grounder grounder, RegressionTestConfig cfg) {
		SystemConfig systemCfg = cfg.toSystemConfig();
		return SolverFactory.getInstance(systemCfg, atomStore, grounder);
	}

	public static Set<AnswerSet> collectRegressionTestAnswerSets(ASPCore2Program prog, RegressionTestConfig cfg) {
		return buildSolverForRegressionTest(prog, cfg).collectSet();
	}

	public static Set<AnswerSet> collectRegressionTestAnswerSets(String aspstr, RegressionTestConfig cfg) {
		ASPCore2Program prog = new ProgramParserImpl().parse(aspstr);
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
	public static WeightedAnswerSet weightedAnswerSetFromStrings(String basicAnswerSetAsString, String weightAtLevelsAsString) {
		AnswerSet basicAnswerSet = AnswerSetsParser.parse("{ " + basicAnswerSetAsString + " }").iterator().next();
		return new WeightedAnswerSet(basicAnswerSet, WeightedAnswerSet.weightPerLevelFromString(weightAtLevelsAsString));
	}

	public static void assertOptimumAnswerSetEquals(String expectedOptimumAnswerSet, String expectedWeightsAtLevels, Set<AnswerSet> actual) {
		WeightedAnswerSet optimumAnswerSet = weightedAnswerSetFromStrings(expectedOptimumAnswerSet, expectedWeightsAtLevels);

		// Check the optimum is contained in the set of actual answer sets.
		if (!actual.contains(optimumAnswerSet)) {
			throw new AssertionError("Expected optimum answer set is not contained in actual.\n" +
				"Expected optimum answer set: " + optimumAnswerSet + "\n" +
				"Actual answer sets: " + actual);
		}
		// Ensure that there is no better answer set contained in the actual answer sets.
		for (AnswerSet actualAnswerSet : actual) {
			if (actualAnswerSet.equals(optimumAnswerSet)) {
				// Skip optimum itself.
				continue;
			}
			if (!(actualAnswerSet instanceof WeightedAnswerSet)) {
				throw new AssertionError("Expecting weighted answer sets but obtained answer set is not: " + actualAnswerSet);
			}
			WeightedAnswerSet actualWeightedAnswerSet = (WeightedAnswerSet) actualAnswerSet;
			if (optimumAnswerSet.compareWeights(actualWeightedAnswerSet) >= 0) {
				throw new AssertionError("Actual answer set is better than expected one.\n" +
					"Expected: " + optimumAnswerSet + "\n" +
					"Actual: " + actualWeightedAnswerSet);
			}
		}
	}
}
