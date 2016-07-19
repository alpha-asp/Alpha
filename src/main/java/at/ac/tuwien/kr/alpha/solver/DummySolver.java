package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.AnswerSet;
import at.ac.tuwien.kr.alpha.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static java.lang.Math.abs;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class DummySolver extends AbstractSolver {
	public DummySolver(Grounder grounder) {
		super(grounder, p -> true);
	}

	private HashSet<Integer> knownAtomIds = new HashSet<>();
	private ArrayList<Integer> orderedAtomIds = new ArrayList<>();
	private HashMap<Integer, Boolean> truthAssignments = new HashMap<>();
	private ArrayList<ArrayList<Integer>> decisionLevels = new ArrayList<>();

	private ArrayList<Integer> noGoodIds = new ArrayList<>();
	private HashMap<Integer, NoGood> knownNoGoods = new HashMap<>();

	boolean doInit = true;

	@Override
	public AnswerSet get() {
		// Get basic rules and facts from grounder
		if (doInit) {
			obtainNoGoodsFromGrounder();
			doInit = false;
		} else {
			// We already found one Answer-Set and are requested to find another one
			doBacktrack();
		}

		// Try all assignments until grounder reports no more NoGoods and all of them are satisfied
		do {
			updateGrounderAssignments();
			obtainNoGoodsFromGrounder();
			doUnitPropagation();
			if (!checkAssignmentSatisfiesAllNoGoods()) {
				doBacktrack();
				if (exhaustedSearchSpace()) {
					return null;
				}
			} else {
				if (choicesLeft()) {
					doChoice();
				} else {
					AnswerSet as = getAnswerSetFromAssignment();
					return as;
				}
			}
		} while (true);
	}

	private AnswerSet getAnswerSetFromAssignment() {
		ArrayList<Integer> trueAtoms = new ArrayList<>();
		for (Map.Entry<Integer, Boolean> atomAssignment : truthAssignments.entrySet()) {
			if (atomAssignment.getValue()) {
				trueAtoms.add(atomAssignment.getKey());
			}
		}
		int[] trueAtomsIntArray = new int[trueAtoms.size()];
		for (int i = 0; i < trueAtoms.size(); i++) {
			trueAtomsIntArray[i] = trueAtoms.get(i);
		}
		return translate(trueAtomsIntArray);
	}


	Map<Integer, Integer> choiceOn = new HashMap<>();
	Map<Integer, Integer> choiceOff = new HashMap<>();
	Integer nextChoice;
	Stack<Integer> guessedAtomIds = new Stack<>();

	private void doChoice() {
		decisionLevel++;
		// We guess true for any unassigned choice atom (backtrack tries false)
		truthAssignments.put(nextChoice, true);
		guessedAtomIds.push(nextChoice);
	}

	private boolean choicesLeft() {
		// Check if there is an enabled choice that is not also disabled
		for (Integer enablerAtom : choiceOn.keySet()) {
			if (truthAssignments.get(enablerAtom)) {
				Integer nextChoiceCandidate = choiceOn.get(enablerAtom);

				// Only consider unassigned choices
				if (truthAssignments.containsKey(nextChoiceCandidate)) {
					continue;
				}

				// Check that candidate is not disabled already
				boolean isDisabled = false;
				for (Map.Entry<Integer, Integer> disablerAtom : choiceOff.entrySet()) {
					if (disablerAtom.getValue() == nextChoiceCandidate
						&& truthAssignments.containsKey(disablerAtom.getKey())
						&& truthAssignments.get(disablerAtom.getKey())) {
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

	private void doBacktrack() {
		if (decisionLevel > 0) {
			Integer lastGuessedAtom = guessedAtomIds.pop();
			Boolean lastGuessedTruthValue = truthAssignments.get(lastGuessedAtom);

			// Remove truth assignments of current decision level
			for (Integer atomId : decisionLevels.get(decisionLevel)) {
				truthAssignments.remove(atomId);
			}
			// Clear atomIds in current decision level
			decisionLevels.set(decisionLevel, new ArrayList<>());

			if (lastGuessedTruthValue) {
				// Guess false now
				guessedAtomIds.push(lastGuessedAtom);
				truthAssignments.put(lastGuessedAtom, !lastGuessedTruthValue);
				decisionLevels.get(decisionLevel).add(lastGuessedAtom);

			} else {
				decisionLevel--;
			}

		}
	}



	private void updateGrounderAssignments() {
		int[] atomIds = new int[truthAssignments.size()];
		boolean[] truthValues = new boolean[truthAssignments.size()];
		int arrPos = 0;
		for (Map.Entry<Integer, Boolean> truthAssignment : truthAssignments.entrySet()) {
			atomIds[arrPos] = truthAssignment.getKey();
			truthValues[arrPos] = truthAssignment.getValue();
		}
		grounder.updateAssignment(atomIds, truthValues);
	}

	private void obtainNoGoodsFromGrounder() {
		Map<Integer, NoGood> basicNoGoods = grounder.getNoGoods();
		for (Integer noGoodId :
			basicNoGoods.keySet()) {
			noGoodIds.add(noGoodId);
			NoGood noGood = basicNoGoods.get(noGoodId);
			knownNoGoods.put(noGoodId, noGood);
			// Extract and save atomIds
			for (int i = 0; i < noGood.size(); i++) {
				int currentAtom = abs(noGood.getLiteral(i));
				if (!knownAtomIds.contains(currentAtom)) {
					knownAtomIds.add(currentAtom);
					orderedAtomIds.add(currentAtom);
				}
			}
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

	private int decisionLevel;


	private void doUnitPropagation() {
		// Check each NoGood if it is unit (naive algorithm)
		for (Integer noGoodId :
			noGoodIds) {
			NoGood noGood = knownNoGoods.get(noGoodId);
			int implied = unitPropagate(noGood);
			int impliedLiteral = noGood.getLiteral(implied);
			int impliedAtomId = abs(impliedLiteral);
			boolean impliedTruthValue = !(impliedLiteral > 0);
			truthAssignments.put(impliedAtomId, impliedTruthValue);
			decisionLevels.get(decisionLevel).add(impliedAtomId);
		}
	}

	private boolean isLiteralAssigned(int literal) {
		return truthAssignments.get(abs(literal)) != null;
	}

	private boolean isLiteralViolated(int literal) {
		int atomId = abs(literal);
		boolean literalPolarity = literal > 0;
		// We assume literal is assigned
		Boolean assignedTruthValue = truthAssignments.get(atomId);
		return literalPolarity ^ assignedTruthValue;
	}

	/**
	 * Returns position of implied literal if input NoGood is unit.
	 * @param noGood
	 * @return -1 if NoGood is not unit.
	 */
	private int unitPropagate(NoGood noGood) {
		int unassignedLiteralsInNoGood = 0;
		int lastUnassignedPosition = -1;
		for (int i = 0; i < noGood.size(); i++) {
			int currentLiteral = noGood.getLiteral(i);
			if (isLiteralAssigned(currentLiteral)) {
				if (isLiteralViolated(currentLiteral)) {
					continue;
				} else {
					// The NoGood is satisfied, hence it cannot be unit.
					return -1;
				}
			} else {
				unassignedLiteralsInNoGood++;
				lastUnassignedPosition = i;
			}
		}
		// NoGood is not unit, if there is not exactly one unassigned literal
		if (unassignedLiteralsInNoGood != 1) {
			return -1;
		}
		return lastUnassignedPosition;
	}

	private boolean checkAssignmentSatisfiesAllNoGoods() {
		// Check each NoGood, if it is violated
		for (Integer noGoodId :
			noGoodIds) {
			NoGood noGood = knownNoGoods.get(noGoodId);
			boolean isSatisfied = false;
			for (int i = 0; i < noGood.size(); i++) {
				int noGoodLiteral = noGood.getLiteral(i);
				if (!isLiteralAssigned(noGoodLiteral) || !isLiteralViolated(noGoodLiteral)) {
					isSatisfied = true;
					break;
				}
			}
			if (!isSatisfied) {
				return false;
			}
		}
		return true;
	}
}
