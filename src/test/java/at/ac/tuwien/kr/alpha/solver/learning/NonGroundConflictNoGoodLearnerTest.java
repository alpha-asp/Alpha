/*
 * Copyright (c) 2020 Siemens AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
package at.ac.tuwien.kr.alpha.solver.learning;

import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.NonGroundNoGood;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.Substitution;
import at.ac.tuwien.kr.alpha.common.Unifier;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.NoGoodGenerator;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import at.ac.tuwien.kr.alpha.solver.Antecedent;
import at.ac.tuwien.kr.alpha.solver.ConflictCause;
import at.ac.tuwien.kr.alpha.solver.NoGoodStore;
import at.ac.tuwien.kr.alpha.solver.NoGoodStoreAlphaRoaming;
import at.ac.tuwien.kr.alpha.solver.TrailAssignment;
import at.ac.tuwien.kr.alpha.solver.WritableAssignment;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Util.intArrayToLinkedHashSet;
import static at.ac.tuwien.kr.alpha.common.ComparisonOperator.EQ;
import static at.ac.tuwien.kr.alpha.common.ComparisonOperator.LT;
import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm.ArithmeticOperator.MINUS;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests {@link NonGroundConflictNoGoodLearner}.
 */
public class NonGroundConflictNoGoodLearnerTest {

	private WritableAssignment assignment;
	private NoGoodStore store;
	private AtomStore atomStore;

	@Before
	public void setUp() {
		atomStore = new AtomStoreImpl();
		this.assignment = new TrailAssignment(atomStore);
		this.store = new NoGoodStoreAlphaRoaming(assignment);
	}

	/**
	 * This is example 4.2.4 from:
	 * Joao Marques-Silva, Ines Lynce and Sharad Malik: Conflict-Driven Clause Learning SAT Solvers
	 * in: Armin Biere, Marijn Heule, Hans van Maaren and Toby Walsh (Eds.): Handbook of Satisfiability
	 */
	@Test
	public void groundExampleFromSatisfiabilityHandbook() {
		final GroundConflictNoGoodLearner groundLearner = new GroundConflictNoGoodLearner(assignment, atomStore);
		final NonGroundConflictNoGoodLearner learner = new NonGroundConflictNoGoodLearner(assignment, atomStore, groundLearner);
		int x1 = atomStore.putIfAbsent(new BasicAtom(Predicate.getInstance("x1", 0)));
		int x2 = atomStore.putIfAbsent(new BasicAtom(Predicate.getInstance("x2", 0)));
		int x3 = atomStore.putIfAbsent(new BasicAtom(Predicate.getInstance("x3", 0)));
		int x4 = atomStore.putIfAbsent(new BasicAtom(Predicate.getInstance("x4", 0)));
		int x5 = atomStore.putIfAbsent(new BasicAtom(Predicate.getInstance("x5", 0)));
		int x6 = atomStore.putIfAbsent(new BasicAtom(Predicate.getInstance("x6", 0)));
		int x21 = atomStore.putIfAbsent(new BasicAtom(Predicate.getInstance("x21", 0)));
		int x31 = atomStore.putIfAbsent(new BasicAtom(Predicate.getInstance("x31", 0)));
		int unrelated1 = atomStore.putIfAbsent(new BasicAtom(Predicate.getInstance("unrelated1", 0)));
		int unrelated4 = atomStore.putIfAbsent(new BasicAtom(Predicate.getInstance("unrelated4", 0)));
		final NoGood ng1 = new NoGood(atomToLiteral(x1, false), atomToLiteral(x31, false), atomToLiteral(x2, true));
		final NoGood ng2 = new NoGood(atomToLiteral(x1, false), atomToLiteral(x3, true));
		final NoGood ng3 = NoGood.headFirst(atomToLiteral(x4, false), atomToLiteral(x2, false), atomToLiteral(x3, false));
		final NoGood ng4 = new NoGood(atomToLiteral(x4, true), atomToLiteral(x5, true));
		final NoGood ng5 = new NoGood(atomToLiteral(x21, false), atomToLiteral(x4, true), atomToLiteral(x6, true));
		final NoGood ng6 = new NoGood(atomToLiteral(x5, false), atomToLiteral(x6, false));

		store.growForMaxAtomId(atomStore.getMaxAtomId());
		this.assignment.growForMaxAtomId();

		store.add(1, ng1);
		store.add(2, ng2);
		store.add(3, ng3);
		store.add(4, ng4);
		store.add(5, ng5);
		store.add(6, ng6);

		assertEquals(0, assignment.getDecisionLevel());
		assignment.choose(unrelated1, TRUE);
		ConflictCause conflictCause = store.propagate();
		assertNull(conflictCause);
		assertEquals(1, assignment.getDecisionLevel());
		assignment.choose(x21, FALSE);
		conflictCause = store.propagate();
		assertNull(conflictCause);
		assertEquals(2, assignment.getDecisionLevel());
		assignment.choose(x31, FALSE);
		conflictCause = store.propagate();
		assertNull(conflictCause);
		assertEquals(3, assignment.getDecisionLevel());
		assignment.choose(unrelated4, TRUE);
		conflictCause = store.propagate();
		assertNull(conflictCause);
		assertEquals(4, assignment.getDecisionLevel());
		assignment.choose(x1, FALSE);
		assertEquals(5, assignment.getDecisionLevel());
		conflictCause = store.propagate();
		assertEquals(FALSE, assignment.get(x2).getTruth());
		assertEquals(FALSE, assignment.get(x3).getTruth());
		assertEquals(TRUE, assignment.get(x4).getTruth());
		assertEquals(FALSE, assignment.get(x5).getTruth());
		assertEquals(FALSE, assignment.get(x6).getTruth());
		assertNotNull(conflictCause);
		final Antecedent antecedent = conflictCause.getAntecedent();
		assertEquals(ng6, antecedent.getOriginalNoGood());

		final ConflictAnalysisResult conflictAnalysisResult = learner.analyzeConflictingNoGoodAndGeneraliseConflict(antecedent);
		Set<Integer> expectedLearnedNoGood = new HashSet<>();
		expectedLearnedNoGood.add(atomToLiteral(x4, true));
		expectedLearnedNoGood.add(atomToLiteral(x21, false));
		assert conflictAnalysisResult.learnedNoGood != null;
		assertEquals(expectedLearnedNoGood, intArrayToLinkedHashSet(conflictAnalysisResult.learnedNoGood.asAntecedent().getReasonLiterals()));

		final List<NoGood> additionalLearnedNoGoods = conflictAnalysisResult.getAdditionalLearnedNoGoods();
		assertEquals(1, additionalLearnedNoGoods.size());
		expectedLearnedNoGood = new HashSet<>();
		expectedLearnedNoGood.add(atomToLiteral(x1, false));
		expectedLearnedNoGood.add(atomToLiteral(x21, false));
		expectedLearnedNoGood.add(atomToLiteral(x31, false));
		System.out.println(atomStore.noGoodToString(additionalLearnedNoGoods.get(0)));
		assertEquals(expectedLearnedNoGood, intArrayToLinkedHashSet(additionalLearnedNoGoods.get(0).asAntecedent().getReasonLiterals()));
	}

	/**
	 * Tests non-ground learning on the following example: <br/>
	 *     Nogood 1: { -(col(1,g), +(_R_("0", "{X->1,Col->g}") }, non-ground: { -(col(X,Col), +(_R_("0", (X,Col)) } <br/>
	 *     Nogood 2: { -(col(0,g)), +(col(1,g)) }, non-ground: { -(col(Y,C)), +(col(X,Col)), +(Y=X-1) } <br/>
	 *     Nogood 3: { +(col(1,g)), +(col(0,g)) }, non-ground: { +(col(N,C)), +(col(M,C)), +(M<N) } <br/>
	 * When nogood 3 is violated, it is first resolved with nogood 2, learning a nogood at the first UIP,
	 * and then with nogood 1, learning another nogood at the second UIP.
	 */
	@Test
	public void smallNonGroundExample() {
		final GroundConflictNoGoodLearner groundLearner = new GroundConflictNoGoodLearner(assignment, atomStore);
		final NonGroundConflictNoGoodLearner learner = new NonGroundConflictNoGoodLearner(assignment, atomStore, groundLearner);

		final Predicate predCol = Predicate.getInstance("col", 2);
		final Predicate predUnrelated = Predicate.getInstance("unrelated", 2);

		final ConstantTerm<Integer> const0 = ConstantTerm.getInstance(0);
		final ConstantTerm<Integer> const1 = ConstantTerm.getInstance(1);
		final ConstantTerm<String> constG = ConstantTerm.getInstance("g");

		final int groundAtomCol0g = atomStore.putIfAbsent(new BasicAtom(predCol, const0, constG));
		final int groundAtomCol1g = atomStore.putIfAbsent(new BasicAtom(predCol, const1, constG));
		final int groundAtomUnrelated = atomStore.putIfAbsent(new BasicAtom(predUnrelated, const1, constG));

		final VariableTerm varX = VariableTerm.getInstance("X");
		final VariableTerm varY = VariableTerm.getInstance("Y");
		final VariableTerm varC = VariableTerm.getInstance("C");
		final VariableTerm varM = VariableTerm.getInstance("M");
		final VariableTerm varN = VariableTerm.getInstance("N");
		final VariableTerm varCol = VariableTerm.getInstance("Col");

		final Atom nonGroundAtomColXCol = new BasicAtom(predCol, varX, varCol);
		final Atom nonGroundAtomColYCol = new BasicAtom(predCol, varY, varCol);
		final Atom nonGroundAtomColMC = new BasicAtom(predCol, varM, varC);
		final Atom nonGroundAtomColNC = new BasicAtom(predCol, varN, varC);
		final Atom unrelated = new BasicAtom(predUnrelated, varX, varCol);

		final Rule rule = new Rule(new DisjunctiveHead(singletonList(nonGroundAtomColXCol)), singletonList(unrelated.toLiteral()));
		final NonGroundRule nonGroundRule = NonGroundRule.constructNonGroundRule(rule);
		final Substitution substitution = new Substitution();
		substitution.put(varX, const1);
		substitution.put(varCol, constG);
		final int groundAtomR = atomStore.putIfAbsent(RuleAtom.ground(nonGroundRule, substitution));
		final RuleAtom nonGroundAtomR = RuleAtom.nonGround(nonGroundRule);

		final NoGood ng1 = NoGood.headFirst(atomToLiteral(groundAtomCol1g, false), atomToLiteral(groundAtomR));
		final Map<Integer, Atom> atomMapping1 = new HashMap<>();
		atomMapping1.put(groundAtomCol1g, nonGroundAtomColXCol);
		atomMapping1.put(groundAtomR, nonGroundAtomR);
		ng1.setNonGroundNoGood(NonGroundNoGood.forGroundNoGood(ng1, atomMapping1));

		final NoGood ng2 = NoGood.learnt(atomToLiteral(groundAtomCol0g, false), atomToLiteral(groundAtomCol1g));
		final Literal nonGroundComparisonLiteralYEqualsXMinus1 = new ComparisonAtom(varY, ArithmeticTerm.getInstance(varX, MINUS, const1), EQ).toLiteral();
		final NoGoodGenerator.CollectedLiterals positiveCollectedLiterals2 = new NoGoodGenerator.CollectedLiterals(
				asList(atomToLiteral(groundAtomCol0g, false), atomToLiteral(groundAtomCol1g)),
				asList(nonGroundAtomColYCol.toLiteral(false), nonGroundAtomColXCol.toLiteral()),
				singletonList(nonGroundComparisonLiteralYEqualsXMinus1),
				emptyList()
		);
		final NoGoodGenerator.CollectedLiterals negativeCollectedLiterals2 = new NoGoodGenerator.CollectedLiterals();
		ng2.setNonGroundNoGood(NonGroundNoGood.fromBody(ng2, positiveCollectedLiterals2, negativeCollectedLiterals2, positiveCollectedLiterals2.getAtomMapping()));

		final List<Integer> groundLiteralsInNoGood3 = asList(atomToLiteral(groundAtomCol1g), atomToLiteral(groundAtomCol0g));
		final NoGood ng3 = NoGood.fromConstraint(groundLiteralsInNoGood3, emptyList());
		final Literal nonGroundComparisonLiteralMLessThanN = new ComparisonAtom(varM, varN, LT).toLiteral();
		final NoGoodGenerator.CollectedLiterals positiveCollectedLiterals3 = new NoGoodGenerator.CollectedLiterals(
				groundLiteralsInNoGood3,
				asList(nonGroundAtomColNC.toLiteral(), nonGroundAtomColMC.toLiteral()),
				singletonList(nonGroundComparisonLiteralMLessThanN),
				emptyList()
		);
		final NoGoodGenerator.CollectedLiterals negativeCollectedLiterals3 = new NoGoodGenerator.CollectedLiterals();
		ng3.setNonGroundNoGood(NonGroundNoGood.fromBody(ng3, positiveCollectedLiterals3, negativeCollectedLiterals3, positiveCollectedLiterals3.getAtomMapping()));

		store.growForMaxAtomId(atomStore.getMaxAtomId());
		this.assignment.growForMaxAtomId();

		store.add(1, ng1);
		store.add(2, ng2);
		store.add(3, ng3);

		assertEquals(0, assignment.getDecisionLevel());
		ConflictCause conflictCause = assignment.choose(groundAtomUnrelated, TRUE);
		assertNull(conflictCause);
		conflictCause = store.propagate();
		assertNull(conflictCause);
		assertEquals(1, assignment.getDecisionLevel());
		conflictCause = assignment.choose(groundAtomR, TRUE);
		assertNull(conflictCause);
		conflictCause = store.propagate();
		assertEquals(2, assignment.getDecisionLevel());
		assertEquals(TRUE, assignment.get(groundAtomCol1g).getTruth());
		assertEquals(MBT, assignment.get(groundAtomCol0g).getTruth());

		assertNotNull(conflictCause);
		final Antecedent antecedent = conflictCause.getAntecedent();
		assertEquals(ng3, antecedent.getOriginalNoGood());

		final ConflictAnalysisResult conflictAnalysisResult = learner.analyzeConflictingNoGoodAndGeneraliseConflict(antecedent);
		Set<Integer> expectedLearnedNoGood1 = new LinkedHashSet<>();
		expectedLearnedNoGood1.add(atomToLiteral(groundAtomCol1g, true));
		assert conflictAnalysisResult.learnedNoGood != null;
		assertEquals(expectedLearnedNoGood1, intArrayToLinkedHashSet(conflictAnalysisResult.learnedNoGood.asAntecedent().getReasonLiterals()));

		final Literal nonGroundComparisonLiteralMEqualsNMinus1 = new ComparisonAtom(varM, ArithmeticTerm.getInstance(varN, MINUS, const1), EQ).toLiteral();

		final NoGoodGenerator.CollectedLiterals positiveCollectedLiteralsLearned1 = new NoGoodGenerator.CollectedLiterals(
				new ArrayList<>(expectedLearnedNoGood1),
				singletonList(nonGroundAtomColNC.toLiteral()),
				asList(nonGroundComparisonLiteralMLessThanN, nonGroundComparisonLiteralMEqualsNMinus1),
				emptyList()
		);
		final NoGoodGenerator.CollectedLiterals negativeCollectedLiteralsLearned1 = new NoGoodGenerator.CollectedLiterals();
		final NonGroundNoGood expectedLearnedNonGroundNoGood1 = NonGroundNoGood.fromBody(conflictAnalysisResult.learnedNoGood, positiveCollectedLiteralsLearned1, negativeCollectedLiteralsLearned1, positiveCollectedLiteralsLearned1.getAtomMapping());
		assertEquals(expectedLearnedNonGroundNoGood1, conflictAnalysisResult.getLearnedNonGroundNoGood());

		assertEquals(1, conflictAnalysisResult.getAdditionalLearnedNoGoods().size());
		assertEquals(1, conflictAnalysisResult.getAdditionalLearnedNonGroundNoGoods().size());

		Set<Integer> expectedLearnedNoGood2 = new LinkedHashSet<>();
		expectedLearnedNoGood2.add(atomToLiteral(groundAtomR, true));
		final NoGood learnedNoGood2 = conflictAnalysisResult.getAdditionalLearnedNoGoods().get(0);
		assertEquals(expectedLearnedNoGood2, intArrayToLinkedHashSet(learnedNoGood2.asAntecedent().getReasonLiterals()));

		final Unifier ruleUnifier = new Unifier();
		ruleUnifier.put(varX, varN);
		ruleUnifier.put(varCol, varC);
		final RuleAtom nonGroundAtomRenamedRule = nonGroundAtomR.substitute(ruleUnifier);

		final NoGoodGenerator.CollectedLiterals positiveCollectedLiteralsLearned2 = new NoGoodGenerator.CollectedLiterals(
				new ArrayList<>(expectedLearnedNoGood2),
				singletonList(nonGroundAtomRenamedRule.toLiteral()),
				asList(nonGroundComparisonLiteralMLessThanN, nonGroundComparisonLiteralMEqualsNMinus1),
				emptyList()
		);
		final NoGoodGenerator.CollectedLiterals negativeCollectedLiteralsLearned2 = new NoGoodGenerator.CollectedLiterals();
		final NonGroundNoGood expectedLearnedNonGroundNoGood2 = NonGroundNoGood.fromBody(learnedNoGood2, positiveCollectedLiteralsLearned2, negativeCollectedLiteralsLearned2, positiveCollectedLiteralsLearned2.getAtomMapping());
		assertEquals(expectedLearnedNonGroundNoGood2, conflictAnalysisResult.getAdditionalLearnedNonGroundNoGoods().get(0));
	}
}