package at.ac.tuwien.kr.alpha.antlr;

import at.ac.tuwien.kr.alpha.Main;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParserTest {
	@Test
	public void parseFact() throws IOException {
		Program parsedProgram = Main.parseVisit("p(a,b).");

		assertEquals("Program contains one fact.", 1, parsedProgram.getFacts().size());
		assertEquals("Predicate name of fact is p.", "p", parsedProgram.getFacts().get(0).getPredicate().getPredicateName());
		assertEquals("Fact has two terms.", 2, parsedProgram.getFacts().get(0).getPredicate().getArity());
		assertEquals("First term is a.", "a", ((ConstantTerm)parsedProgram.getFacts().get(0).getTerms().get(0)).toString());
		assertEquals("Second term is b.", "b", ((ConstantTerm)parsedProgram.getFacts().get(0).getTerms().get(1)).toString());
	}

	@Test
	public void parseFactWithFunctionTerms() throws IOException {
		Program parsedProgram = Main.parseVisit("p(f(a),g(h(Y))).");

		assertEquals("Program contains one fact.", 1, parsedProgram.getFacts().size());
		assertEquals("Predicate name of fact is p.", "p", parsedProgram.getFacts().get(0).getPredicate().getPredicateName());
		assertEquals("Fact has two terms.", 2, parsedProgram.getFacts().get(0).getPredicate().getArity());
		assertEquals("First term is function term f.", "f", ((FunctionTerm)parsedProgram.getFacts().get(0).getTerms().get(0)).getSymbol().getSymbol());
		assertEquals("Second term is function term g.", "g", ((FunctionTerm)parsedProgram.getFacts().get(0).getTerms().get(1)).getSymbol().getSymbol());
	}

	@Test
	public void parseSmallProgram() throws IOException {
		Program parsedProgram = Main.parseVisit("a :- b, not d.\n" +
			"c(X) :- p(X,a,_), q(Xaa,xaa)." +
				":- f(Y).");

		assertEquals("Program contains three rules.", 3, parsedProgram.getRules().size());
	}

	@Test(expected = RecognitionException.class)
	public void parseBadSyntax() throws IOException {
		Main.parseVisit("Wrong Syntax.");
	}

	@Test
	public void parseBuiltinAtom() throws IOException {
		Program parsedProgram = Main.parseVisit("a :- p(X), X != Y, q(Y).");
		assertEquals(1, parsedProgram.getRules().size());
		assertEquals(3, parsedProgram.getRules().get(0).getNumBodyAtoms());
	}

	@Test(expected = UnsupportedOperationException.class)
	// Change expected after Alpha can deal with disjunction.
	public void parseProgramWithDisjunctionInHead() throws IOException {
		Main.parseVisit("r(X) | q(X) :- q(X).\nq(a).\n");
	}

}