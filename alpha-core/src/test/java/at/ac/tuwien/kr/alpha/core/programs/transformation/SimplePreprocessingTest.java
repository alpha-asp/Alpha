package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimplePreprocessingTest {

	private final ProgramParser parser = new ProgramParserImpl();
	private final NormalizeProgramTransformation normalizer = new NormalizeProgramTransformation(SystemConfig.DEFAULT_AGGREGATE_REWRITING_CONFIG);
	private final SimplePreprocessing evaluator = new SimplePreprocessing();

	private NormalProgram parseAndNormalize(String str) {
		return normalizer.apply(parser.parse(str));
	}

	private NormalProgram parseAndPreprocess(String str) {
		return evaluator.apply(normalizer.apply(parser.parse(str)));
	}

	@Test
	public void testDuplicateRuleRemoval() {
		String aspStr1 = "a :- not b. a :- not b. b :- not a.";
		String aspStr2 = "a :- not b.             b :- not a.";
		NormalProgram evaluated1 = parseAndPreprocess(aspStr1);
		NormalProgram evaluated2 = parseAndNormalize(aspStr2);
		assertEquals(evaluated2.getRules(), evaluated1.getRules());
		assertEquals(new HashSet<>(evaluated2.getFacts()), new HashSet<>(evaluated1.getFacts()));
	}

	@Test
	public void testConflictingRuleRemoval() {
		String aspStr1 = "a :- b, not b. b :- not c. c :- not b.";
		String aspStr2 = "               b :- not c. c :- not b.";
		NormalProgram evaluated1 = parseAndPreprocess(aspStr1);
		NormalProgram evaluated2 = parseAndNormalize(aspStr2);
		assertEquals(evaluated2.getRules(), evaluated1.getRules());
		assertEquals(new HashSet<>(evaluated2.getFacts()), new HashSet<>(evaluated1.getFacts()));
	}

	@Test
	public void testHeadInBodyRuleRemoval() {
		String aspStr1 = "a :- a, not b. b :- not c. c :- not b.";
		String aspStr2 = "               b :- not c. c :- not b.";
		NormalProgram evaluated1 = parseAndPreprocess(aspStr1);
		NormalProgram evaluated2 = parseAndNormalize(aspStr2);
		assertEquals(evaluated2.getRules(), evaluated1.getRules());
		assertEquals(new HashSet<>(evaluated2.getFacts()), new HashSet<>(evaluated1.getFacts()));
	}

	@Test
	public void testNoFireRuleRemoval() {
		String aspStr1 = "a :- not b. b :- not a. b :- c. d. e :- not d.";
		String aspStr2 = "a :- not b. b :- not a.         d.";
		NormalProgram evaluated1 = parseAndPreprocess(aspStr1);
		NormalProgram evaluated2 = parseAndNormalize(aspStr2);
		assertEquals(evaluated2.getRules(), evaluated1.getRules());
		assertEquals(new HashSet<>(evaluated2.getFacts()), new HashSet<>(evaluated1.getFacts()));
	}

	@Test
	public void testAlwaysTrueLiteralRemoval() {
		String aspStr1 = "a :- b, not c. b. c :- not a.";
		String aspStr2 = "a :-    not c. b. c :- not a.";
		NormalProgram evaluated1 = parseAndPreprocess(aspStr1);
		NormalProgram evaluated2 = parseAndNormalize(aspStr2);
		assertEquals(evaluated2.getRules(), evaluated1.getRules());
		assertEquals(new HashSet<>(evaluated2.getFacts()), new HashSet<>(evaluated1.getFacts()));
	}

	@Test
	public void testNonDerivableLiteralRemovalNonGround() {
		String aspStr1 = "r(X) :- p(X), not q(c). p(a). u(Y) :- s(Y), not v(Y). s(b) :- p(a), not v(a). v(a) :- not u(b).";
		String aspStr2 = "r(X) :- p(X).           p(a). u(Y) :- s(Y), not v(Y). s(b) :-       not v(a). v(a) :- not u(b).";
		NormalProgram evaluated1 = parseAndPreprocess(aspStr1);
		NormalProgram evaluated2 = parseAndNormalize(aspStr2);
		assertEquals(evaluated2.getRules(), evaluated1.getRules());
		assertEquals(new HashSet<>(evaluated2.getFacts()), new HashSet<>(evaluated1.getFacts()));
	}

	@Test void testNonDerivableLiteralRemovalNonGround2() {
		String aspStr1 = "p(X) :- q(X), not r(X). q(Y) :- p(Y), s(a). s(a).";
		String aspStr2 = "p(X) :- q(X).           q(Y) :- p(Y).       s(a).";
		NormalProgram evaluated1 = parseAndPreprocess(aspStr1);
		NormalProgram evaluated2 = parseAndNormalize(aspStr2);
		assertEquals(evaluated2.getRules(), evaluated1.getRules());
		assertEquals(new HashSet<>(evaluated2.getFacts()), new HashSet<>(evaluated1.getFacts()));
	}

	@Test void testRuleToFact() {
		String aspStr1 = "r(c) :- q(a), not p(b). q(a).";
		String aspStr2 = "r(c).                   q(a).";
		NormalProgram evaluated1 = parseAndPreprocess(aspStr1);
		NormalProgram evaluated2 = parseAndNormalize(aspStr2);
		assertEquals(evaluated2.getRules(), evaluated1.getRules());
		assertEquals(new HashSet<>(evaluated2.getFacts()), new HashSet<>(evaluated1.getFacts()));
	}

	@Test void testAlwaysTrueLiteralRemovalNonGround() {
		String aspStr1 = "r(X) :- t(X), s(b), not q(a). s(b). t(X) :- r(X). q(a) :- not r(a).";
		String aspStr2 = "r(X) :- t(X),       not q(a). s(b). t(X) :- r(X). q(a) :- not r(a).";
		NormalProgram evaluated1 = parseAndPreprocess(aspStr1);
		NormalProgram evaluated2 = parseAndNormalize(aspStr2);
		assertEquals(evaluated2.getRules(), evaluated1.getRules());
		assertEquals(new HashSet<>(evaluated2.getFacts()), new HashSet<>(evaluated1.getFacts()));
	}
}