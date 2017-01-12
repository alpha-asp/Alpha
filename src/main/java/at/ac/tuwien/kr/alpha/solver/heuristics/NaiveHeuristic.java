/**
 * Copyright (c) 2016-2017, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
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

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.Assignment;
import at.ac.tuwien.kr.alpha.solver.GroundConflictNoGoodLearner.ConflictAnalysisResult;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

import java.util.Collection;
import java.util.Map;

import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;

/**
 * The default heuristic that had been used by {@link at.ac.tuwien.kr.alpha.solver.DefaultSolver} before {@link BerkMin} was implemented.
 *
 */
public class NaiveHeuristic implements BranchingHeuristic {
	private Assignment<ThriceTruth> assignment;
	private Map<Integer, Integer> choiceOn;
	private Map<Integer, Integer> choiceOff;

	public NaiveHeuristic(Assignment<ThriceTruth> assignment, Map<Integer, Integer> choiceOn, Map<Integer, Integer> choiceOff) {
		this.assignment = assignment;
		this.choiceOn = choiceOn;
		this.choiceOff = choiceOff;
	}

	@Override
	public void violatedNoGood(NoGood violatedNoGood) {
	}

	@Override
	public void analyzedConflict(ConflictAnalysisResult analysisResult) {
	}

	@Override
	public void newNoGood(NoGood newNoGood) {
	}

	@Override
	public void newNoGoods(Collection<NoGood> newNoGoods) {
	}

	@Override
	public double getActivity(int literal) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int chooseAtom() {
		// Check if there is an enabled choice that is not also disabled
		// HINT: tracking changes of ChoiceOn, ChoiceOff directly could
		// increase performance (analyze store.getChangedAssignments()).

		// Check if there is an enabled choice that is not also disabled
		for (Map.Entry<Integer, Integer> e : choiceOn.entrySet()) {
			final int atom = e.getKey();

			ThriceTruth truth = assignment.getTruth(atom);

			// Only consider unassigned choices or choices currently MBT (and changing to TRUE following the guess)
			if (truth != null && !MBT.equals(truth)) {
				continue;
			}

			// Check that candidate is not disabled already
			truth = assignment.getTruth(choiceOff.getOrDefault(atom, 0));
			if (truth == null || FALSE.equals(truth)) {
				return atom;
			}
		}
		return 0;
	}

	@Override
	public boolean chooseSign(int atom) {
		return true;
	}
}
