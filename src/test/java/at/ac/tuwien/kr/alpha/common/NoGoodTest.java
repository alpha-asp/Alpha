package at.ac.tuwien.kr.alpha.common;

import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.common.Literals.atomToNegatedLiteral;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class NoGoodTest {
	/**
	 * Constructs an array of literals with the new representation (least-significant bit is polarity, other bits
	 * are atom id) from an array of literals from the old representation (positive literal is atom id, negative
	 * literal is negated atom id).
	 * @param literals
	 * @return
	 */
	public static int[] fromOldLiterals(int... literals) {
		int[] newLiterals = new int[literals.length];
		for (int i = 0; i < literals.length; i++) {
			newLiterals[i] = literals[i] >= 0 ? atomToLiteral(literals[i]) : atomToNegatedLiteral(-literals[i]);
		}
		return newLiterals;
	}

	public static int fromOldLiterals(int literal) {
			return literal >= 0 ? atomToLiteral(literal) : atomToNegatedLiteral(-literal);
	}

	@Test
	public void iteration() throws Exception {
		Iterator<Integer> i = new NoGood(1).iterator();
		assertEquals(1, (int)i.next());
		assertFalse(i.hasNext());
	}

	@Test
	public void compareToNull() throws Exception {
		assertThrows(NullPointerException.class, () -> {
			new NoGood().compareTo(null);
		});
	}

	@Test
	public void compareToSame() throws Exception {
		assertEquals(0, new NoGood(1).compareTo(new NoGood(1)));
	}

	@Test
	public void compareToLengthShort() throws Exception {
		assertEquals(-1, new NoGood(1).compareTo(new NoGood(1, 2)));
	}

	@Test
	public void compareToLengthLong() throws Exception {
		assertEquals(+1, new NoGood(1, 2).compareTo(new NoGood(1)));
	}

	@Test
	public void compareToLexicographicSmall() throws Exception {
		assertEquals(-1, new NoGood(1, 2).compareTo(new NoGood(2, 3)));
	}

	@Test
	public void compareToLexicographicBig() throws Exception {
		assertEquals(+1, new NoGood(2, 3).compareTo(new NoGood(1, 2)));
	}

	@Test
	public void deleteDuplicates() {
		NoGood ng = NoGood.headFirst(fromOldLiterals(-3, 1, -2, -2));
		assertEquals(3, ng.size(), "Duplicate entry must be removed.");
		assertEquals(fromOldLiterals(-3), ng.getLiteral(0));
		assertEquals(fromOldLiterals(1), ng.getLiteral(1));
		assertEquals(fromOldLiterals(-2), ng.getLiteral(2));

		NoGood ng2 = NoGood.headFirst(fromOldLiterals(-2, 3, 3, -6, -1, 5, 5, -6, 7));
		assertEquals(6, ng2.size(), "Duplicate entries must be removed.");
		assertEquals(fromOldLiterals(-2), ng2.getLiteral(0));
		assertEquals(fromOldLiterals(-1), ng2.getLiteral(1));
		assertEquals(fromOldLiterals(3), ng2.getLiteral(2));
		assertEquals(fromOldLiterals(5), ng2.getLiteral(3));
		assertEquals(fromOldLiterals(-6), ng2.getLiteral(4));
		assertEquals(fromOldLiterals(7), ng2.getLiteral(5));

		NoGood ng3 = NoGood.headFirst(fromOldLiterals(-1, 2, -3, -4));
		assertEquals(4, ng3.size(), "NoGood contains no duplicates, size must stay the same.");
		assertEquals(fromOldLiterals(-1), ng3.getLiteral(0));
		assertEquals(fromOldLiterals(2), ng3.getLiteral(1));
		assertEquals(fromOldLiterals(-3), ng3.getLiteral(2));
		assertEquals(fromOldLiterals(-4), ng3.getLiteral(3));

	}

	@Test
	public void noGoodsInHashMap() {
		NoGood ng1 = NoGood.headFirst(-1, 2, -3, -4);
		NoGood ng2 = NoGood.headFirst(-1, 2, -4);
		NoGood ng3 = NoGood.headFirst(-1, 2, -3, -4);
		Map<NoGood, Integer> noGoodIdentifiers = new LinkedHashMap<>();
		noGoodIdentifiers.put(ng1, 1);
		noGoodIdentifiers.put(ng1, 2);
		noGoodIdentifiers.put(ng2, 4);
		assertTrue(noGoodIdentifiers.containsKey(ng3));
		noGoodIdentifiers.put(ng3, 5);
		assertEquals(2, noGoodIdentifiers.size());
	}
}