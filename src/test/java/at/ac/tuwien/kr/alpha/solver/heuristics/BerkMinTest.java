/**
 * Copyright (c) 2016-2017 Siemens AG
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
package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.ArrayAssignment;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.learning.GroundConflictNoGoodLearner.ConflictAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link BerkMin}.
 * 
 * Copyright (c) 2016 Siemens AG
 *
 */
public class BerkMinTest {
	
	/**
	 * The tolerable epsilon for double comparisons
	 */
	private static final double EPSILON = 0.000001;
	
	private BerkMin berkmin;
	
	@BeforeEach
	public void setUp() {
		Assignment assignment = new ArrayAssignment();
		assignment.growForMaxAtomId(2);
		this.berkmin = new BerkMin(assignment, new PseudoChoiceManager(assignment), new Random());
	}
	
	@Test
	void countPositiveLiteralsOnce() {
		NoGood violatedNoGood = new NoGood(1, 2);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		assertEquals(1, berkmin.getActivity(1), EPSILON);
		assertEquals(1, berkmin.getActivity(2), EPSILON);
	}
	
	@Test
	void countNegativeLiteralsOnce() {
		NoGood violatedNoGood = new NoGood(-1, -2);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		assertEquals(1, berkmin.getActivity(-1), EPSILON);
		assertEquals(1, berkmin.getActivity(-2), EPSILON);
	}
	
	@Test
	void countPositiveLiteralsTwice() {
		NoGood violatedNoGood = new NoGood(1, 2);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		assertEquals(2, berkmin.getActivity(1), EPSILON);
		assertEquals(2, berkmin.getActivity(2), EPSILON);
	}
	
	@Test
	void countNegativeLiteralsTwice() {
		NoGood violatedNoGood = new NoGood(-1, -2);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		assertEquals(2, berkmin.getActivity(-1), EPSILON);
		assertEquals(2, berkmin.getActivity(-2), EPSILON);
	}
	
	@Test
	void countMixedLiteralsTwice() {
		NoGood violatedNoGood = new NoGood(1, -2);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		assertEquals(2, berkmin.getActivity(1), EPSILON);
		assertEquals(2, berkmin.getActivity(-2), EPSILON);
	}
	
	@Test
	void countPositiveLiteralsThenNegativeLiterals() {
		NoGood violatedNoGood = new NoGood(1, 2);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		violatedNoGood = new NoGood(-1, -2);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		assertEquals(2, berkmin.getActivity(1), EPSILON);
		assertEquals(2, berkmin.getActivity(2), EPSILON);
	}
	
	@Test
	void reachDecayAgeOnce() {
		berkmin.setDecayAge(3);
		berkmin.setDecayFactor(1.0 / 3);
		NoGood violatedNoGood = new NoGood(1, 2);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		assertEquals(1, berkmin.getActivity(1), EPSILON);
		assertEquals(1, berkmin.getActivity(2), EPSILON);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		assertEquals(2, berkmin.getActivity(1), EPSILON);
		assertEquals(2, berkmin.getActivity(2), EPSILON);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		assertEquals(1, berkmin.getActivity(1), EPSILON);
		assertEquals(1, berkmin.getActivity(2), EPSILON);
	}
	
	@Test
	void reachDecayAgeTwice() {
		berkmin.setDecayAge(2);
		berkmin.setDecayFactor(0.75);
		NoGood violatedNoGood = new NoGood(1, 2);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		assertEquals(1, berkmin.getActivity(1), EPSILON);
		assertEquals(1, berkmin.getActivity(2), EPSILON);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		assertEquals(1.5, berkmin.getActivity(1), EPSILON);
		assertEquals(1.5, berkmin.getActivity(2), EPSILON);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		assertEquals(2.5, berkmin.getActivity(1), EPSILON);
		assertEquals(2.5, berkmin.getActivity(2), EPSILON);
		berkmin.violatedNoGood(violatedNoGood);
		berkmin.analyzedConflict(pseudo(violatedNoGood));
		assertEquals(2.625, berkmin.getActivity(1), EPSILON);
		assertEquals(2.625, berkmin.getActivity(2), EPSILON);
	}
	
	@Test
	void learnNoGood() {
		NoGood learnedNoGood = new NoGood(1, 2);
		int backjumpLevel = 1;
		boolean clearLastGuessAfterBackjump = true;
		Set<NoGood> noGoodsResponsibleForConflict = Collections.emptySet();
		berkmin.analyzedConflict(new ConflictAnalysisResult(learnedNoGood, backjumpLevel, clearLastGuessAfterBackjump,
				noGoodsResponsibleForConflict, false));
		assertEquals(learnedNoGood, berkmin.getCurrentTopClause());
	}

	private static ConflictAnalysisResult pseudo(NoGood noGood) {
		return new ConflictAnalysisResult(null, 0, false, Collections.singleton(noGood), false);
	}

	private static class PseudoChoiceManager extends ChoiceManager {

		public PseudoChoiceManager(Assignment assignment) {
			super(assignment);
		}

		@Override
		public boolean isAtomChoice(int atom) {
			return true;
		}

		@Override
		public boolean isActiveChoiceAtom(int atom) {
			return true;
		}
	}
}
