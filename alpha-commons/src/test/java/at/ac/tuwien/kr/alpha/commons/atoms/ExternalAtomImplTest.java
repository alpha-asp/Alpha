package at.ac.tuwien.kr.alpha.commons.atoms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.externals.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.externals.Externals;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;

public class ExternalAtomImplTest {

	private Map<String, PredicateInterpretation> externals;

	public ExternalAtomImplTest() throws NoSuchMethodException, SecurityException {
		externals = new HashMap<>();
		externals.put("isFoo", Externals.processPredicateMethod(ExternalAtomImplTest.class.getMethod("isFoo", int.class)));
		externals.put("extWithOutput", Externals.processPredicateMethod(ExternalAtomImplTest.class.getMethod("extWithOutput", int.class)));
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
	public void testIsExternalAtomGround() {
		List<Term> ext1Input = new ArrayList<>();
		ext1Input.add(Terms.newConstant(1));
		// ext1 := &isFoo[1]
		ExternalAtom ext1 = Atoms.newExternalAtom(Predicates.getPredicate("isFoo", 1), externals.get("isFoo"), ext1Input, Collections.emptyList());
		assertTrue(ext1.isGround());

		// ext2 := &isFoo[bar(1)]
		List<Term> ext2Input = new ArrayList<>();
		ext2Input.add(Terms.newFunctionTerm("bar", Terms.newConstant(1)));
		ExternalAtom ext2 = Atoms.newExternalAtom(Predicates.getPredicate("isFoo", 1), externals.get("isFoo"), ext2Input, Collections.emptyList());
		assertTrue(ext2.isGround());

		// ext3 := &isFoo[BLA]
		List<Term> ext3Input = new ArrayList<>();
		ext3Input.add(Terms.newVariable("BLA"));
		ExternalAtom ext3 = Atoms.newExternalAtom(Predicates.getPredicate("isFoo", 1), externals.get("isFoo"), ext3Input, Collections.emptyList());
		assertFalse(ext3.isGround());
	}

	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testAreExternalAtomsEqual() {
		// ext1 := &isFoo[1]
		List<Term> ext1Input = new ArrayList<>();
		ext1Input.add(Terms.newConstant(1));
		ExternalAtom ext1 = Atoms.newExternalAtom(Predicates.getPredicate("isFoo", 1), externals.get("isFoo"), ext1Input, Collections.emptyList());
		// ext2 := &isFoo[1]
		List<Term> ext2Input = new ArrayList<>();
		ext2Input.add(Terms.newConstant(1));
		ExternalAtom ext2 = Atoms.newExternalAtom(Predicates.getPredicate("isFoo", 1), externals.get("isFoo"), ext2Input, Collections.emptyList());

		assertEquals(ext1, ext2);
		assertEquals(ext2, ext1);

		assertFalse(ext1.equals(null));
		assertFalse(ext1.equals("bla"));
		assertTrue(ext1.hashCode() == ext2.hashCode());
	}

	@Test
	public void testExternalHasOutput() {
		// ext := &extWithOutput[1](OUT)
		List<Term> input = new ArrayList<>();
		List<Term> output = new ArrayList<>();
		input.add(Terms.newConstant(1));
		output.add(Terms.newVariable("OUT"));
		ExternalAtom ext = Atoms.newExternalAtom(Predicates.getPredicate("extWithOutput", 2), externals.get("extWithOutput"), input, output);
		
		assertFalse(ext.isGround());
		assertTrue(ext.hasOutput());
	}

}
