/**
 * Copyright (c) 2018-2019 Siemens AG
 * All rights reserved.
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
package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.HeuristicDirective;
import at.ac.tuwien.kr.alpha.common.Literals;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveValues;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.heuristics.domspec.DomainSpecificHeuristicsStore;
import at.ac.tuwien.kr.alpha.solver.learning.GroundConflictNoGoodLearner.ConflictAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;

/**
 * Analyses information obtained from {@link HeuristicDirective}s to follow domain-specific heuristics specified within the input program.
 */
public class DomainSpecific implements BranchingHeuristic {
	private static final Logger LOGGER = LoggerFactory.getLogger(DomainSpecific.class);

	private final Assignment assignment;
	private final ChoiceManager choiceManager;
	private final BranchingHeuristic fallbackHeuristic;

	DomainSpecific(Assignment assignment, ChoiceManager choiceManager, BranchingHeuristic fallbackHeuristic) {
		this.assignment = assignment;
		this.choiceManager = choiceManager;
		this.fallbackHeuristic = fallbackHeuristic;
	}

	@Override
	public void violatedNoGood(NoGood violatedNoGood) {
		fallbackHeuristic.violatedNoGood(violatedNoGood);
	}

	@Override
	public void analyzedConflict(ConflictAnalysisResult analysisResult) {
		fallbackHeuristic.analyzedConflict(analysisResult);
	}

	@Override
	public void newNoGood(NoGood newNoGood) {
		fallbackHeuristic.newNoGood(newNoGood);
	}

	@Override
	public void newNoGoods(Collection<NoGood> newNoGoods) {
		fallbackHeuristic.newNoGoods(newNoGoods);
	}

	@Override
	public int chooseAtom(Set<Integer> admissibleChoices) {
		return atomOf(chooseLiteral(admissibleChoices));
	}

	@Override
	public int chooseLiteral(Set<Integer> admissibleChoices) {
		// Set<Integer> activeHeuristics = choiceManager.getAllActiveHeuristicAtoms();
		// Collection<Set<Integer>> heuristicsOrderedByDecreasingPriority = choiceManager.getDomainSpecificHeuristics()
		// .getHeuristicsOrderedByDecreasingPriority();
		// for (Set<Integer> potentialHeuristics : heuristicsOrderedByDecreasingPriority) {
		// ActiveHeuristicsToBodies activeHeuristicsToBodies = computeActiveHeuristicsToBodies(
		// Sets.intersection(activeHeuristics, potentialHeuristics));
		// if (activeHeuristicsToBodies.isSeveralValues()) {
		// Set<Integer> admissibleActiveChoices = activeHeuristicsToBodies.getBodiesForActiveHeuristics();
		// if (admissibleChoices != null) {
		// admissibleActiveChoices = Sets.intersection(admissibleActiveChoices, admissibleChoices);
		// }
		// return askFallbackHeuristic(activeHeuristicsToBodies, admissibleActiveChoices);
		// } else if (!activeHeuristicsToBodies.isEmpty()) {
		// return chooseAdmissibleBodyForHeuristic(activeHeuristicsToBodies, admissibleChoices);
		// }
		// // else: continue to set of heuristic values with lower priority
		// }

		// TODO: admissibleChoices
		// Set<HeuristicDirectiveValues> discardedValues = new HashSet<>();
		DomainSpecificHeuristicsStore heuristicsStore = choiceManager.getDomainSpecificHeuristics();
		int chosenLiteral = DEFAULT_CHOICE_LITERAL;
		int discardedBecauseHeadAssigned = 0;
		int discardedBecauseNoBody = 0;
		List<Integer> topIDs;
		poll: while (!(topIDs = heuristicsStore.pollIDsWithHighestPriority()).isEmpty()) {
			LOGGER.debug("Number of top IDs: " + topIDs.size());
			Collections.shuffle(topIDs); // TODO: use specific Random instance
			samePriority: for (int i = 0; i < topIDs.size(); i++) {
				int heuristicId = topIDs.get(i);
				HeuristicDirectiveValues values = heuristicsStore.getValues(heuristicId);
				if (assignment.isUnassignedOrMBT(values.getHeadAtomId())) {
					Optional<Integer> body = chooseLiteralForValues(values);
					if (body.isPresent()) {
						chosenLiteral = body.get();
						reinsertUnusedHeuristicValues(heuristicsStore, topIDs, i);
						break poll;
					} else {
						LOGGER.warn("Ground heuristic directive with head {} not applicable because no active rule can derive it.", values.getGroundHeadAtom());
						discardedBecauseNoBody++;
					}
				} else {
					discardedBecauseHeadAssigned++;
				}
				// discardedValues.add(v);
			}
		}
		LOGGER.debug("{} HeuristicDirectiveValues discarded because head already assigned", discardedBecauseHeadAssigned);
		LOGGER.debug("{} HeuristicDirectiveValues discarded because no active body found", discardedBecauseNoBody);
		// TODO: currently, we just discard values that are currently not applicable and rely on them being added again after backtracking. can we really do this?
		// heuristicsStore.offer(discardedValues);
		// LOGGER.debug("Gave {} values back to heuristics store", discardedValues.size());

		return chosenLiteral;
	}

	private Optional<Integer> chooseLiteralForValues(HeuristicDirectiveValues values) {
		Set<Integer> activeChoiceAtomsDerivingHead = choiceManager.getActiveChoiceAtomsDerivingHead(values.getHeadAtomId());
		int atom;
		if (activeChoiceAtomsDerivingHead.isEmpty()) {
			return Optional.empty();
		} else if (activeChoiceAtomsDerivingHead.size() == 1) {
			atom = activeChoiceAtomsDerivingHead.iterator().next();
		} else {
			atom = fallbackHeuristic.chooseAtom(activeChoiceAtomsDerivingHead); // TODO: intersection with admissible (?)
		}
		return Optional.ofNullable(Literals.atomToLiteral(atom, values.getSign()));
	}

	private void reinsertUnusedHeuristicValues(DomainSpecificHeuristicsStore heuristicsStore, List<Integer> topIDs, int i) {
		if (i < topIDs.size() - 1) {
			heuristicsStore.offer(topIDs.subList(i + 1, topIDs.size()));
		}
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	class ActiveHeuristicsToBodies {
		private Set<Integer> bodiesForActiveHeuristics = new HashSet<>();
		private HeuristicDirectiveValues heuristicDirectiveValues;
		private boolean severalValues;
		private Map<HeuristicDirectiveValues, Set<Integer>> activeHeuristicsToBodies = new HashMap<>();

		public void add(HeuristicDirectiveValues newValues) {
			if (!severalValues) {
				if (heuristicDirectiveValues == null) {
					heuristicDirectiveValues = newValues;
				} else if (!heuristicDirectiveValues.equals(newValues)) {
					switchToSeveralValues();
				}
			}
			addBodies(newValues);
			if (isEmpty()) {
				heuristicDirectiveValues = null;
				severalValues = false;
			}
		}

		public boolean isEmpty() {
			return bodiesForActiveHeuristics.isEmpty();
		}

		private void switchToSeveralValues() {
			severalValues = true;
			addBodiesToMap(heuristicDirectiveValues, bodiesForActiveHeuristics);
			heuristicDirectiveValues = null;
		}

		private void addBodies(HeuristicDirectiveValues newValues) {
			int headAtomId = newValues.getHeadAtomId();
			if (assignment.isUnassignedOrMBT(headAtomId)) {
				Set<Integer> activeChoiceAtomsDerivingHead = choiceManager.getActiveChoiceAtomsDerivingHead(headAtomId);
				addBodiesToSet(activeChoiceAtomsDerivingHead);
				addBodiesToMap(newValues, activeChoiceAtomsDerivingHead);
			}
		}

		private void addBodiesToSet(Set<Integer> bodies) {
			bodiesForActiveHeuristics.addAll(bodies);
			if (!severalValues) {
				activeHeuristicsToBodies.put(heuristicDirectiveValues, bodiesForActiveHeuristics);
			}
		}

		private void addBodiesToMap(HeuristicDirectiveValues values, Set<Integer> bodies) {
			if (bodies.isEmpty()) {
				return;
			}
			Set<Integer> existingBodies = activeHeuristicsToBodies.get(values);
			if (existingBodies == null) {
				existingBodies = new HashSet<>();
				activeHeuristicsToBodies.put(values, existingBodies);
			}
			existingBodies.addAll(bodies);
		}

		public Set<Integer> getBodiesForActiveHeuristics() {
			return bodiesForActiveHeuristics;
		}

		public HeuristicDirectiveValues getHeuristicDirectiveValues() {
			return heuristicDirectiveValues;
		}

		public boolean isSeveralValues() {
			return severalValues;
		}

		public Map<HeuristicDirectiveValues, Set<Integer>> getActiveHeuristicsToBodies() {
			return activeHeuristicsToBodies;
		}
	}
}
