package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.Substitution;
import at.ac.tuwien.kr.alpha.common.Unifier;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class UnifierTest extends SubstitutionTest {

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
		String printedString = NaiveGrounder.groundAndPrintRule(nonGroundRule, substitution);
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
		String printedString = NaiveGrounder.groundLiteralToString(atom.toLiteral(!negated), substitution, true);
		assertEquals((negated ? "not " : "") + "p(a, b)", printedString);
	}
}