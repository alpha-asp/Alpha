package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Literals;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.ResolutionSequence;
import org.apache.commons.lang3.NotImplementedException;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class GroundConflictNoGoodLearner {

	private final Assignment assignment;
	private final NoGoodStore<ThriceTruth> noGoodStore;
	private NoGood learnedNoGood;

	public GroundConflictNoGoodLearner(Assignment assignment, NoGoodStore<ThriceTruth> noGoodStore) {
		this.assignment = assignment;
		this.noGoodStore = noGoodStore;
	}

	public void analyzeConflict(NoGood violatedNoGood) {
		learnedNoGood = null;
		NoGood currentResolutionNoGood = new NoGood(violatedNoGood.getLiteralsClone(), -1);	// Clone violated NoGood and remove potential head.
		LinkedList<Integer> sortedLiteralsToProcess = new LinkedList<>();
		for (Integer literal : currentResolutionNoGood) {
			sortLiteralToProcessIntoList(sortedLiteralsToProcess, literal);
		}
		// TODO: create ResolutionSequence
		if (sortedLiteralsToProcess.size() == 1) {
			// There is only one literal to process, i.e., only one literal in the violatedNoGood is from current decisionLevel.
			// This means that the NoGood already was unit but got violated, because another NoGood propagated earlier.
			// The real conflict therefore is caused by:
			// two NoGoods both propagating the same atom to different truth values in the current decisionLevel.
			// We thus need to work also with the other NoGood.

			Integer singleAtomInCurrentDL = sortedLiteralsToProcess.pollFirst();
			NoGood otherContributingNoGood = assignment.get(singleAtomInCurrentDL).getImpliedBy();
			currentResolutionNoGood = new NoGood(
				resolveNoGoods(sortedLiteralsToProcess,	currentResolutionNoGood, otherContributingNoGood, singleAtomInCurrentDL),
				-1);

			// TODO: create/edit ResolutionSequence
		}

		while (true) {
			// Check if 1UIP was reached.
			if (sortedLiteralsToProcess.size() == 1) {
				// Only one remaining literals to process, we reached 1UIP.
				learnedNoGood = currentResolutionNoGood;
				return;
			}

			// Resolve next NoGood based on current literal
			Integer literal = sortedLiteralsToProcess.pollFirst();
			// Get NoGood it was implied by.
			NoGood impliedByNoGood = assignment.get(Literals.atomOf(literal)).getImpliedBy();
			if (impliedByNoGood == null) {
				// Literal was a decision, keep it in the currentResolutionNoGood by simply skipping.
				continue;
			}
			// TODO: add entry in ResolutionSequence.

			currentResolutionNoGood = new NoGood(resolveNoGoods(sortedLiteralsToProcess, currentResolutionNoGood, impliedByNoGood, literal), -1);
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
	private int[] resolveNoGoods(LinkedList<Integer> sortedLiteralsToProcess, NoGood firstNoGood, NoGood secondNoGood, Integer resolutionLiteral) {
		// Resolve implied nogood into current resolution.
		int resolvedLiterals[] = new int[secondNoGood.size() + firstNoGood.size() - 2];
		int resolvedCounter = 0;
		// Copy over all literals except the resolving ones.
		for (int i = 0; i < firstNoGood.size(); i++) {
			if (firstNoGood.getAtom(i) != Literals.atomOf(resolutionLiteral)) {
				resolvedLiterals[resolvedCounter++] = firstNoGood.getAtom(i);
			}
		}
		// Copy literals from implying nogood except the resolving one and sort additional literals into processing list.
		for (int i = 0; i < secondNoGood.size(); i++) {
			if (secondNoGood.getAtom(i) != Literals.atomOf(resolutionLiteral)) {
				resolvedLiterals[resolvedCounter++] = secondNoGood.getAtom(i);
				// Sort literal also into list for further processing.
				sortLiteralToProcessIntoList(sortedLiteralsToProcess, secondNoGood.getAtom(i));
			}
		}
		return resolvedLiterals;
	}

	private void sortLiteralToProcessIntoList(LinkedList<Integer> sortedLiteralsToProcess, Integer literal) {
		Assignment.Entry assignedEntry = assignment.get(Literals.atomOf(literal));
		int literalDecisionLevel = assignedEntry.getDecisionLevel();

		if (literalDecisionLevel < assignment.getDecisionLevel()) {
			// Do not follow/sort into assignments from lower decision levels.
			return;
		}

		// Find spot to insert the literal
		int literalPropagationLevel = assignedEntry.getPropagationLevel();
		ListIterator<Integer> sortedListIterator = sortedLiteralsToProcess.listIterator();
		while (sortedListIterator.hasNext()) {
			Integer currentLiteral = sortedListIterator.next();
			if (assignment.get(Literals.atomOf(currentLiteral)).getPropagationLevel() >= literalPropagationLevel) {
				sortedListIterator.add(literal);
				return;
			}
		}
		sortedListIterator.add(literal);
	}


	public NoGood obtainLearnedNoGood() {
		return learnedNoGood;
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
		int secondHighestDecisionLevel = -1;
		int currentDecisionLevel = assignment.getDecisionLevel();
		for (Integer integer : learnedNoGood) {
			Assignment.Entry assignmentEntry = assignment.get(Literals.atomOf(integer));
			int atomDecisionLevel = assignmentEntry.getDecisionLevel();
			if (atomDecisionLevel < currentDecisionLevel && atomDecisionLevel > secondHighestDecisionLevel) {
				secondHighestDecisionLevel = atomDecisionLevel;
			}
		}
		if (secondHighestDecisionLevel == -1) {
			throw new RuntimeException("Given NoGood has no second highest decision level. NoGood: " + learnedNoGood);
		}
		return secondHighestDecisionLevel;
	}
}
