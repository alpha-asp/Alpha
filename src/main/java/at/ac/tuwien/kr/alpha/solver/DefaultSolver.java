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
import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristic;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory.Heuristic;
import at.ac.tuwien.kr.alpha.solver.heuristics.NaiveHeuristic;
import at.ac.tuwien.kr.alpha.solver.learning.GroundConflictNoGoodLearner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.learning.GroundConflictNoGoodLearner.ConflictAnalysisResult.UNSAT;

/**
 * The new default solver employed in Alpha.
 * Copyright (c) 2016, the Alpha Team.
 */
public class DefaultSolver extends AbstractSolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSolver.class);

	private final NoGoodStore store;
	private final ChoiceManager choiceManager;
	private final WritableAssignment assignment;

	private final GroundConflictNoGoodLearner learner;

	private final BranchingHeuristic branchingHeuristic;
	private final BranchingHeuristic fallbackBranchingHeuristic;

	private boolean initialize = true;
	private int mbtAtFixpoint;

	public DefaultSolver(Grounder grounder, NoGoodStore store, WritableAssignment assignment, Random random, Heuristic branchingHeuristic, boolean debugInternalChecks) {
		super(grounder);

		this.assignment = assignment;
		this.store = store;
		this.choiceManager = new ChoiceManager(assignment, store, debugInternalChecks);
		this.learner = new GroundConflictNoGoodLearner(assignment);
		this.branchingHeuristic = BranchingHeuristicFactory.getInstance(branchingHeuristic, grounder, assignment, choiceManager, random);
		this.fallbackBranchingHeuristic = new NaiveHeuristic(choiceManager);
	}

	@Override
	protected boolean tryAdvance(Consumer<? super AnswerSet> action) {
		boolean didChange = false;

		// Initially, get NoGoods from grounder.
		if (initialize) {
			Map<Integer, NoGood> obtained = grounder.getNoGoods(assignment);
			didChange = !obtained.isEmpty();
			if (!ingest(obtained)) {
				logStats();
				return false;
			}
			initialize = false;
		} else if (assignment.getDecisionLevel() == 0) {
			logStats();
			return false;
		} else {
			// We already found one Answer-Set and are requested to find another one.
			// Create enumeration NoGood to avoid finding the same Answer-Set twice.
			final NoGood enumerationNoGood = choiceManager.computeEnumeration();
			final int backjumpLevel = assignment.minimumConflictLevel(enumerationNoGood);
			if (backjumpLevel == -1) {
				throw oops("Enumeration nogood is not violated");
			}
			if (backjumpLevel == 0) {
				// Search space exhausted (only happens if first choice is for TRUE at decision level 1 for an atom that was MBT at decision level 0 already).
				return false;
			}
			// Backjump instead of backtrackSlow, enumerationNoGood will invert last choice.
			choiceManager.backjump(backjumpLevel - 1);
			LOGGER.debug("Adding enumeration nogood: {}", enumerationNoGood);
			ConflictCause conflictCause = store.add(grounder.registerOutsideNoGood(enumerationNoGood), enumerationNoGood);
			if (conflictCause != null) {
				throw oops("Adding enumeration NoGood causes conflicts after backjump");
			}
		}

		boolean afterAllAtomsAssigned = false;

		// Try all assignments until grounder reports no more NoGoods and all of them are satisfied
		while (true) {
			ConflictCause conflictCause = store.propagate();
			didChange |= store.didPropagate();
			LOGGER.trace("Assignment after propagation is: {}", assignment);
			if (conflictCause != null) {
				// Learn from conflict.
				NoGood violatedNoGood = conflictCause.getViolatedNoGood();
				LOGGER.debug("Violating assignment is: {}", assignment);
				branchingHeuristic.violatedNoGood(violatedNoGood);
				if (!afterAllAtomsAssigned) {
					if (!learnBackjumpAddFromConflict(conflictCause)) {
						logStats();
						return false;
					}
				} else {
					// Will not learn from violated NoGood, do simple backtrackSlow.
					LOGGER.debug("NoGood was violated after all unassigned atoms were assigned to false; will not learn from it; skipping.");
					if (!backtrack()) {
						logStats();
						return false;
					}
					afterAllAtomsAssigned = false;
				}
			} else if (didChange) {
				// Ask the grounder for new NoGoods, then propagate (again).
				LOGGER.trace("Doing propagation step.");

				grounder.updateAssignment(assignment.getNewAssignmentsIterator());

				Map<Integer, NoGood> obtained = grounder.getNoGoods(assignment);
				didChange = !obtained.isEmpty();
				if (!ingest(obtained)) {
					logStats();
					return false;
				}
			} else if (choose()) {
				LOGGER.debug("Did choice.");
			} else if (close()) {
				LOGGER.debug("Closed unassigned known atoms (assigning FALSE).");
				afterAllAtomsAssigned = true;
			} else if (assignment.getMBTCount() == 0) {
				// NOTE: If we would do optimization, we would now have a guaranteed upper bound.
				AnswerSet as = translate(assignment.getTrueAssignments());
				LOGGER.debug("Answer-Set found: {}", as);
				action.accept(as);
				logStats();
				return true;
			} else {
				LOGGER.debug("Backtracking from wrong choices ({} MBTs).", assignment.getMBTCount());
				mbtAtFixpoint++;
				if (!backtrack()) {
					logStats();
					return false;
				}
				afterAllAtomsAssigned = false;
			}
		}
	}

	/**
	 * Analyzes the conflict and either learns a new NoGood (causing backjumping and addition to the NoGood store),
	 * or backtracks the choice causing the conflict.
	 * @return false iff the analysis result shows that the set of NoGoods is unsatisfiable.
	 */
	private boolean learnBackjumpAddFromConflict(ConflictCause conflictCause) {
		GroundConflictNoGoodLearner.ConflictAnalysisResult analysisResult = learner.analyzeConflictingNoGood(conflictCause.getViolatedNoGood());

		LOGGER.debug("Analysis result: {}", analysisResult);

		if (analysisResult == UNSAT) {
			// Halt if unsatisfiable.
			return false;
		}

		branchingHeuristic.analyzedConflict(analysisResult);

		if (analysisResult.learnedNoGood == null && analysisResult.clearLastChoiceAfterBackjump) {
			// TODO: Temporarily abort resolution with backtrackFast instead of learning a too large nogood.
			backtrack();
			return true;
		}

		if (analysisResult.learnedNoGood != null) {
			choiceManager.backjump(analysisResult.backjumpLevel);

			final NoGood learnedNoGood = analysisResult.learnedNoGood;
			if (store.add(grounder.registerOutsideNoGood(learnedNoGood), learnedNoGood) != null) {
				throw oops("Newly learned NoGood is violated after backjumping");
			}
			return true;
		}

		choiceManager.backjump(analysisResult.backjumpLevel);

		choiceManager.backtrackFast();
		store.propagate();
		if (!store.didPropagate()) {
			throw oops("Nothing to propagate after backtracking from conflict-causing choice");
		}

		return true;
	}

	private boolean close() {
		List<Integer> unassignedAtoms = grounder.getUnassignedAtoms(assignment);

		if (unassignedAtoms.isEmpty()) {
			return false;
		}

		for (Integer atom : unassignedAtoms) {
			assignment.assign(atom, FALSE, null);
		}

		return true;
	}

	/**
	 * Iterative implementation of recursive backtracking.
	 *
	 * @return {@code true} iff it is possible to backtrack even further, {@code false} otherwise
	 */
	private boolean backtrack() {
		while (assignment.getDecisionLevel() != 0) {
			final Assignment.Entry choice = choiceManager.backtrackSlow();

			if (choice == null) {
				LOGGER.debug("Backtracking further, because last choice was already backtracked.");
				continue;
			}

			final int lastChoice = choice.getAtom();
			final boolean choiceValue = choice.getTruth().toBoolean();

			// Chronological backtracking: choose inverse now.
			// Choose FALSE if the previous choice was for TRUE and the atom was not already MBT at that time.
			if (choiceValue && MBT.equals(assignment.getTruth(lastChoice))) {
				LOGGER.debug("Backtracking further, because last choice was MBT before choosing TRUE.");
				continue;
			}

			// If choice was assigned at lower decision level (due to added NoGoods), no inverted choice should be done.
			if (choice.getImpliedBy() != null) {
				LOGGER.debug("Last choice is now implied by {}", choice.getImpliedBy());
				//if (choice.getDecisionLevel() == assignment.getDecisionLevel() + 1) {
				//	throw oops("Choice was assigned but not at a lower decision level");
				//}
				LOGGER.debug("Backtracking further, because last choice was assigned at a lower decision level.");
				continue;
			}

			LOGGER.debug("Choosing inverse.");
			choiceManager.choose(new Choice(lastChoice, !choiceValue, true));
			break;
		}

		return assignment.getDecisionLevel() != 0;
	}

	private boolean ingest(Map<Integer, NoGood> obtained) {
		branchingHeuristic.newNoGoods(obtained.values());
		assignment.growForMaxAtomId(grounder.getMaxAtomId());

		LinkedList<Map.Entry<Integer, NoGood>> noGoodsToAdd = new LinkedList<>(obtained.entrySet());
		Map.Entry<Integer, NoGood> entry;
		while ((entry = noGoodsToAdd.poll()) != null) {
			if (NoGood.UNSAT.equals(entry.getValue())) {
				// Empty NoGood cannot be satisfied, program is unsatisfiable.
				return false;
			}

			final ConflictCause conflictCause = store.add(entry.getKey(), entry.getValue());
			if (conflictCause == null) {
				// There is no conflict, all is fine. Just skip conflict treatment and carry on.
				continue;
			}

			final NoGood learnedNoGood = fixContradiction(entry, conflictCause);
			if (learnedNoGood != null) {
				noGoodsToAdd.addFirst(new AbstractMap.SimpleEntry<>(grounder.registerOutsideNoGood(learnedNoGood), learnedNoGood));
			}
		}
		return true;
	}

	private NoGood fixContradiction(Map.Entry<Integer, NoGood> noGoodEntry, ConflictCause conflictCause) {
		LOGGER.debug("Attempting to fix violation of {} caused by {}", noGoodEntry.getValue(), conflictCause);

		if (conflictCause.getViolatedChoice() != null) {
			choiceManager.backjump(conflictCause.getViolatedChoice().getDecisionLevel());
			choiceManager.backtrackFast();
			return null;
		}

		GroundConflictNoGoodLearner.ConflictAnalysisResult conflictAnalysisResult = learner.analyzeConflictingNoGood(conflictCause.getViolatedNoGood());
		if (conflictAnalysisResult == UNSAT) {
			return NoGood.UNSAT;
		}
		branchingHeuristic.analyzedConflict(conflictAnalysisResult);

		choiceManager.backjump(conflictAnalysisResult.backjumpLevel);
		if (conflictAnalysisResult.clearLastChoiceAfterBackjump) {
			choiceManager.backtrackFast();
		}

		// If NoGood was learned, add it to the store.
		// Note that the learned NoGood may cause further conflicts, since propagation on lower decision levels is lazy,
		// hence backtracking once might not be enough to remove the real conflict cause.
		if (store.add(noGoodEntry.getKey(), noGoodEntry.getValue()) != null) {
			throw oops("Re-adding of former conflicting NoGood still causes conflicts");
		}

		return conflictAnalysisResult.learnedNoGood;
	}

	private boolean choose() {
		choiceManager.addChoiceInformation(grounder.getChoiceAtoms());
		choiceManager.updateAssignments();

		// Hint: for custom heuristics, evaluate them here and pick a value if the heuristics suggests one.

		int atom;

		if ((atom = branchingHeuristic.chooseAtom()) == 0) {
			if ((atom = fallbackBranchingHeuristic.chooseAtom()) == 0) {
				LOGGER.debug("No choices!");
				return false;
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Naive heuristics chose atom {}", grounder.atomToString(atom));
			}
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Branching heuristic chose atom {}", grounder.atomToString(atom));
		}

		choiceManager.choose(new Choice(atom, branchingHeuristic.chooseSign(atom), false));
		return true;
	}
	
	private void logStats() {
		LOGGER.debug("g=" + choiceManager.getChoices() + ", bt=" + choiceManager.getBacktracks() + ", bj=" + choiceManager.getBackjumps() + ", mbt=" + mbtAtFixpoint);
	}
}