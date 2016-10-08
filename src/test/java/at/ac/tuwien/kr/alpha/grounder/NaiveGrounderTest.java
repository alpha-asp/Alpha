package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.solver.NaiveSolver;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static at.ac.tuwien.kr.alpha.MainTest.stream;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.*;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class NaiveGrounderTest {

	@Before
	public void resetCounters() {
		NonGroundRule.RULE_ID_GENERATOR.resetGenerator();
	}


	@Test
	public void unifyTermsSimpleBinding() throws Exception {
		NaiveGrounder grounder = new NaiveGrounder(new ParsedProgram());
		NaiveGrounder.VariableSubstitution variableSubstitution = grounder.new VariableSubstitution();
		Term groundTerm = ConstantTerm.getInstance("abc");
		Term nongroundTerm = VariableTerm.getInstance("Y");
		grounder.unifyTerms(nongroundTerm, groundTerm, variableSubstitution);
		assertEquals("Variable Y must bind to constant term abc", variableSubstitution.substitution.get(VariableTerm.getInstance("Y")), ConstantTerm.getInstance("abc"));
	}

	@Test
	public void unifyTermsFunctionTermBinding() throws Exception {
		NaiveGrounder grounder = new NaiveGrounder(new ParsedProgram());
		NaiveGrounder.VariableSubstitution variableSubstitution = grounder.new VariableSubstitution();
		variableSubstitution.substitution.put(VariableTerm.getInstance("Z"), ConstantTerm.getInstance("aa"));
		FunctionTerm groundFunctionTerm = FunctionTerm.getFunctionTerm("f", asList(new Term[]{ConstantTerm.getInstance("bb"), ConstantTerm.getInstance("cc")}));

		Term nongroundFunctionTerm = FunctionTerm.getFunctionTerm("f", asList(ConstantTerm.getInstance("bb"), VariableTerm.getInstance("X")));
		grounder.unifyTerms(nongroundFunctionTerm, groundFunctionTerm, variableSubstitution);
		assertEquals("Variable X must bind to constant term cc", variableSubstitution.substitution.get(VariableTerm.getInstance("X")), ConstantTerm.getInstance("cc"));

		assertEquals("Variable Z must bind to constant term aa", variableSubstitution.substitution.get(VariableTerm.getInstance("Z")), ConstantTerm.getInstance("aa"));
	}

	@Test
	public void testFactsOnlyProgram() throws IOException {
		String testProgram = "p(a). p(b). foo(13). foo(16). q(a). q(c).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		NaiveSolver solver = new NaiveSolver(grounder);

		Stream<AnswerSet> stream = StreamSupport.stream(solver.spliterator(), false);
		List<AnswerSet> recorder = stream.collect(Collectors.toList());

		assertEquals(1, recorder.size());

		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("q").instance("a").instance("c")
			.predicate("p").instance("a").instance("b")
			.predicate("foo").instance("13").instance("16")
			.build();

		assertEquals(expected, recorder.get(0));
	}

	@Test
	public void testSimpleRule() throws Exception {
		String testProgram = "p(a). p(b). r(X) :- p(X).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		NaiveSolver solver = new NaiveSolver(grounder);
		AnswerSet answerSet = solver.computeNextAnswerSet();
		AnswerSet noAnswerSet = solver.computeNextAnswerSet();

		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("p").instance("a").instance("b")
			.predicate("_R_").instance("0", "_X:a").instance("0", "_X:b")
			.predicate("r").instance("a").instance("b")
			.build();

		assertNotNull("Test program must yield one answer set (no answer-set reported)", answerSet);
		assertEquals(expected, answerSet);

		assertNull("Test program must yield one answer set (second answer-set reported).", noAnswerSet);
	}

	@Test
	public void testSimpleRuleWithGroundPart() throws Exception {
		String testProgram =
			"p(1)." +
				"p(2)." +
				"q(X) :-  p(X), p(1).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		NaiveSolver solver = new NaiveSolver(grounder);
		AnswerSet answerSet = solver.computeNextAnswerSet();
		AnswerSet noAnswerSet = solver.computeNextAnswerSet();

		assertNotNull("Test program must yield one answer set (no answer-set reported)", answerSet);


		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("q").instance("1").instance("2")
			.predicate("p").instance("1").instance("2")
			.predicate("_R_").instance("0", "_X:1").instance("0", "_X:2")
			.build();

		assertEquals(expected, answerSet);

		assertNull("Test program must yield one answer set (second answer-set reported).", noAnswerSet);
	}

	@Test
	public void testProgramZeroArityPredicates() throws Exception {
		String testProgram = "a. p(X) :- b, r(X).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		NaiveSolver solver = new NaiveSolver(grounder);
		AnswerSet answerSet = solver.computeNextAnswerSet();
		AnswerSet noAnswerSet = solver.computeNextAnswerSet();

		assertNotNull("Test program must yield one answer set (no answer-set reported)", answerSet);

		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("a")
			.build();

		assertEquals(expected, answerSet);

		assertNull("Test program must yield one answer set (second answer-set reported).", noAnswerSet);
	}

	@Test
	public void testGuessingGroundProgram() throws Exception {
		String testProgram = "a :- not b. b :- not a.";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		NaiveSolver solver = new NaiveSolver(grounder);

		// NOTE(flowlo): Empty string?!

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			// { ChoiceOn(0), ChoiceOn(1), ChoiceOff(1), _R_(0, ), a }
			new BasicAnswerSet.Builder()
			.predicate("ChoiceOn").instance("0").instance("1")
			.predicate("ChoiceOff").instance("1")
			.predicate("_R_").instance("0", "")
			.predicate("a")
			.build(),
			// { ChoiceOn(0), ChoiceOn(1), ChoiceOff(0), _R_(1, ), b }
			new BasicAnswerSet.Builder()
			.predicate("ChoiceOn").instance("0").instance("1")
			.predicate("ChoiceOff").instance("0")
			.predicate("_R_").instance("1", "")
			.predicate("b")
			.build()
		));

		Set<AnswerSet> recorder = StreamSupport.stream(solver.spliterator(), false).collect(Collectors.toSet());

		assertEquals(expected, recorder);
	}

	private static BasicAnswerSet.Builder base() {
		return new BasicAnswerSet.Builder()
			.predicate("dom").instance("1").instance("2").instance("3")
			.predicate("ChoiceOn").instance("0").instance("1").instance("2").instance("3").instance("4").instance("5");
	}

	@Test
	public void testGuessingProgramNonGround() throws Exception {
		String testProgram = "dom(1). dom(2). dom(3)." +
			"p(X) :- dom(X), not q(X)." +
			"q(X) :- dom(X), not p(X).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram);
		NaiveSolver solver = new NaiveSolver(grounder);

		List<AnswerSet> expected = Arrays.asList(
			base()
				.predicate("q").instance("1").instance("2")
				.predicate("p").instance("3")
				.predicate("ChoiceOff").instance("0").instance("1").instance("5")
				.predicate("_R_").instance("0", "_X:3").instance("1", "_X:1").instance("1", "_X:2")
				.build(),
			base()
				.predicate("q").instance("1")
				.predicate("p").instance("2").instance("3")
				.predicate("ChoiceOff").instance("0").instance("4").instance("5")
				.predicate("_R_").instance("0", "_X:2").instance("0", "_X:3").instance("1", "_X:1")
				.build(),
			base()
				.predicate("q").instance("2")
				.predicate("p").instance("1").instance("3")
				.predicate("ChoiceOff").instance("1").instance("3").instance("5")
				.predicate("_R_").instance("0", "_X:1").instance("0", "_X:3").instance("1", "_X:2")
				.build(),
			base()
				.predicate("p").instance("1").instance("2").instance("3")
				.predicate("ChoiceOff").instance("3").instance("4").instance("5")
				.predicate("_R_").instance("0", "_X:1").instance("0", "_X:3").instance("0", "_X:2")
				.build(),
			base()
				.predicate("q").instance("1").instance("2").instance("3")
				.predicate("ChoiceOff").instance("0").instance("1").instance("2")
				.predicate("_R_").instance("1", "_X:1").instance("1", "_X:2").instance("1", "_X:3")
				.build(),
			base()
				.predicate("q").instance("1").instance("3")
				.predicate("p").instance("2")
				.predicate("ChoiceOff").instance("0").instance("2").instance("4")
				.predicate("_R_").instance("0", "_X:2").instance("1", "_X:1").instance("1", "_X:3")
				.build(),
			base()
				.predicate("q").instance("2").instance("3")
				.predicate("p").instance("1")
				.predicate("ChoiceOff").instance("1").instance("2").instance("3")
				.predicate("_R_").instance("0", "_X:1").instance("1", "_X:2").instance("1", "_X:3")
				.build(),
			base()
				.predicate("q").instance("3")
				.predicate("p").instance("1").instance("2")
				.predicate("ChoiceOff").instance("2").instance("3").instance("4")
				.predicate("_R_").instance("0", "_X:1").instance("0", "_X:2").instance("1", "_X:3")
				.build()
		);

		Stream<AnswerSet> stream = StreamSupport.stream(solver.spliterator(), false);
		List<AnswerSet> recorder = stream.collect(Collectors.toList());

		assertTrue("Solver generated exactly " + expected.size() + " Anser-Sets.", recorder.size() == expected.size());

		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), recorder.get(i));
		}
	}

	/**
	 * Constructs an answer set from a list of atoms.
	 *
	 * @param atoms the atoms contained in the answer set.
	 * @return the constructed answer set.
	 */
	private static AnswerSet constructAnswerSet(List<PredicateInstance> atoms) {
		HashSet<Predicate> predicates = new HashSet<>();
		Map<Predicate, Set<PredicateInstance>> instances = new HashMap<>();
		for (PredicateInstance atom : atoms) {
			predicates.add(atom.predicate);
			instances.putIfAbsent(atom.predicate, new HashSet<>());
			instances.get(atom.predicate).add(atom);
		}
		return new BasicAnswerSet(predicates, instances);
	}
}