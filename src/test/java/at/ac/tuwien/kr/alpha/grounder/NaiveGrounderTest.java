package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.solver.DummySolver;
import at.ac.tuwien.kr.alpha.solver.Solver;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static at.ac.tuwien.kr.alpha.MainTest.stream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class NaiveGrounderTest {
	@Test
	public void testNaiveGrounderFactsOnlyProgram() throws IOException {
		String testProgram = "p(a). p(b). foo(13). foo(16). q(a). q(c).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = new DummySolver(grounder);
		AnswerSet answerSet = solver.get();
		AnswerSet noAnswerSet = solver.get();

		assertTrue("Test program must yield one answer set (no answer-set reported)", answerSet != null);
		assertEquals("Program must yield answer-set: { q(a), q(c), p(a), p(b), foo(13), foo(16) }", "{ q(a), q(c), p(a), p(b), foo(13), foo(16) }", answerSet.toString());

		assertTrue("Test program must yield one answer set (second answer-set reported).", noAnswerSet == null);
	}

	@Test
	public void testSimpleProgram() throws Exception {
		String testProgram = "p(a). p(b). r(X) :- p(X).";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		Grounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = new DummySolver(grounder);
		AnswerSet answerSet = solver.get();
		AnswerSet noAnswerSet = solver.get();

		assertTrue("Test program must yield one answer set (no answer-set reported)", answerSet != null);
		assertEquals("Program must yield answer-set: { p(a), p(b), _R_(0, _X:a), _R_(0, _X:b), r(a), r(b) }", "{ p(a), p(b), _R_(0, _X:a), _R_(0, _X:b), r(a), r(b) }", answerSet.toString());

		assertTrue("Test program must yield one answer set (second answer-set reported).", noAnswerSet == null);
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
}