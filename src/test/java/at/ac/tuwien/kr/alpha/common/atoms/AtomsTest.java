package at.ac.tuwien.kr.alpha.common.atoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.common.atoms.external.ExternalAtoms;
import at.ac.tuwien.kr.alpha.common.atoms.external.Predicate;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.program.impl.InputProgram;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

/**
 * Test for basic functionality of various implementations of {@link Atom}.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class AtomsTest {

	private final ProgramParser parser;

	public AtomsTest() throws NoSuchMethodException, SecurityException {
		Map<String, PredicateInterpretation> externals = new HashMap<>();
		externals.put("isFoo", ExternalAtoms.processPredicateMethod(AtomsTest.class.getMethod("isFoo", int.class)));
		externals.put("extWithOutput", ExternalAtoms.processPredicateMethod(AtomsTest.class.getMethod("extWithOutput", int.class)));
		this.parser = new ProgramParser(externals);
	}

	@Predicate
	public static final boolean isFoo(int bar) {
		return 0xF00 == bar;
	}

	@Predicate
	public static final Set<List<ConstantTerm<Integer>>> extWithOutput(int in) {
		Set<List<ConstantTerm<Integer>>> retVal = new HashSet<>();
		List<ConstantTerm<Integer>> lst = new ArrayList<>();
		lst.add(ConstantTerm.getSymbolicInstance(Integer.toString(in)));
		retVal.add(lst);
		return retVal;
	}

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

	@Test
	public void testAreBasicAtomsEqual() {
		InputProgram p1 = parser.parse("bla(blubb, foo(bar)). bla(blubb, foo(bar)).");
		Atom a1 = p1.getFacts().get(0);
		Atom a2 = p1.getFacts().get(1);
		Assert.assertEquals(a1, a2);
		InputProgram p2 = parser.parse("foo(1, 2, 3, \"bar\"). foo(1, 2, 3, \"bar\").");
		Atom a3 = p2.getFacts().get(0);
		Atom a4 = p2.getFacts().get(1);
		Assert.assertEquals(a3, a4);
		InputProgram p3 = parser.parse("foo(BAR). foo(BAR).");
		Atom a5 = p3.getFacts().get(0);
		Atom a6 = p3.getFacts().get(1);
		Assert.assertEquals(a5, a6);
		InputProgram p4 = parser.parse("foo(b, a, r(\"bla\", BLUBB)). foo(b, a, r(\"bla\", BLUBB)).");
		Atom a7 = p4.getFacts().get(0);
		Atom a8 = p4.getFacts().get(1);
		Assert.assertEquals(a7, a8);

		Assert.assertFalse(a1.equals(a3));
		Assert.assertFalse(a3.equals(a1));
		Assert.assertFalse(a1.equals(a5));
		Assert.assertFalse(a5.equals(a1));
		Assert.assertFalse(a1.equals(a7));
		Assert.assertFalse(a7.equals(a1));
	}

	@Test
	public void testIsExternalAtomGround() {
		InputProgram p1 = parser.parse("a :- &isFoo[1].");
		Atom ext1 = p1.getRules().get(0).getBody().get(0).getAtom();
		this.assertExternalAtomGround(ext1, true);
		InputProgram p2 = parser.parse("a :- &isFoo[bar(1)].");
		Atom ext2 = p2.getRules().get(0).getBody().get(0).getAtom();
		this.assertExternalAtomGround(ext2, true);
		InputProgram p3 = parser.parse("a :- &isFoo[BLA].");
		Atom ext3 = p3.getRules().get(0).getBody().get(0).getAtom();
		this.assertExternalAtomGround(ext3, false);
	}

	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testAreExternalAtomsEqual() {
		InputProgram p1 = parser.parse("a :- &isFoo[1].");
		Atom ext1 = p1.getRules().get(0).getBody().get(0).getAtom();
		InputProgram p2 = parser.parse("a :- &isFoo[1].");
		Atom ext2 = p2.getRules().get(0).getBody().get(0).getAtom();
		Assert.assertEquals(ext1, ext2);
		Assert.assertEquals(ext2, ext1);

		Assert.assertFalse(ext1.equals(null));
		Assert.assertFalse(ext1.equals("bla"));
		Assert.assertTrue(ext1.hashCode() == ext2.hashCode());
	}

	@Test
	public void testExternalHasOutput() {
		InputProgram p = parser.parse("a:- &extWithOutput[1](OUT).");
		Atom ext = p.getRules().get(0).getBody().get(0).getAtom();
		this.assertExternalAtomGround(ext, false);
		Assert.assertTrue(((ExternalAtom) ext).hasOutput());
	}

	private void assertBasicAtomGround(Atom a, boolean expectedGround) {
		Assert.assertTrue(a instanceof BasicAtom);
		Assert.assertEquals(expectedGround, a.isGround());
	}

	private void assertExternalAtomGround(Atom a, boolean expectedGround) {
		Assert.assertTrue(a instanceof ExternalAtom);
		Assert.assertEquals(expectedGround, a.isGround());
	}

}
