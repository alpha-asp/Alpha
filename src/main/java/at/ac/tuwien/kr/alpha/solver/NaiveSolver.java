/**
 * Copyright (c) 2016, the Alpha Team.
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

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;
import static java.lang.Math.abs;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class NaiveSolver extends AbstractSolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(NaiveSolver.class);
	private final ChoiceStack choiceStack;
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
	private HashSet<Integer> mbtAssigned = new HashSet<>();
	private ArrayList<ArrayList<Integer>> mbtAssignedFromUnassigned = new ArrayList<>();
	private ArrayList<ArrayList<Integer>> trueAssignedFromMbt = new ArrayList<>();
	private List<Integer> unassignedAtoms;

	NaiveSolver(Grounder grounder) {
		super(grounder);

		this.choiceStack = new ChoiceStack(grounder);

		decisionLevels.add(0, new ArrayList<>());
		mbtAssignedFromUnassigned.add(0, new ArrayList<>());
		trueAssignedFromMbt.add(0, new ArrayList<>());
	}

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
				LOGGER.trace("Propagating.");
				updateGrounderAssignments();	// After a choice, it would be more efficient to propagate first and only then ask the grounder.
				obtainNoGoodsFromGrounder();
				doUnitPropagation();
				doMBTPropagation();
				LOGGER.trace("Assignment after propagation is: {}", truthAssignments);
			} else if (assignmentViolatesNoGoods()) {
				LOGGER.trace("Backtracking from wrong choices:");
				LOGGER.trace("Choice stack: {}", choiceStack);
				doBacktrack();
				if (isSearchSpaceExhausted()) {
					return false;
				}
			} else if (choicesLeft()) {
				doChoice();
			} else if (!allAtomsAssigned()) {
				LOGGER.trace("Closing unassigned known atoms (assigning FALSE).");
				assignUnassignedToFalse();
				didChange = true;
			} else if (noMBTValuesReamining()) {
				AnswerSet as = getAnswerSetFromAssignment();
				LOGGER.debug("Answer-Set found: {}", as);
				LOGGER.trace("Choice stack: {}", choiceStack);
				action.accept(as);
				return true;
			} else {
				LOGGER.debug("Backtracking from wrong choices (MBT remaining):");
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Currently MBT:");
					for (Integer integer : mbtAssigned) {
						LOGGER.trace(grounder.atomToString(integer));
					}
					LOGGER.trace("Choice stack: {}", choiceStack);
				}
				doBacktrack();
				if (isSearchSpaceExhausted()) {
					return false;
				}
			}
		}
	}

	private void assignUnassignedToFalse() {
		for (Integer atom : unassignedAtoms) {
			truthAssignments.put(atom, false);
			newTruthAssignments.add(atom);
			decisionLevels.get(decisionLevel).add(atom);
		}
	}

	private boolean allAtomsAssigned() {
		unassignedAtoms = new ArrayList<>();
		HashSet<Integer> knownAtoms = new HashSet<>();
		for (Map.Entry<Integer, NoGood> entry : knownNoGoods.entrySet()) {
			for (Integer integer : entry.getValue()) {
				knownAtoms.add(abs(integer));
			}
		}
		for (Integer atom : knownAtoms) {
			if (!truthAssignments.containsKey(atom)) {
				unassignedAtoms.add(atom);
			}
		}
		return unassignedAtoms.isEmpty();
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
		choiceStack.push(nextChoice, true);
		// Record change to compute propagation fixpoint again.
		didChange = true;
	}

	private boolean choicesLeft() {
		// Check if there is an enabled choice that is not also disabled
		for (Map.Entry<Integer, Integer> e : choiceOn.entrySet()) {
			final int atom = e.getKey();

			// Only consider unassigned choices that are enabled.
			if (truthAssignments.containsKey(atom) || !truthAssignments.getOrDefault(e.getValue(), false)) {
				continue;
			}

			// Check that candidate is not disabled already
			if (!truthAssignments.getOrDefault(choiceOff.getOrDefault(atom, 0), false)) {
				nextChoice = atom;
				return true;
			}
		}
		return false;
	}

	private void doBacktrack() {
		if (decisionLevel <= 0) {
			return;
		}

		int lastGuessedAtom = choiceStack.peekAtom();
		boolean lastGuessedTruthValue = choiceStack.peekValue();
		choiceStack.remove();

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
			truthAssignments.put(lastGuessedAtom, false);
			choiceStack.pushBacktrack(lastGuessedAtom, false);
			newTruthAssignments.add(lastGuessedAtom);
			decisionLevels.get(decisionLevel).add(lastGuessedAtom);
			didChange = true;
		} else {
			decisionLevel--;
			doBacktrack();
		}
	}

	private void updateGrounderAssignments() {
		grounder.updateAssignment(newTruthAssignments.stream().map(atom -> {
			return (Assignment.Entry)new Entry(atom, truthAssignments.get(atom) ? TRUE : FALSE);
		}).iterator());
		newTruthAssignments.clear();
	}


	private static final class Entry implements Assignment.Entry {
		private final ThriceTruth value;
		private final int atom;

		Entry(int atom, ThriceTruth value) {
			this.value = value;
			this.atom = atom;
		}

		@Override
		public ThriceTruth getTruth() {
			return value;
		}

		@Override
		public int getDecisionLevel() {
			throw new UnsupportedOperationException();
		}

		@Override
		public NoGood getImpliedBy() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Entry getPrevious() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getAtom() {
			return atom;
		}

		@Override
		public int getPropagationLevel() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isReassignAtLowerDecisionLevel() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setReassignFalse() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			throw new UnsupportedOperationException();
		}
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
				LOGGER.trace("Violated NoGood: {}", noGood);
				return true;
			}
		}
		return false;
	}
}
