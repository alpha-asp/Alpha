package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.GlobalSettings;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Consumer;

/**
 * The new default solver employed in Alpha.
 * Copyright (c) 2016, the Alpha Team.
 */
public class DefaultSolver extends AbstractSolver {
	public DefaultSolver(Grounder grounder) {
		super(grounder, p -> true);
	}

	private NoGoodStoreInterface noGoodStore;

	private boolean doInit = true;
	private boolean didChange;
	private int decisionLevel;


	public AnswerSet computeNextAnswerSet() {
		// Get basic rules and facts from grounder
		if (doInit) {
			noGoodStore.init();
			obtainNoGoodsFromGrounder();
			doInit = false;
		} else {
			// We already found one Answer-Set and are requested to find another one
			doBacktrack();
			if (exhaustedSearchSpace()) {
				return null;
			}
		}
		// Try all assignments until grounder reports no more NoGoods and all of them are satisfied
		while (true) {
			if (!propagationFixpointReached()) {
				updateGrounderAssignments();	// After a choice, it would be more efficient to propagate first and only then ask the grounder.
				obtainNoGoodsFromGrounder();
				if (noGoodStore.doPropagation()) {
					didChange = true;
				}
			} else if (noGoodStore.isNoGoodViolated()) {
				if (GlobalSettings.DEBUG_OUTPUTS) {
					System.out.println("Backtracking from wrong choices:");
					System.out.println(reportChoiceStack());
				}
				doBacktrack();
				if (exhaustedSearchSpace()) {
					return null;
				}
			} else if (choicesLeft()) {
				doChoice();
			} else if (noGoodStore.isAssignmentMBTFree()) {
				AnswerSet as = getAnswerSetFromAssignment();
				if (GlobalSettings.DEBUG_OUTPUTS) {
					System.out.println("Answer-Set found: " + as.toString());
					System.out.println(reportChoiceStack());
				}
				return as;
			} else {
				if (GlobalSettings.DEBUG_OUTPUTS) {
					System.out.println("Backtracking from wrong choices (MBT remaining).");
					System.out.println(reportChoiceStack());
				}
				doBacktrack();
				if (exhaustedSearchSpace()) {
					return null;
				}
			}
		}
	}


	private void doBacktrack() {
		if (decisionLevel > 0) {
			noGoodStore.backtrack(decisionLevel - 1);
			Pair<Integer, Boolean> lastChoice = choiceStack.pop();

			Integer lastGuessedAtom = lastChoice.getLeft();
			Boolean lastGuessedTruthValue = lastChoice.getRight();

			if (lastGuessedTruthValue) {
				// Guess false now
				choiceStack.push(new ImmutablePair<>(lastGuessedAtom, !lastGuessedTruthValue));
				noGoodStore.assign(lastGuessedAtom, lastGuessedTruthValue ? ThriceTruth.TRUE : ThriceTruth.FALSE);

				didChange = true;
			} else {
				decisionLevel--;
				doBacktrack();
			}

		}
	}


	private void updateGrounderAssignments() {
		List<Pair<Integer, ThriceTruth>> changedAssignments = noGoodStore.getChangedAssignments();
		int[] atomIds = new int[changedAssignments.size()];
		boolean[] truthValues = new boolean[changedAssignments.size()];
		int arrPos = 0;
		for (Pair<Integer, ThriceTruth> assignment : changedAssignments) {
			atomIds[arrPos] = assignment.getLeft();
			truthValues[arrPos] = !assignment.getRight().isNegative();
			arrPos++;
		}
		grounder.updateAssignment(atomIds, truthValues);
	}


	private void obtainNoGoodsFromGrounder() {
		// Obtain and record NoGoods
		Map<Integer, NoGood> basicNoGoods = grounder.getNoGoods();
		for (Map.Entry<Integer, NoGood> noGoodEntry : basicNoGoods.entrySet()) {
			noGoodStore.addNoGood(noGoodEntry.getKey(), noGoodEntry.getValue());
		}
		if (!basicNoGoods.isEmpty()) {
			didChange = true;        // Record to detect propagation fixpoint, checking if new NoGoods were reported might be better here.
		}
		// Record choice atoms
		Pair<Map<Integer, Integer>, Map<Integer, Integer>> choiceAtoms = grounder.getChoiceAtoms();
		Map<Integer, Integer> choiceOn = choiceAtoms.getKey();
		Map<Integer, Integer> choiceOff = choiceAtoms.getValue();
		this.choiceOn.putAll(choiceOn);
		this.choiceOff.putAll(choiceOff);
	}


	private boolean exhaustedSearchSpace() {
		return decisionLevel == 0;
	}


	private boolean propagationFixpointReached() {
		// Check if anything changed: didChange is updated in places of change.
		boolean changeCopy = didChange;
		didChange = false;
		return !changeCopy;
	}


	private AnswerSet getAnswerSetFromAssignment() {
		List<Integer> trueAssignments = noGoodStore.getTrueAssignments();
		int[] trueAtoms = new int[trueAssignments.size()];
		int arrPos = 0;
		for (Integer assignment : trueAssignments) {
			trueAtoms[arrPos++] = assignment;
		}
		return grounder.assignmentToAnswerSet(predicate -> true, trueAtoms);
	}


	Map<Integer, Integer> choiceOn = new LinkedHashMap<>();
	Map<Integer, Integer> choiceOff = new HashMap<>();
	Integer nextChoice;
	Stack<Pair<Integer, Boolean>> choiceStack = new Stack<>();

	private void doChoice() {
		decisionLevel++;
		noGoodStore.setDecisionLevel(decisionLevel);
		// We guess true for any unassigned choice atom (backtrack tries false)
		noGoodStore.assign(nextChoice, ThriceTruth.TRUE);
		didChange = true;	// Record change to compute propagation fixpoint again.
	}

	private boolean choicesLeft() {
		// Check if there is an enabled choice that is not also disabled
		// HINT: tracking changes of ChoiceOn, ChoiceOff directly could increase performance (analyze noGoodStore.getChangedAssignments()).
		for (Integer enablerAtom : choiceOn.keySet()) {
			if (!noGoodStore.getTruthValue(enablerAtom).isNegative()) {
				Integer nextChoiceCandidate = choiceOn.get(enablerAtom);

				// Only consider unassigned choices
				if (noGoodStore.getTruthValue(nextChoiceCandidate) != null) {
					continue;
				}

				// Check that candidate is not disabled already
				boolean isDisabled = false;
				for (Map.Entry<Integer, Integer> disablerAtom : choiceOff.entrySet()) {
					if (disablerAtom.getValue() == nextChoiceCandidate
						&& noGoodStore.getTruthValue(disablerAtom.getKey()) != null
						&& !noGoodStore.getTruthValue(disablerAtom.getKey()).isNegative()) {
						isDisabled = true;
						break;
					}
				}
				if (!isDisabled) {
					nextChoice = nextChoiceCandidate;
					return true;
				}
			}
		}
		return false;
	}

	private String reportChoiceStack() {
		StringBuilder ret = new StringBuilder("Choice stack is: ");
		for (Pair<Integer, Boolean> choice : choiceStack) {
			ret.append((choice.getRight() ? "+" : "-") + grounder.atomIdToString(choice.getLeft()) + " ");
		}
		return ret.toString();
	}


	@Override
	protected boolean tryAdvance(Consumer<? super AnswerSet> action) {
		AnswerSet nextAnswerSet = computeNextAnswerSet();
		if (nextAnswerSet != null) {
			action.accept(nextAnswerSet);
			return true;
		} else {
			return false;
		}
	}
}
