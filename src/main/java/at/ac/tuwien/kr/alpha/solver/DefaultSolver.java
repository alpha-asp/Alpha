package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.GlobalSettings;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Consumer;

import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;

/**
 * The new default solver employed in Alpha.
 * Copyright (c) 2016, the Alpha Team.
 */
public class DefaultSolver extends AbstractSolver {
	private NoGoodStore<ThriceTruth> store;
	private Assignment assignment;

	private boolean doInit = true;
	private boolean didChange;
	private int decisionLevel;

	public DefaultSolver(Grounder grounder) {
		super(grounder, p -> true);

		this.assignment = new BasicAssignment();
		this.store = new BasicNoGoodStore(assignment);
	}

	public AnswerSet computeNextAnswerSet() {
		// Get basic rules and facts from grounder
		if (doInit) {
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
				if (store.propagate()) {
					didChange = true;
				}
			} else if (store.isNoGoodViolated()) {
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
			} else if (!assignment.containsMBT()) {
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
			assignment.backtrack(decisionLevel - 1);
			Pair<Integer, Boolean> lastChoice = choiceStack.pop();

			Integer lastGuessedAtom = lastChoice.getLeft();
			Boolean lastGuessedTruthValue = lastChoice.getRight();

			if (lastGuessedTruthValue) {
				// Guess false now
				choiceStack.push(new ImmutablePair<>(lastGuessedAtom, !lastGuessedTruthValue));
				store.assign(lastGuessedAtom, lastGuessedTruthValue ? ThriceTruth.TRUE : FALSE);

				didChange = true;
			} else {
				decisionLevel--;
				doBacktrack();
			}

		}
	}


	private void updateGrounderAssignments() {
		Map<Integer, ThriceTruth> changedAssignments = store.getChangedAssignments();
		int[] atomIds = new int[changedAssignments.size()];
		boolean[] truthValues = new boolean[changedAssignments.size()];
		int arrPos = 0;
		for (Map.Entry<Integer, ThriceTruth> assignment : changedAssignments.entrySet()) {
			atomIds[arrPos] = assignment.getKey();
			truthValues[arrPos] = !(FALSE.equals(assignment.getValue()));
			arrPos++;
		}
		grounder.updateAssignment(atomIds, truthValues);
	}


	private void obtainNoGoodsFromGrounder() {
		// Obtain and record NoGoods
		Map<Integer, NoGood> basicNoGoods = grounder.getNoGoods();
		for (Map.Entry<Integer, NoGood> noGoodEntry : basicNoGoods.entrySet()) {
			store.add(noGoodEntry.getKey(), noGoodEntry.getValue());
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
		Set<Integer> trueAssignments = assignment.getTrueAssignments();
		return grounder.assignmentToAnswerSet(predicate -> true, ArrayUtils.toPrimitive(trueAssignments.toArray(new Integer[trueAssignments.size()])));
	}

	Map<Integer, Integer> choiceOn = new LinkedHashMap<>();
	Map<Integer, Integer> choiceOff = new HashMap<>();
	Integer nextChoice;
	Stack<Pair<Integer, Boolean>> choiceStack = new Stack<>();

	private void doChoice() {
		decisionLevel++;
		store.setDecisionLevel(decisionLevel);
		// We guess true for any unassigned choice atom (backtrack tries false)
		store.assign(nextChoice, ThriceTruth.TRUE);
		didChange = true;	// Record change to compute propagation fixpoint again.
	}

	private boolean choicesLeft() {
		// Check if there is an enabled choice that is not also disabled
		// HINT: tracking changes of ChoiceOn, ChoiceOff directly could increase performance (analyze store.getChangedAssignments()).
		for (Integer enablerAtom : choiceOn.keySet()) {
			if (!(FALSE.equals(assignment.getTruth(enablerAtom)))) {
				Integer nextChoiceCandidate = choiceOn.get(enablerAtom);

				// Only consider unassigned choices
				if (assignment.getTruth(nextChoiceCandidate) != null) {
					continue;
				}

				// Check that candidate is not disabled already
				boolean isDisabled = false;
				for (Map.Entry<Integer, Integer> disablerAtom : choiceOff.entrySet()) {
					if (nextChoiceCandidate.equals(disablerAtom.getValue())
						&& assignment.getTruth(disablerAtom.getKey()) != null
						&& !(FALSE.equals(assignment.getTruth(disablerAtom.getKey())))) {
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
			ret.append(choice.getRight() ? "+" : "-").append(grounder.atomIdToString(choice.getLeft())).append(" ");
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
