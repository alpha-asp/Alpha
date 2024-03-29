package at.ac.tuwien.kr.alpha.commons.programs.terms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.terms.ConstantTerm;

public class TermsTest {

	@Test
	public void integersAsTermList() {
		List<ConstantTerm<Integer>> intTerms = Terms.asTermList(1, 2, 3, 4, 5, 6);
		assertEquals(6, intTerms.size());
		assertEquals(Terms.newConstant(1), intTerms.get(0));
		assertEquals(Terms.newConstant(2), intTerms.get(1));
		assertEquals(Terms.newConstant(3), intTerms.get(2));
		assertEquals(Terms.newConstant(4), intTerms.get(3));
		assertEquals(Terms.newConstant(5), intTerms.get(4));
		assertEquals(Terms.newConstant(6), intTerms.get(5));
	}

	@Test
	public void stringsAsTermList() {
		List<ConstantTerm<String>> terms = Terms.asTermList("bla", "blubb");
		assertEquals(2, terms.size());
		assertEquals("\"bla\"", terms.get(0).toString());
		assertEquals("\"blubb\"", terms.get(1).toString());
	}

}
