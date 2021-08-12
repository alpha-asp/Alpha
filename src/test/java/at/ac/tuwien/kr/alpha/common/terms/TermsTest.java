package at.ac.tuwien.kr.alpha.common.terms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class TermsTest {

	@Test
	public void integersAsTermList() {
		List<ConstantTerm<Integer>> intTerms = Terms.asTermList(1, 2, 3, 4, 5, 6);
		assertEquals(6, intTerms.size());
		assertEquals(ConstantTerm.getInstance(1), intTerms.get(0));
		assertEquals(ConstantTerm.getInstance(2), intTerms.get(1));
		assertEquals(ConstantTerm.getInstance(3), intTerms.get(2));
		assertEquals(ConstantTerm.getInstance(4), intTerms.get(3));
		assertEquals(ConstantTerm.getInstance(5), intTerms.get(4));
		assertEquals(ConstantTerm.getInstance(6), intTerms.get(5));
	}

	@Test
	public void stringsAsTermList() {
		List<ConstantTerm<String>> terms = Terms.asTermList("bla", "blubb");
		assertEquals(2, terms.size());
		assertEquals("\"bla\"", terms.get(0).toString());
		assertEquals("\"blubb\"", terms.get(1).toString());
	}

}
