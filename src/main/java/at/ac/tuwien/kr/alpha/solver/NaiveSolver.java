package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static java.lang.Math.abs;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class NaiveSolver extends AbstractSolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(NaiveSolver.class);

	public NaiveSolver(Grounder grounder) {
		super(grounder, p -> true);

		decisionLevels.add(0, new ArrayList<>());
		mbtAssignedFromUnassigned.add(0, new ArrayList<>());
		trueAssignedFromMbt.add(0, new ArrayList<>());
	}

	private HashSet<Integer> knownAtomIds = new HashSet<>();
	private ArrayList<Integer> orderedAtomIds = new ArrayList<>();
	private HashMap<Integer, Boolean> truthAssignments = new HashMap<>();
	private ArrayList<Integer> newTruthAssignments = new ArrayList<>();
	private ArrayList<ArrayList<Integer>> decisionLevels = new ArrayList<>();

	private ArrayList<Integer> noGoodIds = new ArrayList<>();
	private HashMap<Integer, NoGood> knownNoGoods = new HashMap<>();

	boolean doInit = true;
	private int decisionLevel;

	Map<Integer, Integer> choiceOn = new HashMap<>();
	Map<Integer, Integer> choiceOff = new HashMap<>();
	Integer nextChoice;
	Stack<Integer> guessedAtomIds = new Stack<>();

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
				doUnitPropagation();
				doMBTPropagation();
			} else if (assignmentViolatesNoGoods()) {
				System.out.println("Backtracking from wrong choices:");
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace(reportChoiceStack());
				}
				doBacktrack();
				if (exhaustedSearchSpace()) {
					return null;
				}
			} else if (choicesLeft()) {
				doChoice();
			} else if (noMBTValuesReamining()) {
				AnswerSet as = getAnswerSetFromAssignment();
				LOGGER.info("Answer-Set found: {}", as);
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace(reportChoiceStack());
				}
				return as;
			} else {
				LOGGER.debug("Backtracking from wrong choices (MBT remaining):");
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Currently MBT:");
					for (Integer integer : mbtAssigned) {
						LOGGER.trace(grounder.atomIdToString(integer));
					}
					LOGGER.trace(reportChoiceStack());
				}
				doBacktrack();
				if (exhaustedSearchSpace()) {
					return null;
				}
			}
		}
	}

	private boolean noMBTValuesReamining() {
		return mbtAssigned.size() == 0;
	}

	private void doMBTPropagation() {
		boolean didPropagate = true;
		while (didPropagate) {
			didPropagate = false;
			for (Map.Entry<Integer, NoGood> noGoodEntry : knownNoGoods.entrySet()) {
				if (propagateMBT(noGoodEntry.getValue())) {
					didPropagate = true;
					didChange = true;
				}
			}
		}
	}

	private String reportChoiceStack() {
		String report = "Choice stack is: ";
		for (Integer guessedAtomId : guessedAtomIds) {
			report += (truthAssignments.get(guessedAtomId) ? "+" : "-") + guessedAtomId + " [" + grounder.atomIdToString(guessedAtomId) + "] ";
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

	boolean didChange;
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
		trueAssignedFromMbt.add(decisionLevel, new ArrayList<>());
		mbtAssignedFromUnassigned.add(decisionLevel, new ArrayList<>());
		// We guess true for any unassigned choice atom (backtrack tries false)
		truthAssignments.put(nextChoice, true);
		newTruthAssignments.add(nextChoice);
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
		if (decisionLevel <= 0) {
			return;
		}

		Integer lastGuessedAtom = guessedAtomIds.pop();
		Boolean lastGuessedTruthValue = truthAssignments.get(lastGuessedAtom);

		// Remove truth assignments of current decision level
		for (Integer atomId : decisionLevels.get(decisionLevel)) {
			truthAssignments.remove(atomId);
		}

		// Handle MBT assigned values:
		// First, restore mbt when it got assigned true in this decision level
		for (Integer atomId : trueAssignedFromMbt.get(decisionLevel)) {
			mbtAssigned.add(atomId);
		}

		// Second, remove mbt indicator for values that were unassigned
		for (Integer atomId : mbtAssignedFromUnassigned.get(decisionLevel)) {
			mbtAssigned.remove(atomId);
		}

		// Clear atomIds in current decision level
		decisionLevels.set(decisionLevel, new ArrayList<>());
		mbtAssignedFromUnassigned.set(decisionLevel, new ArrayList<>());
		trueAssignedFromMbt.set(decisionLevel, new ArrayList<>());

		if (lastGuessedTruthValue) {
			// Guess false now
			guessedAtomIds.push(lastGuessedAtom);
			truthAssignments.put(lastGuessedAtom, !lastGuessedTruthValue);
			newTruthAssignments.add(lastGuessedAtom);
			decisionLevels.get(decisionLevel).add(lastGuessedAtom);
			didChange = true;
		} else {
			decisionLevel--;
			doBacktrack();
		}
	}

	private void updateGrounderAssignments() {
		int[] atomIds = new int[newTruthAssignments.size()];
		boolean[] truthValues = new boolean[newTruthAssignments.size()];
		int arrPos = 0;
		for (Integer newTruthAssignment : newTruthAssignments) {
			atomIds[arrPos] = newTruthAssignment;
			truthValues[arrPos] = truthAssignments.get(newTruthAssignment);
			arrPos++;
		}
		newTruthAssignments = new ArrayList<>();
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
		for (Integer noGoodId :
			noGoodIds) {
			NoGood noGood = knownNoGoods.get(noGoodId);
			int implied = unitPropagate(noGood);
			if (implied == -1) {	// NoGood is not unit, skip.
				continue;
			}
			int impliedLiteral = noGood.getLiteral(implied);
			int impliedAtomId = abs(impliedLiteral);
			boolean impliedTruthValue = !(impliedLiteral > 0);
			if (truthAssignments.get(impliedAtomId) != null) {	// Skip if value already was assigned.
				continue;
			}
			truthAssignments.put(impliedAtomId, impliedTruthValue);
			newTruthAssignments.add(impliedAtomId);
			didChange = true;	// Record to detect propagation fixpoint
			decisionLevels.get(decisionLevel).add(impliedAtomId);
			if (impliedTruthValue) {	// Record MBT value in case true is assigned
				mbtAssigned.add(impliedAtomId);
				mbtAssignedFromUnassigned.get(decisionLevel).add(impliedAtomId);
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

	private HashSet<Integer> mbtAssigned = new HashSet<>();
	private ArrayList<ArrayList<Integer>> mbtAssignedFromUnassigned = new ArrayList<>();
	private ArrayList<ArrayList<Integer>> trueAssignedFromMbt = new ArrayList<>();

	private boolean propagateMBT(NoGood noGood) {
		// The MBT propagation checks whether the head-indicated literal is MBT
		// and the remaining literals are violated
		// and none of them are MBT,
		// then the head literal is set from MBT to true.

		if (!noGood.hasHead()) {
			return false;
		}

		int headAtom = abs(noGood.getLiteral(noGood.getHead()));

		if (!mbtAssigned.contains(headAtom)) {
			return false;
		}

		// Check that NoGood is violated except for the head (i.e., without the head set it would be unit)
		// and that none of the true values is MBT.
		for (int i = 0; i < noGood.size(); i++) {
			if (noGood.getHead() == i) {
				continue;
			}
			int literal = noGood.getLiteral(i);
			if (isLiteralAssigned(literal) && isLiteralViolated(literal)) {
				continue;
			} else {
				return false;
			}
		}
		// Set truth value from MBT to true.
		mbtAssigned.remove(headAtom);
		trueAssignedFromMbt.get(decisionLevel).add(headAtom);
		return true;
	}

	private boolean assignmentViolatesNoGoods() {
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
				return true;
			}
		}
		return false;
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
