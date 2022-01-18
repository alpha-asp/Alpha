package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.Solver;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.rules.NormalRule;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.core.grounder.Grounder;
import at.ac.tuwien.kr.alpha.core.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;
import at.ac.tuwien.kr.alpha.core.programs.CompiledProgram;
import at.ac.tuwien.kr.alpha.core.solver.SolverFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimplePreprocessingTest {

	private final ProgramParser parser = new ProgramParserImpl();
	private final NormalizeProgramTransformation normalizer = new NormalizeProgramTransformation(SystemConfig.DEFAULT_AGGREGATE_REWRITING_CONFIG);
	private final SimplePreprocessing evaluator = new SimplePreprocessing();
	private final Function<String, NormalProgram> parseAndEvaluate = (str) -> {
		return evaluator.apply(normalizer.apply(parser.parse(str)));
	};

	private final Function<CompiledProgram, Set<AnswerSet>> solveCompiledProg = (prog) -> {
		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance("naive", prog, atomStore, false);
		Solver solver = SolverFactory.getInstance(new SystemConfig(), atomStore, grounder);
		return solver.collectSet();
	};

	@Test
	public void testDuplicateRules() {
		String aspStr1 = "a :- b. a :- b. b.";
		String aspStr2 = "a :- b.         b.";
		NormalProgram evaluated1 = parseAndEvaluate.apply(aspStr1);
		NormalProgram evaluated2 = parseAndEvaluate.apply(aspStr2);
		assertEquals(evaluated1.getRules(), evaluated2.getRules());
	}

	@Test
	public void testConflictingBodyLiterals() {
		String aspStr1 = "a :- b, not b. b :- c.";
		String aspStr2 = "               b :- c.";
		NormalProgram evaluated1 = parseAndEvaluate.apply(aspStr1);
		NormalProgram evaluated2 = parseAndEvaluate.apply(aspStr2);
		assertEquals(evaluated1.getRules(), evaluated2.getRules());
	}

	@Test
	public void testHeadInBody() {
		String aspStr1 = "a :- a, not b. b :- c.";
		String aspStr2 = "               b :- c.";
		NormalProgram evaluated1 = parseAndEvaluate.apply(aspStr1);
		NormalProgram evaluated2 = parseAndEvaluate.apply(aspStr2);
		assertEquals(evaluated1.getRules(), evaluated2.getRules());
	}

	//@Test
	public void testNonDerivableLiterals() {
		String aspStr1 = "a :- not b. b :- c. d. e :- not d.";
		String aspStr2 = "a :- not b.         d.";
		NormalProgram evaluated1 = parseAndEvaluate.apply(aspStr1);
		NormalProgram evaluated2 = parseAndEvaluate.apply(aspStr2);
		assertEquals(evaluated1.getRules(), evaluated2.getRules());
	}

	//@Test
	public void testSimplifiedRules() {
		String aspStr1 = "a :- b, not c. b. c :- not a. e :- a, not d.";
		String aspStr2 = "a :-    not c. b. c :- not a.";
		NormalProgram evaluated1 = parseAndEvaluate.apply(aspStr1);
		NormalProgram evaluated2 = parseAndEvaluate.apply(aspStr2);
		assertEquals(evaluated1.getRules(), evaluated2.getRules());
	}

	@Test
	public void testNonGroundProgram() {
		String aspStr1 = "r(X) :- q(Y), not p(Y). q(Y). p(Y) :- not r(X). s(b) :- r(a), not t(d).";
		String aspStr2 = "r(X) :-       not p(Y). q(Y). p(Y) :- not r(X).";
		NormalProgram evaluated1 = parseAndEvaluate.apply(aspStr1);
		NormalProgram evaluated2 = parseAndEvaluate.apply(aspStr2);
		assertEquals(evaluated1.getRules(), evaluated2.getRules());
	}
}
