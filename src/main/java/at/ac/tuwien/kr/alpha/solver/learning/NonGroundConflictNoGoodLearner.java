/*
 * Copyright (c) 2020 Siemens AG
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
package at.ac.tuwien.kr.alpha.solver.learning;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.NonGroundNoGood;
import at.ac.tuwien.kr.alpha.common.Unifier;
import at.ac.tuwien.kr.alpha.common.UniqueVariableNames;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.solver.Antecedent;
import at.ac.tuwien.kr.alpha.solver.TrailAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.Util.collectionToIntArray;
import static at.ac.tuwien.kr.alpha.Util.intArrayToLinkedHashSet;
import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.common.Literals.isPositive;
import static at.ac.tuwien.kr.alpha.common.Literals.negateLiteral;
import static at.ac.tuwien.kr.alpha.solver.Atoms.FALSUM;

/**
 * Conflict-driven learning on ground clauses that also learns non-ground nogoods.
 * <p>
 * This class builds upon an underlying {@link GroundConflictNoGoodLearner}, to which it acts as a proxy for all operations
 * not implemented on the non-ground level.
 * The non-ground learning capabilities implemented here are not designed for efficiency (yet), so this class should only
 * be used in an "offline" conflict generalisation mode.
 */
public class NonGroundConflictNoGoodLearner implements ConflictNoGoodLearner {
	private static final Logger LOGGER = LoggerFactory.getLogger(NonGroundConflictNoGoodLearner.class);

	private final Assignment assignment;
	private final AtomStore atomStore;
	private final GroundConflictNoGoodLearner groundLearner;

	private Map<NonGroundNoGood, Integer> nonGroundNoGoodViolationCounter = new HashMap<>();
	private Map<NonGroundNoGood, Set<NonGroundNoGood>> learnedOnFirstUIP = new HashMap<>();
	private Map<NonGroundNoGood, Set<NonGroundNoGood>> learnedOnLastUIP = new HashMap<>();
	private Map<NonGroundNoGood, Set<NonGroundNoGood>> learnedOnOtherUIPs = new HashMap<>();

	public NonGroundConflictNoGoodLearner(Assignment assignment, AtomStore atomStore, GroundConflictNoGoodLearner groundLearner) {
		this.assignment = assignment;
		this.atomStore = atomStore;
		this.groundLearner = groundLearner;
	}

	@Override
	public int computeConflictFreeBackjumpingLevel(NoGood noGood) {
		return groundLearner.computeConflictFreeBackjumpingLevel(noGood);
	}

	@Override
	public ConflictAnalysisResult analyzeConflictFromAddingNoGood(Antecedent antecedent) {
		return groundLearner.analyzeConflictFromAddingNoGood(antecedent);
	}

	@Override
	public ConflictAnalysisResult analyzeConflictingNoGood(Antecedent violatedNoGood) {
		LOGGER.trace("Analyzing violated nogood: {}", violatedNoGood);
		return analyzeConflictingNoGoodAndGeneraliseConflict(violatedNoGood);
	}

	/**
	 * Analyzes a conflict and learns both a ground nogood (if possible) and one or more non-ground nogoods (if possible).
	 * <p>
	 * This method also contains an implementation of first UIP learning that is redundant to the one in
	 * {@link GroundConflictNoGoodLearner#analyzeTrailBased(Antecedent, boolean)} on purpose:
	 * While the other implementation is designed for efficiency, this one is designed to be easily understood such that
	 * the connection to conflict generalisation (non-ground conflict learning) becomes apparent.
	 * This implementation also uses the other one internally to check the correctness of learned ground nogoods.
	 *
	 * @param violatedNoGood the violated nogood to start analysis from
	 * @return an analysis result, possibly including a learned ground nogood and one or more learned non-ground nogoods
	 */
	ConflictAnalysisResult analyzeConflictingNoGoodAndGeneraliseConflict(Antecedent violatedNoGood) {
		if (assignment.getDecisionLevel() == 0) {
			LOGGER.trace("Conflict on decision level 0.");
			return ConflictAnalysisResult.UNSAT;
		}

		final TrailAssignment.TrailBackwardsWalker trailWalker = ((TrailAssignment) assignment).getTrailBackwardsWalker();
		NoGood firstLearnedNoGood = null;
		NonGroundNoGood firstLearnedNonGroundNoGood = null;
		List<NoGood> additionalLearnedNoGoods = new ArrayList<>();
		List<NonGroundNoGood> additionalLearnedNonGroundNoGoods = new ArrayList<>();
		GroundAndNonGroundNoGood currentNoGood = new GroundAndNonGroundNoGood(violatedNoGood);
		while (makeOneResolutionStep(currentNoGood, trailWalker)) {
			if (containsUIP(currentNoGood)) {
				final NoGood learntNoGood = createLearntNoGood(currentNoGood);
				final NonGroundNoGood learntNonGroundNoGood = currentNoGood.toNonGroundNoGood();
				learntNoGood.setNonGroundNoGood(learntNonGroundNoGood);
				if (firstLearnedNoGood == null) {
					firstLearnedNoGood = learntNoGood;
					firstLearnedNonGroundNoGood = learntNonGroundNoGood;
				} else {
					additionalLearnedNoGoods.add(learntNoGood);
					if (learntNonGroundNoGood != null) {
						additionalLearnedNonGroundNoGoods.add(learntNonGroundNoGood);
					}
				}
			}
		}
		final ConflictAnalysisResult groundAnalysisResultNotMinimized = groundLearner.analyzeTrailBased(violatedNoGood, false);
		if (!Objects.equals(groundAnalysisResultNotMinimized.learnedNoGood, firstLearnedNoGood)) {
			throw oops("Learned nogood is not the same as the one computed by ground analysis");
		}
		final ConflictAnalysisResult analysisResult = groundLearner.analyzeConflictingNoGood(violatedNoGood);
		if (analysisResult.backjumpLevel >= 0) {
			// if backJumpLevel < 0, then problem is UNSAT (ground analysis result does additional checks for this)
			analysisResult.setLearnedNonGroundNoGood(firstLearnedNonGroundNoGood);
			LOGGER.info("Learned non-ground nogood for ground conflict {}: {}", violatedNoGood, firstLearnedNonGroundNoGood);
			if (!additionalLearnedNoGoods.isEmpty()) {
				analysisResult.addLearnedNoGoods(additionalLearnedNoGoods);
				analysisResult.addLearnedNonGroundNoGoods(additionalLearnedNonGroundNoGoods);
				LOGGER.info("Additionally learned non-ground nogoods: {}", additionalLearnedNonGroundNoGoods);
			}
			countAndRememberLearnedNonGroundNoGoods(violatedNoGood, analysisResult);
		}
		return analysisResult;
	}

	/**
	 * Resolves the current nogood with the antecedent of one of its literals assigned on the current decision level.
	 * The given {@code currentNoGood} is modified in situ.
	 *
	 * @param currentNoGood the nogood to resolve with the antecedent of one of its literals
	 * @param trailWalker   a backwards-walker on the trail
	 * @return {@code true} if resolution succeeded, or {@code false} if no further resolution step is possible
	 */
	private boolean makeOneResolutionStep(GroundAndNonGroundNoGood currentNoGood, TrailAssignment.TrailBackwardsWalker trailWalker) {
		if (!containsLiteralForResolution(currentNoGood)) {
			return false;
		}
		final int literal = findNextLiteralToResolve(currentNoGood, trailWalker);
		resolve(currentNoGood, literal);
		return true;
	}

	private Integer findNextLiteralToResolve(GroundAndNonGroundNoGood noGood, TrailAssignment.TrailBackwardsWalker trailWalker) {
		final int currentDecisionLevel = assignment.getDecisionLevel();
		int literal;
		int atom;
		do {
			literal = trailWalker.getNextLowerLiteral();
			atom = atomOf(literal);
		} while (assignment.getWeakDecisionLevel(atom) != currentDecisionLevel || assignment.getImpliedBy(atom) == null || !noGood.groundNoGood.contains(literal));
		return literal;
	}

	private void resolve(GroundAndNonGroundNoGood noGood1, int literal1) {
		final Antecedent antecedentOfLiteral = assignment.getImpliedBy(atomOf(literal1));
		noGood1.resolveWith(antecedentOfLiteral, literal1);
	}

	private boolean containsLiteralForResolution(GroundAndNonGroundNoGood noGood) {
		final int currentDecisionLevel = assignment.getDecisionLevel();
		for (Integer literal : noGood.groundNoGood) {
			final int atom = atomOf(literal);
			if (assignment.getWeakDecisionLevel(atom) == currentDecisionLevel && assignment.getImpliedBy(atom) != null) {
				return true;
			}
		}
		return false;
	}

	private boolean containsUIP(GroundAndNonGroundNoGood resolvent) {
		final int currentDecisionLevel = assignment.getDecisionLevel();
		boolean containsLiteralOnCurrentDecisionLevel = false;
		for (int literal : resolvent.groundNoGood) {
			if (assignment.getWeakDecisionLevel(atomOf(literal)) == currentDecisionLevel) {
				if (containsLiteralOnCurrentDecisionLevel) {
					return false;
				} else {
					containsLiteralOnCurrentDecisionLevel = true;
				}
			}
		}
		// TODO: is it problematic if containsLiteralOnCurrentDecisionLevel == false?
		return containsLiteralOnCurrentDecisionLevel;
	}

	private NoGood createLearntNoGood(GroundAndNonGroundNoGood resolvent) {
		return NoGood.learnt(collectionToIntArray(resolvent.groundNoGood));
	}

	private static List<? extends Literal> getAdditionalLiterals(NonGroundNoGood nonGroundNoGood, int numberOfAlreadyConsideredLiterals) {
		final List<Literal> result = new ArrayList<>(nonGroundNoGood.size() - numberOfAlreadyConsideredLiterals);
		for (int i = numberOfAlreadyConsideredLiterals; i < nonGroundNoGood.size(); i++) {
			result.add(nonGroundNoGood.getLiteral(i));
		}
		return result;
	}

	private void countAndRememberLearnedNonGroundNoGoods(Antecedent violatedNoGood, ConflictAnalysisResult analysisResult) {
		if (violatedNoGood.getOriginalNoGood() == null) {
			return;
		}
		final NonGroundNoGood violatedNonGroundNoGood = violatedNoGood.getOriginalNoGood().getNonGroundNoGood();
		if (violatedNonGroundNoGood == null) {
			return;
		}
		final NonGroundNoGood learnedNonGroundNoGood = analysisResult.getLearnedNonGroundNoGood();
		final List<NonGroundNoGood> additionalLearnedNonGroundNoGoods = analysisResult.getAdditionalLearnedNonGroundNoGoods();
		if (!nonGroundNoGoodViolationCounter.containsKey(violatedNonGroundNoGood)) {
			nonGroundNoGoodViolationCounter.put(violatedNonGroundNoGood, 0);
			learnedOnFirstUIP.put(violatedNonGroundNoGood, new HashSet<>());
			learnedOnLastUIP.put(violatedNonGroundNoGood, new HashSet<>());
			learnedOnOtherUIPs.put(violatedNonGroundNoGood, new HashSet<>());
		}
		nonGroundNoGoodViolationCounter.computeIfPresent(violatedNonGroundNoGood, (n, c) -> c + 1);
		if (learnedNonGroundNoGood != null) {
			learnedOnFirstUIP.get(violatedNonGroundNoGood).add(learnedNonGroundNoGood);
		}
		if (!additionalLearnedNonGroundNoGoods.isEmpty()) {
			learnedOnLastUIP.get(violatedNonGroundNoGood).add(additionalLearnedNonGroundNoGoods.get(additionalLearnedNonGroundNoGoods.size() - 1));
			for (int i = 0; i < additionalLearnedNonGroundNoGoods.size() - 1; i++) {
				learnedOnOtherUIPs.get(violatedNonGroundNoGood).add(additionalLearnedNonGroundNoGoods.get(i));
			}
		}
	}

	public void logLearnedNonGroundNoGoods() {
		LOGGER.info("LEARNED NON-GROUND NOGOODS:");
		for (NonGroundNoGood violatedNonGroundNoGood : nonGroundNoGoodViolationCounter.keySet()) {
			LOGGER.info("Violated {} times: {}", nonGroundNoGoodViolationCounter.get(violatedNonGroundNoGood), violatedNonGroundNoGood);
			LOGGER.info("Learned on first UIP: {}", learnedOnFirstUIP.get(violatedNonGroundNoGood));
			LOGGER.info("Learned on last UIP: {}", learnedOnLastUIP.get(violatedNonGroundNoGood));
			LOGGER.info("Learned on other UIPs: {}", learnedOnOtherUIPs.get(violatedNonGroundNoGood));
		}
	}

	class GroundAndNonGroundNoGood {

		final UniqueVariableNames uniqueVariableNames = new UniqueVariableNames();

		Set<Integer> groundNoGood;
		Map<Integer, Atom> mapToNonGroundAtoms = new HashMap<>();
		Set<Literal> additionalNonGroundLiterals = new LinkedHashSet<>();

		boolean canLearnNonGround;

		public GroundAndNonGroundNoGood(Antecedent antecedent) {
			this.groundNoGood = intArrayToLinkedHashSet(antecedent.getReasonLiterals());
			canLearnNonGround = digestOriginalNonGroundNoGood(antecedent, atomToLiteral(FALSUM));
		}

		private boolean digestOriginalNonGroundNoGood(Antecedent antecedent, int impliedLiteral) {
			final NoGood originalNoGood = antecedent.getOriginalNoGood();
			if (originalNoGood == null) {
				LOGGER.warn("Cannot generalise conflict because original nogood unknown for " + antecedent);
				return false;
			}
			NonGroundNoGood nonGroundNoGood = originalNoGood.getNonGroundNoGood();
			if (nonGroundNoGood == null) {
				LOGGER.warn("Cannot generalise conflict because non-ground nogood unknown for " + atomStore.noGoodToString(originalNoGood));
				return false;
			}

			nonGroundNoGood = uniqueVariableNames.makeVariableNamesUnique(nonGroundNoGood);
			final Unifier unifier = new Unifier();
			assert (impliedLiteral == atomToLiteral(FALSUM)) == mapToNonGroundAtoms.isEmpty(); // impliedLiteral is only FALSUM for violated nogood
			if (!mapToNonGroundAtoms.isEmpty()) {
				// if we already have atoms stored, we need to unify conflicting non-ground atoms:
				final  Unifier conflictUnifier = new Unifier();
				for (int literal : antecedent) {
					final Literal nonGroundLiteral = findNonGroundLiteral(literal, originalNoGood, nonGroundNoGood);
					final Atom nonGroundAtom = nonGroundLiteral.getAtom();
					final Atom existingNonGroundAtom = mapToNonGroundAtoms.get(atomOf(literal));
					if (literal == negateLiteral(impliedLiteral)) {
						// this is the literal used for resolution
						assert existingNonGroundAtom != null;
						if (!unifier.unify(nonGroundAtom, existingNonGroundAtom)) {
							// try unification in other direction (this time with conflictUnifier, because this will be applied to original nogood):
							if (!conflictUnifier.unify(existingNonGroundAtom, nonGroundAtom)) {
								throw new IllegalArgumentException("Cannot unify existing non-ground atom " + existingNonGroundAtom + " with new atom " + nonGroundAtom);
							}
						}
					} else if (existingNonGroundAtom != null) {
						// this is another atom that already exists in the current nogood. It may be that because of this,
						// the current nogood uses two variable names for what should be the same variable.
						// Therefore, additional unification may be necessary in the current nogood:
						// all atoms that are substituted in the conflicting atom must also be substituted in the existing atom
						if (!conflictUnifier.unify(existingNonGroundAtom, nonGroundAtom)) {
							throw new IllegalArgumentException("Cannot unify existing non-ground atom " + existingNonGroundAtom + " with conflicting atom " + nonGroundAtom);
						}
					}
				}
				final Unifier mergedUnifier = Unifier.mergeIntoLeft(conflictUnifier, unifier);
				mapToNonGroundAtoms.replaceAll((a, n) -> n.substitute(mergedUnifier));
				additionalNonGroundLiterals = additionalNonGroundLiterals.stream().map(n -> n.substitute(mergedUnifier)).collect(Collectors.toCollection(LinkedHashSet::new));
			}
			for (int literal : antecedent) {
				final Atom unifiedNonGroundAtom = findNonGroundLiteral(literal, originalNoGood, nonGroundNoGood).getAtom().substitute(unifier);
				final Atom existingNonGroundAtom = mapToNonGroundAtoms.get(atomOf(literal));
				if (existingNonGroundAtom != null) {
					if (!existingNonGroundAtom.equals(unifiedNonGroundAtom)) {
						throw oops("Existing non-ground atom " + existingNonGroundAtom + " for " + atomOf(literal) + " does not match unified atom " + unifiedNonGroundAtom);
					}
				} else {
					mapToNonGroundAtoms.put(atomOf(literal), unifiedNonGroundAtom);
				}
			}

			for (Literal additionalLiteral : getAdditionalLiterals(nonGroundNoGood, antecedent.size())) {
				additionalNonGroundLiterals.add(additionalLiteral.substitute(unifier));
			}
			return true;
		}

		private Literal findNonGroundLiteral(int literal, NoGood originalNoGood, NonGroundNoGood nonGroundNoGood) {
			// index must be looked up because literals may have different order in antecedent than in original nogood:
			final int indexOfLiteralInOriginalNogood = originalNoGood.indexOf(literal);
			final Literal nonGroundLiteral = nonGroundNoGood.getLiteral(indexOfLiteralInOriginalNogood);
			if (!nonGroundLiteral.getPredicate().equals(atomStore.get(atomOf(literal)).getPredicate())) {
				throw oops("Wrong non-ground literal assigned to ground literal");
				// TODO: execute this and other checks only if internal checks enabled (?)
			}
			return nonGroundLiteral;
		}

		void resolveWith(Antecedent antecedent, int literalInThisNoGood) {
			canLearnNonGround = canLearnNonGround && digestOriginalNonGroundNoGood(antecedent, literalInThisNoGood);
			this.groundNoGood.remove(literalInThisNoGood);
			for (int literal : antecedent.getReasonLiterals()) {
				if (literal != negateLiteral(literalInThisNoGood)) {
					this.groundNoGood.add(literal);
				}
			}
		}

		NonGroundNoGood toNonGroundNoGood() {
			if (!canLearnNonGround) {
				return null;
			}
			final List<Literal> literals = new ArrayList<>();
			for (int literal : groundNoGood) {
				literals.add(mapToNonGroundAtoms.get(atomOf(literal)).toLiteral(isPositive(literal)));
			}
			literals.addAll(additionalNonGroundLiterals);
			return NonGroundNoGood.learnt(literals);
		}

		@Override
		public String toString() {
			return String.valueOf(toNonGroundNoGood());
		}
	}

}
