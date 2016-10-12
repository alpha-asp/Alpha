package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static at.ac.tuwien.kr.alpha.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.Literals.isNegated;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class NaiveSolver extends AbstractSolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(NaiveSolver.class);

	NaiveSolver(Grounder grounder, java.util.function.Predicate<Predicate> filter) {
		super(grounder, filter);

		decisionLevels.add(0, new ArrayList<>());
		mbtAssignedFromUnassigned.add(0, new ArrayList<>());
		trueAssignedFromMbt.add(0, new ArrayList<>());
	}

	private HashMap<Integer, Boolean> truthAssignments = new HashMap<>();
	private ArrayList<Integer> newTruthAssignments = new ArrayList<>();
	private ArrayList<ArrayList<Integer>> decisionLevels = new ArrayList<>();

	private HashMap<Integer, NoGood> knownNoGoods = new HashMap<>();

	private boolean doInit = true;
	private boolean didChange;
	private int decisionLevel;

	private Map<Integer, Integer> choiceOn = new HashMap<>();
	private Map<Integer, Integer> choiceOff = new HashMap<>();
	private Integer nextChoice;
	private Stack<Integer> guessedAtomIds = new Stack<>();

	private HashSet<Integer> mbtAssigned = new HashSet<>();
	private ArrayList<ArrayList<Integer>> mbtAssignedFromUnassigned = new ArrayList<>();
	private ArrayList<ArrayList<Integer>> trueAssignedFromMbt = new ArrayList<>();

	@Override
	protected boolean tryAdvance(Consumer<? super AnswerSet> action) {
		// Get basic rules and facts from grounder
		if (doInit) {
			obtainNoGoodsFromGrounder();
			doInit = false;
		} else {
			// We already found one Answer-Set and are requested to find another one
			doBacktrack();
			if (isSearchSpaceExhausted()) {
				return false;
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
				LOGGER.info("Backtracking from wrong choices:");
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace(reportChoiceStack());
				}
				doBacktrack();
				if (isSearchSpaceExhausted()) {
					return false;
				}
			} else if (choicesLeft()) {
				doChoice();
			} else if (noMBTValuesReamining()) {
				AnswerSet as = getAnswerSetFromAssignment();
				LOGGER.info("Answer-Set found: {}", as);
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace(reportChoiceStack());
				}
				action.accept(as);
				return true;
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
				if (isSearchSpaceExhausted()) {
					return false;
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
		if (guessedAtomIds.isEmpty()) {
			return "Choice stack is empty.";
		}

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
		return translate(trueAtoms);
	}

	private void doChoice() {
		decisionLevel++;
		ArrayList<Integer> list = new ArrayList<>();
		list.add(nextChoice);
		decisionLevels.add(decisionLevel, list);
		trueAssignedFromMbt.add(decisionLevel, new ArrayList<>());
		mbtAssignedFromUnassigned.add(decisionLevel, new ArrayList<>());
		// We guess true for any unassigned choice atom (backtrack tries false)
		truthAssignments.put(nextChoice, true);
		newTruthAssignments.add(nextChoice);
		guessedAtomIds.push(nextChoice);
		// Record change to compute propagation fixpoint again.
		didChange = true;
	}

	private boolean choicesLeft() {
		// Check if there is an enabled choice that is not also disabled
		for (Map.Entry<Integer, Integer> e : choiceOn.entrySet()) {
			if (!truthAssignments.get(e.getKey())) {
				continue;
			}

			Integer nextChoiceCandidate = choiceOn.get(e.getKey());

			// Only consider unassigned choices
			if (truthAssignments.containsKey(nextChoiceCandidate)) {
				continue;
			}

			// Check that candidate is not disabled already
			boolean isDisabled = false;
			for (Map.Entry<Integer, Integer> disablerAtom : choiceOff.entrySet()) {
				if (!disablerAtom.getValue().equals(nextChoiceCandidate)) {
					continue;
				}
				if (truthAssignments.containsKey(disablerAtom.getKey())  && truthAssignments.get(disablerAtom.getKey())) {
					isDisabled = true;
					break;
				}
			}
			if (!isDisabled) {
				nextChoice = nextChoiceCandidate;
				return true;
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
			truthAssignments.put(lastGuessedAtom, false);
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
		for (int i = 0; i < newTruthAssignments.size(); i++) {
			final Integer newTruthAssignment = newTruthAssignments.get(i);
			atomIds[i] = newTruthAssignment;
			truthValues[i] = truthAssignments.get(newTruthAssignment);
		}
		newTruthAssignments.clear();
		grounder.updateAssignment(atomIds, truthValues);
	}

	private void obtainNoGoodsFromGrounder() {
		final int oldSize = knownNoGoods.size();
		knownNoGoods.putAll(grounder.getNoGoods());
		if (oldSize != knownNoGoods.size()) {
			// Record to detect propagation fixpoint, checking if new NoGoods were reported would be better here.
			didChange = true;
		}

		// Record choice atoms
		final Pair<Map<Integer, Integer>, Map<Integer, Integer>> choiceAtoms = grounder.getChoiceAtoms();
		choiceOn.putAll(choiceAtoms.getKey());
		choiceOff.putAll(choiceAtoms.getValue());
	}

	private boolean isSearchSpaceExhausted() {
		return decisionLevel == 0;
	}

	private void doUnitPropagation() {
		// Check each NoGood if it is unit (naive algorithm)
		for (NoGood noGood : knownNoGoods.values()) {
			int implied = unitPropagate(noGood);
			if (implied == -1) {	// NoGood is not unit, skip.
				continue;
			}
			int impliedLiteral = noGood.getLiteral(implied);
			int impliedAtomId = atomOf(impliedLiteral);
			boolean impliedTruthValue = isNegated(impliedLiteral);
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
		return truthAssignments.get(atomOf(literal)) != null;
	}

	private boolean isLiteralViolated(int literal) {
		final int atom = atomOf(literal);
		final Boolean assignment = truthAssignments.get(atom);

		// For unassigned atoms, any literal is not violated.
		return assignment != null && isNegated(literal) != assignment;
	}

	/**
	 * Returns position of implied literal if input NoGood is unit.
	 * @param noGood
	 * @return -1 if NoGood is not unit.
	 */
	private int unitPropagate(NoGood noGood) {
		int lastUnassignedPosition = -1;
		for (int i = 0; i < noGood.size(); i++) {
			int literal = noGood.getLiteral(i);
			if (isLiteralAssigned(literal)) {
				if (!isLiteralViolated(literal)) {
					// The NoGood is satisfied, hence it cannot be unit.
					return -1;
				}
			} else if (lastUnassignedPosition != -1) {
				// NoGood is not unit, if there is not exactly one unassigned literal
				return -1;
			} else {
				lastUnassignedPosition = i;
			}
		}
		return lastUnassignedPosition;
	}

	private boolean propagateMBT(NoGood noGood) {
		// The MBT propagation checks whether the head-indicated literal is MBT
		// and the remaining literals are violated
		// and none of them are MBT,
		// then the head literal is set from MBT to true.

		if (!noGood.hasHead()) {
			return false;
		}

		int headAtom = noGood.getAtom(noGood.getHead());

		// Check whether head is assigned MBT.
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
			if (!(isLiteralAssigned(literal) && isLiteralViolated(literal))) {
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
