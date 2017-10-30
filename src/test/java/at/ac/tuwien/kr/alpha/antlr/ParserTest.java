package at.ac.tuwien.kr.alpha.antlr;

import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
class ParserTest {
	private final ProgramParser parser = new ProgramParser();

	@Test
	void parseFact() throws IOException {
		Program parsedProgram = parser.parse("p(a,b).");

		assertEquals(1, parsedProgram.getFacts().size(), "Program contains one fact.");

		Atom fact = parsedProgram.getFacts().get(0);

		assertEquals("p", fact.getPredicate().getPredicateName(), "Predicate name of fact is p.");
		assertEquals(2, fact.getPredicate().getArity(), "Fact has two terms.");
		assertEquals("a", fact.getTerms().get(0).toString(), "First term is a.");
		assertEquals("b", fact.getTerms().get(1).toString(), "Second term is b.");
	}

	@Test
	void parseFactWithFunctionTerms() throws IOException {
		Program parsedProgram = parser.parse("p(f(a),g(h(Y))).");

		assertEquals(1, parsedProgram.getFacts().size(), "Program contains one fact.");
		assertEquals("p", parsedProgram.getFacts().get(0).getPredicate().getPredicateName(), "Predicate name of fact is p.");
		assertEquals(2, parsedProgram.getFacts().get(0).getPredicate().getArity(), "Fact has two terms.");
		assertEquals("f", ((FunctionTerm)parsedProgram.getFacts().get(0).getTerms().get(0)).getSymbol().toString(), "First term is function term f.");
		assertEquals("g", ((FunctionTerm)parsedProgram.getFacts().get(0).getTerms().get(1)).getSymbol().toString(), "Second term is function term g.");
	}

	@Test
	void parseSmallProgram() throws IOException {
		Program parsedProgram = parser.parse("a :- b, not d.\n" +
			"c(X) :- p(X,a,_), q(Xaa,xaa)." +
				":- f(Y).");

		assertEquals(3, parsedProgram.getRules().size(), "Program contains three rules.");
	}

	@Test
	void parseBadSyntax() throws IOException {
		assertThrows(IllegalArgumentException.class, () ->
			parser.parse("Wrong Syntax.")
		);
	}

	@Test
	void parseBuiltinAtom() throws IOException {
		Program parsedProgram = parser.parse("a :- p(X), X != Y, q(Y).");
		assertEquals(1, parsedProgram.getRules().size());
		assertEquals(3, parsedProgram.getRules().get(0).getBody().size());
	}

	@Test
	// Change expected after Alpha can deal with disjunction.
	void parseProgramWithDisjunctionInHead() throws IOException {
		assertThrows(UnsupportedOperationException.class, () ->
			parser.parse("r(X) | q(X) :- q(X).\nq(a).\n")
		);
	}

	@Test
	void parseInterval() throws IOException {
		Program parsedProgram = parser.parse("fact(2..5). p(X) :- q(a, 3 .. X).");
		IntervalTerm factInterval = (IntervalTerm) parsedProgram.getFacts().get(0).getTerms().get(0);
		assertTrue(factInterval.equals(IntervalTerm.getInstance(ConstantTerm.getInstance(2), ConstantTerm.getInstance(5))));
		IntervalTerm bodyInterval = (IntervalTerm) parsedProgram.getRules().get(0).getBody().get(0).getTerms().get(1);
		assertTrue(bodyInterval.equals(IntervalTerm.getInstance(ConstantTerm.getInstance(3), VariableTerm.getInstance("X"))));
	}
}