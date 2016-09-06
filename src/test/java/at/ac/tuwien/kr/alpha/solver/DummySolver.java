package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Consumer;

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

	private HashMap<Integer, NoGood> knownNoGoods = new HashMap<>();

	boolean doInit = true;
	boolean didChange;

	private int decisionLevel;
	private HashSet<Integer> mbtAssigned = new HashSet<>();

	Map<Integer, Integer> choiceOn = new HashMap<>();
	Map<Integer, Integer> choiceOff = new HashMap<>();
	Integer nextChoice;
	Stack<Integer> guessedAtomIds = new Stack<>();

	@Override
	protected boolean tryAdvance(Consumer<? super AnswerSet> action) {
		// Get basic rules and facts from grounder
		if (doInit) {
			obtainNoGoodsFromGrounder();
			decisionLevels.add(0, new ArrayList<>());
			doInit = false;
		} else {
			// We already found one Answer-Set and are requested to find another one
			doBacktrack();
			if (exhaustedSearchSpace()) {
				return false;
			}
		}

		// Try all assignments until grounder reports no more NoGoods and all of them are satisfied
		while (true) {
			if (!propagationFixpointReached()) {
				updateGrounderAssignments();
				obtainNoGoodsFromGrounder();
				doUnitPropagation();
				doMBTPropagation();
			} else if (assignmentViolatesNoGoods()) {
				doBacktrack();
				if (exhaustedSearchSpace()) {
					return false;
				}
			} else if (choicesLeft()) {
				doChoice();
			} else if (noMBTValuesRemaining()) {
				AnswerSet as = getAnswerSetFromAssignment();
				System.out.println(reportChoiceStack());
				action.accept(as);
				return true;
			} else {
				doBacktrack();
				if (exhaustedSearchSpace()) {
					return false;
				}
			}
		}
	}

	private boolean noMBTValuesRemaining() {
		return mbtAssigned.size() == 0;
	}

	private void doMBTPropagation() {
		boolean didPropagate;
		do {
			didPropagate = false;
			for (Map.Entry<Integer, NoGood> noGoodEntry : knownNoGoods.entrySet()) {
				if (propagateMBT(noGoodEntry.getValue())) {
					didPropagate = true;
					didChange = true;
				}
			}
		} while (didPropagate);
	}

	private String reportChoiceStack() {
		String report = "Choice stack is: ";
		for (Integer guessedAtomId : guessedAtomIds) {
			report += (truthAssignments.get(guessedAtomId) ? "+" : "-") + guessedAtomId + " ";
		}
		return report;
	}

	private String reportTruthAssignments() {
		String report = "Current Truth assignments: ";
		for (Integer atomId : truthAssignments.keySet()) {
			report += (truthAssignments.get(atomId) ? "+" : "-") + atomId + " ";
		}
		return report;
	}

	private boolean propagationFixpointReached() {
		// Check if anything changed.
		// didChange is updated in places of change.
		boolean changeCopy = didChange;
		didChange = false;
		return !changeCopy;
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
		return grounder.assignmentToAnswerSet(predicate -> true, trueAtomsIntArray);
	}

	private void doChoice() {
		decisionLevel++;
		decisionLevels.add(decisionLevel, new ArrayList<>());
		// We guess true for any unassigned choice atom (backtrack tries false)
		truthAssignments.put(nextChoice, true);
		guessedAtomIds.push(nextChoice);
		didChange = true;	// Record change to compute propagation fixpoint again.
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
				didChange = true;

			} else {
				decisionLevel--;
				doBacktrack();
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
			arrPos++;
		}
		grounder.updateAssignment(atomIds, truthValues);
	}

	private void obtainNoGoodsFromGrounder() {
		Map<Integer, NoGood> basicNoGoods = grounder.getNoGoods();
		for (Integer noGoodId :
			basicNoGoods.keySet()) {
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
			didChange = true;	// Record to detect propagation fixpoint, checking if new NoGoods were reported would be better here.
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

	private void doUnitPropagation() {
		// Check each NoGood if it is unit (naive algorithm)
		for (NoGood noGood : knownNoGoods.values()) {
			int implied = unitPropagate(noGood);
			if (implied == -1) {	// NoGood is not unit, skip.
				continue;
			}
			didChange = true;	// Record to detect propagation fixpoint
			int impliedLiteral = noGood.getLiteral(implied);
			int impliedAtomId = abs(impliedLiteral);
			boolean impliedTruthValue = !(impliedLiteral > 0);
			truthAssignments.put(impliedAtomId, impliedTruthValue);
			decisionLevels.get(decisionLevel).add(impliedAtomId);
			if (impliedTruthValue) {	// Record MBT value in case true is assigned
				mbtAssigned.add(impliedAtomId);
			}
		}
	}

	private boolean isLiteralAssigned(int literal) {
		return truthAssignments.get(abs(literal)) != null;
	}

	private boolean isLiteralViolated(int literal) {
		// Check if literal of a NoGood is violated
		int atomId = abs(literal);
		boolean literalPolarity = literal > 0;
		// We assume literal is assigned
		Boolean assignedTruthValue = truthAssignments.get(atomId);
		return literalPolarity == assignedTruthValue;
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
				if (!isLiteralViolated(currentLiteral)) {
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

	private boolean propagateMBT(NoGood noGood) {
		// The MBT propagation checks whether the head-indicated literal is MBT
		// and the remaining literals are violated
		// and none of them are MBT,
		// then the head literal is set from MBT to true.

		if (!noGood.hasHead() || !mbtAssigned.contains(abs(noGood.getLiteral(noGood.getHead())))) {
			return false;
		}

		// Check that NoGood is violated except for the head (i.e., without the head set it would be unit)
		// and that none of the true values is MBT.
		for (int i = 0; i < noGood.size(); i++) {
			if (noGood.getHead() == i) {
				continue;
			}
			int literal = noGood.getLiteral(i);
			if (!(isLiteralAssigned(literal) && isLiteralViolated(literal))) {
				return false;
			}
		}
		// Set truth value from MBT to true.
		mbtAssigned.remove(abs(noGood.getLiteral(noGood.getHead())));
		return true;
	}

	private boolean assignmentViolatesNoGoods() {
		// Check each NoGood, if it is violated
		for (NoGood noGood : knownNoGoods.values()) {
			boolean isSatisfied = false;
			for (Integer noGoodLiteral : noGood) {
				if (!isLiteralAssigned(noGoodLiteral) || !isLiteralViolated(noGoodLiteral)) {
					isSatisfied = true;
					break;
				}
			}
			if (!isSatisfied) {
				return true;
			}
		}
		return false;
	}
}
