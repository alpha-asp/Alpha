package at.ac.tuwien.kr.alpha.core.externals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;

public class AspStandardLibraryTest {

	@Test
	public void parseDateTime1() {
		Set<List<ConstantTerm<Integer>>> dtSubstitution = AspStandardLibrary.datetimeParse("20.05.2020 01:19:13", "dd.MM.yyyy HH:mm:ss");
		assertEquals(1, dtSubstitution.size());
		List<ConstantTerm<Integer>> dtTerms = dtSubstitution.stream().findFirst().get();
		assertEquals(6, dtTerms.size());
		assertEquals(Terms.newConstant(2020), dtTerms.get(0));
		assertEquals(Terms.newConstant(5), dtTerms.get(1));
		assertEquals(Terms.newConstant(20), dtTerms.get(2));
		assertEquals(Terms.newConstant(1), dtTerms.get(3));
		assertEquals(Terms.newConstant(19), dtTerms.get(4));
		assertEquals(Terms.newConstant(13), dtTerms.get(5));
	}

	@Test
	public void parseDateTime2() {
		Set<List<ConstantTerm<Integer>>> dtSubstitution = AspStandardLibrary.datetimeParse("07/2123/18 22/37/01", "MM/yyyy/dd HH/mm/ss");
		assertEquals(1, dtSubstitution.size());
		List<ConstantTerm<Integer>> dtTerms = dtSubstitution.stream().findFirst().get();
		assertEquals(6, dtTerms.size());
		assertEquals(Terms.newConstant(2123), dtTerms.get(0));
		assertEquals(Terms.newConstant(7), dtTerms.get(1));
		assertEquals(Terms.newConstant(18), dtTerms.get(2));
		assertEquals(Terms.newConstant(22), dtTerms.get(3));
		assertEquals(Terms.newConstant(37), dtTerms.get(4));
		assertEquals(Terms.newConstant(1), dtTerms.get(5));
	}

	@Test
	public void parseDateTime3() {
		Set<List<ConstantTerm<Integer>>> dtSubstitution = AspStandardLibrary.datetimeParse("\"03,12,2019\", \"11:00:00\"",
				"\"dd,MM,yyyy\", \"HH:mm:ss\"");
		assertEquals(1, dtSubstitution.size());
		List<ConstantTerm<Integer>> dtTerms = dtSubstitution.stream().findFirst().get();
		assertEquals(6, dtTerms.size());
		assertEquals(Terms.newConstant(2019), dtTerms.get(0));
		assertEquals(Terms.newConstant(12), dtTerms.get(1));
		assertEquals(Terms.newConstant(3), dtTerms.get(2));
		assertEquals(Terms.newConstant(11), dtTerms.get(3));
		assertEquals(Terms.newConstant(0), dtTerms.get(4));
		assertEquals(Terms.newConstant(0), dtTerms.get(5));
	}

	@Test
	public void datetimeBefore() {
		assertTrue(AspStandardLibrary.datetimeIsBefore(1990, 2, 14, 15, 16, 17, 1990, 3, 1, 0, 59, 1));
		assertFalse(AspStandardLibrary.datetimeIsBefore(2015, 5, 13, 12, 1, 33, 2003, 1, 1, 0, 0, 1));
		assertFalse(AspStandardLibrary.datetimeIsBefore(2022, 2, 22, 22, 22, 22, 2022, 2, 22, 22, 22, 22));
	}
	
	@Test
	public void datetimeEqual() {
		assertTrue(AspStandardLibrary.datetimeIsEqual(1990, 2, 14, 15, 16, 17, 1990, 2, 14, 15, 16, 17));
		assertFalse(AspStandardLibrary.datetimeIsEqual(2015, 5, 13, 12, 1, 33, 2003, 1, 1, 0, 0, 1));
	}	

	@Test
	public void datetimeBeforeOrEqual() {
		assertTrue(AspStandardLibrary.datetimeIsBeforeOrEqual(1990, 2, 14, 15, 16, 17, 1990, 3, 1, 0, 59, 1));
		assertFalse(AspStandardLibrary.datetimeIsBeforeOrEqual(2015, 5, 13, 12, 1, 33, 2003, 1, 1, 0, 0, 1));
		assertTrue(AspStandardLibrary.datetimeIsBeforeOrEqual(2022, 2, 22, 22, 22, 22, 2022, 2, 22, 22, 22, 22));
		
	}
	
	@Test
	public void matchesRegex() {
		assertTrue(AspStandardLibrary.stringMatchesRegex("Blaaaaa Blubbb!!", "Bla+ Blub+!!"));
		assertFalse(AspStandardLibrary.stringMatchesRegex("Foobar", "Bla+ Blub+!!"));
	}

	@Test
	public void stringLength() {
		Set<List<ConstantTerm<Integer>>> result = AspStandardLibrary.stringLength("A String of length 21");
		assertEquals(1, result.size());
		List<ConstantTerm<Integer>> lengthTerms = result.stream().findFirst().get();
		assertEquals(1, lengthTerms.size());
		ConstantTerm<Integer> lenTerm = lengthTerms.get(0);
		assertEquals(Terms.newConstant(21), lenTerm);
	}

	@Test
	public void stringConcat() {
		Set<List<ConstantTerm<String>>> result = AspStandardLibrary.stringConcat("Foo", "bar");
		assertEquals(1, result.size());
		List<ConstantTerm<String>> concatTerms = result.stream().findFirst().get();
		assertEquals(1, concatTerms.size());
		ConstantTerm<String> concat = concatTerms.get(0);
		assertEquals(Terms.newConstant("Foobar"), concat);
	}

}
