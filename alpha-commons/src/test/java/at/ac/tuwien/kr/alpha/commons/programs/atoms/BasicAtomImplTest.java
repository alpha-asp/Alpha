package at.ac.tuwien.kr.alpha.commons.programs.atoms;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for basic functionality of various implementations of {@link Atom}.
 * 
 * Copyright (c) 2019-2021, the Alpha Team.
 */
public class BasicAtomImplTest {

	@Test
	public void testIsBasicAtomGround() {
		BasicAtomImpl a = new BasicAtomImpl(Predicates.getPredicate("bla", 2), Terms.newSymbolicConstant("blubb"),
				Terms.newFunctionTerm("foo", Terms.newSymbolicConstant("bar")));
		assertTrue(a.isGround());
		BasicAtomImpl a1 = new BasicAtomImpl(Predicates.getPredicate("foo", 4), Terms.newConstant(1), Terms.newConstant(2), Terms.newConstant(3),
				Terms.newConstant("bar"));
		assertTrue(a1.isGround());
		BasicAtomImpl a2 = new BasicAtomImpl(Predicates.getPredicate("foo", 1), Terms.newVariable("BAR"));
		assertFalse(a2.isGround());
		BasicAtomImpl a3 = new BasicAtomImpl(Predicates.getPredicate("foo", 3), Terms.newSymbolicConstant("b"), Terms.newSymbolicConstant("a"),
				Terms.newFunctionTerm("r", Terms.newConstant("bla"), Terms.newVariable("BLUBB")));
		assertFalse(a3.isGround());
	}

	@Test
	public void testAreBasicAtomsEqual() {
		BasicAtomImpl a1 = new BasicAtomImpl(Predicates.getPredicate("bla", 2), Terms.newSymbolicConstant("blubb"),
				Terms.newFunctionTerm("foo", Terms.newSymbolicConstant("bar")));
		BasicAtomImpl a2 = new BasicAtomImpl(Predicates.getPredicate("bla", 2), Terms.newSymbolicConstant("blubb"),
				Terms.newFunctionTerm("foo", Terms.newSymbolicConstant("bar")));
		assertEquals(a1, a2);
		
		BasicAtomImpl a3 = new BasicAtomImpl(Predicates.getPredicate("foo", 4), Terms.newConstant(1), Terms.newConstant(2), Terms.newConstant(3),
				Terms.newConstant("bar"));
		BasicAtomImpl a4 = new BasicAtomImpl(Predicates.getPredicate("foo", 4), Terms.newConstant(1), Terms.newConstant(2), Terms.newConstant(3),
				Terms.newConstant("bar"));
		assertEquals(a3, a4);
		
		BasicAtomImpl a5 = new BasicAtomImpl(Predicates.getPredicate("foo", 1), Terms.newVariable("BAR"));
		BasicAtomImpl a6 = new BasicAtomImpl(Predicates.getPredicate("foo", 1), Terms.newVariable("BAR"));
		assertEquals(a5, a6);
		
		BasicAtomImpl a7 = new BasicAtomImpl(Predicates.getPredicate("foo", 3), Terms.newSymbolicConstant("b"), Terms.newSymbolicConstant("a"),
				Terms.newFunctionTerm("r", Terms.newConstant("bla"), Terms.newVariable("BLUBB")));
		BasicAtomImpl a8 = new BasicAtomImpl(Predicates.getPredicate("foo", 3), Terms.newSymbolicConstant("b"), Terms.newSymbolicConstant("a"),
				Terms.newFunctionTerm("r", Terms.newConstant("bla"), Terms.newVariable("BLUBB")));
		assertEquals(a7, a8);

		assertNotEquals(a1, a3);
		assertNotEquals(a3, a1);
		assertNotEquals(a1, a5);
		assertNotEquals(a5, a1);
		assertNotEquals(a1, a7);
		assertNotEquals(a7, a1);
	}

}
