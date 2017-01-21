package at.ac.tuwien.kr.alpha.antlr;

import at.ac.tuwien.kr.alpha.grounder.parser.ParsedConstant;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedFunctionTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.Test;

import java.io.IOException;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static org.junit.Assert.assertEquals;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParserTest {
	@Test
	public void parseFact() throws IOException {
		ParsedProgram parsedProgram = parseVisit("p(a,b).");

		assertEquals("Program contains one fact.", 1, parsedProgram.facts.size());
		assertEquals("Predicate name of fact is p.", "p", parsedProgram.facts.get(0).getFact().getPredicate());
		assertEquals("Fact has two terms.", 2, parsedProgram.facts.get(0).getFact().getArity());
		assertEquals("First term is a.", "a", ((ParsedConstant)parsedProgram.facts.get(0).getFact().getTerms().get(0)).getContent());
		assertEquals("Second term is b.", "b", ((ParsedConstant)parsedProgram.facts.get(0).getFact().getTerms().get(1)).getContent());
	}

	@Test
	public void parseFactWithFunctionTerms() throws IOException {
		ParsedProgram parsedProgram = parseVisit("p(f(a),g(h(Y))).");

		assertEquals("Program contains one fact.", 1, parsedProgram.facts.size());
		assertEquals("Predicate name of fact is p.", "p", parsedProgram.facts.get(0).getFact().getPredicate());
		assertEquals("Fact has two terms.", 2, parsedProgram.facts.get(0).getFact().getArity());
		assertEquals("First term is function term f.", "f", ((ParsedFunctionTerm)parsedProgram.facts.get(0).getFact().getTerms().get(0)).getFunctionName());
		assertEquals("Second term is function term g.", "g", ((ParsedFunctionTerm)parsedProgram.facts.get(0).getFact().getTerms().get(1)).getFunctionName());
	}

	@Test
	public void parseSmallProgram() throws IOException {
		ParsedProgram parsedProgram = parseVisit("a :- b, not d.\n" +
			"c(X) :- p(X,a,_), q(Xaa,xaa). :- f(Y).");

		assertEquals("Program contains two rules.", 2, parsedProgram.rules.size());
		assertEquals("Program contains one constraint.", 1, parsedProgram.constraints.size());
	}

	@Test(expected = RecognitionException.class)
	public void parseBadSyntax() throws IOException {
		parseVisit("Wrong Syntax.");
	}

	@Test
	public void parseBuiltinAtom() throws IOException {
		ParsedProgram parsedProgram = parseVisit("a :- p(X), X != Y, q(Y).");
		assertEquals(1, parsedProgram.rules.size());
		assertEquals(3, parsedProgram.rules.get(0).body.size());
	}

}