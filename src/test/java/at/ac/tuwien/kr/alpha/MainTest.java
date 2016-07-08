package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.grounder.parser.ParsedConstant;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedFunctionTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class MainTest {
	private InputStream stream(String file) {
		return new ByteArrayInputStream(file.getBytes());
	}

	/*@Test
	public void parseSimpleProgram() throws IOException {
		Main.parse(stream(
			"p(X) :- q(X).\n" +
			"q(a).\n" +
			"q(b).\n"
		));
	}

	@Test
	public void parseProgramWithNegativeBody() throws IOException {
		Main.parse(stream(
			"p(X) :- q(X), not q(a).\n" +
				"q(a).\n"
		));
	}

	/*@Test(expected = UnsupportedOperationException.class)
	public void parseProgramWithFunction() throws IOException {
		Main.parse(stream(
			"p(X) :- q(f(X)).\n" +
				"q(a).\n"
		));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void parseProgramWithDisjunctionInHead() throws IOException {
		Main.parse(stream(
			"r(X) | q(X) :- q(X).\n" +
				"q(a).\n"
		));
	}*/

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


	@Rule
	public final ExpectedSystemExit exit = ExpectedSystemExit.none();

	@Test
	public void parseBadSyntaxAndExit() throws IOException {
		exit.expectSystemExitWithStatus(-1);
		Main.parseVisit(stream("Wrong Syntax."));
	}
}