/**
 * Copyright (c) 2016-2017, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.antlr;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.heuristics.NonGroundDomainSpecificHeuristicValues;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParserTest {
	private final ProgramParser parser = new ProgramParser();

	@Test
	public void parseFact() throws IOException {
		Program parsedProgram = parser.parse("p(a,b).");

		assertEquals("Program contains one fact.", 1, parsedProgram.getFacts().size());
		assertEquals("Predicate name of fact is p.", "p", parsedProgram.getFacts().get(0).getPredicate().getName());
		assertEquals("Fact has two terms.", 2, parsedProgram.getFacts().get(0).getPredicate().getArity());
		assertEquals("First term is a.", "a", (parsedProgram.getFacts().get(0).getTerms().get(0)).toString());
		assertEquals("Second term is b.", "b", (parsedProgram.getFacts().get(0).getTerms().get(1)).toString());
	}

	@Test
	public void parseFactWithFunctionTerms() throws IOException {
		Program parsedProgram = parser.parse("p(f(a),g(h(Y))).");

		assertEquals("Program contains one fact.", 1, parsedProgram.getFacts().size());
		assertEquals("Predicate name of fact is p.", "p", parsedProgram.getFacts().get(0).getPredicate().getName());
		assertEquals("Fact has two terms.", 2, parsedProgram.getFacts().get(0).getPredicate().getArity());
		assertEquals("First term is function term f.", "f", ((FunctionTerm)parsedProgram.getFacts().get(0).getTerms().get(0)).getSymbol());
		assertEquals("Second term is function term g.", "g", ((FunctionTerm)parsedProgram.getFacts().get(0).getTerms().get(1)).getSymbol());
	}

	@Test
	public void parseSmallProgram() throws IOException {
		Program parsedProgram = parser.parse(
				"a :- b, not d." + System.lineSeparator() + 
				"c(X) :- p(X,a,_), q(Xaa,xaa)." + System.lineSeparator() + 
				":- f(Y).");

		assertEquals("Program contains three rules.", 3, parsedProgram.getRules().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseBadSyntax() throws IOException {
		parser.parse("Wrong Syntax.");
	}

	@Test
	public void parseBuiltinAtom() throws IOException {
		Program parsedProgram = parser.parse("a :- p(X), X != Y, q(Y).");
		assertEquals(1, parsedProgram.getRules().size());
		assertEquals(3, parsedProgram.getRules().get(0).getBody().size());
	}

	@Test(expected = UnsupportedOperationException.class)
	// Change expected after Alpha can deal with disjunction.
	public void parseProgramWithDisjunctionInHead() throws IOException {
		parser.parse("r(X) | q(X) :- q(X)." + System.lineSeparator() + "q(a)." + System.lineSeparator());
	}

	@Test
	public void parseInterval() throws IOException {
		Program parsedProgram = parser.parse("fact(2..5). p(X) :- q(a, 3 .. X).");
		IntervalTerm factInterval = (IntervalTerm) parsedProgram.getFacts().get(0).getTerms().get(0);
		assertTrue(factInterval.equals(IntervalTerm.getInstance(ConstantTerm.getInstance(2), ConstantTerm.getInstance(5))));
		IntervalTerm bodyInterval = (IntervalTerm) parsedProgram.getRules().get(0).getBody().get(0).getTerms().get(1);
		assertTrue(bodyInterval.equals(IntervalTerm.getInstance(ConstantTerm.getInstance(3), VariableTerm.getInstance("X"))));
	}

	@Test
	public void parseChoiceRule() throws IOException {
		Program parsedProgram = parser.parse("dom(1). dom(2). { a ; b } :- dom(X).");
		ChoiceHead choiceHead = (ChoiceHead) parsedProgram.getRules().get(0).getHead();
		BasicAtom atomA = new BasicAtom(Predicate.getInstance("a", 0));
		assertEquals(2, choiceHead.getChoiceElements().size());
		assertTrue(choiceHead.getChoiceElements().get(0).choiceAtom.toString().equals("a"));
		assertTrue(choiceHead.getChoiceElements().get(1).choiceAtom.toString().equals("b"));
		assertEquals(null, choiceHead.getLowerBound());
		assertEquals(null, choiceHead.getUpperBound());
	}

	@Test
	public void parseChoiceRuleBounded() throws IOException {
		Program parsedProgram = parser.parse("dom(1). dom(2). 1 < { a: p(v,w), not r; b } <= 13 :- dom(X). foo.");
		ChoiceHead choiceHead = (ChoiceHead) parsedProgram.getRules().get(0).getHead();
		BasicAtom atomA = new BasicAtom(Predicate.getInstance("a", 0));
		assertEquals(2, choiceHead.getChoiceElements().size());
		assertTrue(choiceHead.getChoiceElements().get(0).choiceAtom.toString().equals("a"));
		assertTrue(choiceHead.getChoiceElements().get(1).choiceAtom.toString().equals("b"));
		List<Literal> conditionalLiterals = choiceHead.getChoiceElements().get(0).conditionLiterals;
		assertEquals(2, conditionalLiterals.size());
		assertFalse(conditionalLiterals.get(0).isNegated());
		assertTrue(conditionalLiterals.get(1).isNegated());
		assertEquals(ConstantTerm.getInstance(1), choiceHead.getLowerBound());
		assertEquals(ComparisonOperator.LT, choiceHead.getLowerOperator());
		assertEquals(ConstantTerm.getInstance(13), choiceHead.getUpperBound());
		assertEquals(ComparisonOperator.LE, choiceHead.getUpperOperator());
	}

	@Test
	public void literate() throws IOException {
		final ReadableByteChannel input = Util.streamToChannel(Util.literate(Stream.of(
			"This is some description.",
			"",
			"    p(a).",
			"",
			"Test!"
		)));

		final String actual = new ProgramParser().parse(CharStreams.fromChannel(input)).toString();
		final String expected = "p(a)." + System.lineSeparator();

		assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMalformedInputNotIgnored() {
		String program = "foo(a) :- p(b).\n" +
			"// rule :- q.\n" +
			"r(1).\n" +
			"r(2).\n";
		parser.parse(program);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMissingDotNotIgnored() {
		parser.parse("p(X,Y) :- q(X), r(Y) p(a). q(b).");
	}

	@Test
	public void parseProgramWithHeuristicAtoms() throws IOException {
		Program parsedProgram = parser.parse(
			"a :- b, not _h(1), not d.\n" +
			"c(X) :- p(X,a,_), not _h(X), q(Xaa,xaa)." +
			":- f(Y).");

		assertEquals("Program contains three rules.", 3, parsedProgram.getRules().size());
		System.out.println(parsedProgram.getRules().toString());
	}

	@Test
	public void parseIncorrectProgramWithHeuristicAtoms() throws IOException {
		int faults = 0;
		faults += parseFaultyRule("a :- b, not _h(Y), not d.\n", 1);
		faults += parseFaultyRule("c(X) :- p(X,a,_), not _h(X,Xaa,Z), q(Xaa,xaa).", 1);
		faults += parseFaultyRule(":- f(Y), not _h(Y,X).", 1);

		assertEquals("Three faults were expected", 3, faults);
	}

	@Test
	public void parseProgramWithHeuristicAnnotation_W() {
		Program parsedProgram = parser.parse("c(X) :- p(X,a,_), q(Xaa,xaa). [X]");

		assertEquals("X", parsedProgram.getRules().iterator().next().getHeuristic().getWeight().toString());
		System.out.println(parsedProgram.getRules().toString());
	}

	@Test
	public void parseProgramWithHeuristicAnnotation_WL() {
		Program parsedProgram = parser.parse("c(X) :- p(X,a,_), q(Xaa,xaa). [X@2]");

		assertEquals("X", parsedProgram.getRules().iterator().next().getHeuristic().getWeight().toString());
		assertEquals("2", parsedProgram.getRules().iterator().next().getHeuristic().getLevel().toString());
		System.out.println(parsedProgram.getRules().toString());
	}

	@Test
	public void parseProgramWithHeuristicAnnotation_Generator() {
		Program parsedProgram = parser.parse("c(X) :- p(X,a,_), q(Xaa,xaa). [X@2 : not c(X)]");

		NonGroundDomainSpecificHeuristicValues heuristic = parsedProgram.getRules().iterator().next().getHeuristic();
		assertEquals("X", heuristic.getWeight().toString());
		assertEquals("2", heuristic.getLevel().toString());
		assertEquals("not c(X)", Literals.toString(heuristic.getGenerator()));
		System.out.println(parsedProgram.getRules().toString());
	}

	@Test
	public void parseProgramWithHeuristicAnnotation_GeneratorWithArithmetics() {
		Program parsedProgram = parser.parse("holds(F,T) :- fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T). "
				+ "[LTp1mT@1 : holds(F,Tp1), Tp1=T+1, LTp1mT=LT+1-T]");

		NonGroundDomainSpecificHeuristicValues heuristic = parsedProgram.getRules().iterator().next().getHeuristic();
		assertEquals("LTp1mT", heuristic.getWeight().toString());
		assertEquals("1", heuristic.getLevel().toString());
		assertEquals("holds(F, Tp1), Tp1 = T + 1, LTp1mT = LT + 1 - T", Literals.toString(heuristic.getGenerator()));
		System.out.println(parsedProgram.getRules().toString());
	}

	@Test(expected = RuntimeException.class)
	public void parseProgramWithHeuristicAnnotation_GeneratorWithArithmetics_Unsafe1() {
		parser.parse("holds(F,T) :- fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T). "
				+ "[T2@1 : holds(F,Tp1), Tp1=T+1, LTp1mT=LT+1-T]");
	}

	@Test(expected = RuntimeException.class)
	public void parseProgramWithHeuristicAnnotation_GeneratorWithArithmetics_Unsafe2() {
		parser.parse("holds(F,T) :- fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T). "
				+ "[LTp1mT@1 : holds(F,T2), Tp1=T+1, LTp1mT=LT+1-T]");
	}

	@Test(expected = RuntimeException.class)
	public void parseProgramWithHeuristicAnnotation_GeneratorWithArithmetics_Unsafe3() {
		parser.parse("holds(F,T) :- fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T). "
				+ "[LTp1mT@1 : holds(F,Tp1), Tp1=T2+1, LTp1mT=LT+1-T]");
	}

	private int parseFaultyRule(String program, int rules) {
		try {
			Program prg = parser.parse(program);
			assertEquals(prg.getRules().size(), rules);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return 1;
		}
		return 0;
	}
}