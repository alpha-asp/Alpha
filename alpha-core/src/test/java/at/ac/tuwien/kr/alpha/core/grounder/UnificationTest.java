package at.ac.tuwien.kr.alpha.core.grounder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.terms.ArithmeticOperator;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.substitutions.Unifier;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;

/**
 * Copyright (c) 2021, the Alpha Team.
 */
public class UnificationTest {

	@Test
	public void simpleGroundUnification() {
		Atom pX = Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X"));
		Atom pa = Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newSymbolicConstant("abc"));
		Unifier unifier = Unification.unifyAtoms(pa, pX);
		assertNotNull(unifier);
		assertEquals(1, unifier.getMappedVariables().size());
		assertEquals("abc", unifier.eval(Terms.newVariable("X")).toString());
	}

	@Test
	public void unificationBothSides() {
		Atom left = Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newConstant(1));
		Atom right = Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newSymbolicConstant("d"), Terms.newVariable("Y"));
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNotNull(unifier);
		assertEquals(2, unifier.getMappedVariables().size());
		assertEquals("d", unifier.eval(Terms.newVariable("X")).toString());
		assertEquals("1", unifier.eval(Terms.newVariable("Y")).toString());
	}

	@Test
	public void unificationNonGround() {
		Atom left = Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newConstant(13));
		Atom right = Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("Z"), Terms.newVariable("Y"));
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNotNull(unifier);
		assertEquals(3, unifier.getMappedVariables().size());
		assertEquals("13", unifier.eval(Terms.newVariable("Y")).toString());
		// Check that the unifier sets X=Z by either mapping X -> Z or Z -> X.
		if (unifier.eval(Terms.newVariable("X")) != null) {
			// X is mapped, it must map to Z now.
			assertEquals("Z", unifier.eval(Terms.newVariable("X")).toString());
		} else {
			// X is not mapped, so Z must map to X.
			assertEquals("X", unifier.eval(Terms.newVariable("Z")).toString());
		}
	}

	@Test
	public void unificationWithFunctionTerms() {
		// left := a(b, f(X, 13), g(Z), d)
		Atom left = Atoms.newBasicAtom(Predicates.getPredicate("a", 4),
				Terms.newSymbolicConstant("b"),
				Terms.newFunctionTerm("f", Terms.newVariable("X"), Terms.newConstant(13)),
				Terms.newFunctionTerm("g", Terms.newVariable("Z")),
				Terms.newSymbolicConstant("d"));
		// right := a(b, A, g(e), d)
		Atom right = Atoms.newBasicAtom(Predicates.getPredicate("a", 4),
				Terms.newSymbolicConstant("b"),
				Terms.newVariable("A"),
				Terms.newFunctionTerm("g", Terms.newSymbolicConstant("e")),
				Terms.newSymbolicConstant("d"));
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNotNull(unifier);
		assertEquals(3, unifier.getMappedVariables().size());
		assertEquals(left.substitute(unifier).toString(), right.substitute(unifier).toString());
	}

	@Test
	public void unificationWithArithmeticTerms() {
		// left := a(X - (3 * Y))
		Atom left = Atoms.newBasicAtom(Predicates.getPredicate("a", 1),
				Terms.newArithmeticTerm(
						Terms.newVariable("X"),
						ArithmeticOperator.MINUS,
						Terms.newArithmeticTerm(Terms.newConstant(3), ArithmeticOperator.TIMES, Terms.newVariable("Y"))));
		// right := a(15 - Z)
		Atom right = Atoms.newBasicAtom(Predicates.getPredicate("a", 1),
				Terms.newArithmeticTerm(Terms.newConstant(15), ArithmeticOperator.MINUS, Terms.newVariable("Z")));
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNotNull(unifier);
		assertEquals(3, unifier.getMappedVariables().size());
		assertEquals(left.substitute(unifier).toString(), right.substitute(unifier).toString());
	}

	@Test
	public void nonunificationWithArithmeticTerms() {
		// left := a(X + 4)
		Atom left = Atoms.newBasicAtom(Predicates.getPredicate("a", 1),
				Terms.newArithmeticTerm(Terms.newVariable("X"), ArithmeticOperator.PLUS, Terms.newConstant(4)));
		// right := a(Y - 4)
		Atom right = Atoms.newBasicAtom(Predicates.getPredicate("a", 1),
				Terms.newArithmeticTerm(Terms.newVariable("Y"), ArithmeticOperator.MINUS, Terms.newConstant(4)));
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNull(unifier);
	}

	@Test
	public void nonunificationWithArithmeticTermsNested() {
		// left := a(X + 7)
		Atom left = Atoms.newBasicAtom(Predicates.getPredicate("a", 1),
				Terms.newArithmeticTerm(Terms.newVariable("X"), ArithmeticOperator.PLUS, Terms.newConstant(7)));
		// right := a(Y + (Z - 2))
		Atom right = Atoms.newBasicAtom(Predicates.getPredicate("a", 1),
				Terms.newArithmeticTerm(
						Terms.newVariable("Y"),
						ArithmeticOperator.PLUS,
						Terms.newArithmeticTerm(Terms.newVariable("Z"), ArithmeticOperator.MINUS, Terms.newConstant(2))));
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNull(unifier);
	}

	@Test
	public void nonunificationSimple() {
		// left := a(b, X)
		Atom left = Atoms.newBasicAtom(Predicates.getPredicate("a", 2),
				Terms.newSymbolicConstant("b"), Terms.newVariable("X"));
		// right := a(c, Y)
		Atom right = Atoms.newBasicAtom(Predicates.getPredicate("a", 2),
				Terms.newSymbolicConstant("c"), Terms.newVariable("Y"));
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNull(unifier);
	}

	@Test
	public void nonunificationNested() {
		// left := a(f(X,a))
		Atom left = Atoms.newBasicAtom(Predicates.getPredicate("a", 1),
				Terms.newFunctionTerm("f", Terms.newVariable("X"), Terms.newSymbolicConstant("a")));
		// right := a(f(a,b))
		Atom right = Atoms.newBasicAtom(Predicates.getPredicate("a", 1),
				Terms.newFunctionTerm("f", Terms.newSymbolicConstant("a"), Terms.newSymbolicConstant("b")));
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNull(unifier);
	}

	@Test
	public void extendUnifier() {
		VariableTerm varX = Terms.newVariable("X");
		VariableTerm varY = Terms.newVariable("Y");
		Unifier sub1 = new Unifier();
		sub1.put(varX, varY);
		Unifier sub2 = new Unifier();
		sub2.put(varY, Terms.newConstant("a"));

		sub1.extendWith(sub2);
		BasicAtom atom1 = Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X"));

		Atom atomSubstituted = atom1.substitute(sub1);
		assertEquals(Terms.newConstant("a"), atomSubstituted.getTerms().get(0));
	}

	@Test
	public void mergeUnifierIntoLeft() {
		VariableTerm varX = Terms.newVariable("X");
		VariableTerm varY = Terms.newVariable("Y");
		VariableTerm varZ = Terms.newVariable("Z");
		Term constA = Terms.newConstant("a");
		Unifier left = new Unifier();
		left.put(varX, varY);
		left.put(varZ, varY);
		Unifier right = new Unifier();
		right.put(varX, constA);
		Unifier merged = Unifier.mergeIntoLeft(left, right);
		assertEquals(constA, merged.eval(varY));
		assertEquals(constA, merged.eval(varZ));
	}
}
