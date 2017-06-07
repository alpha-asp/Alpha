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
import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class NaiveSolver extends AbstractSolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(NaiveSolver.class);
	private final ChoiceStack choiceStack;
	private ArrayAssignment assignment = new ArrayAssignment();
	private final NoGoodStore store;

	private boolean doInit = true;
	private boolean didChange;
	private int decisionLevel;

	private Map<Integer, Integer> choiceOn = new HashMap<>();
	private Map<Integer, Integer> choiceOff = new HashMap<>();
	private Integer nextChoice;

	private List<Integer> unassignedAtoms;

	NaiveSolver(Grounder grounder) {
		super(grounder);

		this.choiceStack = new ChoiceStack(grounder, false);
		this.store = new NaiveNoGoodStore(assignment);
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
				if (store.propagate()) {
					didChange = true;
				}
				//LOGGER.trace("Assignment after propagation is: {}", truthAssignments);
			} else if (store.getViolatedNoGood() != null) {
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
				AnswerSet as = translate(assignment.getTrueAssignments());
				LOGGER.debug("Answer-Set found: {}", as);
				LOGGER.trace("Choice stack: {}", choiceStack);
				action.accept(as);
				return true;
			} else {
				LOGGER.debug("Backtracking from wrong choices (MBT remaining):");
				if (LOGGER.isTraceEnabled()) {
					//LOGGER.trace("Currently MBT:");
					//for (Integer integer : mbtAssigned) {
					//	LOGGER.trace(grounder.atomToString(integer));
					//}
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
			assignment.assign(atom, FALSE);
		}
	}

	private boolean allAtomsAssigned() {
		unassignedAtoms = grounder.getUnassignedAtoms(assignment);
		return unassignedAtoms.isEmpty();
	}

	private boolean noMBTValuesReamining() {
		return assignment.getMBTCount() == 0;
	}

	private boolean propagationFixpointReached() {
		// Check if anything changed.
		// didChange is updated in places of change.
		boolean changeCopy = didChange;
		didChange = false;
		return !changeCopy;
	}

	private void doChoice() {
		// We guess true for any unassigned choice atom (backtrack tries false)
		assignment.guess(nextChoice, true);
		choiceStack.push(nextChoice, true);
		// Record change to compute propagation fixpoint again.
		didChange = true;
	}

	private boolean choicesLeft() {
		// Check if there is an enabled choice that is not also disabled
		for (Map.Entry<Integer, Integer> e : choiceOn.entrySet()) {
			final int atom = e.getKey();

			// Only consider unassigned choices
			if (assignment.isAssigned(atom)) {
				continue;
			}

			ThriceTruth truth = assignment.getTruth(e.getValue());

			if (truth == null || !truth.toBoolean()) {
				continue;
			}

			// Check that candidate is not disabled already
			truth = assignment.getTruth(choiceOff.getOrDefault(atom, 0));

			if (truth == null || !truth.toBoolean()) {
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

		if (lastGuessedTruthValue) {
			// Guess false now
			assignment.assign(lastGuessedAtom, FALSE);
			choiceStack.pushBacktrack(lastGuessedAtom, false);
			didChange = true;
		} else {
			decisionLevel--;
			doBacktrack();
		}
	}

	private void updateGrounderAssignments() {
		grounder.updateAssignment(assignment.getNewAssignmentsIterator());
	}

	private void obtainNoGoodsFromGrounder() {
		final int oldSize = store.size();

		Map<Integer, NoGood> obtained = grounder.getNoGoods(null);
		assignment.growForMaxAtomId(grounder.getMaxAtomId());

		for (Map.Entry<Integer, NoGood> e : obtained.entrySet()) {
			store.add(e.getKey(), e.getValue());
		}

		if (oldSize != store.size()) {
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
}
