package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.BasicAnswerSet;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static at.ac.tuwien.kr.alpha.MainTest.stream;
import static org.junit.Assert.assertEquals;

public abstract class AbstractSolverTest {
	protected abstract Solver getInstance(Grounder grounder);

	@Test
	public void testFactsOnlyProgram() throws IOException {
		String testProgram = "p(a). p(b). foo(13). foo(16). q(a). q(c).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = getInstance(grounder);

		List<AnswerSet> answerSets = solver.collectList();

		assertEquals(1, answerSets.size());

		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("q").instance("a").instance("c")
			.predicate("p").instance("a").instance("b")
			.predicate("foo").instance("13").instance("16")
			.build();

		assertEquals(expected, answerSets.get(0));
	}

	@Test
	public void testSimpleRule() throws Exception {
		String testProgram = "p(a). p(b). r(X) :- p(X).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = getInstance(grounder);

		List<AnswerSet> answerSets = solver.collectList();

		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("p").instance("a").instance("b")
			.predicate("_R_").instance("0", "_X:a").instance("0", "_X:b")
			.predicate("r").instance("a").instance("b")
			.build();

		assertEquals(1, answerSets.size());
		assertEquals(expected, answerSets.get(0));
	}

	@Test
	public void testSimpleRuleWithGroundPart() throws Exception {
		String testProgram =
			"p(1)." +
				"p(2)." +
				"q(X) :-  p(X), p(1).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = getInstance(grounder);

		List<AnswerSet> answerSets = solver.collectList();

		assertEquals(1, answerSets.size());
		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("q").instance("1").instance("2")
			.predicate("p").instance("1").instance("2")
			.predicate("_R_").instance("0", "_X:1").instance("0", "_X:2")
			.build();

		assertEquals(expected, answerSets.get(0));
	}

	@Test
	public void testProgramZeroArityPredicates() throws Exception {
		String testProgram = "a. p(X) :- b, r(X).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = getInstance(grounder);

		List<AnswerSet> answerSets = solver.collectList();

		assertEquals(1, answerSets.size());

		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("a")
			.build();

		assertEquals(expected, answerSets.get(0));
	}

	@Test
	public void testGuessingGroundProgram() throws Exception {
		String testProgram = "a :- not b. b :- not a.";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = getInstance(grounder);

		// NOTE(flowlo): Empty string?!

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			// { ChoiceOn(0), ChoiceOn(1), ChoiceOff(1), _R_(0, ), a }
			new BasicAnswerSet.Builder()
				.predicate("ChoiceOn").instance("0").instance("1")
				.predicate("ChoiceOff").instance("1")
				.predicate("_R_").instance("0", "")
				.predicate("a")
				.build(),
			// { ChoiceOn(0), ChoiceOn(1), ChoiceOff(0), _R_(1, ), b }
			new BasicAnswerSet.Builder()
				.predicate("ChoiceOn").instance("0").instance("1")
				.predicate("ChoiceOff").instance("0")
				.predicate("_R_").instance("1", "")
				.predicate("b")
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();

		assertEquals(expected, answerSets);
	}

	private static BasicAnswerSet.Builder base() {
		return new BasicAnswerSet.Builder()
			.predicate("dom").instance("1").instance("2").instance("3")
			.predicate("ChoiceOn").instance("0").instance("1").instance("2").instance("3").instance("4").instance("5");
	}

	@Test
	public void testGuessingProgramNonGround() throws Exception {
		String testProgram = "dom(1). dom(2). dom(3)." +
			"p(X) :- dom(X), not q(X)." +
			"q(X) :- dom(X), not p(X).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = getInstance(grounder);

		List<AnswerSet> expected = Arrays.asList(
			base()
				.predicate("q").instance("1").instance("2")
				.predicate("p").instance("3")
				.predicate("ChoiceOff").instance("0").instance("1").instance("5")
				.predicate("_R_").instance("0", "_X:3").instance("1", "_X:1").instance("1", "_X:2")
				.build(),
			base()
				.predicate("q").instance("1")
				.predicate("p").instance("2").instance("3")
				.predicate("ChoiceOff").instance("0").instance("4").instance("5")
				.predicate("_R_").instance("0", "_X:2").instance("0", "_X:3").instance("1", "_X:1")
				.build(),
			base()
				.predicate("q").instance("2")
				.predicate("p").instance("1").instance("3")
				.predicate("ChoiceOff").instance("1").instance("3").instance("5")
				.predicate("_R_").instance("0", "_X:1").instance("0", "_X:3").instance("1", "_X:2")
				.build(),
			base()
				.predicate("p").instance("1").instance("2").instance("3")
				.predicate("ChoiceOff").instance("3").instance("4").instance("5")
				.predicate("_R_").instance("0", "_X:1").instance("0", "_X:3").instance("0", "_X:2")
				.build(),
			base()
				.predicate("q").instance("1").instance("2").instance("3")
				.predicate("ChoiceOff").instance("0").instance("1").instance("2")
				.predicate("_R_").instance("1", "_X:1").instance("1", "_X:2").instance("1", "_X:3")
				.build(),
			base()
				.predicate("q").instance("1").instance("3")
				.predicate("p").instance("2")
				.predicate("ChoiceOff").instance("0").instance("2").instance("4")
				.predicate("_R_").instance("0", "_X:2").instance("1", "_X:1").instance("1", "_X:3")
				.build(),
			base()
				.predicate("q").instance("2").instance("3")
				.predicate("p").instance("1")
				.predicate("ChoiceOff").instance("1").instance("2").instance("3")
				.predicate("_R_").instance("0", "_X:1").instance("1", "_X:2").instance("1", "_X:3")
				.build(),
			base()
				.predicate("q").instance("3")
				.predicate("p").instance("1").instance("2")
				.predicate("ChoiceOff").instance("2").instance("3").instance("4")
				.predicate("_R_").instance("0", "_X:1").instance("0", "_X:2").instance("1", "_X:3")
				.build()
		);

		List<AnswerSet> answerSets = solver.collectList();

		assertEquals(expected, answerSets);
	}
}
