/**
 * Copyright (c) 2016-2019, the Alpha Team.
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
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.*;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.HeuristicDirectiveToRule;
import at.ac.tuwien.kr.alpha.solver.heuristics.HeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.solver.heuristics.HeuristicsConfigurationBuilder;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2016-2019, the Alpha Team.
 */
public class ParserTest {
	private final ProgramParser parser = new ProgramParser();
	private final HeuristicsConfiguration heuristicsConfiguration = new HeuristicsConfigurationBuilder().setRespectDomspecHeuristics(true).build();
	
	@Before
	public void setUp() {
		VariableTerm.ANONYMOUS_VARIABLE_COUNTER.resetGenerator();
	}

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
		IntervalTerm bodyInterval = (IntervalTerm) ((Literal)parsedProgram.getRules().get(0).getBody().get(0)).getTerms().get(1);
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
	public void parseEnumerationDirective() throws IOException {
		Program parsedProgram = parser.parse("p(a,1)." +
			"# enumeration_predicate_is mune." +
			"r(X) :- p(X), mune(X)." +
			"p(b,2).");
		String directive = ((EnumerationDirective)parsedProgram.getInlineDirectives().getDirectiveValue(InlineDirectives.DIRECTIVE.enum_predicate_is)).getValue();
		assertEquals("mune", directive);
	}

	@Test
	public void cardinalityAggregate() throws IOException {
		Program parsedProgram = parser.parse("num(K) :-  K <= #count {X,Y,Z : p(X,Y,Z) }, dom(K).");
		Literal bodyElement = parsedProgram.getRules().get(0).getBody().get(0);
		assertTrue(bodyElement instanceof AggregateLiteral);
		AggregateLiteral parsedAggregate = (AggregateLiteral) bodyElement;
		VariableTerm x = VariableTerm.getInstance("X");
		VariableTerm y = VariableTerm.getInstance("Y");
		VariableTerm z = VariableTerm.getInstance("Z");
		List<Term> basicTerms = Arrays.asList(x, y, z);
		AggregateAtom.AggregateElement aggregateElement = new AggregateAtom.AggregateElement(basicTerms, Collections.singletonList(new BasicAtom(Predicate.getInstance("p", 3), x, y, z).toLiteral()));
		AggregateAtom expectedAggregate = new AggregateAtom(ComparisonOperator.LE, VariableTerm.getInstance("K"), null, null, AggregateAtom.AGGREGATEFUNCTION.COUNT, Collections.singletonList(aggregateElement));
		assertEquals(expectedAggregate, parsedAggregate.getAtom());
	}
	
	@Test
	public void parseProgramWithHeuristicDirective_W() {
		Program parsedProgram = parser.parse("c(X) :- p(X,a,_), q(Xaa,xaa). "
				+ "#heuristic c(X) : p(X,a,_), q(Xaa,xaa). [X]");

		HeuristicDirective directive = (HeuristicDirective) parsedProgram.getInlineDirectives().getDirectives().iterator().next();
		assertEquals("c(X)", directive.getHead().toString());
		assertEquals("[p(X, a, _1), q(Xaa, xaa)]", directive.getBody().toString());
		WeightAtLevel weightAtLevel = directive.getWeightAtLevel();
		assertEquals("X", weightAtLevel.getWeight().toString());
		assertEquals("1", weightAtLevel.getLevel().toString());
	}

	@Test
	public void parseProgramWithHeuristicDirective_WP() {
		Program parsedProgram = parser.parse("c(X) :- p(X,a,_), q(Xaa,xaa). "
				+ "#heuristic c(X) : p(X,a,_), q(Xaa,xaa). [X@2]");

		HeuristicDirective directive = (HeuristicDirective) parsedProgram.getInlineDirectives().getDirectives().iterator().next();
		assertEquals("c(X)", directive.getHead().toString());
		assertEquals("[p(X, a, _1), q(Xaa, xaa)]", directive.getBody().toString());
		WeightAtLevel weightAtLevel = directive.getWeightAtLevel();
		assertEquals("X", weightAtLevel.getWeight().toString());
		assertEquals("2", weightAtLevel.getLevel().toString());
	}

	@Test
	public void parseProgramWithHeuristicDirective_Generator() {
		Program parsedProgram = parser.parse("c(X) :- p(X,a,_), q(Xaa,xaa). "
				+ "#heuristic c(X) : p(X,a,_), q(Xaa,xaa), not c(X). [X@2]");

		HeuristicDirective directive = (HeuristicDirective) parsedProgram.getInlineDirectives().getDirectives().iterator().next();
		assertEquals("c(X)", directive.getHead().toString());
		assertEquals("[p(X, a, _1), q(Xaa, xaa), not c(X)]", directive.getBody().toString());
		WeightAtLevel weightAtLevel = directive.getWeightAtLevel();
		assertEquals("X", weightAtLevel.getWeight().toString());
		assertEquals("2", weightAtLevel.getLevel().toString());
	}

	@Test
	public void parseProgramWithHeuristicDirective_GeneratorWithArithmetics() {
		Program parsedProgram = parser.parse("holds(F,T) :- fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T). "
				+ "#heuristic holds(F,T) : fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T), holds(F,Tp1), Tp1=T+1, LTp1mT=LT+1-T. [LTp1mT@1]");

		HeuristicDirective directive = (HeuristicDirective) parsedProgram.getInlineDirectives().getDirectives().iterator().next();
		assertEquals("holds(F, T)", directive.getHead().toString());
		assertEquals("[fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F, T), holds(F, Tp1), Tp1 = T + 1, LTp1mT = LT + 1 - T]", directive.getBody().toString());
		WeightAtLevel weightAtLevel = directive.getWeightAtLevel();
		assertEquals("LTp1mT", weightAtLevel.getWeight().toString());
		assertEquals("1", weightAtLevel.getLevel().toString());
	}

	/**
	 * Classical negation in heuristic conditions is not yet supported.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void parseProgramWithClassicalNegationInHeuristicCondition() {
		parser.parse("holds(F,T) :- fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T). "
				+ "#heuristic holds(F,T) : fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T), -holds(F,Tp1), Tp1=T+1, LTp1mT=LT+1-T. [LTp1mT@1]");
	}
	
	/**
	 * Empty heuristic conditions are not yet supported.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testEmptyCondition() {
		parser.parse("a(1)."
				+ "{ b(N) } :- a(N)."
				+ "#heuristic b(1). [1@2]");
		}

	@Test(expected = RuntimeException.class)
	@Ignore("Currently, Rule#isSafe does nothing")
	public void parseProgramWithHeuristicDirective_GeneratorWithArithmetics_Unsafe1() {
		parseProgramAndTransformHeuristicDirectives("holds(F,T) :- fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T). "
				+ "#heuristic holds(F,T) : fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T), holds(F,Tp1), Tp1=T+1, LTp1mT=LT+1-T. [T2@1]");
	}

	@Test(expected = RuntimeException.class)
	@Ignore("Currently, Rule#isSafe does nothing")
	public void parseProgramWithHeuristicDirective_GeneratorWithArithmetics_Unsafe2() {
		parseProgramAndTransformHeuristicDirectives("holds(F,T) :- fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T). "
				+ "#heuristic holds(F,T) : fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T), holds(F,T2), Tp1=T+1, LTp1mT=LT+1-T. [LTp1mT@1]");
	}

	@Test(expected = RuntimeException.class)
	@Ignore("Currently, Rule#isSafe does nothing")
	public void parseProgramWithHeuristicDirective_GeneratorWithArithmetics_Unsafe3() {
		parseProgramAndTransformHeuristicDirectives("holds(F,T) :- fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T). "
				+ "#heuristic holds(F,T) : fluent(F), time(T), T > 0, lasttime(LT), not not_holds(F,T), holds(F,Tp1), Tp1=T2+1, LTp1mT=LT+1-T. [LTp1mT@1]");
	}
	
	private void parseProgramAndTransformHeuristicDirectives(String input) {
		Program program = parser.parse(input);
		new HeuristicDirectiveToRule(heuristicsConfiguration).transform(program); // without transforming it to a rule, the safety of a heuristic directive is not checked currently
	}
}