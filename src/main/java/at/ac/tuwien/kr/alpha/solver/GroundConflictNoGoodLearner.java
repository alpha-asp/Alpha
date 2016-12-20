package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.ResolutionSequence;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class GroundConflictNoGoodLearner {

	private final Assignment assignment;
	private final NoGoodStore<ThriceTruth> noGoodStore;

	class ConflictAnalysisResult {
		public NoGood learnedNoGood;
		public int backjumpLevel;
		public boolean clearLastGuessAfterBackjump;
		public Set<NoGood> noGoodsResponsibleForConflict;

		public ConflictAnalysisResult(NoGood learnedNoGood, int backjumpLevel, boolean clearLastGuessAfterBackjump, Set<NoGood> noGoodsResponsibleForConflict) {
			this.learnedNoGood = learnedNoGood;
			this.backjumpLevel = backjumpLevel;
			this.clearLastGuessAfterBackjump = clearLastGuessAfterBackjump;
			this.noGoodsResponsibleForConflict = noGoodsResponsibleForConflict;
		}
	}

	public GroundConflictNoGoodLearner(Assignment assignment, NoGoodStore<ThriceTruth> noGoodStore) {
		this.assignment = assignment;
		this.noGoodStore = noGoodStore;
	}

	public ConflictAnalysisResult analyzeConflictingNoGood(NoGood violatedNoGood) {
		NoGood learnedNoGood;
		Set<NoGood> noGoodsResponsible = new HashSet<>();
		noGoodsResponsible.add(violatedNoGood);
		NoGood currentResolutionNoGood = new NoGood(violatedNoGood.getLiteralsClone(), -1);	// Clone violated NoGood and remove potential head.
		LinkedList<Integer> sortedLiteralsToProcess = new LinkedList<>();
		// Find decision level where conflict occurs (i.e., highest decision level of violatedNoGood).
		int conflictDecisionLevel = -1;
		for (Integer literal : currentResolutionNoGood) {
			int literalDL = assignment.get(atomOf(literal)).getDecisionLevel();
			if (literalDL > conflictDecisionLevel) {
				conflictDecisionLevel = literalDL;
			}
		}
		for (Integer literal : currentResolutionNoGood) {
			sortLiteralToProcessIntoList(sortedLiteralsToProcess, literal, conflictDecisionLevel);
		}
		// TODO: create ResolutionSequence
		if (sortedLiteralsToProcess.size() == 1) {
			// There is only one literal to process, i.e., only one literal in the violatedNoGood is from conflict decision level.
			// This means that the NoGood already was unit but got violated, because another NoGood propagated earlier or a wrong guess was made.
			// The real conflict therefore is caused by either:
			// a) two NoGoods propagating the same atom to different truth values in the current decisionLevel, or
			// b) a NoGood propagating at a lower decision level to the inverse value of a guess with higher decision level.
			// For a) we need to work also with the other NoGood.
			// For b) we need to backtrack the wrong guess.

			Integer singleLiteralInCurrentDL = sortedLiteralsToProcess.pollFirst();
			Assignment.Entry atomAssignementEntry = assignment.get(atomOf(singleLiteralInCurrentDL));
			NoGood otherContributingNoGood = atomAssignementEntry.getImpliedBy();
			if (otherContributingNoGood == null) {
				// Case b), the other assignment is a decision.
				return new ConflictAnalysisResult(null, atomAssignementEntry.getDecisionLevel(), true, noGoodsResponsible);
			}
			// Case a) take other implying NoGood into account.
			currentResolutionNoGood = new NoGood(
				resolveNoGoods(sortedLiteralsToProcess,	currentResolutionNoGood, otherContributingNoGood, singleLiteralInCurrentDL, conflictDecisionLevel),
				-1);
			noGoodsResponsible.add(otherContributingNoGood);

			// TODO: create/edit ResolutionSequence
		}

		while (true) {
			// Check if 1UIP was reached.
			if (sortedLiteralsToProcess.size() == 1) {
				// Only one remaining literals to process, we reached 1UIP.
				learnedNoGood = currentResolutionNoGood;
				return new ConflictAnalysisResult(learnedNoGood, computeBackjumpingDecisionLevel(learnedNoGood), false, noGoodsResponsible);
			}

			// Resolve next NoGood based on current literal
			Integer literal = sortedLiteralsToProcess.pollFirst();
			// Get NoGood it was implied by.
			NoGood impliedByNoGood = assignment.get(atomOf(literal)).getImpliedBy();
			if (impliedByNoGood == null) {
				// Literal was a decision, keep it in the currentResolutionNoGood by simply skipping.
				continue;
			}
			// TODO: add entry in ResolutionSequence.

			currentResolutionNoGood = new NoGood(resolveNoGoods(sortedLiteralsToProcess, currentResolutionNoGood, impliedByNoGood, literal, conflictDecisionLevel), -1);
			noGoodsResponsible.add(impliedByNoGood);
		}
	}

	/**
	 * Resolves two NoGoods and returns the new NoGood as array of literals.
	 * Literals of the second NoGood are sorted into the sortedLiteralsToProcess list.
	 * Literals of the first NoGood are assumed to be already in that list and not added to it.
	 * @param sortedLiteralsToProcess
	 * @param firstNoGood
	 * @param secondNoGood
	 * @param resolutionLiteral
	 * @return
	 */
	private int[] resolveNoGoods(LinkedList<Integer> sortedLiteralsToProcess, NoGood firstNoGood, NoGood secondNoGood, Integer resolutionLiteral, int conflictDecisionLevel) {
		// Resolve implied nogood into current resolution.
		int resolvedLiterals[] = new int[secondNoGood.size() + firstNoGood.size() - 2];
		int resolvedCounter = 0;
		// Copy over all literals except the resolving ones.
		for (int i = 0; i < firstNoGood.size(); i++) {
			if (firstNoGood.getAtom(i) != atomOf(resolutionLiteral)) {
				resolvedLiterals[resolvedCounter++] = firstNoGood.getLiteral(i);
			}
		}
		// Copy literals from implying nogood except the resolving one and sort additional literals into processing list.
		for (int i = 0; i < secondNoGood.size(); i++) {
			if (secondNoGood.getAtom(i) != atomOf(resolutionLiteral)) {
				resolvedLiterals[resolvedCounter++] = secondNoGood.getLiteral(i);
				// Sort literal also into list for further processing.
				sortLiteralToProcessIntoList(sortedLiteralsToProcess, secondNoGood.getLiteral(i), conflictDecisionLevel);
			}
		}
		return resolvedLiterals;
	}

	private void sortLiteralToProcessIntoList(LinkedList<Integer> sortedLiteralsToProcess, Integer literal, int conflictDecisionLevel) {
		Assignment.Entry assignedEntry = assignment.get(atomOf(literal));
		int literalDecisionLevel = assignedEntry.getDecisionLevel();

		if (literalDecisionLevel < conflictDecisionLevel) {
			// Do not follow/sort into assignments from lower decision levels.
			return;
		}

		// Find spot to insert the literal
		int literalPropagationLevel = assignedEntry.getPropagationLevel();
		ListIterator<Integer> sortedListIterator = sortedLiteralsToProcess.listIterator();
		while (sortedListIterator.hasNext()) {
			Integer currentLiteral = sortedListIterator.next();
			if (assignment.get(atomOf(currentLiteral)).getPropagationLevel() >= literalPropagationLevel) {
				// Do not add literal twice.
				if (currentLiteral.equals(literal)) {
					return;
				}
				sortedListIterator.add(literal);
				return;
			}
		}
		sortedListIterator.add(literal);
	}

	public ResolutionSequence obtainResolutionSequence() {
		throw new NotImplementedException("Method not yet implemented.");
	}

	/**
	 * Compute the second highest decision level of any literal occurring in the learned nogood.
	 * @param learnedNoGood
	 * @return
	 */
	public int computeBackjumpingDecisionLevel(NoGood learnedNoGood) {
		int highestDecisionLevel = -1;
		int secondHighestDecisionLevel = -1;
		for (Integer integer : learnedNoGood) {
			Assignment.Entry assignmentEntry = assignment.get(atomOf(integer));
			int atomDecisionLevel = assignmentEntry.getDecisionLevel();
			if (atomDecisionLevel > highestDecisionLevel) {
				secondHighestDecisionLevel = highestDecisionLevel;
				highestDecisionLevel = atomDecisionLevel;
			} else {
				if (atomDecisionLevel < highestDecisionLevel && atomDecisionLevel > secondHighestDecisionLevel) {
					secondHighestDecisionLevel = atomDecisionLevel;
				}
			}
		}
		if (secondHighestDecisionLevel == -1) {
			throw new RuntimeException("Given NoGood has no second highest decision level. NoGood: " + learnedNoGood);
		}
		return secondHighestDecisionLevel;
	}
}
