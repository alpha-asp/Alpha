package at.ac.tuwien.kr.alpha.common.atoms;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.common.program.impl.InputProgram;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

/**
 * Test for basic functionality of various implementations of {@link Atom}.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class AtomsTest {

	private final ProgramParser parser = new ProgramParser(new HashMap<>());

	@Test
	public void testIsBasicAtomGround() {
		InputProgram p = parser.parse("bla(blubb, foo(bar)).");
		Atom a = p.getFacts().get(0);
		this.assertBasicAtomGround(a, true);
		InputProgram p1 = parser.parse("foo(1, 2, 3, \"bar\").");
		Atom a1 = p1.getFacts().get(0);
		this.assertBasicAtomGround(a1, true);
		InputProgram p2 = parser.parse("foo(BAR).");
		Atom a2 = p2.getFacts().get(0);
		this.assertBasicAtomGround(a2, false);
		InputProgram p3 = parser.parse("foo(b, a, r(\"bla\", BLUBB)).");
		Atom a3 = p3.getFacts().get(0);
		this.assertBasicAtomGround(a3, false);
	}

	private void assertBasicAtomGround(Atom a, boolean expectedGround) {
		Assert.assertTrue(a instanceof BasicAtom);
		Assert.assertEquals(expectedGround, a.isGround());
	}

}
