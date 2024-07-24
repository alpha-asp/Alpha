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
package at.ac.tuwien.kr.alpha.core.parser;

import at.ac.tuwien.kr.alpha.api.programs.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ModuleAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.AggregateLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.literals.ModuleLiteral;
import at.ac.tuwien.kr.alpha.api.programs.modules.Module;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.ChoiceHead;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.programs.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.programs.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.api.programs.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.api.programs.tests.Assertion;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestCase;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import at.ac.tuwien.kr.alpha.commons.util.Util;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParserTest {

	private static final String UNIT_TEST_EXPECT_UNSAT =
			"p(1). p(2). "
					+ ":- p(X), p(Y), X + Y = 3."
					+ "#test expected_unsat(expect: unsat) {"
					+ "given {}"
					+ "}";

	private static final String UNIT_TEST_BASIC_TEST =
			"a :- b. #test ensure_a(expect: 1) { given { b. } assertForAll { :- not a. } }";
	private static final String UNIT_TEST_MORE_ASSERTIONS =
			"a :- b. #test ensure_a(expect: 1) { given { b. } assertForAll { :- not a. } assertForSome { :- not a.} }";

	private static final String UNIT_TEST_MORE_TCS =
			"a :- b. #test ensure_a(expect: 1) { given { b. } assertForAll { :- not a. }} " +
					"#test ensure_not_c (expect: 1) { given { b.} assertForAll { :- c. }}";

	private static final String UNIT_TEST_KEYWORDS_AS_IDS =
			"assert(a) :- given(b). # test test(expect: 1) { given { given(b). } assertForAll { :- not assert(a). :- assertForSome(b).}}";

	private static final String MODULE_SIMPLE = "#module aSimpleModule(input/1 => {out1/2, out2/3}) { p(a). p(b). q(X) :- p(X). }";

	private static final String MODULE_OUTPUT_ALL = "#module mod(in/1 => {*}) { a(X). b(X) :- a(X).}";

	private static final String MODULE_WITH_REGULAR_STMTS = "p(a). p(b). q(X) :- p(X). #module aSimpleModule(input/1 => {out1/2, out2/3}) { p(a). p(b). q(X) :- p(X). }";

	private static final String MODULE_MULTIPLE_DEFINITIONS = "a. b(5). #module aSimpleModule(input/1 => {out1/2, out2/3}) { p(a). p(b). q(X) :- p(X). } q(Y) :- r(S, Y), t(S). #module anotherModule(input/1 => {out1/2, out2/3}) { p(a). p(b). q(X) :- p(X). }";

	private static final String MODULE_LITERAL = "p(a). q(b). r(X) :- p(X), q(Y), #mod[X, Y](X).";

	private static final String MODULE_LITERAL_WITH_NUM_ANSWER_SETS = ":- r(1), q(B, 1). r(X) :- p(X), q(Y), #mod{4}[X, Y](X).";

	private static final String MODULE_LITERAL_NO_INPUT = "a(X) :- #something(X).";

	private static final String MODULE_LITERAL_NO_INPUT_WITH_NUM_ANSWER_SETS = "a(X) :- #something{4}(X).";

	private static final String MODULE_LITERAL_NO_OUTPUT = "a(X) :- #something[X].";

	private static final String MODULE_LITERAL_NO_OUTPUT_WITH_NUM_ANSWER_SETS = "a(X) :- #something{4}[X].";

	private final ProgramParserImpl parser = new ProgramParserImpl();

	@Test
	public void parseFact() {
		InputProgram parsedProgram = parser.parse("p(a,b).");

		assertEquals(1, parsedProgram.getFacts().size(), "Program contains one fact.");
		assertEquals("p", parsedProgram.getFacts().get(0).getPredicate().getName(), "Predicate name of fact is p.");
		assertEquals(2, parsedProgram.getFacts().get(0).getPredicate().getArity(), "Fact has two terms.");
		assertEquals("a", (parsedProgram.getFacts().get(0).getTerms().get(0)).toString(), "First term is a.");
		assertEquals("b", (parsedProgram.getFacts().get(0).getTerms().get(1)).toString(), "Second term is b.");
	}

	@Test
	public void parseFactWithFunctionTerms() {
		InputProgram parsedProgram = parser.parse("p(f(a),g(h(Y))).");

		assertEquals(1, parsedProgram.getFacts().size(), "Program contains one fact.");
		assertEquals("p", parsedProgram.getFacts().get(0).getPredicate().getName(), "Predicate name of fact is p.");
		assertEquals(2, parsedProgram.getFacts().get(0).getPredicate().getArity(), "Fact has two terms.");
		assertEquals("f", ((FunctionTerm) parsedProgram.getFacts().get(0).getTerms().get(0)).getSymbol(), "First term is function term f.");
		assertEquals("g", ((FunctionTerm) parsedProgram.getFacts().get(0).getTerms().get(1)).getSymbol(), "Second term is function term g.");
	}

	@Test
	public void parseSmallProgram() {
		InputProgram parsedProgram = parser.parse(
				"a :- b, not d." + System.lineSeparator() +
						"c(X) :- p(X,a,_), q(Xaa,xaa)." + System.lineSeparator() +
						":- f(Y).");

		assertEquals(3, parsedProgram.getRules().size(), "Program contains three rules.");
	}

	@Test
	public void parseBadSyntax() {
		assertThrows(IllegalArgumentException.class, () -> {
			parser.parse("Wrong Syntax.");
		});
	}

	@Test
	public void parseBuiltinAtom() {
		InputProgram parsedProgram = parser.parse("a :- p(X), X != Y, q(Y).");
		assertEquals(1, parsedProgram.getRules().size());
		assertEquals(3, parsedProgram.getRules().get(0).getBody().size());
	}

	@Test
	// Change expected after Alpha can deal with disjunction.
	public void parseProgramWithDisjunctionInHead() {
		assertThrows(UnsupportedOperationException.class, () -> {
			parser.parse("r(X) | q(X) :- q(X)." + System.lineSeparator() + "q(a)." + System.lineSeparator());
		});
	}

	@Test
	public void parseInterval() {
		InputProgram parsedProgram = parser.parse("fact(2..5). p(X) :- q(a, 3 .. X).");
		IntervalTerm factInterval = (IntervalTerm) parsedProgram.getFacts().get(0).getTerms().get(0);
		assertEquals(factInterval, Terms.newIntervalTerm(Terms.newConstant(2), Terms.newConstant(5)));
		IntervalTerm bodyInterval = (IntervalTerm) parsedProgram.getRules().get(0).getBody().stream().findFirst().get().getTerms().get(1);
		assertEquals(bodyInterval, Terms.newIntervalTerm(Terms.newConstant(3), Terms.newVariable("X")));
	}

	@Test
	public void parseChoiceRule() {
		InputProgram parsedProgram = parser.parse("dom(1). dom(2). { a ; b } :- dom(X).");
		ChoiceHead choiceHead = (ChoiceHead) parsedProgram.getRules().get(0).getHead();
		assertEquals(2, choiceHead.getChoiceElements().size());
		assertEquals("a", choiceHead.getChoiceElements().get(0).getChoiceAtom().toString());
		assertEquals("b", choiceHead.getChoiceElements().get(1).getChoiceAtom().toString());
		assertNull(choiceHead.getLowerBound());
		assertNull(choiceHead.getUpperBound());
	}

	@Test
	public void parseChoiceRuleBounded() {
		InputProgram parsedProgram = parser.parse("dom(1). dom(2). 1 < { a: p(v,w), not r; b } <= 13 :- dom(X). foo.");
		ChoiceHead choiceHead = (ChoiceHead) parsedProgram.getRules().get(0).getHead();
		assertEquals(2, choiceHead.getChoiceElements().size());
		assertEquals("a", choiceHead.getChoiceElements().get(0).getChoiceAtom().toString());
		assertEquals("b", choiceHead.getChoiceElements().get(1).getChoiceAtom().toString());
		List<Literal> conditionalLiterals = choiceHead.getChoiceElements().get(0).getConditionLiterals();
		assertEquals(2, conditionalLiterals.size());
		assertFalse(conditionalLiterals.get(0).isNegated());
		assertTrue(conditionalLiterals.get(1).isNegated());
		assertEquals(Terms.newConstant(1), choiceHead.getLowerBound());
		assertEquals(ComparisonOperators.LT, choiceHead.getLowerOperator());
		assertEquals(Terms.newConstant(13), choiceHead.getUpperBound());
		assertEquals(ComparisonOperators.LE, choiceHead.getUpperOperator());
	}

	@Test
	public void literate() throws IOException {
		final ReadableByteChannel input = Util.streamToChannel(Util.literate(Stream.of(
				"This is some description.",
				"",
				"    p(a).",
				"",
				"Test!")));

		final String actual = new ProgramParserImpl().parse(CharStreams.fromChannel(input)).toString();
		final String expected = "p(a)." + System.lineSeparator();

		assertEquals(expected, actual);
	}

	@Test
	public void testMalformedInputNotIgnored() {
		String program = "foo(a) :- p(b).\n" +
				"// rule :- q.\n" +
				"r(1).\n" +
				"r(2).\n";
		assertThrows(IllegalArgumentException.class, () -> {
			parser.parse(program);
		});
	}

	@Test
	public void testMissingDotNotIgnored() {
		assertThrows(IllegalArgumentException.class, () -> {
			parser.parse("p(X,Y) :- q(X), r(Y) p(a). q(b).");
		});
	}

	@Test
	public void parseEnumerationDirective() {
		InputProgram parsedProgram = parser.parse("p(a,1)." +
				"# enumeration_predicate_is mune." +
				"r(X) :- p(X), mune(X)." +
				"p(b,2).");
		String directive = parsedProgram.getInlineDirectives().getDirectiveValue(InlineDirectives.DIRECTIVE.enum_predicate_is);
		assertEquals("mune", directive);
	}

	@Test
	public void cardinalityAggregate() {
		InputProgram parsedProgram = parser.parse("num(K) :-  K <= #count {X,Y,Z : p(X,Y,Z) }, dom(K).");
		Optional<Literal> optionalBodyElement = parsedProgram.getRules().get(0).getBody().stream().filter((lit) -> lit instanceof AggregateLiteral).findFirst();
		assertTrue(optionalBodyElement.isPresent());
		Literal bodyElement = optionalBodyElement.get();
		AggregateLiteral parsedAggregate = (AggregateLiteral) bodyElement;
		VariableTerm x = Terms.newVariable("X");
		VariableTerm y = Terms.newVariable("Y");
		VariableTerm z = Terms.newVariable("Z");
		List<Term> basicTerms = Arrays.asList(x, y, z);
		AggregateAtom.AggregateElement aggregateElement = Atoms.newAggregateElement(basicTerms,
				Collections.singletonList(Atoms.newBasicAtom(Predicates.getPredicate("p", 3), x, y, z).toLiteral()));
		AggregateAtom expectedAggregate = Atoms.newAggregateAtom(ComparisonOperators.LE, Terms.newVariable("K"), null, null,
				AggregateAtom.AggregateFunctionSymbol.COUNT, Collections.singletonList(aggregateElement));
		assertEquals(expectedAggregate, parsedAggregate.getAtom());
	}

	@Test
	public void stringWithEscapedQuotes() throws IOException {
		CharStream stream = CharStreams.fromStream(ParserTest.class.getResourceAsStream("/escaped_quotes.asp"));
		InputProgram prog = parser.parse(stream);
		assertEquals(1, prog.getFacts().size());
		Atom stringAtom = prog.getFacts().get(0);
		String stringWithQuotes = stringAtom.getTerms().get(0).toString();
		assertEquals("\"a string with \"quotes\"\"", stringWithQuotes);
	}

	@Test
	public void unitTestExpectUnsat() {
		InputProgram prog = parser.parse(UNIT_TEST_EXPECT_UNSAT);
		assertEquals(1, prog.getTestCases().size());
		TestCase tc = prog.getTestCases().get(0);
		assertEquals("expected_unsat", tc.getName());
		assertTrue(tc.getInput().isEmpty());
		assertTrue(tc.getAssertions().isEmpty());
	}

	@Test
	public void unitTestBasicTest() {
		InputProgram prog = parser.parse(UNIT_TEST_BASIC_TEST);
		assertEquals(1, prog.getTestCases().size());
		TestCase tc = prog.getTestCases().get(0);
		assertEquals("ensure_a", tc.getName());
		assertEquals(1, tc.getInput().size());
		assertEquals(1, tc.getAssertions().size());
		assertEquals(Assertion.Mode.FOR_ALL, tc.getAssertions().get(0).getMode());
	}

	@Test
	public void unitTestMultipleAsserts() {
		InputProgram prog = parser.parse(UNIT_TEST_MORE_ASSERTIONS);
		assertEquals(1, prog.getTestCases().size());
		TestCase tc = prog.getTestCases().get(0);
		assertEquals("ensure_a", tc.getName());
		assertEquals(1, tc.getInput().size());
		assertEquals(2, tc.getAssertions().size());
		assertEquals(Assertion.Mode.FOR_ALL, tc.getAssertions().get(0).getMode());
		assertEquals(Assertion.Mode.FOR_SOME, tc.getAssertions().get(1).getMode());
	}

	@Test
	public void unitTestMoreTCs() {
		InputProgram prog = parser.parse(UNIT_TEST_MORE_TCS);
		assertEquals(2, prog.getTestCases().size());
		TestCase tc1 = prog.getTestCases().get(0);
		assertEquals("ensure_a", tc1.getName());
		TestCase tc2 = prog.getTestCases().get(1);
		assertEquals("ensure_not_c", tc2.getName());
	}

	@Test
	public void unitTestKeywordsAsIds() {
		InputProgram prog = parser.parse(UNIT_TEST_KEYWORDS_AS_IDS);
		assertEquals(1, prog.getTestCases().size());
		TestCase tc = prog.getTestCases().get(0);
		assertEquals("test", tc.getName());
		assertEquals(1, tc.getInput().size());
		assertEquals(1, tc.getAssertions().size());
		assertEquals(Assertion.Mode.FOR_ALL, tc.getAssertions().get(0).getMode());
	}

	@Test
	public void simpleModule() {
		InputProgram prog = parser.parse(MODULE_SIMPLE);
		List<Module> modules = prog.getModules();
		assertFalse(modules.isEmpty());
		assertEquals(1, modules.size());
		Module module = modules.get(0);
		assertEquals("aSimpleModule", module.getName());
		Predicate inputSpec = module.getInputSpec();
		assertEquals("input", inputSpec.getName());
		assertEquals(1, inputSpec.getArity());
		Set<Predicate> outputSpec = module.getOutputSpec();
		assertEquals(2, outputSpec.size());
		assertTrue(outputSpec.contains(Predicates.getPredicate("out1", 2)));
		assertTrue(outputSpec.contains(Predicates.getPredicate("out2", 3)));
		InputProgram implementation = module.getImplementation();
		assertEquals(2, implementation.getFacts().size());
		assertEquals(1, implementation.getRules().size());
	}

	@Test
	public void moduleOutputAll() {
		InputProgram prog = parser.parse(MODULE_OUTPUT_ALL);
		List<Module> modules = prog.getModules();
		assertFalse(modules.isEmpty());
		assertEquals(1, modules.size());
		Module module = modules.get(0);
		assertEquals("mod", module.getName());
		Predicate inputSpec = module.getInputSpec();
		assertEquals("in", inputSpec.getName());
		assertEquals(1, inputSpec.getArity());
		assertTrue(module.getOutputSpec().isEmpty());
		InputProgram implementation = module.getImplementation();
		assertEquals(1, implementation.getFacts().size());
		assertEquals(1, implementation.getRules().size());
	}

	@Test
	public void moduleAndRegularStmts() {
		InputProgram prog = parser.parse(MODULE_WITH_REGULAR_STMTS);
		assertEquals(2, prog.getFacts().size());
		assertEquals(1, prog.getRules().size());
		List<Module> modules = prog.getModules();
		assertFalse(modules.isEmpty());
		assertEquals(1, modules.size());
		Module module = modules.get(0);
		assertEquals("aSimpleModule", module.getName());
		Predicate inputSpec = module.getInputSpec();
		assertEquals("input", inputSpec.getName());
		assertEquals(1, inputSpec.getArity());
		Set<Predicate> outputSpec = module.getOutputSpec();
		assertEquals(2, outputSpec.size());
		assertTrue(outputSpec.contains(Predicates.getPredicate("out1", 2)));
		assertTrue(outputSpec.contains(Predicates.getPredicate("out2", 3)));
		InputProgram implementation = module.getImplementation();
		assertEquals(2, implementation.getFacts().size());
		assertEquals(1, implementation.getRules().size());
	}

	@Test
	public void multipleModuleDefinitions() {
		InputProgram prog = parser.parse(MODULE_MULTIPLE_DEFINITIONS);
		assertEquals(2, prog.getFacts().size());
		assertEquals(1, prog.getRules().size());

		List<Module> modules = prog.getModules();
		assertFalse(modules.isEmpty());
		assertEquals(2, modules.size());
	}

	@Test
	public void invalidNestedModule() {
		assertThrows(IllegalStateException.class, () ->
				parser.parse("#module aSimpleModule(input/1 => {out1/2, out2/3}) { p(a). p(b). #module anotherModule(input/1 => {out1/2, out2/3}) { p(a). p(b). } }"));
	}

	@Test
	public void invalidNestedTest() {
		assertThrows(IllegalStateException.class, () ->
				parser.parse("#module mod(foo/1 => {*}) { #test test(expect: 1) { given { b. } assertForAll { :- a. } } }"));
	}

	@Test
	public void moduleLiteral() {
		InputProgram prog = parser.parse(MODULE_LITERAL);
		assertEquals(2, prog.getFacts().size());
		assertEquals(1, prog.getRules().size());
		Rule<?> rule = prog.getRules().get(0);
		assertEquals(3, rule.getBody().size());
		assertEquals(1, rule.getBody().stream().filter(lit -> lit instanceof ModuleLiteral).count());
		ModuleLiteral moduleLiteral = (ModuleLiteral) rule.getBody().stream().filter(lit -> lit instanceof ModuleLiteral).findFirst().get();
		assertEquals("mod", moduleLiteral.getAtom().getModuleName());
		assertEquals(2, moduleLiteral.getAtom().getInput().size());
		assertEquals(1, moduleLiteral.getAtom().getOutput().size());
		assertEquals(ModuleAtom.ModuleInstantiationMode.ALL, moduleLiteral.getAtom().getInstantiationMode());
	}

	@Test
	public void moduleLiteralWithNumAnswerSets() {
		InputProgram prog = parser.parse(MODULE_LITERAL_WITH_NUM_ANSWER_SETS);
		assertEquals(2, prog.getRules().size());
		Optional<Rule<Head>> ruleWithModuleLiteral = prog.getRules().stream().filter(rule -> rule.getBody().stream().anyMatch(lit -> lit instanceof ModuleLiteral)).findFirst();
		assertTrue(ruleWithModuleLiteral.isPresent());
		Rule<?> rule = ruleWithModuleLiteral.get();
		assertEquals(3, rule.getBody().size());
		assertEquals(1, rule.getBody().stream().filter(lit -> lit instanceof ModuleLiteral).count());
		Optional<Literal> optModuleLiteral = rule.getBody().stream().filter(lit -> lit instanceof ModuleLiteral).findFirst();
		assertTrue(optModuleLiteral.isPresent());
		ModuleLiteral moduleLiteral = (ModuleLiteral) optModuleLiteral.get();
		assertEquals("mod", moduleLiteral.getAtom().getModuleName());
		assertEquals(2, moduleLiteral.getAtom().getInput().size());
		assertEquals(1, moduleLiteral.getAtom().getOutput().size());
		assertTrue(moduleLiteral.getAtom().getInstantiationMode().requestedAnswerSets().isPresent());
		assertEquals(4, moduleLiteral.getAtom().getInstantiationMode().requestedAnswerSets().get());
	}

	@Test
	public void moduleLiteralNoInput() {
		InputProgram prog = parser.parse(MODULE_LITERAL_NO_INPUT);
		assertEquals(1, prog.getRules().size());
		Rule<?> rule = prog.getRules().get(0);
		assertEquals(1, rule.getBody().size());
		assertEquals(1, rule.getBody().stream().filter(lit -> lit instanceof ModuleLiteral).count());
		ModuleLiteral moduleLiteral = (ModuleLiteral) rule.getBody().stream().filter(lit -> lit instanceof ModuleLiteral).findFirst().get();
		assertEquals("something", moduleLiteral.getAtom().getModuleName());
		assertTrue(moduleLiteral.getAtom().getInput().isEmpty());
		assertEquals(1, moduleLiteral.getAtom().getOutput().size());
		assertEquals(ModuleAtom.ModuleInstantiationMode.ALL, moduleLiteral.getAtom().getInstantiationMode());
	}

	@Test
	public void moduleLiteralNoInputWithNumAnswerSets() {
		InputProgram prog = parser.parse(MODULE_LITERAL_NO_INPUT_WITH_NUM_ANSWER_SETS);
		assertEquals(1, prog.getRules().size());
		Rule<?> rule = prog.getRules().get(0);
		assertEquals(1, rule.getBody().size());
		assertEquals(1, rule.getBody().stream().filter(lit -> lit instanceof ModuleLiteral).count());
		ModuleLiteral moduleLiteral = (ModuleLiteral) rule.getBody().stream().filter(lit -> lit instanceof ModuleLiteral).findFirst().get();
		assertEquals("something", moduleLiteral.getAtom().getModuleName());
		assertTrue(moduleLiteral.getAtom().getInput().isEmpty());
		assertEquals(1, moduleLiteral.getAtom().getOutput().size());
		assertTrue(moduleLiteral.getAtom().getInstantiationMode().requestedAnswerSets().isPresent());
		assertEquals(4, moduleLiteral.getAtom().getInstantiationMode().requestedAnswerSets().get());
	}

	@Test
	public void moduleLiteralNoOutput() {
		InputProgram prog = parser.parse(MODULE_LITERAL_NO_OUTPUT);
		assertEquals(1, prog.getRules().size());
		Rule<?> rule = prog.getRules().get(0);
		assertEquals(1, rule.getBody().size());
		assertEquals(1, rule.getBody().stream().filter(lit -> lit instanceof ModuleLiteral).count());
		ModuleLiteral moduleLiteral = (ModuleLiteral) rule.getBody().stream().filter(lit -> lit instanceof ModuleLiteral).findFirst().get();
		assertEquals("something", moduleLiteral.getAtom().getModuleName());
		assertEquals(1, moduleLiteral.getAtom().getInput().size());
		assertTrue(moduleLiteral.getAtom().getOutput().isEmpty());
		assertFalse(moduleLiteral.getAtom().getInstantiationMode().requestedAnswerSets().isPresent());
		assertEquals(ModuleAtom.ModuleInstantiationMode.ALL, moduleLiteral.getAtom().getInstantiationMode());
	}

	@Test
	public void moduleLiteralNoOutputWithNumAnswerSets() {
		InputProgram prog = parser.parse(MODULE_LITERAL_NO_OUTPUT_WITH_NUM_ANSWER_SETS);
		assertEquals(1, prog.getRules().size());
		Rule<?> rule = prog.getRules().get(0);
		assertEquals(1, rule.getBody().size());
		assertEquals(1, rule.getBody().stream().filter(lit -> lit instanceof ModuleLiteral).count());
		ModuleLiteral moduleLiteral = (ModuleLiteral) rule.getBody().stream().filter(lit -> lit instanceof ModuleLiteral).findFirst().get();
		assertEquals("something", moduleLiteral.getAtom().getModuleName());
		assertEquals(1, moduleLiteral.getAtom().getInput().size());
		assertTrue(moduleLiteral.getAtom().getOutput().isEmpty());
		assertTrue(moduleLiteral.getAtom().getInstantiationMode().requestedAnswerSets().isPresent());
		assertEquals(4, moduleLiteral.getAtom().getInstantiationMode().requestedAnswerSets().get());
	}

}
