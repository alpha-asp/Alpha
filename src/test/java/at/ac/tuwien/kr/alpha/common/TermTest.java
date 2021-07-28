package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class TermTest {

	@Test
	public void testTermReferenceEquality() {
		// Terms must have a unique representation so that reference comparison is
		// sufficient to check
		// whether two terms are equal.
		ConstantTerm<?> ta1 = ConstantTerm.getInstance("a");
		ConstantTerm<?> ta2 = ConstantTerm.getInstance("a");
		assertTrue(ta1 == ta2, "Two instances of ConstantTerms for the same term symbol must be the same object");

		List<Term> termList = new LinkedList<>();
		termList.add(ta1);
		termList.add(ta2);
		FunctionTerm ft1 = FunctionTerm.getInstance("f", termList);
		List<Term> termList2 = new LinkedList<>();
		termList2.add(ta1);
		termList2.add(ta2);
		FunctionTerm ft2 = FunctionTerm.getInstance("f", termList2);
		assertTrue(ft1 == ft2, "Two instances of FunctionTerms for the same term symbol and equal term lists must be the same object");
	}

	@Test
	public void testTermVariableOccurrences() {
		ConstantTerm<?> ta = ConstantTerm.getInstance("a");
		VariableTerm tx = VariableTerm.getInstance("X");
		FunctionTerm tf = FunctionTerm.getInstance("f", ta, tx);
		List<VariableTerm> occurringVariables = tf.getOccurringVariables();

		assertEquals(tx, occurringVariables.get(0), "Variable occurring as subterm must be reported as occurring variable.");
	}

	@Test
	public void testTermOrdering() throws Exception {
		Term cts = ConstantTerm.getInstance("abc");
		Term cti = ConstantTerm.getInstance(2);
		Term cto = ConstantTerm.getInstance(new UUID(0, 0));
		Term ft = FunctionTerm.getInstance("f", ConstantTerm.getInstance("a"));

		assertTrue(cts.compareTo(cti) > 0);
		assertTrue(cti.compareTo(cts) < 0);

		assertTrue(cts.compareTo(cto) < 0);
		assertTrue(cto.compareTo(cts) > 0);

		assertTrue(cts.compareTo(ft) < 0);
		assertTrue(ft.compareTo(cts) > 0);

		assertTrue(cto.compareTo(ft) < 0);
		assertTrue(ft.compareTo(cto) > 0);
	}

	@Test
	public void testStringVsConstantSymbolEquality() {
		String theString = "string";
		ConstantTerm<String> stringConstant = ConstantTerm.getInstance(theString);
		ConstantTerm<String> constantSymbol = ConstantTerm.getSymbolicInstance(theString);
		// Reference equality must hold for both the string constant and the constant
		// symbol.
		assertTrue(stringConstant == ConstantTerm.getInstance(theString));
		ConstantTerm<String> sameConstantSymbol = ConstantTerm.getSymbolicInstance(theString);
		assertTrue(constantSymbol == sameConstantSymbol);
		// Make sure both hashCode and equals understand that stringConstant and
		// constantSymbol are NOT the same thing!
		assertNotEquals(stringConstant.hashCode(), constantSymbol.hashCode());
		assertNotEquals(stringConstant, constantSymbol);
		// This also applies to compareTo - it must behave in sync with equals and
		// hashCode, i.e. return a non-zero result for non-equal objects
		assertNotEquals(0, stringConstant.compareTo(constantSymbol));
	}
}
