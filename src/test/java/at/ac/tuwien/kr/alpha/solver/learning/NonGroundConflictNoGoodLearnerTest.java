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
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.solver.Antecedent;
import at.ac.tuwien.kr.alpha.solver.ConflictCause;
import at.ac.tuwien.kr.alpha.solver.NoGoodStore;
import at.ac.tuwien.kr.alpha.solver.NoGoodStoreAlphaRoaming;
import at.ac.tuwien.kr.alpha.solver.TrailAssignment;
import at.ac.tuwien.kr.alpha.solver.WritableAssignment;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Util.intArrayToLinkedHashSet;
import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;
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
	public void exampleFromSatisfiabilityHandbook() {
		final NonGroundConflictNoGoodLearner learner = new NonGroundConflictNoGoodLearner(assignment, atomStore);
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
}