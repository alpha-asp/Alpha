package at.ac.tuwien.kr.alpha.core.test.util;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static at.ac.tuwien.kr.alpha.test.AlphaAssertions.assertAnswerSetsEqual;
import static at.ac.tuwien.kr.alpha.test.AlphaAssertions.assertAnswerSetsEqualWithBase;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.function.Executable;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.Solver;
import at.ac.tuwien.kr.alpha.api.config.Heuristic;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.Program;
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
import at.ac.tuwien.kr.alpha.core.programs.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.core.programs.InternalProgram;
import at.ac.tuwien.kr.alpha.core.programs.transformation.NormalizeProgramTransformation;
import at.ac.tuwien.kr.alpha.core.programs.transformation.StratifiedEvaluation;
import at.ac.tuwien.kr.alpha.core.solver.Antecedent;
import at.ac.tuwien.kr.alpha.core.solver.RegressionTestConfig;
import at.ac.tuwien.kr.alpha.core.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.test.AnswerSetsParser;

public class TestUtils {

	public static void fillAtomStore(AtomStore atomStore, int numberOfAtomsToFill) {
		Predicate predA = Predicates.getPredicate("a", 1);
		for (int i = 0; i < numberOfAtomsToFill; i++) {
			atomStore.putIfAbsent(Atoms.newBasicAtom(predA, Terms.newConstant(i)));
		}
	}
	
	/**
	 * Tests whether two Antecedent objects have the same reason literals (irrespective of their order).
	 * Note that both Antecedents are assumed to contain no duplicate literals.
	 * @param l left Antecedent.
	 * @param r right Antecedent
	 * @return true iff both Antecedents contain the same literals.
	 */
	public static boolean antecedentsEquals(Antecedent l, Antecedent r) {
		if (l == r) {
			return true;
		}
		if (l != null && r != null && l.getReasonLiterals().length == r.getReasonLiterals().length) {
			HashSet<Integer> lSet = new HashSet<>();
			for (int literal : l.getReasonLiterals()) {
				lSet.add(literal);
			}
			for (int literal : r.getReasonLiterals()) {
				if (!lSet.contains(literal)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public static void printNoGoods(AtomStore atomStore, Collection<NoGood> noGoods) {
		System.out.println(noGoods.stream().map(atomStore::noGoodToString).collect(Collectors.toSet()));
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
		assertAnswerSetsEqual(answerSet, actualAnswerSets);
	}
	
	public static void assertRegressionTestAnswerSets(RegressionTestConfig cfg, String program, String... answerSets) {
		Set<AnswerSet> actualAnswerSets = collectRegressionTestAnswerSets(program, cfg);
		assertAnswerSetsEqual(answerSets, actualAnswerSets);		
	}
	
	public static void assertRegressionTestAnswerSetsWithBase(RegressionTestConfig cfg, String program, String base, String... answerSets) {
		Set<AnswerSet> actualAnswerSets = collectRegressionTestAnswerSets(program, cfg);
		assertAnswerSetsEqualWithBase(base, answerSets, actualAnswerSets);		
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

}
