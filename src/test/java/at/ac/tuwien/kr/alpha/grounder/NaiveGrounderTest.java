package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.Term;
import at.ac.tuwien.kr.alpha.common.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class NaiveGrounderTest {
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
		FunctionTerm groundFunctionTerm = FunctionTerm.getInstance("f", asList(new Term[]{ConstantTerm.getInstance("bb"), ConstantTerm.getInstance("cc")}));

		Term nongroundFunctionTerm = FunctionTerm.getInstance("f", asList(ConstantTerm.getInstance("bb"), VariableTerm.getInstance("X")));
		grounder.unifyTerms(nongroundFunctionTerm, groundFunctionTerm, variableSubstitution);
		assertEquals("Variable X must bind to constant term cc", variableSubstitution.substitution.get(VariableTerm.getInstance("X")), ConstantTerm.getInstance("cc"));

		assertEquals("Variable Z must bind to constant term aa", variableSubstitution.substitution.get(VariableTerm.getInstance("Z")), ConstantTerm.getInstance("aa"));
	}
}