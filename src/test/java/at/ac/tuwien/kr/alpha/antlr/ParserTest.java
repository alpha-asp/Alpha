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

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.ChoiceHead;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.Variable;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

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
		assertTrue(bodyInterval.equals(IntervalTerm.getInstance(ConstantTerm.getInstance(3), Variable.getInstance("X"))));
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
}