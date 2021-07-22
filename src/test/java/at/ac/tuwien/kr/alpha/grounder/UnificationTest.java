package at.ac.tuwien.kr.alpha.grounder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramPartParser;


/**
 * Copyright (c) 2021, the Alpha Team.
 */
public class UnificationTest {

	private ProgramPartParser partsParser = new ProgramPartParser();

	@Test
	public void simpleGroundUnification() {
		Atom pX = partsParser.parseLiteral("p(X)").getAtom();
		Atom pa = partsParser.parseLiteral("p(abc)").getAtom();
		Unifier unifier = Unification.unifyAtoms(pa, pX);
		assertNotNull(unifier);
		assertEquals(1, unifier.getMappedVariables().size());
		assertEquals("abc", unifier.eval(VariableTerm.getInstance("X")).toString());
	}

	@Test
	public void unificationBothSides() {
		Atom left = partsParser.parseLiteral("p(X, 1)").getAtom();
		Atom right = partsParser.parseLiteral("p(d, Y)").getAtom();
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNotNull(unifier);
		assertEquals(2, unifier.getMappedVariables().size());
		assertEquals("d", unifier.eval(VariableTerm.getInstance("X")).toString());
		assertEquals("1", unifier.eval(VariableTerm.getInstance("Y")).toString());
	}

	@Test
	public void unificationNonGround() {
		Atom left = partsParser.parseLiteral("p(X, 13)").getAtom();
		Atom right = partsParser.parseLiteral("p(Z, Y)").getAtom();
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNotNull(unifier);
		assertEquals(3, unifier.getMappedVariables().size());
		assertEquals("13", unifier.eval(VariableTerm.getInstance("Y")).toString());
		// Check that the unifier sets X=Z by either mapping X -> Z or Z -> X.
		if (unifier.eval(VariableTerm.getInstance("X")) != null) {
			// X is mapped, it must map to Z now.
			assertEquals("Z", unifier.eval(VariableTerm.getInstance("X")).toString());
		} else {
			// X is not mapped, so Z must map to X.
			assertEquals("X", unifier.eval(VariableTerm.getInstance("Z")).toString());
		}
	}

	@Test
	public void unificationWithFunctionTerms() {
		Atom left = partsParser.parseLiteral("a(b, f(X, 13), g(Z), d)").getAtom();
		Atom right = partsParser.parseLiteral("a(b, A, g(e), d)").getAtom();
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNotNull(unifier);
		assertEquals(3, unifier.getMappedVariables().size());
		assertEquals(left.substitute(unifier).toString(), right.substitute(unifier).toString());
	}

	@Test
	public void unificationWithArithmeticTerms() {
		Atom left = partsParser.parseLiteral("a(X - (3 * Y))").getAtom();
		Atom right = partsParser.parseLiteral("a(15 - Z)").getAtom();
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNotNull(unifier);
		assertEquals(3, unifier.getMappedVariables().size());
		assertEquals(left.substitute(unifier).toString(), right.substitute(unifier).toString());
	}

	@Test
	public void nonunificationWithArithmeticTerms() {
		Atom left = partsParser.parseLiteral("a(X + 4)").getAtom();
		Atom right = partsParser.parseLiteral("a(Y - 4)").getAtom();
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNull(unifier);
	}

	@Test
	public void nonunificationWithArithmeticTermsNested() {
		Atom left = partsParser.parseLiteral("a(X + 7)").getAtom();
		Atom right = partsParser.parseLiteral("a(Y + (Z - 2))").getAtom();
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNull(unifier);
	}

	@Test
	public void nonunificationSimple() {
		Atom left = partsParser.parseLiteral("a(b,X)").getAtom();
		Atom right = partsParser.parseLiteral("a(c,Y)").getAtom();
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNull(unifier);
	}

	@Test
	public void nonunificationNested() {
		Atom left = partsParser.parseLiteral("a(f(X,a))").getAtom();
		Atom right = partsParser.parseLiteral("a(f(a,b))").getAtom();
		Unifier unifier = Unification.unifyAtoms(left, right);
		assertNull(unifier);
	}
}