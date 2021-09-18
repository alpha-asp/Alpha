package at.ac.tuwien.kr.alpha.common.atoms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.externals.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.externals.Externals;
import at.ac.tuwien.kr.alpha.core.parser.aspcore2.ASPCore2ProgramParser;

/**
 * Test for basic functionality of various implementations of {@link Atom}.
 * 
 * Copyright (c) 2019-2020, the Alpha Team.
 */
public class AtomsTest {

	private final ProgramParser parser;
	private Map<String, PredicateInterpretation> externals;

	public AtomsTest() throws NoSuchMethodException, SecurityException {
		externals = new HashMap<>();
		externals.put("isFoo", Externals.processPredicateMethod(AtomsTest.class.getMethod("isFoo", int.class)));
		externals.put("extWithOutput", Externals.processPredicateMethod(AtomsTest.class.getMethod("extWithOutput", int.class)));
		parser = new ASPCore2ProgramParser();
	}

	@Predicate
	public static final boolean isFoo(int bar) {
		return 0xF00 == bar;
	}

	@Predicate
	public static final Set<List<ConstantTerm<Integer>>> extWithOutput(int in) {
		Set<List<ConstantTerm<Integer>>> retVal = new HashSet<>();
		List<ConstantTerm<Integer>> lst = new ArrayList<>();
		lst.add(Terms.newConstant(in));
		retVal.add(lst);
		return retVal;
	}

	@Test
	public void testIsBasicAtomGround() {
		InputProgram p = parser.parse("bla(blubb, foo(bar)).");
		Atom a = p.getFacts().get(0);
		assertBasicAtomGround(a, true);
		InputProgram p1 = parser.parse("foo(1, 2, 3, \"bar\").");
		Atom a1 = p1.getFacts().get(0);
		assertBasicAtomGround(a1, true);
		InputProgram p2 = parser.parse("foo(BAR).");
		Atom a2 = p2.getFacts().get(0);
		assertBasicAtomGround(a2, false);
		InputProgram p3 = parser.parse("foo(b, a, r(\"bla\", BLUBB)).");
		Atom a3 = p3.getFacts().get(0);
		assertBasicAtomGround(a3, false);
	}

	@Test
	public void testAreBasicAtomsEqual() {
		InputProgram p1 = parser.parse("bla(blubb, foo(bar)). bla(blubb, foo(bar)).");
		Atom a1 = p1.getFacts().get(0);
		Atom a2 = p1.getFacts().get(1);
		assertEquals(a1, a2);
		InputProgram p2 = parser.parse("foo(1, 2, 3, \"bar\"). foo(1, 2, 3, \"bar\").");
		Atom a3 = p2.getFacts().get(0);
		Atom a4 = p2.getFacts().get(1);
		assertEquals(a3, a4);
		InputProgram p3 = parser.parse("foo(BAR). foo(BAR).");
		Atom a5 = p3.getFacts().get(0);
		Atom a6 = p3.getFacts().get(1);
		assertEquals(a5, a6);
		InputProgram p4 = parser.parse("foo(b, a, r(\"bla\", BLUBB)). foo(b, a, r(\"bla\", BLUBB)).");
		Atom a7 = p4.getFacts().get(0);
		Atom a8 = p4.getFacts().get(1);
		assertEquals(a7, a8);

		assertFalse(a1.equals(a3));
		assertFalse(a3.equals(a1));
		assertFalse(a1.equals(a5));
		assertFalse(a5.equals(a1));
		assertFalse(a1.equals(a7));
		assertFalse(a7.equals(a1));
	}

	@Test
	public void testIsExternalAtomGround() {
		InputProgram p1 = parser.parse("a :- &isFoo[1].", externals);
		Atom ext1 = p1.getRules().get(0).getBody().stream().findFirst().get().getAtom();
		assertExternalAtomGround(ext1, true);
		InputProgram p2 = parser.parse("a :- &isFoo[bar(1)].", externals);
		Atom ext2 = p2.getRules().get(0).getBody().stream().findFirst().get().getAtom();
		assertExternalAtomGround(ext2, true);
		InputProgram p3 = parser.parse("a :- &isFoo[BLA].", externals);
		Atom ext3 = p3.getRules().get(0).getBody().stream().findFirst().get().getAtom();
		assertExternalAtomGround(ext3, false);
	}

	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testAreExternalAtomsEqual() {
		InputProgram p1 = parser.parse("a :- &isFoo[1].", externals);
		Atom ext1 = p1.getRules().get(0).getBody().stream().findFirst().get().getAtom();
		InputProgram p2 = parser.parse("a :- &isFoo[1].", externals);
		Atom ext2 = p2.getRules().get(0).getBody().stream().findFirst().get().getAtom();
		assertEquals(ext1, ext2);
		assertEquals(ext2, ext1);

		assertFalse(ext1.equals(null));
		assertFalse(ext1.equals("bla"));
		assertTrue(ext1.hashCode() == ext2.hashCode());
	}

	@Test
	public void testExternalHasOutput() {
		InputProgram p = parser.parse("a:- &extWithOutput[1](OUT).", externals);
		Atom ext = p.getRules().get(0).getBody().stream().findFirst().get().getAtom();
		assertExternalAtomGround(ext, false);
		assertTrue(((ExternalAtom) ext).hasOutput());
	}

	private void assertBasicAtomGround(Atom a, boolean expectedGround) {
		assertTrue(a instanceof BasicAtom);
		assertEquals(expectedGround, a.isGround());
	}

	private void assertExternalAtomGround(Atom a, boolean expectedGround) {
		assertTrue(a instanceof ExternalAtom);
		assertEquals(expectedGround, a.isGround());
	}

}
