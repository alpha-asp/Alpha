package at.ac.tuwien.kr.alpha.test.util;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ibm.icu.impl.Assert;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.AbstractProgram;
import at.ac.tuwien.kr.alpha.common.program.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.InternalProgram;
import at.ac.tuwien.kr.alpha.common.program.NormalProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.config.SystemConfig;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.NormalizeProgramTransformation;
import at.ac.tuwien.kr.alpha.grounder.transformation.StratifiedEvaluation;
import at.ac.tuwien.kr.alpha.solver.RegressionTestConfig;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory;

public class TestUtils {

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

	public static void assertFactsContainedInProgram(AbstractProgram<?> prog, Atom... facts) {
		for (Atom fact : facts) {
			assertTrue(prog.getFacts().contains(fact));
		}
	}

	public static Atom basicAtomWithStringTerms(String predicate, String... terms) {
		Predicate pred = Predicate.getInstance(predicate, terms.length);
		List<Term> trms = new ArrayList<>();
		for (String str : terms) {
			trms.add(ConstantTerm.getInstance(str));
		}
		return new BasicAtom(pred, trms);
	}

	public static Atom basicAtomWithSymbolicTerms(String predicate, String... constantSymbols) {
		Predicate pred = Predicate.getInstance(predicate, constantSymbols.length);
		List<Term> trms = new ArrayList<>();
		for (String str : constantSymbols) {
			trms.add(ConstantTerm.getSymbolicInstance(str));
		}
		return new BasicAtom(pred, trms);
	}

	public static Atom atom(String predicateName, String... termStrings) {
		Term[] terms = new Term[termStrings.length];
		for (int i = 0; i < termStrings.length; i++) {
			String termString = termStrings[i];
			if (StringUtils.isAllUpperCase(termString.substring(0, 1))) {
				terms[i] = VariableTerm.getInstance(termString);
			} else {
				terms[i] = ConstantTerm.getInstance(termString);
			}
		}
		return new BasicAtom(Predicate.getInstance(predicateName, terms.length), terms);
	}

	public static Atom atom(String predicateName, int... termInts) {
		Term[] terms = new Term[termInts.length];
		for (int i = 0; i < termInts.length; i++) {
			terms[i] = ConstantTerm.getInstance(termInts[i]);
		}
		return new BasicAtom(Predicate.getInstance(predicateName, terms.length), terms);
	}

	public static void printNoGoods(AtomStore atomStore, Collection<NoGood> noGoods) {
		System.out.println(noGoods.stream().map(atomStore::noGoodToString).collect(Collectors.toSet()));
	}

	public static void assertProgramContainsRule(InputProgram prog, BasicRule containedRule) {
		for (BasicRule rule : prog.getRules()) {
			if (rule.equals(containedRule)) {
				return;
			}
		}
		Assert.fail("Program should contain rule, but does not! (rule = " + containedRule + ")");
	}

	private static Solver buildSolverFromSystemConfig(InputProgram prog, SystemConfig cfg) {
		AtomStore atomStore = new AtomStoreImpl();
		NormalProgram normalProg = new NormalizeProgramTransformation(cfg.getAggregateRewritingConfig()).apply(prog);
		InternalProgram preprocessed = cfg.isEvaluateStratifiedPart() ? new StratifiedEvaluation().apply(AnalyzedProgram.analyzeNormalProgram(normalProg))
				: InternalProgram.fromNormalProgram(normalProg);
		return SolverFactory.getInstance(cfg, atomStore,
				GrounderFactory.getInstance(cfg.getGrounderName(), preprocessed, atomStore, cfg.isDebugInternalChecks()));
	}

	public static Solver buildSolverForRegressionTest(InputProgram prog, RegressionTestConfig cfg) {
		return buildSolverFromSystemConfig(prog, cfg.toSystemConfig());
	}

	public static Solver buildSolverForRegressionTest(String prog, RegressionTestConfig cfg) {
		return buildSolverFromSystemConfig(new ProgramParser().parse(prog), cfg.toSystemConfig());
	}

	public static Solver buildSolverForRegressionTest(AtomStore atomStore, Grounder grounder, RegressionTestConfig cfg) {
		SystemConfig systemCfg = cfg.toSystemConfig();
		return SolverFactory.getInstance(systemCfg, atomStore, grounder);
	}

	public static Set<AnswerSet> collectRegressionTestAnswerSets(InputProgram prog, RegressionTestConfig cfg) {
		return buildSolverForRegressionTest(prog, cfg).collectSet();
	}

	public static Set<AnswerSet> collectRegressionTestAnswerSets(String aspstr, RegressionTestConfig cfg) {
		InputProgram prog = new ProgramParser().parse(aspstr);
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
		Assumptions.assumeTrue(cfg.getBranchingHeuristic() == BranchingHeuristicFactory.Heuristic.VSIDS);
	}
	
	public static void ignoreTestForSimplifiedSumAggregates(RegressionTestConfig cfg) {
		Assumptions.assumeTrue(cfg.isSupportNegativeSumElements());
	}

}
