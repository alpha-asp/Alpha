/**
 * Copyright (c) 2016-2018, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.grounder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.InternalRule;
import at.ac.tuwien.kr.alpha.common.rule.NormalRule;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.test.util.SubstitutionTestUtil;

public class SubstitutionTest {
	static final ProgramParser PARSER = new ProgramParser();

	static final ConstantTerm<?> A = ConstantTerm.getSymbolicInstance("a");
	static final ConstantTerm<?> B = ConstantTerm.getSymbolicInstance("b");
	static final ConstantTerm<?> C = ConstantTerm.getSymbolicInstance("c");

	static final VariableTerm X = VariableTerm.getInstance("X");
	static final VariableTerm Y = VariableTerm.getInstance("Y");

	@Test
	public void unifyTermsSimpleBinding() throws Exception {
		Substitution substitution = new Substitution();
		substitution.unifyTerms(Y, A);
		assertEquals(A, substitution.eval(Y));
	}

	@Test
	public void unifyTermsFunctionTermBinding() throws Exception {
		Substitution substitution = new Substitution();
		substitution.put(Y, A);

		FunctionTerm groundFunctionTerm = FunctionTerm.getInstance("f", B, C);
		Term nongroundFunctionTerm = FunctionTerm.getInstance("f", B, X);

		substitution.unifyTerms(nongroundFunctionTerm, groundFunctionTerm);

		assertEquals(C, substitution.eval(X));
		assertEquals(A, substitution.eval(Y));
	}

	@Test
	public void substitutePositiveBasicAtom() {
		substituteBasicAtomLiteral(false);
	}

	@Test
	public void substituteNegativeBasicAtom() {
		substituteBasicAtomLiteral(true);
	}

	@Test
	public void groundAndPrintRule() {
		BasicRule rule = PARSER.parse("x :- p(X,Y), not q(X,Y).").getRules().get(0);
		InternalRule nonGroundRule = InternalRule.fromNormalRule(NormalRule.fromBasicRule(rule));
		Substitution substitution = new Substitution();
		substitution.unifyTerms(X, A);
		substitution.unifyTerms(Y, B);
		String printedString = SubstitutionTestUtil.groundAndPrintRule(nonGroundRule, substitution);
		assertEquals("x :- p(a, b), not q(a, b).", printedString);
	}

	private void substituteBasicAtomLiteral(boolean negated) {
		Predicate p = Predicate.getInstance("p", 2);
		BasicAtom atom = new BasicAtom(p, Arrays.asList(X, Y));
		Literal literal = new BasicLiteral(atom, !negated);
		Substitution substitution = new Substitution();
		substitution.unifyTerms(X, A);
		substitution.unifyTerms(Y, B);
		literal = literal.substitute(substitution);
		assertEquals(p, literal.getPredicate());
		assertEquals(A, literal.getTerms().get(0));
		assertEquals(B, literal.getTerms().get(1));
		assertEquals(negated, literal.isNegated());
	}

	@Test
	public void groundLiteralToString_PositiveBasicAtom() {
		groundLiteralToString(false);
	}

	@Test
	public void groundLiteralToString_NegativeBasicAtom() {
		groundLiteralToString(true);
	}

	private void groundLiteralToString(boolean negated) {
		Predicate p = Predicate.getInstance("p", 2);
		BasicAtom atom = new BasicAtom(p, Arrays.asList(X, Y));
		Substitution substitution = new Substitution();
		substitution.unifyTerms(X, A);
		substitution.unifyTerms(Y, B);
		String printedString = SubstitutionTestUtil.groundLiteralToString(atom.toLiteral(!negated), substitution, true);
		assertEquals((negated ? "not " : "") + "p(a, b)", printedString);
	}

	@Test
	public void substitutionFromString() {
		BasicRule rule = PARSER.parse("x :- p(X,Y), not q(X,Y).").getRules().get(0);
		InternalRule nonGroundRule = InternalRule.fromNormalRule(NormalRule.fromBasicRule(rule));
		Substitution substitution = new Substitution();
		substitution.unifyTerms(X, A);
		substitution.unifyTerms(Y, B);
		RuleAtom ruleAtom = new RuleAtom(nonGroundRule, substitution);
		String substitutionString = (String) ((ConstantTerm<?>) ruleAtom.getTerms().get(1)).getObject();
		Substitution fromString = Substitution.fromString(substitutionString);
		assertTrue(substitution.equals(fromString));
	}
}
