package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.terms.Constant;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.Variable;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class TermTest {

	@Test
	public void testTermReferenceEquality() {
		// Terms must have a unique representation so that reference comparison is sufficient to check
		// whether two terms are equal.
		Constant ta1 = Constant.getInstance("a");
		Constant ta2 = Constant.getInstance("a");
		assertTrue("Two instances of ConstantTerms for the same term symbol must be the same object", ta1 == ta2);

		List<Term> termList = new LinkedList<>();
		termList.add(ta1);
		termList.add(ta2);
		FunctionTerm ft1 = FunctionTerm.getInstance("f", termList);
		List<Term> termList2 = new LinkedList<>();
		termList2.add(ta1);
		termList2.add(ta2);
		FunctionTerm ft2 = FunctionTerm.getInstance("f", termList2);
		assertTrue("Two instances of FunctionTerms for the same term symbol and equal term lists must be the same object", ft1 == ft2);
	}

	@Test
	public void testTermVariableOccurrences() {
		Constant ta = Constant.getInstance("a");
		Variable tx = Variable.getInstance("X");
		FunctionTerm tf = FunctionTerm.getInstance("f", ta, tx);
		List<Variable> occurringVariables = tf.getOccurringVariables();

		assertEquals("Variable occurring as subterm must be reported as occurring variable.", occurringVariables.get(0), tx);
	}

	@Test
	public void testTermOrdering() throws Exception {
		Term cts = Constant.getInstance("abc");
		Term cti = Constant.getInstance(2);
		Term cto = Constant.getInstance(new UUID(0, 0));
		Term ft = FunctionTerm.getInstance("f", Constant.getInstance("a"));

		assertTrue(cts.compareTo(cti) > 0);
		assertTrue(cti.compareTo(cts) < 0);

		assertTrue(cts.compareTo(cto) < 0);
		assertTrue(cto.compareTo(cts) > 0);

		assertTrue(cts.compareTo(ft) < 0);
		assertTrue(ft.compareTo(cts) > 0);

		assertTrue(cto.compareTo(ft) < 0);
		assertTrue(ft.compareTo(cto) > 0);
	}
}