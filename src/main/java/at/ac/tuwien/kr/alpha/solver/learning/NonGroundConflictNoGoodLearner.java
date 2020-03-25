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
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.NonGroundNoGood;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.solver.Antecedent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Util.collectionToIntArray;
import static at.ac.tuwien.kr.alpha.Util.intArrayToLinkedHashSet;
import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.negateLiteral;

/**
 * Conflict-driven learning on ground clauses that also learns non-ground nogoods.
 *
 * This class builds upon an underlying {@link GroundConflictNoGoodLearner}, to which it acts as a proxy for all operations
 * not implemented on the non-ground level.
 * The non-ground learning capabilities implemented here are not designed for efficiency (yet), so this class should only
 * be used in an "offline" conflict generalisation mode.
 */
public class NonGroundConflictNoGoodLearner implements ConflictNoGoodLearner {
	private static final Logger LOGGER = LoggerFactory.getLogger(NonGroundConflictNoGoodLearner.class);

	private final Assignment assignment;
	private final GroundConflictNoGoodLearner groundLearner;

	public NonGroundConflictNoGoodLearner(Assignment assignment, GroundConflictNoGoodLearner groundLearner) {
		this.assignment = assignment;
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
	 *
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
		NoGood firstLearnedNoGood = null;
		List<NoGood> additionalLearnedNoGoods = new ArrayList<>();
		Set<Integer> currentNoGood;
		Set<Integer> resolvent = intArrayToLinkedHashSet(violatedNoGood.getReasonLiterals());
		do {
			currentNoGood = resolvent;
			resolvent = makeOneResolutionStep(currentNoGood);
			if (resolvent != null && containsUIP(resolvent)) {
				final NoGood learntNoGood = createLearntNoGood(resolvent);
				if (firstLearnedNoGood == null) {
					firstLearnedNoGood = learntNoGood;
				} else {
					additionalLearnedNoGoods.add(learntNoGood);
				}
			}
		} while (resolvent != null);
		final ConflictAnalysisResult groundAnalysisResultNotMinimized = groundLearner.analyzeTrailBased(violatedNoGood, false);
		if (!Objects.equals(groundAnalysisResultNotMinimized.learnedNoGood, firstLearnedNoGood)) {
			throw oops("Learned nogood is not the same as the one computed by ground analysis");
		}
		final ConflictAnalysisResult analysisResult = groundLearner.analyzeConflictingNoGood(violatedNoGood);
		if (!additionalLearnedNoGoods.isEmpty()) {
			analysisResult.addLearnedNoGoods(additionalLearnedNoGoods);
		}
		return analysisResult;
	}

	/**
	 * Resolves the current nogood with the antecedent of one of its literals assigned on the current decision level.
	 * @param currentNoGood the nogood to resolve with the antecedent of one of its literals
	 * @return the resolvent (as a set of literals), or {@code null} if no further resolution step is possible
	 */
	private Set<Integer> makeOneResolutionStep(Set<Integer> currentNoGood) {
		final int currentDecisionLevel = assignment.getDecisionLevel();
		for (int literal : currentNoGood) {
			final int atom = atomOf(literal);
			if (assignment.getWeakDecisionLevel(atom) == currentDecisionLevel && assignment.getImpliedBy(atom) != null) {
				return resolve(currentNoGood, literal, intArrayToLinkedHashSet(assignment.getImpliedBy(atom).getReasonLiterals()));
			}
		}
		return null;
	}

	private Set<Integer> resolve(Set<Integer> noGood1, int literalInNoGood1, Set<Integer> noGood2) {
		final int literalInNoGood2 = negateLiteral(literalInNoGood1);
		final Set<Integer> resolvent = new LinkedHashSet<>();
		for (int literal : noGood1) {
			if (literal != literalInNoGood1) {
				resolvent.add(literal);
			}
		}
		for (int literal : noGood2) {
			if (literal != literalInNoGood2) {
				resolvent.add(literal);
			}
		}
		return resolvent;
	}

	private boolean containsUIP(Set<Integer> resolvent) {
		final int currentDecisionLevel = assignment.getDecisionLevel();
		boolean containsLiteralOnCurrentDecisionLevel = false;
		for (Integer literal : resolvent) {
			if (assignment.getWeakDecisionLevel(atomOf(literal)) == currentDecisionLevel) {
				if (containsLiteralOnCurrentDecisionLevel) {
					return false;
				} else {
					containsLiteralOnCurrentDecisionLevel = true;
				}
			}
		}
		if (!containsLiteralOnCurrentDecisionLevel) {
			throw oops("Resolvent does not contain any literal from the current decsion level: " + resolvent);
		}
		return true;
	}

	private NoGood createLearntNoGood(Set<Integer> resolvent) {
		return NoGood.learnt(collectionToIntArray(resolvent));
	}

	private List<? extends Literal> getAdditionalLiterals(NonGroundNoGood nonGroundNoGood, int numberOfAlreadyConsideredLiterals) {
		final List<Literal> result = new ArrayList<>(nonGroundNoGood.size() - numberOfAlreadyConsideredLiterals);
		for (int i = numberOfAlreadyConsideredLiterals; i < nonGroundNoGood.size(); i++) {
			result.add(nonGroundNoGood.getLiteral(i));
		}
		return result;
	}

}
