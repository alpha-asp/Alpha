package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.solver.NaiveSolver;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static at.ac.tuwien.kr.alpha.MainTest.stream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
		Term groundTerm = ConstantTerm.getConstantTerm("abc");
		Term nongroundTerm = VariableTerm.getVariableTerm("Y");
		grounder.unifyTerms(nongroundTerm, groundTerm, variableSubstitution);
		assertEquals("Variable Y must bind to constant term abc", variableSubstitution.substitution.get(VariableTerm.getVariableTerm("Y")), ConstantTerm.getConstantTerm("abc"));
	}


	@Test
	public void unifyTermsFunctionTermBinding() throws Exception {
		NaiveGrounder grounder = new NaiveGrounder(new ParsedProgram());
		NaiveGrounder.VariableSubstitution variableSubstitution = grounder.new VariableSubstitution();
		variableSubstitution.substitution.put(VariableTerm.getVariableTerm("Z"), ConstantTerm.getConstantTerm("aa"));
		FunctionTerm groundFunctionTerm = FunctionTerm.getFunctionTerm("f", Arrays.asList(new Term[]{ConstantTerm.getConstantTerm("bb"), ConstantTerm.getConstantTerm("cc")}));

		Term nongroundFunctionTerm = FunctionTerm.getFunctionTerm("f", Arrays.asList(new Term[]{ConstantTerm.getConstantTerm("bb"), VariableTerm.getVariableTerm("X")}));
		grounder.unifyTerms(nongroundFunctionTerm, groundFunctionTerm, variableSubstitution);
		assertEquals("Variable X must bind to constant term cc", variableSubstitution.substitution.get(VariableTerm.getVariableTerm("X")), ConstantTerm.getConstantTerm("cc"));

		assertEquals("Variable Z must bind to constant term aa", variableSubstitution.substitution.get(VariableTerm.getVariableTerm("Z")), ConstantTerm.getConstantTerm("aa"));
	}

	@Test
	public void testFactsOnlyProgram() throws IOException {
		String testProgram = "p(a). p(b). foo(13). foo(16). q(a). q(c).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		NaiveSolver solver = new NaiveSolver(grounder);
		AnswerSet answerSet = solver.computeNextAnswerSet();
		AnswerSet noAnswerSet = solver.computeNextAnswerSet();

		assertTrue("Test program must yield one answer set (no answer-set reported)", answerSet != null);
		assertEquals("Program must yield answer-set: { q(a), q(c), p(a), p(b), foo(13), foo(16) }", "{ q(a), q(c), p(a), p(b), foo(13), foo(16) }", answerSet.toString());

		assertTrue("Test program must yield one answer set (second answer-set reported).", noAnswerSet == null);
	}

	@Test
	public void testSimpleRule() throws Exception {
		String testProgram = "p(a). p(b). r(X) :- p(X).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		NaiveSolver solver = new NaiveSolver(grounder);
		AnswerSet answerSet = solver.computeNextAnswerSet();
		AnswerSet noAnswerSet = solver.computeNextAnswerSet();

		assertTrue("Test program must yield one answer set (no answer-set reported)", answerSet != null);
		assertEquals("Program must yield answer-set: { p(a), p(b), _R_(0, _X:a), _R_(0, _X:b), r(a), r(b) }", "{ p(a), p(b), _R_(0, _X:a), _R_(0, _X:b), r(a), r(b) }", answerSet.toString());

		assertTrue("Test program must yield one answer set (second answer-set reported).", noAnswerSet == null);
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

		assertTrue("Test program must yield one answer set (no answer-set reported)", answerSet != null);
		assertEquals("Program must yield answer-set: { q(1), q(2), p(1), p(2), _R_(0, _X:1), _R_(0, _X:2) }", "{ q(1), q(2), p(1), p(2), _R_(0, _X:1), _R_(0, _X:2) }", answerSet.toString());

		assertTrue("Test program must yield one answer set (second answer-set reported).", noAnswerSet == null);
	}

	@Test
	public void testProgramZeroArityPredicates() throws Exception {
		String testProgram = "a. p(X) :- b, r(X).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		NaiveSolver solver = new NaiveSolver(grounder);
		AnswerSet answerSet = solver.computeNextAnswerSet();
		AnswerSet noAnswerSet = solver.computeNextAnswerSet();

		assertTrue("Test program must yield one answer set (no answer-set reported)", answerSet != null);
		assertEquals("Program must yield answer-set: { a }", "{ a }", answerSet.toString());

		assertTrue("Test program must yield one answer set (second answer-set reported).", noAnswerSet == null);
	}

	@Test
	public void testGuessingGroundProgram() throws Exception {

		String testProgram = "a :- not b. b :- not a.";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		NaiveSolver solver = new NaiveSolver(grounder);
		AnswerSet answerSet1 = solver.computeNextAnswerSet();
		AnswerSet answerSet2 = solver.computeNextAnswerSet();
		AnswerSet answerSet3 = solver.computeNextAnswerSet();

		HashSet<AnswerSet> obtainedAnswerSets = new HashSet<>();
		obtainedAnswerSets.add(answerSet1);
		obtainedAnswerSets.add(answerSet2);

		// Construct first AnswerSet:
		// { ChoiceOn(0), ChoiceOn(1), ChoiceOff(1), _R_(0, ), a }
		AnswerSet as1 = AnswerSetUtil.constructAnswerSet(Arrays.asList(
			new PredicateInstance(new BasicPredicate("ChoiceOn", 1), new Term[]{ConstantTerm.getConstantTerm("0")}),
			new PredicateInstance(new BasicPredicate("ChoiceOn", 1), new Term[]{ConstantTerm.getConstantTerm("1")}),
			new PredicateInstance(new BasicPredicate("ChoiceOff", 1), new Term[]{ConstantTerm.getConstantTerm("1")}),
			new PredicateInstance(new BasicPredicate("_R_", 2), new Term[]{ConstantTerm.getConstantTerm("0"), ConstantTerm.getConstantTerm("")}),
			new PredicateInstance(new BasicPredicate("a", 0), new Term[]{})
		));
		// { ChoiceOn(0), ChoiceOn(1), ChoiceOff(0), _R_(1, ), b }
		AnswerSet as2 = AnswerSetUtil.constructAnswerSet(Arrays.asList(
			new PredicateInstance(new BasicPredicate("ChoiceOn", 1), new Term[]{ConstantTerm.getConstantTerm("0")}),
			new PredicateInstance(new BasicPredicate("ChoiceOn", 1), new Term[]{ConstantTerm.getConstantTerm("1")}),
			new PredicateInstance(new BasicPredicate("ChoiceOff", 1), new Term[]{ConstantTerm.getConstantTerm("0")}),
			new PredicateInstance(new BasicPredicate("_R_", 2), new Term[]{ConstantTerm.getConstantTerm("1"), ConstantTerm.getConstantTerm("")}),
			new PredicateInstance(new BasicPredicate("b", 0), new Term[]{})
		));
		HashSet<AnswerSet> expectedAnswerSets = new HashSet<>();
		expectedAnswerSets.add(as1);
		expectedAnswerSets.add(as2);
		assertTrue("First answer-set is not the expected.", AnswerSetUtil.areAnswerSetsEqual(answerSet1, as1));
		assertTrue("Second answer-set is not the expected.", AnswerSetUtil.areAnswerSetsEqual(answerSet2, as2));
		assertTrue("There must be two answer sets: { ChoiceOn(0), ChoiceOn(1), ChoiceOff(1), _R_(0, ), a } and { ChoiceOn(0), ChoiceOn(1), ChoiceOff(0), _R_(1, ), b }.", AnswerSetUtil.areSetsOfAnswerSetsEqual(expectedAnswerSets, obtainedAnswerSets));

		// TODO: test depends on choice order of the employed solver.
		// TODO: We need methods to check whether a correct set of answer-sets is returned!

		assertTrue("Test program must yield an answer set (no answer-set reported)", answerSet1 != null);
		assertEquals("Program must yield answer-set: { ChoiceOn(0), ChoiceOn(1), ChoiceOff(1), _R_(0, ), a }", "{ ChoiceOn(0), ChoiceOn(1), ChoiceOff(1), _R_(0, ), a }", answerSet1.toString());

		assertTrue("Test program must yield a second answer set (only one answer-set reported)", answerSet2 != null);
		assertEquals("Program must yield answer-set: { ChoiceOn(0), ChoiceOn(1), ChoiceOff(0), _R_(1, ), b }", "{ ChoiceOn(0), ChoiceOn(1), ChoiceOff(0), _R_(1, ), b }", answerSet2.toString());

		assertTrue("Test program must yield two answer sets (third answer-set reported)", answerSet3 == null);
	}

	@Test
	public void testGuessingProgramNonGround() throws Exception {
		String testProgram = "dom(1). dom(2). dom(3)." +
			"p(X) :- dom(X), not q(X)." +
			"q(X) :- dom(X), not p(X).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram);
		NaiveSolver solver = new NaiveSolver(grounder);
		AnswerSet answerSet1 = solver.computeNextAnswerSet();
		AnswerSet answerSet2 = solver.computeNextAnswerSet();
		AnswerSet answerSet3 = solver.computeNextAnswerSet();
		AnswerSet answerSet4 = solver.computeNextAnswerSet();
		AnswerSet answerSet5 = solver.computeNextAnswerSet();
		AnswerSet answerSet6 = solver.computeNextAnswerSet();
		AnswerSet answerSet7 = solver.computeNextAnswerSet();
		AnswerSet answerSet8 = solver.computeNextAnswerSet();
		AnswerSet answerSet9 = solver.computeNextAnswerSet();


		String textAS1 = "{ dom(1), dom(2), dom(3), q(1), q(2), p(3), ChoiceOn(0), ChoiceOn(1), ChoiceOn(2), ChoiceOn(3), ChoiceOn(4), ChoiceOn(5), ChoiceOff(0), ChoiceOff(1), ChoiceOff(5), _R_(0, _X:3), _R_(1, _X:1), _R_(1, _X:2) }";
		String textAS2 = "{ dom(1), dom(2), dom(3), q(1), p(2), p(3), ChoiceOn(0), ChoiceOn(1), ChoiceOn(2), ChoiceOn(3), ChoiceOn(4), ChoiceOn(5), ChoiceOff(0), ChoiceOff(4), ChoiceOff(5), _R_(0, _X:2), _R_(0, _X:3), _R_(1, _X:1) }";
		String textAS3 = "{ dom(1), dom(2), dom(3), q(2), p(1), p(3), ChoiceOn(0), ChoiceOn(1), ChoiceOn(2), ChoiceOn(3), ChoiceOn(4), ChoiceOn(5), ChoiceOff(1), ChoiceOff(3), ChoiceOff(5), _R_(0, _X:1), _R_(0, _X:3), _R_(1, _X:2) }";
		String textAS4 = "{ dom(1), dom(2), dom(3), p(1), p(2), p(3), ChoiceOn(0), ChoiceOn(1), ChoiceOn(2), ChoiceOn(3), ChoiceOn(4), ChoiceOn(5), ChoiceOff(3), ChoiceOff(4), ChoiceOff(5), _R_(0, _X:1), _R_(0, _X:2), _R_(0, _X:3) }";
		String textAS5 = "{ dom(1), dom(2), dom(3), q(1), q(2), q(3), ChoiceOn(0), ChoiceOn(1), ChoiceOn(2), ChoiceOn(3), ChoiceOn(4), ChoiceOn(5), ChoiceOff(0), ChoiceOff(1), ChoiceOff(2), _R_(1, _X:1), _R_(1, _X:2), _R_(1, _X:3) }";
		String textAS6 = "{ dom(1), dom(2), dom(3), q(1), q(3), p(2), ChoiceOn(0), ChoiceOn(1), ChoiceOn(2), ChoiceOn(3), ChoiceOn(4), ChoiceOn(5), ChoiceOff(0), ChoiceOff(2), ChoiceOff(4), _R_(0, _X:2), _R_(1, _X:1), _R_(1, _X:3) }";
		String textAS7 = "{ dom(1), dom(2), dom(3), q(2), q(3), p(1), ChoiceOn(0), ChoiceOn(1), ChoiceOn(2), ChoiceOn(3), ChoiceOn(4), ChoiceOn(5), ChoiceOff(1), ChoiceOff(2), ChoiceOff(3), _R_(0, _X:1), _R_(1, _X:2), _R_(1, _X:3) }";
		String textAS8 = "{ dom(1), dom(2), dom(3), q(3), p(1), p(2), ChoiceOn(0), ChoiceOn(1), ChoiceOn(2), ChoiceOn(3), ChoiceOn(4), ChoiceOn(5), ChoiceOff(2), ChoiceOff(3), ChoiceOff(4), _R_(0, _X:1), _R_(0, _X:2), _R_(1, _X:3) }";

		assertTrue("Test program must yield 8 answer sets (no answer-set reported)", answerSet1 != null);
		assertEquals("Program must yield answer-set: " + textAS1, textAS1, answerSet1.toString());

		assertTrue("Test program must yield 8 answer sets.", answerSet2 != null);
		assertEquals("Program must yield answer-set: " + textAS2, textAS2, answerSet2.toString());

		assertTrue("Test program must yield 8 answer sets.", answerSet3 != null);
		assertEquals("Program must yield answer-set: " + textAS3, textAS3, answerSet3.toString());

		assertTrue("Test program must yield 8 answer sets.", answerSet4 != null);
		assertEquals("Program must yield answer-set: " + textAS4, textAS4, answerSet4.toString());

		assertTrue("Test program must yield 8 answer sets.", answerSet5 != null);
		assertEquals("Program must yield answer-set: " + textAS5, textAS5, answerSet5.toString());

		assertTrue("Test program must yield 8 answer sets.", answerSet6 != null);
		assertEquals("Program must yield answer-set: " + textAS6, textAS6, answerSet6.toString());

		assertTrue("Test program must yield 8 answer sets.", answerSet7 != null);
		assertEquals("Program must yield answer-set: " + textAS7, textAS7, answerSet7.toString());

		assertTrue("Test program must yield 8 answer sets.", answerSet8 != null);
		assertEquals("Program must yield answer-set: " + textAS8, textAS8, answerSet8.toString());

		assertTrue("Test program must yield no more than 8 answer sets (ninth answer-set reported)", answerSet9 == null);
	}

}