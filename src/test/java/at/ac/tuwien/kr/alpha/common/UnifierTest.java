/*
 * Copyright (c) 2018, 2020, the Alpha Team.
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

package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramPartParser;
import org.junit.Test;

import java.util.Arrays;

import static at.ac.tuwien.kr.alpha.common.NoGoodInterface.Type.STATIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnifierTest extends SubstitutionTest {

	private static final ProgramPartParser PROGRAM_PART_PARSER = new ProgramPartParser();

	@Test
	public void extendUnifier() {
		VariableTerm varX = VariableTerm.getInstance("X");
		VariableTerm varY = VariableTerm.getInstance("Y");
		Unifier sub1 = new Unifier();
		sub1.put(varX, varY);
		Unifier sub2 = new Unifier();
		sub2.put(varY, ConstantTerm.getInstance("a"));

		sub1.extendWith(sub2);
		BasicAtom atom1 = parseAtom("p(X)");

		Atom atomSubstituted = atom1.substitute(sub1);
		assertEquals(ConstantTerm.getInstance("a"), atomSubstituted.getTerms().get(0));
	}

	@Test
	public void mergeUnifierIntoLeft() {
		VariableTerm varX = VariableTerm.getInstance("X");
		VariableTerm varY = VariableTerm.getInstance("Y");
		VariableTerm varZ = VariableTerm.getInstance("Z");
		Term constA = ConstantTerm.getInstance("a");
		Unifier left = new Unifier();
		left.put(varX, varY);
		left.put(varZ, varY);
		Unifier right = new Unifier();
		right.put(varX, constA);
		Unifier merged = Unifier.mergeIntoLeft(left, right);
		assertEquals(constA, merged.eval(varY));
		assertEquals(constA, merged.eval(varZ));
	}

	private BasicAtom parseAtom(String atom) {
		ProgramParser programParser = new ProgramParser();
		Program program = programParser.parse(atom + ".");
		return (BasicAtom) program.getFacts().get(0);
	}

	@Test
	public void unifyTermsSimpleBinding() throws Exception {
		Substitution substitution = new Unifier();
		substitution.unifyTerms(Y, A);
		assertEquals(A, substitution.eval(Y));
	}

	@Test
	public void unifyTermsFunctionTermBinding() throws Exception {
		Substitution substitution = new Unifier();
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
		Rule rule = PARSER.parse("x :- p(X,Y), not q(X,Y).").getRules().get(0);
		NonGroundRule nonGroundRule = NonGroundRule.constructNonGroundRule(rule);
		Substitution substitution = new Unifier();
		substitution.unifyTerms(X, A);
		substitution.unifyTerms(Y, B);
		String printedString = SubstitutionTestUtil.groundAndPrintRule(nonGroundRule, substitution);
		assertEquals("x :- p(a, b), not q(a, b).", printedString);
	}

	private void substituteBasicAtomLiteral(boolean negated) {
		Predicate p = Predicate.getInstance("p", 2);
		BasicAtom atom = new BasicAtom(p, Arrays.asList(X, Y));
		Literal literal = new BasicLiteral(atom, !negated);
		Substitution substitution = new Unifier();
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
		Substitution substitution = new Unifier();
		substitution.unifyTerms(X, A);
		substitution.unifyTerms(Y, B);
		String printedString = SubstitutionTestUtil.groundLiteralToString(atom.toLiteral(!negated), substitution, true);
		assertEquals((negated ? "not " : "") + "p(a, b)", printedString);
	}

	@Test
	public void unsuccessfullyUnifyNonGroundNoGoodsMismatchingHead() {
		final Literal lit1 = PROGRAM_PART_PARSER.parseLiteral("not a(X)");
		final Literal lit2 = PROGRAM_PART_PARSER.parseLiteral("b(X)");
		final Literal[] literals = new Literal[]{lit1, lit2};
		final NonGroundNoGood nonGroundNoGood1 = new NonGroundNoGood(STATIC, literals, true);
		final NonGroundNoGood nonGroundNoGood2 = new NonGroundNoGood(STATIC, literals, false);
		assertFalse(new Unifier().unify(nonGroundNoGood1, nonGroundNoGood2));
		assertFalse(new Unifier().unify(nonGroundNoGood2, nonGroundNoGood1));
	}

	@Test
	public void unsuccessfullyUnifyNonGroundNoGoodsMismatchingPredicate() {
		final Literal lit1a = PROGRAM_PART_PARSER.parseLiteral("a(X)");
		final Literal lit1b = PROGRAM_PART_PARSER.parseLiteral("b(X)");
		final Literal lit2 = PROGRAM_PART_PARSER.parseLiteral("not c(X)");
		final NonGroundNoGood nonGroundNoGood1 = new NonGroundNoGood(lit1a, lit2);
		final NonGroundNoGood nonGroundNoGood2 = new NonGroundNoGood(lit1b, lit2);
		assertFalse(new Unifier().unify(nonGroundNoGood1, nonGroundNoGood2));
		assertFalse(new Unifier().unify(nonGroundNoGood2, nonGroundNoGood1));
	}

	@Test
	public void unsuccessfullyUnifyNonGroundNoGoodsAdditionalLiteral() {
		final Literal lit1 = PROGRAM_PART_PARSER.parseLiteral("a(X,Y)");
		final Literal lit2 = PROGRAM_PART_PARSER.parseLiteral("b(X)");
		final Literal lit3 = PROGRAM_PART_PARSER.parseLiteral("X < Y");
		final NonGroundNoGood nonGroundNoGood1 = new NonGroundNoGood(lit1, lit2);
		final NonGroundNoGood nonGroundNoGood2 = new NonGroundNoGood(lit1, lit2, lit3);
		assertFalse(new Unifier().unify(nonGroundNoGood1, nonGroundNoGood2));
		assertFalse(new Unifier().unify(nonGroundNoGood2, nonGroundNoGood1));
	}

	@Test
	public void successfullyUnifyNonGroundNoGoods() {
		final Literal lit1 = PROGRAM_PART_PARSER.parseLiteral("a(X,Y)");
		final Literal lit2 = PROGRAM_PART_PARSER.parseLiteral("b(X)");
		final Literal lit3 = PROGRAM_PART_PARSER.parseLiteral("X < Y");
		final Unifier substitution = new Unifier();
		substitution.put(VariableTerm.getInstance("X"), VariableTerm.getInstance("X1"));
		substitution.put(VariableTerm.getInstance("Y"), VariableTerm.getInstance("Z"));
		final NonGroundNoGood nonGroundNoGood1 = new NonGroundNoGood(lit1, lit2, lit3);
		final NonGroundNoGood nonGroundNoGood2 = new NonGroundNoGood(
				lit1.substitute(substitution), lit2.substitute(substitution), lit3.substitute(substitution));
		assertTrue(new Unifier().unify(nonGroundNoGood1, nonGroundNoGood2));
		assertTrue(new Unifier().unify(nonGroundNoGood2, nonGroundNoGood1));
	}

	@Test
	public void successfullyUnifyNonGroundNoGoodsOneIsPartiallyGround() {
		final Literal lit1 = PROGRAM_PART_PARSER.parseLiteral("a(X,Y)");
		final Literal lit2 = PROGRAM_PART_PARSER.parseLiteral("b(X)");
		final Literal lit3 = PROGRAM_PART_PARSER.parseLiteral("X < Y");
        final Unifier substitution = new Unifier();
		substitution.put(VariableTerm.getInstance("X"), VariableTerm.getInstance("X1"));
		substitution.put(VariableTerm.getInstance("Y"), ConstantTerm.getInstance("y"));
		final NonGroundNoGood nonGroundNoGood1 = new NonGroundNoGood(lit1, lit2, lit3);
		final NonGroundNoGood nonGroundNoGood2 = new NonGroundNoGood(
				lit1.substitute(substitution), lit2.substitute(substitution), lit3.substitute(substitution));
		assertTrue(new Unifier().unify(nonGroundNoGood1, nonGroundNoGood2));
		assertFalse(new Unifier().unify(nonGroundNoGood2, nonGroundNoGood1));
	}

	@Test
	public void unsuccessfullyUnifyPartiallyGroundNonGroundNoGoods() {
		final Literal lit1a = PROGRAM_PART_PARSER.parseLiteral("a(1,Y)");
		final Literal lit2a = PROGRAM_PART_PARSER.parseLiteral("b(1)");
		final Literal lit3a = PROGRAM_PART_PARSER.parseLiteral("1 < Y");
		final Literal lit1b = PROGRAM_PART_PARSER.parseLiteral("a(X,2)");
		final Literal lit2b = PROGRAM_PART_PARSER.parseLiteral("b(X)");
		final Literal lit3b = PROGRAM_PART_PARSER.parseLiteral("X < 2");
		final NonGroundNoGood nonGroundNoGood1 = new NonGroundNoGood(lit1a, lit2a, lit3a);
		final NonGroundNoGood nonGroundNoGood2 = new NonGroundNoGood(lit1b, lit2b, lit3b);
		assertFalse(new Unifier().unify(nonGroundNoGood1, nonGroundNoGood2));
		assertFalse(new Unifier().unify(nonGroundNoGood2, nonGroundNoGood1));
	}
}