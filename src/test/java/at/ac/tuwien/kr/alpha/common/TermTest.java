package at.ac.tuwien.kr.alpha.common;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

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
		ConstantTerm ta1 = ConstantTerm.getInstance("a");
		ConstantTerm ta2 = ConstantTerm.getInstance("a");
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
		ConstantTerm ta = ConstantTerm.getInstance("a");
		VariableTerm tx = VariableTerm.getInstance("X");
		FunctionTerm tf = FunctionTerm.getInstance("f", ta, tx);
		List<VariableTerm> occurringVariables = tf.getOccurringVariables();

		assertEquals("Variable occurring as subterm must be reported as occurring variable.", occurringVariables.get(0), tx);
	}

}