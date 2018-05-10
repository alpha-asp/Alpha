/**
 * Copyright (c) 2018 Siemens AG
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
import at.ac.tuwien.kr.alpha.common.Literals;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveValues;
import at.ac.tuwien.kr.alpha.grounder.atoms.HeuristicAtom;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.learning.GroundConflictNoGoodLearner.ConflictAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.Util.union;
import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isPositive;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

/**
 * Analyses {@link HeuristicAtom}s to follow domain-specific heuristics specified within the input program.
 * 
 * Each rule can contain a negative {@code _h(W,L)} atom where {@code W} denotes the rule's weight and {@code L} denotes the rule's level.
 * Both values default to 1.
 * When asked for a choice, the domain-specific heuristics will choose to fire from the applicable rules with the highest weight one of those with the highest
 * level.
 */
public class DomainSpecific implements BranchingHeuristic {
	private static final Logger LOGGER = LoggerFactory.getLogger(DomainSpecific.class);
	static final int DEFAULT_CHOICE_ATOM = 0;

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
	public int chooseLiteral() {
		// TODO: improve performance of this method (and called methods)
		Set<Integer> activeHeuristics = choiceManager.getAllActiveHeuristicAtoms();
		Collection<Set<Integer>> heuristicsOrderedByDecreasingPriority = choiceManager.getDomainSpecificHeuristics()
				.getHeuristicsOrderedByDecreasingPriority();
		for (Set<Integer> potentialHeuristics : heuristicsOrderedByDecreasingPriority) {
			Map<HeuristicDirectiveValues, Set<Integer>> activeHeuristicsToBodies = new HashMap<>();
			for (int h : potentialHeuristics) {
				HeuristicDirectiveValues values = choiceManager.getDomainSpecificHeuristics().getValues(h);
				int headAtomId = values.getHeadAtomId();
				if (activeHeuristics.contains(h) && isUnassigned(headAtomId)) {
					// TODO: do we mention in paper that MBT heads can be chosen?
					Set<Integer> activeChoiceAtomsDerivingHead = choiceManager.getActiveChoiceAtomsDerivingHead(headAtomId);
					if (!activeChoiceAtomsDerivingHead.isEmpty()) {
						activeHeuristicsToBodies.put(values, activeChoiceAtomsDerivingHead);
					}
				}
			}
			if (activeHeuristicsToBodies.size() > 1) {
				Set<Integer> admissibleChoices = union(activeHeuristicsToBodies.values());
				return askFallbackHeuristic(activeHeuristicsToBodies, admissibleChoices);
			} else if (activeHeuristicsToBodies.size() == 1) {
				Entry<HeuristicDirectiveValues, Set<Integer>> activeHeadToBodies = activeHeuristicsToBodies.entrySet().iterator().next();
				HeuristicDirectiveValues heuristic = activeHeadToBodies.getKey();
				Set<Integer> bodies = activeHeadToBodies.getValue();
				if (bodies.isEmpty()) {
					throw oops("Set of active bodies for heuristic is empty: " + heuristic);
				} else if (bodies.size() > 1) {
					return askFallbackHeuristic(activeHeuristicsToBodies, bodies);
				} else {
					return Literals.fromAtomAndSign(bodies.iterator().next(), heuristic.getSign());
				}
			}
			// else: continue to set of heuristic values with lower priority
		}

		return DEFAULT_CHOICE_LITERAL;
	}

	private int askFallbackHeuristic(Map<HeuristicDirectiveValues, Set<Integer>> activeHeuristicsToBodies, Set<Integer> admissibleChoices) {
		// sign of literal chosen by fallback heuristic is ignored
		// TODO: ask fallback heuristic only for atom?!
		int literal = fallbackHeuristic.chooseLiteral(admissibleChoices);
		int atom = atomOf(literal);
		boolean sign = isPositive(literal);
		for (Entry<HeuristicDirectiveValues, Set<Integer>> entry : activeHeuristicsToBodies.entrySet()) {
			if (entry.getValue().contains(atom)) {
				sign = entry.getKey().getSign();
			}
		}
		return Literals.fromAtomAndSign(atom, sign);
	}

	@Override
	public int chooseLiteral(Set<Integer> admissibleChoices) {
		// TODO: implement
		// Optional<Integer> chosenAtom = choiceManager.getDomainSpecificHeuristics().streamEntriesOrderedByDecreasingPriority()
		// .map(s -> keepAdmissibleEntries(s, admissibleChoices)).map(c -> chooseFromEquallyWeighted(c))
		// .filter(a -> a != DEFAULT_CHOICE_ATOM).findFirst();
		// return chooseOrFallback(chosenAtom);
		return DEFAULT_CHOICE_LITERAL;
	}

	// private Set<Entry> keepAdmissibleEntries(Set<Entry> entries, Set<Integer> admissibleChoices) {
	// Set<Entry> filtered = new HashSet<>();
	// for (Entry entry : entries) {
	// if (admissibleChoices.contains(entry.getChoicePoint())) {
	// filtered.add(entry);
	// }
	// }
	// return filtered;
	// }
	//
	// private int chooseFromEquallyWeighted(Set<Entry> possibleChoices) {
	// DomainSpecificHeuristicsStore domainSpecificHeuristics = choiceManager.getDomainSpecificHeuristics();
	// int chosenAtom;
	// Set<Entry> filteredChoices = possibleChoices.stream()
	// .filter(e -> !e.isDefaultPriority()) // entries with default prio are handled together with unsatisfied conditions in chooseOrFallback
	// .filter(e -> choiceManager.isActiveChoiceAtom(e.getChoicePoint()))
	// .filter(e -> isUnassigned(e.getChoicePoint()))
	// .filter(e -> domainSpecificHeuristics.isConditionSatisfied(e.getChoicePoint(), assignment))
	// .collect(Collectors.toSet());
	// if (filteredChoices.isEmpty()) {
	// chosenAtom = DEFAULT_CHOICE_ATOM;
	// } else if (filteredChoices.size() == 1) {
	// Entry chosenEntry = filteredChoices.iterator().next();
	// chosenAtom = chosenEntry.getChoicePoint();
	// LOGGER.debug("Unique best choice in terms of domain-specific heuristics: " + chosenEntry);
	// } else {
	// LOGGER.debug("Using fallback heuristics to choose from " + filteredChoices);
	// chosenAtom = fallbackHeuristic.chooseLiteral(filteredChoices.stream().map(Entry::getChoicePoint).collect(Collectors.toSet()));
	// }
	// return chosenAtom;
	// }
	//
	// private int chooseOrFallback(Optional<Integer> chosenAtom) {
	// if (chosenAtom.isPresent()) {
	// return chosenAtom.get();
	// } else {
	// Set<Integer> ruleAtomsWithDefaultPriority = choiceManager.getDomainSpecificHeuristics().getChoicePointsWithDefaultPriority(assignment);
	// if (!ruleAtomsWithDefaultPriority.isEmpty()) {
	// LOGGER.debug("Using fallback heuristics to choose from atoms with default priority: " + ruleAtomsWithDefaultPriority);
	// return fallbackHeuristic.chooseLiteral(ruleAtomsWithDefaultPriority);
	// } else {
	// LOGGER.debug("Using fallback heuristics to choose");
	// return fallbackHeuristic.chooseLiteral();
	// }
	// }
	// }

	protected boolean isUnassigned(int atom) {
		ThriceTruth truth = assignment.getTruth(atom);
		return truth != FALSE && truth != TRUE; // do not use assignment.isAssigned(atom) because we may also choose MBTs
	}
}
