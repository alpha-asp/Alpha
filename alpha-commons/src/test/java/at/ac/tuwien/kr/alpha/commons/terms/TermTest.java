package at.ac.tuwien.kr.alpha.commons.terms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.terms.ArithmeticOperator;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;

/**
 * Copyright (c) 2016-2021, the Alpha Team.
 */
// TODO maybe this should actually work against the AbstractTerm type since it tests properties of interned terms which is not part of the public Term interface
public class TermTest {

	@Test
	public void testTermReferenceEquality() {
		// Terms must have a unique representation so that reference comparison is
		// sufficient to check
		// whether two terms are equal.

		// Test for constant terms.
		ConstantTerm<?> ta1 = Terms.newConstant("a");
		ConstantTerm<?> ta2 = Terms.newConstant("a");
		assertTrue(ta1 == ta2, "Two instances of ConstantTerms for the same term symbol must be the same object");

		// Test for function terms.
		List<Term> termList = new LinkedList<>();
		termList.add(ta1);
		termList.add(ta2);
		FunctionTermImpl ft1 = FunctionTermImpl.getInstance("f", termList);
		List<Term> termList2 = new LinkedList<>();
		termList2.add(ta1);
		termList2.add(ta2);
		FunctionTermImpl ft2 = FunctionTermImpl.getInstance("f", termList2);
		assertTrue(ft1 == ft2, "Two instances of FunctionTerms for the same term symbol and equal term lists must be the same object");
	
		// Test for arithmetic terms.
		Term tArith1 = ArithmeticTermImpl.getInstance(Terms.newConstant(2), ArithmeticOperator.PLUS, Terms.newConstant(2));
		Term tArith2 = ArithmeticTermImpl.getInstance(Terms.newConstant(2), ArithmeticOperator.PLUS, Terms.newConstant(2));
		assertTrue(tArith1 == tArith2, "Two instances of ArithmeticTerms for the same operator symbol and equal term lists must be the same object");
	
		// Test for interval terms.
		IntervalTerm tInt1 = IntervalTermImpl.getInstance(Terms.newConstant(-3), Terms.newVariable("X"));
		IntervalTerm tInt2 = IntervalTermImpl.getInstance(Terms.newConstant(-3), Terms.newVariable("X"));
		assertTrue(tInt1 == tInt2, "Two instances of IntervalTerms for the same bounds must be the same object");

		// Test for variables.
		VariableTerm tVar1 = VariableTermImpl.getInstance("X");
		VariableTerm tVar2 = VariableTermImpl.getInstance("X");
		assertTrue(tVar1 == tVar2, "Two instances of VariableTerms for the same name must be the same object");
	}

	@Test
	public void testTermVariableOccurrences() {
		ConstantTerm<?> ta = Terms.newConstant("a");
		VariableTerm tx = VariableTermImpl.getInstance("X");
		FunctionTermImpl tf = FunctionTermImpl.getInstance("f", ta, tx);
		Set<VariableTerm> occurringVariables = tf.getOccurringVariables();

		assertEquals(tx, new ArrayList<>(occurringVariables).get(0), "Variable occurring as subterm must be reported as occurring variable.");
	}

	@Test
	public void testTermOrdering() {
		Term cts = Terms.newConstant("abc");
		Term cti = Terms.newConstant(2);
		Term cto = Terms.newConstant(new UUID(0, 0));
		Term ft = FunctionTermImpl.getInstance("f", Terms.newConstant("a"));

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
		ConstantTerm<String> stringConstant = Terms.newConstant(theString);
		ConstantTerm<String> constantSymbol = Terms.newSymbolicConstant(theString);
		// Reference equality must hold for both the string constant and the constant
		// symbol.
		assertTrue(stringConstant == Terms.newConstant(theString));
		ConstantTerm<String> sameConstantSymbol = Terms.newSymbolicConstant(theString);
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
