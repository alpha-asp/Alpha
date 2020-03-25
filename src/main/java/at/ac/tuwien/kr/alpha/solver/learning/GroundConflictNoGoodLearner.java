/*
 * Copyright (c) 2016-2020, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.solver.learning;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.NonGroundNoGood;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.solver.Antecedent;
import at.ac.tuwien.kr.alpha.solver.TrailAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.common.Literals.isPositive;
import static at.ac.tuwien.kr.alpha.solver.NoGoodStore.LBD_NO_VALUE;

/**
 * Conflict-driven learning on ground clauses.
 *
 * Copyright (c) 2016-2020, the Alpha Team.
 */
public class GroundConflictNoGoodLearner {
	private static final Logger LOGGER = LoggerFactory.getLogger(GroundConflictNoGoodLearner.class);

	private final Assignment assignment;
	private final AtomStore atomStore;

	private final boolean conflictGeneralisationEnabled = true; // TODO: make parameterisable

	public int computeConflictFreeBackjumpingLevel(NoGood violatedNoGood) {
		int highestDecisionLevel = -1;
		int secondHighestDecisionLevel = -1;
		int numAtomsInHighestLevel = 0;
		int[] reasonLiterals = violatedNoGood.asAntecedent().getReasonLiterals();
		for (int literal : reasonLiterals) {
			int literalDecisionLevel = assignment.getRealWeakDecisionLevel(atomOf(literal));
			if (literalDecisionLevel == highestDecisionLevel) {
				numAtomsInHighestLevel++;
			} else if (literalDecisionLevel > highestDecisionLevel) {
				secondHighestDecisionLevel = highestDecisionLevel;
				highestDecisionLevel = literalDecisionLevel;
				numAtomsInHighestLevel = 1;
			} else if (literalDecisionLevel > secondHighestDecisionLevel) {
				secondHighestDecisionLevel = literalDecisionLevel;
			}
		}
		if (numAtomsInHighestLevel == 1) {
			return secondHighestDecisionLevel;
		}
		return highestDecisionLevel - 1;
	}

	public static class ConflictAnalysisResult {
		public static final ConflictAnalysisResult UNSAT = new ConflictAnalysisResult();

		public final NoGood learnedNoGood;
		public final int backjumpLevel;
		public final Collection<Integer> resolutionAtoms;
		public final int lbd;

		private ConflictAnalysisResult() {
			learnedNoGood = null;
			backjumpLevel = -1;
			resolutionAtoms = null;
			lbd = LBD_NO_VALUE;
		}

		public ConflictAnalysisResult(NoGood learnedNoGood, int backjumpLevel, Collection<Integer> resolutionAtoms) {
			this(learnedNoGood, backjumpLevel, resolutionAtoms, LBD_NO_VALUE);
		}

		public ConflictAnalysisResult(NoGood learnedNoGood, int backjumpLevel, Collection<Integer> resolutionAtoms, int lbd) {
			if (backjumpLevel < 0) {
				throw oops("Backjumping level is smaller than 0");
			}

			this.learnedNoGood = learnedNoGood;
			this.backjumpLevel = backjumpLevel;
			this.resolutionAtoms = resolutionAtoms;
			this.lbd = lbd;
		}

		@Override
		public String toString() {
			if (this == UNSAT) {
				return "UNSATISFIABLE";
			}
			return learnedNoGood + "@" + backjumpLevel;
		}
	}

	public GroundConflictNoGoodLearner(Assignment assignment, AtomStore atomStore) {
		this.assignment = assignment;
		this.atomStore = atomStore;
	}

	public ConflictAnalysisResult analyzeConflictingNoGood(Antecedent violatedNoGood) {
		LOGGER.trace("Analyzing violated nogood: {}", violatedNoGood);
		return analyzeTrailBased(violatedNoGood);
	}

	public ConflictAnalysisResult analyzeConflictFromAddingNoGood(Antecedent violatedNoGood) {
		LOGGER.trace("Analyzing conflict caused by adding the (violated) nogood: {}", violatedNoGood);
		// Simply compute appropriate backjumping level.
		int removingConflict = backjumpLevelRemovingConflict(violatedNoGood);
		if (removingConflict < 0) {
			return ConflictAnalysisResult.UNSAT;
		}
		return new ConflictAnalysisResult(null, removingConflict, Collections.emptyList(), LBD_NO_VALUE);
	}

	private int backjumpLevelRemovingConflict(Antecedent violatedNoGood) {
		int highestDL = 0;
		int[] reasonLiterals = violatedNoGood.getReasonLiterals();
		for (int literal : reasonLiterals) {
			int literalDL = assignment.getWeakDecisionLevel(atomOf(literal));
			if (literalDL > highestDL) {
				highestDL = literalDL;
			}
		}
		return highestDL - 1;
	}

	private String reasonsToString(Collection<Integer> reasons) {
		StringBuilder sb = new StringBuilder("{");
		for (int reasonLiteral : reasons) {
			sb.append(isPositive(reasonLiteral) ? "+" + atomOf(reasonLiteral) : "-" + atomOf(reasonLiteral));
			sb.append("@");
			sb.append(assignment.getWeakDecisionLevel(atomOf(reasonLiteral)));
			sb.append(", ");
		}
		sb.append("}");
		return sb.toString();
	}

	private String reasonsToString(int[] reasons) {
		return reasonsToString(IntStream.of(reasons != null ? reasons : new int[0]).boxed().collect(Collectors.toList()));
	}

	private ConflictAnalysisResult analyzeTrailBased(Antecedent conflictReason) {
		LOGGER.trace("Analyzing trail based.");
		if (assignment.getDecisionLevel() == 0) {
			LOGGER.trace("Conflict on decision level 0.");
			return ConflictAnalysisResult.UNSAT;
		}
		int numLiteralsInConflictLevel = 0;
		List<Integer> resolutionLiterals = new ArrayList<>();
		List<Integer> resolutionAtoms = new ArrayList<>();
		List<Literal> nonGroundResolutionLiterals = new ArrayList<>();
		int currentDecisionLevel = assignment.getDecisionLevel();
		Set<Integer> seenAtoms = new HashSet<>();		// NOTE: other solvers use a global array for seen atoms, this might be slightly faster (initial tests with local arrays showed no significant improvement).
		Set<Integer> processedAtoms = new HashSet<>();	// Since trail contains 2 entries for MBT->TRUE assigned atoms, explicitly record which seen atoms have ben processed to avoid processing seen atoms twice.
		int[] currentConflictReason = conflictReason.getReasonLiterals();
		NoGood currentOriginalNoGood = conflictReason.getOriginalNoGood();
		NonGroundNoGood currentOriginalNonGroundNoGood = currentOriginalNoGood.getNonGroundNoGood();
		int backjumpLevel = -1;
		conflictReason.bumpActivity();
		TrailAssignment.TrailBackwardsWalker trailWalker = ((TrailAssignment)assignment).getTrailBackwardsWalker();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Current trail is: {}", trailWalker);
			LOGGER.trace("Violated nogood is: {}", reasonsToString(conflictReason.getReasonLiterals()));
		}
		int nextAtom = -1;
		do {
			// Add current conflict reasons; only add those of lower decision levels, since from current one, only the 1UIP literal will be added.
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Atom {} implied by {}, resolving with that nogood", nextAtom, reasonsToString(currentConflictReason));
			}
			for (int literal : currentConflictReason) {
				// Seen atoms have already been dealt with.
				if (!seenAtoms.contains(atomOf(literal))) {
					seenAtoms.add(atomOf(literal));

					Literal nonGroundLiteral = null;
					if (conflictGeneralisationEnabled) {
						if (currentOriginalNoGood == null) {
							nonGroundResolutionLiterals = null;
							LOGGER.warn("Cannot generalise conflict because original nogood unknown for " + reasonsToString(currentConflictReason));
						} else if (nonGroundResolutionLiterals != null) {
							if (currentOriginalNonGroundNoGood == null) {
								nonGroundResolutionLiterals = null;
								LOGGER.warn("Cannot generalise conflict because non-ground nogood unknown for " + atomStore.noGoodToString(currentOriginalNoGood));
							} else {
								// index must be looked up because literals may have different order in antecedent than in original nogood:
								final int indexOfLiteralInOriginalNogood = currentOriginalNoGood.indexOf(literal);
								final Literal nonGroundResolutionLiteral = currentOriginalNonGroundNoGood.getLiteral(indexOfLiteralInOriginalNogood);
								nonGroundLiteral = nonGroundResolutionLiteral;
								if (!nonGroundResolutionLiteral.getPredicate().equals(atomStore.get(atomOf(literal)).getPredicate())) {
									throw oops("Wrong non-ground literal assigned to ground literal");
									// TODO: execute this check only if internal checks enabled (?)
								}
							}
						}
					}

					int literalDecisionLevel = assignment.getWeakDecisionLevel(atomOf(literal));
					if (literalDecisionLevel == currentDecisionLevel) {
						numLiteralsInConflictLevel++;
					} else {
						resolutionLiterals.add(literal);
						if (nonGroundLiteral != null) {
							nonGroundResolutionLiterals.add(nonGroundLiteral);
						}
						if (literalDecisionLevel > backjumpLevel) {
							backjumpLevel = literalDecisionLevel;
						}
					}
					resolutionAtoms.add(atomOf(literal));
				}
			}
			if (conflictGeneralisationEnabled && nonGroundResolutionLiterals != null && currentOriginalNonGroundNoGood != null) {
				nonGroundResolutionLiterals.addAll(getAdditionalLiterals(currentOriginalNonGroundNoGood, currentConflictReason.length));
			}
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("LiteralsInConflictLevel now: {}", numLiteralsInConflictLevel);
				LOGGER.trace("Seen atoms are {}.", seenAtoms);
				LOGGER.trace("Intermediate learned literals: {}", reasonsToString(resolutionLiterals));
			}
			// Find next literal, i.e. first from top of trail that has been seen but is not yet processed, also skip atoms whose TRUE assignment is on current level but their MBT/weak assignment is lower.
			do {
				int nextLiteral = trailWalker.getNextLowerLiteral();
				nextAtom = atomOf(nextLiteral);
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Next literal on trail is: {}", isPositive(nextLiteral) ? "+" + nextAtom : "-" + nextAtom);
				}
			} while (assignment.getWeakDecisionLevel(nextAtom) != currentDecisionLevel || !seenAtoms.contains(nextAtom) || processedAtoms.contains(nextAtom));
			Antecedent impliedBy = assignment.getImpliedBy(nextAtom);
			if (impliedBy != null) {
				currentConflictReason = impliedBy.getReasonLiterals();
				currentOriginalNoGood = impliedBy.getOriginalNoGood();
				currentOriginalNonGroundNoGood = currentOriginalNoGood == null ? null : currentOriginalNoGood.getNonGroundNoGood();
				impliedBy.bumpActivity();
			}
			processedAtoms.add(nextAtom);
		} while (numLiteralsInConflictLevel-- > 1);
		// Add the 1UIP literal.
		resolutionLiterals.add(atomToLiteral(nextAtom, assignment.getTruth(nextAtom).toBoolean()));
		// TODO: find and add non-ground 1UIP literal

		int[] learnedLiterals = new int[resolutionLiterals.size()];
		int i = 0;
		for (Integer resolutionLiteral : resolutionLiterals) {
			learnedLiterals[i++] = resolutionLiteral;
		}
		if (conflictGeneralisationEnabled && nonGroundResolutionLiterals != null && !nonGroundResolutionLiterals.isEmpty()) {
			final NonGroundNoGood learnedNonGroundNoGood = NonGroundNoGood.learnt(nonGroundResolutionLiterals);
			LOGGER.info("Learnt non-ground nogood: " + learnedNonGroundNoGood);
			// TODO: do something more with it
		}

		NoGood learnedNoGood = NoGood.learnt(learnedLiterals);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Learned NoGood is: {}", atomStore.noGoodToString(learnedNoGood));
		}

		int backjumpingDecisionLevel = computeBackjumpingDecisionLevel(learnedNoGood);
		if (backjumpingDecisionLevel < 0) {
			// Due to out-of-order assigned literals, the learned nogood may be not assigning.
			backjumpingDecisionLevel = computeConflictFreeBackjumpingLevel(learnedNoGood);
			if (backjumpingDecisionLevel < 0) {
				return ConflictAnalysisResult.UNSAT;
			}
		}
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Backjumping decision level: {}", backjumpingDecisionLevel);
		}
		return new ConflictAnalysisResult(learnedNoGood, backjumpingDecisionLevel, resolutionAtoms, computeLBD(learnedLiterals));
	}

	private List<? extends Literal> getAdditionalLiterals(NonGroundNoGood nonGroundNoGood, int numberOfAlreadyConsideredLiterals) {
		final List<Literal> result = new ArrayList<>(nonGroundNoGood.size() - numberOfAlreadyConsideredLiterals);
		for (int i = numberOfAlreadyConsideredLiterals; i < nonGroundNoGood.size(); i++) {
			result.add(nonGroundNoGood.getLiteral(i));
		}
		return result;
	}

	private int computeLBD(int[] literals) {
		HashSet<Integer> occurringDecisionLevels = new HashSet<>();
		for (int literal : literals) {
			if (!assignment.isAssigned(atomOf(literal))) {
				throw oops("Atom is not assigned: " + atomOf(literal));
			}
			occurringDecisionLevels.add(assignment.getWeakDecisionLevel(atomOf(literal)));
		}
		return occurringDecisionLevels.size();
	}

	/**
	 * Compute the backjumping decision level, i.e., the decision level on which the learned NoGood is assigning (NoGood is unit and propagates).
	 * This usually is the second highest decision level occurring in the learned NoGood, but due to assignments of MBT no such decision level may exist.
	 * @param learnedNoGood
	 * @return -1 if there is no decisionLevel such that backjumping to it makes the learnedNoGood unit.
	 */
	private int computeBackjumpingDecisionLevel(NoGood learnedNoGood) {
		LOGGER.trace("Computing backjumping decision level for {}.", learnedNoGood);
		int highestDecisionLevel = -1;
		int secondHighestDecisionLevel = -1;
		int numLiteralsOfHighestDecisionLevel = -1;
		if (learnedNoGood.isUnary()) {
			// Singleton NoGoods induce a backjump to the decision level before the NoGood got violated.
			int singleLiteralDecisionLevel = assignment.get(atomOf(learnedNoGood.getLiteral(0))).getDecisionLevel();
			if (assignment instanceof TrailAssignment) {
				singleLiteralDecisionLevel = Math.min(singleLiteralDecisionLevel, ((TrailAssignment) assignment).getOutOfOrderDecisionLevel(learnedNoGood.getPositiveLiteral(0)));
			}
			int singleLiteralBackjumpingLevel = singleLiteralDecisionLevel - 1 >= 0 ? singleLiteralDecisionLevel - 1 : 0;
			LOGGER.trace("NoGood has only one literal, backjumping to level: {}", singleLiteralBackjumpingLevel);
			return singleLiteralBackjumpingLevel;
		}
		int[] reasonLiterals = learnedNoGood.asAntecedent().getReasonLiterals();
		for (int integer : reasonLiterals) {
			int atomDecisionLevel = assignment.getWeakDecisionLevel(atomOf(integer));
			if (assignment instanceof TrailAssignment) {
				atomDecisionLevel = Math.min(atomDecisionLevel, ((TrailAssignment) assignment).getOutOfOrderDecisionLevel(atomOf(integer)));
			}
			if (atomDecisionLevel == highestDecisionLevel) {
				numLiteralsOfHighestDecisionLevel++;
			}
			if (atomDecisionLevel > highestDecisionLevel) {
				secondHighestDecisionLevel = highestDecisionLevel;
				highestDecisionLevel = atomDecisionLevel;
				numLiteralsOfHighestDecisionLevel = 1;
			} else {
				if (atomDecisionLevel < highestDecisionLevel && atomDecisionLevel > secondHighestDecisionLevel) {
					secondHighestDecisionLevel = atomDecisionLevel;
				}
			}
		}
		if (numLiteralsOfHighestDecisionLevel != 1) {
			LOGGER.trace("NoGood contains not just one literal in the second-highest decision level. Backjumping decision level is -1.");
			return -1;
		}
		LOGGER.trace("Backjumping decision level is: {}", secondHighestDecisionLevel);
		return secondHighestDecisionLevel;
	}
}
