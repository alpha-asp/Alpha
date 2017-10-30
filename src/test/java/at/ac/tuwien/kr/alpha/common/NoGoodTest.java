package at.ac.tuwien.kr.alpha.common;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NoGoodTest {
	@Test
	void iteration() throws Exception {
		Iterator<Integer> i = new NoGood(1).iterator();
		assertEquals(1, (int)i.next());
		assertFalse(i.hasNext());
	}

	@Test
	void compareToNull() {
		assertThrows(NullPointerException.class, () ->
			new NoGood().compareTo(null)
		);
	}

	@Test
	void compareToSame() throws Exception {
		assertEquals(0, new NoGood(1).compareTo(new NoGood(1)));
	}

	@Test
	void compareToLengthShort() throws Exception {
		assertEquals(-1, new NoGood(1).compareTo(new NoGood(1, 2)));
	}

	@Test
	void compareToLengthLong() throws Exception {
		assertEquals(+1, new NoGood(1, 2).compareTo(new NoGood(1)));
	}

	@Test
	void compareToLexicographicSmall() throws Exception {
		assertEquals(-1, new NoGood(1, 2).compareTo(new NoGood(2, 3)));
	}

	@Test
	void compareToLexicographicBig() throws Exception {
		assertEquals(+1, new NoGood(2, 3).compareTo(new NoGood(1, 2)));
	}

	@Test
	void deleteDuplicates() {
		NoGood ng = new NoGood(new int[]{1, -2, -2, 3}, 3);
		assertEquals(3, ng.size(), "Duplicate entry must be removed.");
		assertEquals(2, ng.getHead(), "Head pointer must be moved to correct position.");
		assertEquals(-2, ng.getLiteral(0));
		assertEquals(1, ng.getLiteral(1));
		assertEquals(3, ng.getLiteral(2));

		NoGood ng2 = new NoGood(new int[]{3, 3, -6, -1, 2, 5, 5, -6, 7}, 4);
		assertEquals(6, ng2.size(), "Duplicate entries must be removed.");
		assertEquals(2, ng2.getHead(), "Head pointer must be moved to correct position.");
		assertEquals(-6, ng2.getLiteral(0));
		assertEquals(-1, ng2.getLiteral(1));
		assertEquals(2, ng2.getLiteral(2));
		assertEquals(3, ng2.getLiteral(3));
		assertEquals(5, ng2.getLiteral(4));
		assertEquals(7, ng2.getLiteral(5));

		NoGood ng3 = new NoGood(new int[]{1, 2, -3, -4}, 0);
		assertEquals(4, ng3.size(), "NoGood contains no duplicates, size must stay the same.");
		assertEquals(2, ng3.getHead(), "Head pointer must be moved to correct position.");
		assertEquals(-4, ng3.getLiteral(0));
		assertEquals(-3, ng3.getLiteral(1));
		assertEquals(1, ng3.getLiteral(2));
		assertEquals(2, ng3.getLiteral(3));
	}

	@Test
	void noGoodsInHashMap() {
		NoGood ng1 = new NoGood(new int[]{1, 2, -3, -4}, 0);
		NoGood ng2 = new NoGood(new int[]{1, 2, -4}, 0);
		NoGood ng3 = new NoGood(new int[]{1, 2, -3, -4}, 0);
		Map<NoGood, Integer> noGoodIdentifiers = new LinkedHashMap<>();
		noGoodIdentifiers.put(ng1, 1);
		noGoodIdentifiers.put(ng1, 2);
		noGoodIdentifiers.put(ng2, 4);
		assertTrue(noGoodIdentifiers.containsKey(ng3));
		noGoodIdentifiers.put(ng3, 5);
		assertEquals(2, noGoodIdentifiers.size());
	}
}