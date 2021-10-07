package at.ac.tuwien.kr.alpha.commons.atoms;

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
import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;

public class ExternalAtomImplTest {

	private final ProgramParser parser;
	private Map<String, PredicateInterpretation> externals;

	public BasicAtomImplTest() throws NoSuchMethodException, SecurityException {
		externals = new HashMap<>();
		externals.put("isFoo", Externals.processPredicateMethod(BasicAtomImplTest.class.getMethod("isFoo", int.class)));
		externals.put("extWithOutput", Externals.processPredicateMethod(BasicAtomImplTest.class.getMethod("extWithOutput", int.class)));
		parser = new ProgramParserImpl();
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
		ASPCore2Program p1 = parser.parse("a :- &isFoo[1].", externals);
		Atom ext1 = p1.getRules().get(0).getBody().stream().findFirst().get().getAtom();
		assertExternalAtomGround(ext1, true);
		ASPCore2Program p2 = parser.parse("a :- &isFoo[bar(1)].", externals);
		Atom ext2 = p2.getRules().get(0).getBody().stream().findFirst().get().getAtom();
		assertExternalAtomGround(ext2, true);
		ASPCore2Program p3 = parser.parse("a :- &isFoo[BLA].", externals);
		Atom ext3 = p3.getRules().get(0).getBody().stream().findFirst().get().getAtom();
		assertExternalAtomGround(ext3, false);
	}

	@Test
	@SuppressWarnings("unlikely-arg-type")
	public void testAreExternalAtomsEqual() {
		ASPCore2Program p1 = parser.parse("a :- &isFoo[1].", externals);
		Atom ext1 = p1.getRules().get(0).getBody().stream().findFirst().get().getAtom();
		ASPCore2Program p2 = parser.parse("a :- &isFoo[1].", externals);
		Atom ext2 = p2.getRules().get(0).getBody().stream().findFirst().get().getAtom();
		assertEquals(ext1, ext2);
		assertEquals(ext2, ext1);

		assertFalse(ext1.equals(null));
		assertFalse(ext1.equals("bla"));
		assertTrue(ext1.hashCode() == ext2.hashCode());
	}

	@Test
	public void testExternalHasOutput() {
		ASPCore2Program p = parser.parse("a:- &extWithOutput[1](OUT).", externals);
		Atom ext = p.getRules().get(0).getBody().stream().findFirst().get().getAtom();
		assertExternalAtomGround(ext, false);
		assertTrue(((ExternalAtom) ext).hasOutput());
	}

	
}
