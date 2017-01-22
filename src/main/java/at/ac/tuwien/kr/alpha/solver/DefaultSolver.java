/**
 * Copyright (c) 2016-2017, the Alpha Team.
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
import at.ac.tuwien.kr.alpha.solver.heuristics.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isPositive;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;

/**
 * The new default solver employed in Alpha.
 * Copyright (c) 2016, the Alpha Team.
 */
public class DefaultSolver extends AbstractSolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSolver.class);

	private final NoGoodStore<ThriceTruth> store;
	private final ChoiceStack choiceStack;
	private final Assignment assignment;
	private final GroundConflictNoGoodLearner learner;
	private final BranchingHeuristic branchingHeuristic;
	private final BranchingHeuristic fallbackBranchingHeuristic;
	private final ChoiceManager choiceManager;

	private boolean initialize = true;

	private boolean didChange;

	private int decisionCounter;

	public DefaultSolver(Grounder grounder, Random random, String branchingHeuristicName, boolean debugInternalChecks) {
		super(grounder);

		this.assignment = new BasicAssignment(grounder);
		this.store = new BasicNoGoodStore(assignment, grounder);
		if (debugInternalChecks) {
			store.enableInternalChecks();
		}
		this.choiceStack = new ChoiceStack(grounder);
		this.learner = new GroundConflictNoGoodLearner(assignment);
		this.choiceManager = new ChoiceManager(assignment);
		this.branchingHeuristic = BranchingHeuristicFactory.getInstance(branchingHeuristicName, assignment, choiceManager, random);
		this.fallbackBranchingHeuristic = new NaiveHeuristic(choiceManager);
	}

	@Override
	protected boolean tryAdvance(Consumer<? super AnswerSet> action) {
		// Initially, get NoGoods from grounder.
		if (initialize) {
			if (!obtainNoGoodsFromGrounder()) {
				// NoGoods are unsatisfiable.
				LOGGER.info("{} decisions done.", decisionCounter);
				return false;
			}
			initialize = false;
		} else {
			// We already found one Answer-Set and are requested to find another one.
			// Create enumeration NoGood to avoid finding the same Answer-Set twice.
			if (!isSearchSpaceExhausted()) {
				NoGood enumerationNoGood = createEnumerationNoGood();
				int backjumpLevel = computeMinimumConflictLevel(enumerationNoGood);
				if (backjumpLevel == -1) {
					throw new RuntimeException("Enumeration NoGood is currently not violated. Should not happen.");
				}
				// Backjump instead of backtrack, enumerationNoGood will invert lass guess.
				doBackjump(backjumpLevel - 1);
				LOGGER.debug("Adding enumeration NoGood: {}", enumerationNoGood);
				NoGoodStore.ConflictCause conflictCause = store.add(grounder.registerOutsideNoGood(enumerationNoGood), enumerationNoGood);
				if (conflictCause != null) {
					throw new RuntimeException("Adding enumeration NoGood causes conflicts after backjump. Should not happen.");
				}
			} else {
				LOGGER.info("{} decisions done.", decisionCounter);
				return false;
			}
		}

		int nextChoice;
		boolean afterAllAtomsAssigned = false;

		// Try all assignments until grounder reports no more NoGoods and all of them are satisfied
		while (true) {
			didChange |= store.propagate();
			LOGGER.debug("Assignment after propagation is: {}", assignment);
			if (store.getViolatedNoGood() != null) {
				// Learn from conflict.
				NoGood violatedNoGood = store.getViolatedNoGood();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("NoGood violated ({}) by wrong choices ({} violated): {}", grounder.noGoodToString(violatedNoGood), choiceStack);
				}
				LOGGER.debug("Violating assignment is: {}", assignment);
				branchingHeuristic.violatedNoGood(violatedNoGood);
				if (!afterAllAtomsAssigned) {
					if (!learnBackjumpAddFromConflict()) {
						// NoGoods are unsatisfiable.
						LOGGER.info("{} decisions done.", decisionCounter);
						return false;
					}
				} else {
					// Will not learn from violated NoGood, do simple backtrack.
					LOGGER.debug("NoGood was violated after all unassigned atoms were assigned to false; will not learn from it; skipping.");
					doBacktrack();
					afterAllAtomsAssigned = false;
					if (isSearchSpaceExhausted()) {
						LOGGER.info("{} decisions done.", decisionCounter);
						return false;
					}
				}
			} else if (!propagationFixpointReached()) {
				// Ask the grounder for new NoGoods, then propagate (again).
				LOGGER.trace("Doing propagation step.");
				updateGrounderAssignment();
				if (!obtainNoGoodsFromGrounder()) {
					// NoGoods are unsatisfiable.
					LOGGER.info("{} decisions done.", decisionCounter);
					return false;
				}
			} else if ((nextChoice = computeChoice()) != 0) {
				LOGGER.debug("Doing choice.");
				doChoice(nextChoice);
			} else if (!allAtomsAssigned()) {
				LOGGER.debug("Closing unassigned known atoms (assigning FALSE).");
				assignUnassignedToFalse();
				afterAllAtomsAssigned = true;
			} else if (assignment.getMBTCount() == 0) {
				AnswerSet as = translate(assignment.getTrueAssignments());
				LOGGER.debug("Answer-Set found: {}", as);
				LOGGER.debug("Choices of Answer-Set were: {}", choiceStack);
				action.accept(as);
				LOGGER.info("{} decisions done.", decisionCounter);
				return true;
			} else {
				LOGGER.debug("Backtracking from wrong choices ({} MBTs): {}", assignment.getMBTCount(), choiceStack);
				doBacktrack();
				afterAllAtomsAssigned = false;
				if (isSearchSpaceExhausted()) {
					LOGGER.info("{} decisions done.", decisionCounter);
					return false;
				}
			}
		}
	}

	private NoGood createEnumerationNoGood() {
		int[] enumerationLiterals = new int[choiceStack.size()];
		int enumerationPos = 0;
		for (Integer integer : choiceStack) {
			enumerationLiterals[enumerationPos++] = integer;
		}
		return new NoGood(enumerationLiterals, -1);
	}


	private int computeMinimumConflictLevel(NoGood noGood) {
		int minimumConflictLevel = -1;
		for (Integer literal : noGood) {
			Assignment.Entry entry = assignment.get(atomOf(literal));
			if (entry == null || isPositive(literal) != entry.getTruth().toBoolean()) {
				return -1;
			}
			int literalDecisionLevel = entry.getPrevious() != null ? entry.getPrevious().getDecisionLevel() : entry.getDecisionLevel();
			if (literalDecisionLevel > minimumConflictLevel) {
				minimumConflictLevel = literalDecisionLevel;
			}
		}
		return minimumConflictLevel;
	}

	/**
	 * Analyzes the conflict and either learns a new NoGood (causing backjumping and addition to the NoGood store),
	 * or backtracks the guess causing the conflict.
	 * @return false iff the analysis result shows that the set of NoGoods is unsatisfiable.
	 */
	private boolean learnBackjumpAddFromConflict() {
		LOGGER.debug("Analyzing conflict.");
		GroundConflictNoGoodLearner.ConflictAnalysisResult analysisResult = learner.analyzeConflictingNoGood(store.getViolatedNoGood());
		if (analysisResult.isUnsatisfiable) {
			// Halt if unsatisfiable.
			return false;
		}

		branchingHeuristic.analyzedConflict(analysisResult);
		if (analysisResult.learnedNoGood == null) {
			LOGGER.debug("Conflict results from wrong guess, backjumping and removing guess.");
			LOGGER.debug("Backjumping to decision level: {}", analysisResult.backjumpLevel);
			doBackjump(analysisResult.backjumpLevel);
			store.backtrack();
			LOGGER.debug("Backtrack: Removing last choice because of conflict, setting decision level to {}.", assignment.getDecisionLevel());
			choiceStack.remove();
			choiceManager.backtrack();
			LOGGER.debug("Backtrack: choice stack size: {}, choice stack: {}", choiceStack.size(), choiceStack);
			if (!store.propagate()) {
				throw new RuntimeException("Nothing to propagate after backtracking from conflict-causing guess. Should not happen.");
			}
		} else {
			NoGood learnedNoGood = analysisResult.learnedNoGood;
			LOGGER.debug("Learned NoGood is: {}", learnedNoGood);
			int backjumpingDecisionLevel = analysisResult.backjumpLevel;
			LOGGER.debug("Computed backjumping level: {}", backjumpingDecisionLevel);
			doBackjump(backjumpingDecisionLevel);

			int learnedNoGoodId = grounder.registerOutsideNoGood(learnedNoGood);
			NoGoodStore.ConflictCause conflictCause = store.add(learnedNoGoodId, learnedNoGood);
			if (conflictCause != null) {
				throw new RuntimeException("Learned NoGood is violated after backjumping, should not happen.");
			}
		}
		return true;
	}

	private void doBackjump(int backjumpingDecisionLevel) {
		LOGGER.debug("Backjumping to decisionLevel: {}.", backjumpingDecisionLevel);
		if (backjumpingDecisionLevel < 0) {
			throw new RuntimeException("Backjumping decision level less than 0, should not happen.");
		}
		// Remove everything above the backjumpingDecisionLevel, but keep the backjumpingDecisionLevel unchanged.
		while (assignment.getDecisionLevel() > backjumpingDecisionLevel) {
			store.backtrack();
			choiceStack.remove();
			choiceManager.backtrack();
		}
	}

	private void assignUnassignedToFalse() {
		for (Integer atom : unassignedAtoms) {
			assignment.assign(atom, FALSE, null);
		}
	}

	private List<Integer> unassignedAtoms;
	private boolean allAtomsAssigned() {
		unassignedAtoms = grounder.getUnassignedAtoms(assignment);
		return unassignedAtoms.isEmpty();
	}

	private void doBacktrack() {
		boolean repeatBacktrack;	// Iterative implementation of recursive backtracking.
		do {
			repeatBacktrack = false;
			if (isSearchSpaceExhausted()) {
				return;
			}

			int lastGuessedAtom = choiceStack.peekAtom();
			boolean lastGuessedValue = choiceStack.peekValue();
			Assignment.Entry lastChoiceEntry = assignment.get(lastGuessedAtom);

			store.backtrack();
			LOGGER.debug("Backtrack: Removing last choice, setting decision level to {}.", assignment.getDecisionLevel());

			boolean backtrackedAlready = choiceStack.peekBacktracked();
			choiceStack.remove();
			choiceManager.backtrack();
			LOGGER.debug("Backtrack: choice stack size: {}, choice stack: {}", choiceStack.size(), choiceStack);

			if (!backtrackedAlready) {
				// Chronological backtracking: guess inverse now.
				// Guess FALSE if the previous guess was for TRUE and the atom was not already MBT at that time.
				if (lastGuessedValue && MBT.equals(assignment.getTruth(lastGuessedAtom))) {
					LOGGER.debug("Backtrack: inverting last guess not possible, atom was MBT before guessing TRUE.");
					LOGGER.debug("Recursive backtracking.");
					repeatBacktrack = true;
					continue;
				}
				// If choice was assigned at lower decision level (due to added NoGoods), no inverted guess should be done.
				if (lastChoiceEntry.getImpliedBy() != null) {
					LOGGER.debug("Last choice now is implied by: {}.", lastChoiceEntry.getImpliedBy());
					if (lastChoiceEntry.getDecisionLevel() == assignment.getDecisionLevel() + 1) {
						throw new RuntimeException("Choice was assigned but not at a lower decision level. This should not happen.");
					}
					LOGGER.debug("Choice was assigned at a lower decision level");
					LOGGER.debug("Recursive backtracking.");
					repeatBacktrack = true;
					continue;
				}

				decisionCounter++;
				boolean newGuess = !lastGuessedValue;
				assignment.guess(lastGuessedAtom, newGuess);
				LOGGER.debug("Backtrack: setting decision level to {}.", assignment.getDecisionLevel());
				LOGGER.debug("Backtrack: inverting last guess. Now: {}={}@{}", grounder.atomToString(lastGuessedAtom), newGuess, assignment.getDecisionLevel());
				choiceStack.pushBacktrack(lastGuessedAtom, newGuess);
				choiceManager.nextDecisionLevel();
				LOGGER.debug("Backtrack: choice stack size: {}, choice stack: {}", choiceStack.size(), choiceStack);
				LOGGER.debug("Backtrack: {} choices so far.", decisionCounter);
			} else {
				LOGGER.debug("Recursive backtracking.");
				repeatBacktrack = true;
			}
		} while (repeatBacktrack);
	}

	private void updateGrounderAssignment() {
		grounder.updateAssignment(assignment.getNewAssignmentsIterator());
	}

	/**
	 * Obtains new NoGoods from grounder and adds them to the NoGoodStore and the heuristics.
	 * @return false iff the set of NoGoods is detected to be unsatisfiable.
	 */
	private boolean obtainNoGoodsFromGrounder() {
		Map<Integer, NoGood> obtained = grounder.getNoGoods();
		LOGGER.debug("Obtained NoGoods from grounder: {}", obtained);

		if (!obtained.isEmpty()) {
			// Record to detect propagation fixpoint, checking if new NoGoods were reported would be better here.
			didChange = true;
		}

		if (!addAllNoGoodsAndTreatContradictions(obtained)) {
			return false;
		}
		// Record choice atoms.
		final Pair<Map<Integer, Integer>, Map<Integer, Integer>> choiceAtoms = grounder.getChoiceAtoms();
		choiceManager.addChoiceInformation(choiceAtoms);
		// Inform heuristics.
		branchingHeuristic.newNoGoods(obtained.values());
		return true;
	}

	/**
	 * Adds all NoGoods in the given map to the NoGoodStore and treats eventual contradictions.
	 * If the set of NoGoods is unsatisfiable, this method returns false.
	 * @param obtained
	 * @return false iff the new set of NoGoods is detected to be unsatisfiable.
	 */
	private boolean addAllNoGoodsAndTreatContradictions(Map<Integer, NoGood> obtained) {
		LinkedList<Map.Entry<Integer, NoGood>> noGoodsToAdd = new LinkedList<>(obtained.entrySet());
		while (!noGoodsToAdd.isEmpty()) {
			Map.Entry<Integer, NoGood> noGoodEntry = noGoodsToAdd.poll();
			NoGoodStore.ConflictCause conflictCause = store.add(noGoodEntry.getKey(), noGoodEntry.getValue());
			if (conflictCause == null) {
				// There is no conflict, all is fine. Just skip conflict treatment and carry on.
				continue;
			}

			LOGGER.debug("Adding obtained NoGoods from grounder violates current assignment: learning, backjumping, and adding again.");
			if (conflictCause.violatedGuess != null) {
				LOGGER.debug("Added NoGood {} violates guess {}.", noGoodEntry.getKey(), conflictCause.violatedGuess);
				LOGGER.debug("Backjumping to decision level: {}", conflictCause.violatedGuess.getDecisionLevel());
				doBackjump(conflictCause.violatedGuess.getDecisionLevel());
				store.backtrack();
				LOGGER.debug("Backtrack: Removing last choice because of conflict with newly added NoGoods, setting decision level to {}.", assignment.getDecisionLevel());
				choiceStack.remove();
				choiceManager.backtrack();
				LOGGER.debug("Backtrack: choice stack size: {}, choice stack: {}", choiceStack.size(), choiceStack);
			} else {
				LOGGER.debug("Violated NoGood is {}. Analyzing the conflict.", conflictCause.violatedNoGood);
				GroundConflictNoGoodLearner.ConflictAnalysisResult conflictAnalysisResult = null;
				conflictAnalysisResult = learner.analyzeConflictingNoGood(conflictCause.violatedNoGood);
				if (conflictAnalysisResult.isUnsatisfiable) {
					// Halt if unsatisfiable.
					return false;
				}
				LOGGER.debug("Backjumping to decision level: {}", conflictAnalysisResult.backjumpLevel);
				doBackjump(conflictAnalysisResult.backjumpLevel);
				if (conflictAnalysisResult.clearLastGuessAfterBackjump) {
					store.backtrack();
					LOGGER.debug("Backtrack: Removing last choice because of conflict with newly added NoGoods, setting decision level to {}.", assignment.getDecisionLevel());
					choiceStack.remove();
					choiceManager.backtrack();
					LOGGER.debug("Backtrack: choice stack size: {}, choice stack: {}", choiceStack.size(), choiceStack);
				}
				// If NoGood was learned, add it to the store.
				// Note that the learned NoGood may cause further conflicts, since propagation on lower decision levels is lazy, hence backtracking once might not be enough to remove the real conflict cause.
				if (conflictAnalysisResult.learnedNoGood != null) {
					noGoodsToAdd.addFirst(new AbstractMap.SimpleEntry<>(grounder.registerOutsideNoGood(conflictAnalysisResult.learnedNoGood), conflictAnalysisResult.learnedNoGood));
				}
			}
			if (store.add(noGoodEntry.getKey(), noGoodEntry.getValue()) != null) {
				throw new RuntimeException("Re-adding of former conflicting NoGood still causes conflicts. This should not happen.");
			}
		}
		return true;
	}

	private boolean isSearchSpaceExhausted() {
		return assignment.getDecisionLevel() == 0;
	}

	private boolean propagationFixpointReached() {
		// Check if anything changed: didChange is updated in places of change.
		boolean changeCopy = didChange;
		didChange = false;
		return !changeCopy;
	}

	private void doChoice(int nextChoice) {
		decisionCounter++;
		boolean sign = branchingHeuristic.chooseSign(nextChoice);
		assignment.guess(nextChoice, sign);
		choiceStack.push(nextChoice, sign);
		choiceManager.nextDecisionLevel();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Choice: guessing {}={}@{}", grounder.atomToString(nextChoice), sign, assignment.getDecisionLevel());
			LOGGER.debug("Choice: stack size: {}, choice stack: {}", choiceStack.size(), choiceStack);
			LOGGER.debug("Choice: {} choices so far.", decisionCounter);
		}
	}

	private int computeChoice() {
		// Update ChoiceManager.
		Iterator<Assignment.Entry> it = assignment.getNewAssignmentsIterator2();
		while (it.hasNext()) {
			choiceManager.updateAssignment(it.next().getAtom());
		}
		// Run Heuristics.
		int heuristicChoice = branchingHeuristic.chooseAtom();
		if (heuristicChoice != 0) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Atom chosen by branching heuristic: {}", grounder.atomToString(heuristicChoice));
			}
			return heuristicChoice;
		}

		// TODO: remove fallback as soon as we are sure that BerkMin will always choose an atom
		LOGGER.debug("Falling back to NaiveHeuristics.");
		return fallbackBranchingHeuristic.chooseAtom();
	}
}
