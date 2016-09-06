package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.grounder.ChoiceGrounder;
import at.ac.tuwien.kr.alpha.grounder.DummyGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedConstant;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedFunctionTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.solver.DummySolver;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

public class MainTest {
	private InputStream stream(String file) {
		return new ByteArrayInputStream(file.getBytes());
	}

	@Test
	@Ignore
	public void parseSimpleProgram() throws IOException {
		Main.parseVisit(stream(
			"p(X) :- q(X).\n" +
			"q(a).\n" +
			"q(b).\n"
		));
	}

	@Test
	public void parseProgramWithNegativeBody() throws IOException {
		Main.parseVisit(stream(
			"p(X) :- q(X), not q(a).\n" +
				"q(a).\n"
		));
	}

	@Test(expected = UnsupportedOperationException.class)
	@Ignore
	public void parseProgramWithFunction() throws IOException {
		Main.parseVisit(stream(
			"p(X) :- q(f(X)).\n" +
				"q(a).\n"
		));
	}

	@Test(expected = UnsupportedOperationException.class)
	@Ignore
	public void parseProgramWithDisjunctionInHead() throws IOException {
		Main.parseVisit(stream(
			"r(X) | q(X) :- q(X).\n" +
				"q(a).\n"
		));
	}

	@Test
	public void parseFact() throws IOException {
		ParsedProgram parsedProgram = Main.parseVisit(stream("p(a,b)."));

		assertEquals("Program contains one fact.", 1, parsedProgram.facts.size());
		assertEquals("Predicate name of fact is p.", "p", parsedProgram.facts.get(0).fact.predicate);
		assertEquals("Fact has two terms.", 2, parsedProgram.facts.get(0).fact.arity);
		assertEquals("First term is a.", "a", ((ParsedConstant)parsedProgram.facts.get(0).fact.terms.get(0)).content);
		assertEquals("Second term is b.", "b", ((ParsedConstant)parsedProgram.facts.get(0).fact.terms.get(1)).content);
	}

	@Test
	public void parseFactWithFunctionTerms() throws IOException {
		ParsedProgram parsedProgram = Main.parseVisit(stream("p(f(a),g(h(Y)))."));

		assertEquals("Program contains one fact.", 1, parsedProgram.facts.size());
		assertEquals("Predicate name of fact is p.", "p", parsedProgram.facts.get(0).fact.predicate);
		assertEquals("Fact has two terms.", 2, parsedProgram.facts.get(0).fact.arity);
		assertEquals("First term is function term f.", "f", ((ParsedFunctionTerm)parsedProgram.facts.get(0).fact.terms.get(0)).functionName);
		assertEquals("Second term is function term g.", "g", ((ParsedFunctionTerm)parsedProgram.facts.get(0).fact.terms.get(1)).functionName);
	}

	@Test
	public void parseSmallProgram() throws IOException {
		ParsedProgram parsedProgram = Main.parseVisit(stream("a :- b, not d.\n" +
				"c(X) :- p(X,a,_), q(Xaa,xaa). :- f(Y)."));

		assertEquals("Program contains two rules.", 2, parsedProgram.rules.size());
		assertEquals("Program contains one constraint.", 1, parsedProgram.constraints.size());
	}

	@Test(expected = RecognitionException.class)
	public void parseBadSyntax() throws IOException {
		Main.parseVisit(stream("Wrong Syntax."));
	}

	@Test
	public void testDummyGrounderAndSolver() {
		final List<AnswerSet> recorder = new ArrayList<>(1);
		final Spliterator<AnswerSet> spliterator = new DummySolver(new DummyGrounder()).spliterator();

		assertTrue(spliterator.tryAdvance(recorder::add));
		assertEquals(1, recorder.size());
		assertEquals("Answer set is { a, b, _br1, c }.", "{ a, b, _br1, c }", recorder.get(0).toString());

		assertFalse(spliterator.tryAdvance(recorder::add));
		assertEquals(1, recorder.size());
	}

	@Test
	public void testGrounderChoiceAndSolver() {
		final Spliterator<AnswerSet> spliterator = new DummySolver(new ChoiceGrounder()).spliterator();
		final List<AnswerSet> answerSets = StreamSupport.stream(spliterator, false).collect(Collectors.toList());

		assertEquals("Program has two answer sets.", ChoiceGrounder.EXPECTED_ANSWER_SETS, answerSets.size());
	}
}