/*
 * Copyright (c) 2018, 2020 Siemens AG
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.atoms.ChoiceAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link NoGoodGenerator}
 */
public class NoGoodGeneratorTest {
	private static final ProgramParser PARSER = new ProgramParser();
	
	private static final ConstantTerm<?> A = ConstantTerm.getSymbolicInstance("a");
	private static final ConstantTerm<?> B = ConstantTerm.getSymbolicInstance("b");

	private static final VariableTerm X = VariableTerm.getInstance("X");
	private static final VariableTerm Y = VariableTerm.getInstance("Y");

	@Before
	public void setUp() {
		NonGroundRule.ID_GENERATOR.resetGenerator();
	}
	
	/**
	 * Calls {@link NoGoodGenerator#collectNegLiterals(NonGroundRule, Substitution)},
	 * which puts the atom occuring negatively in a rule into the atom store.
	 * It is then checked whether the atom in the atom store is positive.
	 */
	@Test
	public void collectNeg_ContainsOnlyPositiveLiterals() {
		Program program = PARSER.parse("p(a,b). "
				+ "q(a,b) :- not nq(a,b). "
				+ "nq(a,b) :- not q(a,b).");
		
		Rule rule = program.getRules().get(1);
		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore, true);
		NoGoodGenerator noGoodGenerator = ((NaiveGrounder)grounder).noGoodGenerator;
		NonGroundRule nonGroundRule = NonGroundRule.constructNonGroundRule(rule);
		Substitution substitution = new Substitution();
		substitution.unifyTerms(X, A);
		substitution.unifyTerms(Y, B);
		List<Integer> collectedNeg = noGoodGenerator.collectNegLiterals(nonGroundRule, substitution).getCollectedGroundLiterals();
		assertEquals(1, collectedNeg.size());
		String negAtomString = atomStore.atomToString(atomOf(collectedNeg.get(0)));
		assertEquals("q(a, b)", negAtomString);
	}

	@Test
	public void testNonGroundNoGoods_normalRuleWithArithmetics() {
		final Program program = PARSER.parse("p(1,1). " +
				"{ p(X1,Y) } :- p(X,Y), X1=X+1. " +
				"q(X,Y) :- p(X,Y), X1=X+1, not p(X1,Y).");
		final Rule rule = program.getRules().get(1);
		final AtomStore atomStore = new AtomStoreImpl();
		final Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore, true);
		final NoGoodGenerator noGoodGenerator = ((NaiveGrounder)grounder).noGoodGenerator;
		final NonGroundRule nonGroundRule = NonGroundRule.constructNonGroundRule(rule);
		final Substitution substitution = new Substitution();

		substitution.put(VariableTerm.getInstance("X"), ConstantTerm.getInstance(2));
		substitution.put(VariableTerm.getInstance("X1"), ConstantTerm.getInstance(3));
		substitution.put(VariableTerm.getInstance("Y"), ConstantTerm.getInstance(1));
		final List<NoGood> noGoods = noGoodGenerator.generateNoGoodsFromGroundSubstitution(nonGroundRule, substitution);

		assertEquals(6, noGoods.size());
		int seenExpected = 0;
		for (NoGood noGood : noGoods) {
			final boolean hasHead = noGood.hasHead();
			final int headAtom = atomOf(noGood.getHead());
			final Integer firstLiteral = noGood.getLiteral(0);
			final String groundNoGoodToString = atomStore.noGoodToString(noGood);
			final String nonGroundNoGoodToString = noGood.getNonGroundNoGood() == null ? null : noGood.getNonGroundNoGood().toString();
			if (hasHead && atomStore.get(headAtom).getPredicate().getName().equals("q")) {
				// head to body
				expectNonGroundNoGoodForGroundNoGood(groundNoGoodToString, "*{ -(q(X, Y)), +(_R_(\"3\",\"{}\")) }", nonGroundNoGoodToString);
			} else if (hasHead && atomStore.get(headAtom) instanceof RuleAtom) {
				// body-representing atom to full body
				expectNonGroundNoGoodForGroundNoGood(groundNoGoodToString, "*{ -(_R_(\"3\",\"{}\")), +(p(X, Y)), -(p(X1, Y)), +(X1 = X + 1) }", nonGroundNoGoodToString);
			} else if (!hasHead && isNegated(firstLiteral)) {
				// positive body atom to body-representing atom
				expectNonGroundNoGoodForGroundNoGood(groundNoGoodToString, "{ -(p(X, Y)), +(_R_(\"3\",\"{}\")) }", nonGroundNoGoodToString);
			} else if (!hasHead && !isNegated(firstLiteral)) {
				// negative body atom to body-representing atom
				expectNonGroundNoGoodForGroundNoGood(groundNoGoodToString, "{ +(p(X1, Y)), +(_R_(\"3\",\"{}\")) }", nonGroundNoGoodToString);
			} else if (hasHead && atomStore.get(atomOf(firstLiteral)).getPredicate().equals(ChoiceAtom.OFF)) {
				// ChoiceOff
				expectNonGroundNoGoodForGroundNoGood(groundNoGoodToString, null, nonGroundNoGoodToString);
			} else if (hasHead && atomStore.get(atomOf(firstLiteral)).getPredicate().equals(ChoiceAtom.ON)) {
				// ChoiceOn
				expectNonGroundNoGoodForGroundNoGood(groundNoGoodToString, null, nonGroundNoGoodToString);
			} else {
				continue;
			}
			seenExpected++;
		}
		assertEquals(6, seenExpected);
	}

	@Test
	public void testNonGroundNoGoods_constraint() {
		final Program program = PARSER.parse("p(1,1). " +
				"{ p(X1,Y) } :- p(X,Y), X1=X+1. " +
				":- p(X,Y), X1=X+1, not p(X1,Y).");
		final Rule rule = program.getRules().get(1);
		final AtomStore atomStore = new AtomStoreImpl();
		final Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore, true);
		final NoGoodGenerator noGoodGenerator = ((NaiveGrounder)grounder).noGoodGenerator;
		final NonGroundRule nonGroundRule = NonGroundRule.constructNonGroundRule(rule);
		final Substitution substitution = new Substitution();

		substitution.put(VariableTerm.getInstance("X"), ConstantTerm.getInstance(2));
		substitution.put(VariableTerm.getInstance("X1"), ConstantTerm.getInstance(3));
		substitution.put(VariableTerm.getInstance("Y"), ConstantTerm.getInstance(1));
		final List<NoGood> noGoods = noGoodGenerator.generateNoGoodsFromGroundSubstitution(nonGroundRule, substitution);

		assertEquals(1, noGoods.size());
		final NoGood noGood = noGoods.get(0);
		final String groundNoGoodToString = atomStore.noGoodToString(noGood);
		final String nonGroundNoGoodToString = noGood.getNonGroundNoGood() == null ? null : noGood.getNonGroundNoGood().toString();
		expectNonGroundNoGoodForGroundNoGood(groundNoGoodToString, "{ +(p(X, Y)), -(p(X1, Y)), +(X1 = X + 1) }", nonGroundNoGoodToString);
	}

	private void expectNonGroundNoGoodForGroundNoGood(String groundNoGoodToString, String expectedNonGroundNoGoodToString, String nonGroundNoGoodToString) {
		assertEquals("Unexpected non-ground nogood for ground nogood " + groundNoGoodToString, expectedNonGroundNoGoodToString, nonGroundNoGoodToString);
	}

}
