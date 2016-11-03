package at.ac.tuwien.kr.alpha.antlr;

import at.ac.tuwien.kr.alpha.grounder.parser.ParsedConstant;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedFunctionTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static org.junit.Assert.assertEquals;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParserTest {
	public static InputStream stream(String file) {
		return new ByteArrayInputStream(file.getBytes());
	}

	@Test
	public void parseFact() throws IOException {
		ParsedProgram parsedProgram = parseVisit(stream("p(a,b)."));

		assertEquals("Program contains one fact.", 1, parsedProgram.facts.size());
		assertEquals("Predicate name of fact is p.", "p", parsedProgram.facts.get(0).fact.predicate);
		assertEquals("Fact has two terms.", 2, parsedProgram.facts.get(0).fact.arity);
		assertEquals("First term is a.", "a", ((ParsedConstant)parsedProgram.facts.get(0).fact.terms.get(0)).content);
		assertEquals("Second term is b.", "b", ((ParsedConstant)parsedProgram.facts.get(0).fact.terms.get(1)).content);
	}

	@Test
	public void parseFactWithFunctionTerms() throws IOException {
		ParsedProgram parsedProgram = parseVisit(stream("p(f(a),g(h(Y)))."));

		assertEquals("Program contains one fact.", 1, parsedProgram.facts.size());
		assertEquals("Predicate name of fact is p.", "p", parsedProgram.facts.get(0).fact.predicate);
		assertEquals("Fact has two terms.", 2, parsedProgram.facts.get(0).fact.arity);
		assertEquals("First term is function term f.", "f", ((ParsedFunctionTerm)parsedProgram.facts.get(0).fact.terms.get(0)).functionName);
		assertEquals("Second term is function term g.", "g", ((ParsedFunctionTerm)parsedProgram.facts.get(0).fact.terms.get(1)).functionName);
	}

	@Test
	public void parseSmallProgram() throws IOException {
		ParsedProgram parsedProgram = parseVisit(stream("a :- b, not d.\n" +
			"c(X) :- p(X,a,_), q(Xaa,xaa). :- f(Y)."));

		assertEquals("Program contains two rules.", 2, parsedProgram.rules.size());
		assertEquals("Program contains one constraint.", 1, parsedProgram.constraints.size());
	}

	@Test(expected = RecognitionException.class)
	public void parseBadSyntax() throws IOException {
		parseVisit(stream("Wrong Syntax."));
	}

	@Test
	public void parseBuiltinAtom() throws IOException {
		ParsedProgram parsedProgram = parseVisit(stream("a :- p(X), X != Y, q(Y)."));
		assertEquals(1, parsedProgram.rules.size());
		assertEquals(3, parsedProgram.rules.get(0).body.size());
	}

}