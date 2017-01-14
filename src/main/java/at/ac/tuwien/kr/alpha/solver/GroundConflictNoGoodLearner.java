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
package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.ReadableAssignment;
import at.ac.tuwien.kr.alpha.common.ResolutionSequence;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class GroundConflictNoGoodLearner {
	private static final Logger LOGGER = LoggerFactory.getLogger(GroundConflictNoGoodLearner.class);

	private final Assignment assignment;

	public static class ConflictAnalysisResult {
		public final NoGood learnedNoGood;
		public final int backjumpLevel;
		public final boolean clearLastGuessAfterBackjump;
		public final Set<NoGood> noGoodsResponsibleForConflict;
		public final boolean isUnsatisfiable;

		public ConflictAnalysisResult(NoGood learnedNoGood, int backjumpLevel, boolean clearLastGuessAfterBackjump, Set<NoGood> noGoodsResponsibleForConflict, boolean isUnsatisfiable) {
			if (backjumpLevel < 0) {
				throw new IllegalArgumentException("Backjumping level must be at least 0.");
			}

			this.learnedNoGood = learnedNoGood;
			this.backjumpLevel = backjumpLevel;
			this.clearLastGuessAfterBackjump = clearLastGuessAfterBackjump;
			this.noGoodsResponsibleForConflict = noGoodsResponsibleForConflict;
			this.isUnsatisfiable = isUnsatisfiable;
		}
	}

	public GroundConflictNoGoodLearner(Assignment assignment) {
		this.assignment = assignment;
	}

	public ConflictAnalysisResult analyzeConflictingNoGood(NoGood violatedNoGood) {
		return analyzeConflictingNoGoodRepetition(violatedNoGood, new HashSet<>());
	}

	private ConflictAnalysisResult analyzeConflictingNoGoodRepetition(NoGood violatedNoGood, Set<NoGood> noGoodsResponsible) {
		noGoodsResponsible.add(violatedNoGood);
		NoGood currentResolutionNoGood = new NoGood(violatedNoGood.getLiteralsClone());	// Clone violated NoGood and remove potential head.
		// Find decision level where conflict occurs (i.e., highest decision level of violatedNoGood).
		int conflictDecisionLevel = -1;
		for (Integer literal : currentResolutionNoGood) {
			int literalDL = assignment.get(atomOf(literal)).getDecisionLevel();
			if (literalDL > conflictDecisionLevel) {
				conflictDecisionLevel = literalDL;
			}
		}
		if (conflictDecisionLevel == 0) {
			// The given set of NoGoods is unsatisfiable (conflict at decisionLevel 0).
			return new ConflictAnalysisResult(null, 0, false, null, true);
		}
		FirstUIPPriorityQueue firstUIPPriorityQueue = new FirstUIPPriorityQueue(conflictDecisionLevel);
		for (Integer literal : currentResolutionNoGood) {
			firstUIPPriorityQueue.add(assignment.get(atomOf(literal)));
			//sortLiteralToProcessIntoList(sortedLiteralsToProcess, literal, conflictDecisionLevel);
		}
		// TODO: create ResolutionSequence
		if (firstUIPPriorityQueue.size() == 1) {
			// There is only one literal to process, i.e., only one literal in the violatedNoGood is from conflict decision level.
			// This means that the NoGood already was unit but got violated, because another NoGood propagated earlier or a wrong guess was made.
			// The real conflict therefore is caused by either:
			// a) two NoGoods propagating the same atom to different truth values in the current decisionLevel, or
			// b) a NoGood propagating at a lower decision level to the inverse value of a guess with higher decision level.
			// For a) we need to work also with the other NoGood.
			// For b) we need to backtrack the wrong guess.

			ReadableAssignment.Entry atomAssignmentEntry = firstUIPPriorityQueue.poll();
			NoGood otherContributingNoGood = atomAssignmentEntry.getImpliedBy();
			if (otherContributingNoGood == null) {
				// Case b), the other assignment is a decision.
				return new ConflictAnalysisResult(null, atomAssignmentEntry.getDecisionLevel(), true, noGoodsResponsible, false);
			}
			// Case a) take other implying NoGood into account.
			currentResolutionNoGood = new NoGood(
				resolveNoGoods(firstUIPPriorityQueue, currentResolutionNoGood, otherContributingNoGood, atomAssignmentEntry),
				-1);
			noGoodsResponsible.add(otherContributingNoGood);

			// TODO: create/edit ResolutionSequence
		}

		while (true) {
			// Check if 1UIP was reached.
			if (firstUIPPriorityQueue.size() == 1) {
				// Only one remaining literals to process, we reached 1UIP.
				return new ConflictAnalysisResult(currentResolutionNoGood, computeBackjumpingDecisionLevel(currentResolutionNoGood), false, noGoodsResponsible, false);
			} else if (firstUIPPriorityQueue.size() < 1) {
				// This can happen if some NoGood implied a literal at a higher decision level and later the implying literals become (re-)assigned at lower decision levels.
				// Lowering the decision level may be possible but requires further analysis.
				// For the moment, just report the learned NoGood.
				return repeatAnalysisIfNotAssigning(currentResolutionNoGood, noGoodsResponsible);
				//return new ConflictAnalysisResult(currentResolutionNoGood, computeBackjumpingDecisionLevel(currentResolutionNoGood), false, noGoodsResponsible);
			}

			// Resolve next NoGood based on current literal
			ReadableAssignment.Entry currentLiteralAssignment = firstUIPPriorityQueue.poll();
			// Get NoGood it was implied by.
			NoGood impliedByNoGood = currentLiteralAssignment.getImpliedBy();
			if (impliedByNoGood == null) {
				// Literal was a decision, keep it in the currentResolutionNoGood by simply skipping.
				continue;
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("ImpliedBy NoGood is: {}.", impliedByNoGood);
				for (Integer literal : impliedByNoGood) {
					LOGGER.debug("Literal assignment: {}={}.", atomOf(literal), assignment.get(atomOf(literal)));
				}
			}
			// TODO: add entry in ResolutionSequence.

			currentResolutionNoGood = new NoGood(resolveNoGoods(firstUIPPriorityQueue, currentResolutionNoGood, impliedByNoGood, currentLiteralAssignment));
			noGoodsResponsible.add(impliedByNoGood);
		}
	}

	private ConflictAnalysisResult repeatAnalysisIfNotAssigning(NoGood learnedNoGood, Set<NoGood> noGoodsResponsibleForConflict) {
		int backjumpingDecisionLevel = computeBackjumpingDecisionLevel(learnedNoGood);
		if (backjumpingDecisionLevel < 0) {
			// The learned NoGood is not yet assigning, repeat the learning on the now-highest decision level.
			// This can only be the case if a literal got assigned at a lower decision level, otherwise the learnedNoGood is always assigning.
			return analyzeConflictingNoGoodRepetition(learnedNoGood, noGoodsResponsibleForConflict);
		}
		return new ConflictAnalysisResult(learnedNoGood, backjumpingDecisionLevel, false, noGoodsResponsibleForConflict, false);
	}

	/**
	 * Resolves two NoGoods and returns the new NoGood as array of literals.
	 * Literals of the second NoGood are sorted into the firstUIPPriorityQueue list.
	 * Literals of the first NoGood are assumed to be already in that queue and not added to it.
	 * @param firstUIPPriorityQueue
	 * @param firstNoGood
	 * @param secondNoGood
	 * @param resolutionLiteral
	 * @return
	 */
	private int[] resolveNoGoods(FirstUIPPriorityQueue firstUIPPriorityQueue, NoGood firstNoGood, NoGood secondNoGood, ReadableAssignment.Entry resolutionLiteral) {
		// Resolve implied nogood into current resolution.
		int resolvedLiterals[] = new int[secondNoGood.size() + firstNoGood.size() - 2];
		int resolvedCounter = 0;
		// Copy over all literals except the resolving ones.
		for (int i = 0; i < firstNoGood.size(); i++) {
			if (firstNoGood.getAtom(i) != resolutionLiteral.getAtom()) {
				resolvedLiterals[resolvedCounter++] = firstNoGood.getLiteral(i);
			}
		}
		// Copy literals from implying nogood except the resolving one and sort additional literals into processing list.
		for (int i = 0; i < secondNoGood.size(); i++) {
			if (secondNoGood.getAtom(i) != resolutionLiteral.getAtom()) {
				resolvedLiterals[resolvedCounter++] = secondNoGood.getLiteral(i);

				// Sort literal also into queue for further processing.
				ReadableAssignment.Entry newLiteral = assignment.get(atomOf(secondNoGood.getLiteral(i)));
				// Check for special case where literal was assigned from MBT to TRUE on the same decisionLevel and the propagationLevel of TRUE is higher than the one of the resolutionLiteral.
				if (newLiteral.getDecisionLevel() == resolutionLiteral.getDecisionLevel()
					&& newLiteral.getPropagationLevel() > resolutionLiteral.getPropagationLevel()) {
					if (TRUE.equals(newLiteral.getTruth()) && newLiteral.getPrevious() != null
						&& MBT.equals(newLiteral.getPrevious().getTruth())
						&& newLiteral.getPrevious().getDecisionLevel() == resolutionLiteral.getDecisionLevel()
						&& newLiteral.getPrevious().getPropagationLevel() < resolutionLiteral.getPropagationLevel()) {
						// Resort to the previous entry (i.e., the one for MBT) and use that one.
						firstUIPPriorityQueue.add(newLiteral.getPrevious());
						continue;
					}
					throw new RuntimeException("Implying literal on current decisionLevel has higher propagationLevel than the implied literal and this was no assignment from MBT to TRUE. Should not happen.");
				}
				// Add literal to queue for finding 1UIP.
				firstUIPPriorityQueue.add(newLiteral);
			}
		}
		return resolvedLiterals;
	}

	public ResolutionSequence obtainResolutionSequence() {
		throw new NotImplementedException("Method not yet implemented.");
	}

	/**
	 * Compute the backjumping decision level, i.e., the decision level on which the learned NoGood is assigning (NoGood is unit and propagates).
	 * This usually is the second highest decision level occurring in the learned NoGood, but due to assignments of MBT no such decision level may exist.
	 * @param learnedNoGood
	 * @return -1 if there is no decisionLevel such that backjumping to it makes the learnedNoGood unit.
	 */
	private int computeBackjumpingDecisionLevel(NoGood learnedNoGood) {
		int highestDecisionLevel = -1;
		int secondHighestDecisionLevel = -1;
		int numLiteralsOfHighestDecisionLevel = -1;
		if (learnedNoGood.size() == 1) {
			// Singleton NoGoods induce a backjump to the decision level before the NoGood got violated.
			int singleLiteralDecisionLevel = assignment.get(learnedNoGood.getLiteral(0)).getDecisionLevel();
			return singleLiteralDecisionLevel - 1 >= 0 ? singleLiteralDecisionLevel - 1 : 0;
		}
		for (Integer integer : learnedNoGood) {
			ReadableAssignment.Entry assignmentEntry = assignment.get(atomOf(integer));
			int atomDecisionLevel = assignmentEntry.getDecisionLevel();
			if (TRUE.equals(assignmentEntry.getTruth()) && assignmentEntry.getPrevious() != null) {
				// Literal is assigned TRUE and was MBT before, it gets unassigned only if the MBT assignment is backtracked.
				atomDecisionLevel = assignmentEntry.getPrevious().getDecisionLevel();
			}
			if (atomDecisionLevel == highestDecisionLevel) {
				numLiteralsOfHighestDecisionLevel++;
			}
			if (atomDecisionLevel > highestDecisionLevel) {
				secondHighestDecisionLevel = highestDecisionLevel;
				highestDecisionLevel = atomDecisionLevel;
				numLiteralsOfHighestDecisionLevel = 1;
			} else {
				if (atomDecisionLevel < highestDecisionLevel && atomDecisionLevel > secondHighestDecisionLevel) {
					secondHighestDecisionLevel = atomDecisionLevel;
				}
			}
		}
		if (numLiteralsOfHighestDecisionLevel != 1) {
			return -1;
		}
		return secondHighestDecisionLevel;
	}
}
