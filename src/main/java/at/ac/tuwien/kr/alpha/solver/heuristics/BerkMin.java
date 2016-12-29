/**
 * Copyright (c) 2016 Siemens AG
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

import at.ac.tuwien.kr.alpha.common.Literals;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.Assignment;
import at.ac.tuwien.kr.alpha.solver.GroundConflictNoGoodLearner.ConflictAnalysisResult;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.common.Atoms.isAtom;
import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

/**
 * The BerkMin heuristic, as described in (but adapted for lazy grounding):
 * Goldberg, E.; Novikov, Y. (2002): BerkMin: A fast and robust SAT-solver.
 * In : Design, Automation and Test in Europe Conference and Exhibition, 2002. Proceedings. IEEE, pp. 142-149.
 * 
 * Copyright (c) 2016 Siemens AG
 */
public class BerkMin implements BranchingHeuristic {
	
	public static final double DEFAULT_ACTIVITY = 0.0;
	public static final int DEFAULT_SIGN_COUNTER = 0;
	public static final int DEFAULT_CHOICE_ATOM = 0;

	public static final int DEFAULT_DECAY_AGE = 10;
	public static final double DEFAULT_DECAY_FACTOR = 0.25;

	private Assignment assignment;
	private Map<Integer, Double> activityCounters = new HashMap<>();
	private Map<Integer, Integer> signCounters = new HashMap<>();
	private Deque<NoGood> stackOfNoGoods = new ArrayDeque<>();
	private int decayAge;
	private double decayFactor;
	private int stepsSinceLastDecay;
	private Random rand = new Random();
	private Predicate<? super Integer> isAtomActiveChoicePoint;

	public BerkMin(Assignment assignment, Predicate<? super Integer> isAtomActiveChoicePoint, int decayAge, double decayFactor) {
		this.assignment = assignment;
		this.isAtomActiveChoicePoint = isAtomActiveChoicePoint;
		this.decayAge = decayAge;
		this.decayFactor = decayFactor;
	}

	public BerkMin(Assignment assignment, Predicate<? super Integer> isAtomActiveChoicePoint) {
		this(assignment, isAtomActiveChoicePoint, DEFAULT_DECAY_AGE, DEFAULT_DECAY_FACTOR);
	}

	/**
	 * Gets the number of steps after which all counters are decayed (i.e. multiplied by {@link #getDecayFactor()}.
	 */
	public int getDecayAge() {
		return decayAge;
	}

	/**
	 * Sets the number of steps after which all counters are decayed (i.e. multiplied by {@link #getDecayFactor()}.
	 */
	public void setDecayAge(int decayAge) {
		this.decayAge = decayAge;
	}

	/**
	 * Gets the factor by which all counters are multiplied to decay after {@link #getDecayAge()}.
	 */
	public double getDecayFactor() {
		return decayFactor;
	}

	/**
	 * Sets the factor by which all counters are multiplied to decay after {@link #getDecayAge()}.
	 */
	public void setDecayFactor(double decayFactor) {
		this.decayFactor = decayFactor;
	}

	@Override
	public void violatedNoGood(NoGood violatedNoGood) {
		pushToStack(violatedNoGood);
	}

	@Override
	public void analyzedConflict(ConflictAnalysisResult analysisResult) {
		pushToStack(analysisResult.learnedNoGood);
		for (NoGood noGood : analysisResult.noGoodsResponsibleForConflict) {
			for (Integer literal : noGood) {
				incrementActivityCounter(literal);
				incrementSignCounter(literal);
			}
		}
		decayAllIfTimeHasCome();
	}

	@Override
	public void newNoGood(NoGood newNoGood) {
		pushToStack(newNoGood);
		for (Integer literal : newNoGood) {
			incrementSignCounter(literal);
		}
	}

	@Override
	public void newNoGoods(Collection<NoGood> newNoGoods) {
		newNoGoods.forEach(this::newNoGood);
	}

	@Override
	public double getActivity(int literal) {
		return activityCounters.getOrDefault(atomOf(literal), DEFAULT_ACTIVITY);
	}
	
	/**
	 * {@inheritDoc}
	 * In BerkMin, the atom to choose on is the most active atom in the current top clause.
	 * Here, we can only consider atoms which are currently active choice points. If we do
	 * not find such an atom in the current top clause, we consider the next undefined
	 * nogood in the stack, then the one after that and so on.
	 */
	@Override
	public int chooseAtom() {
		for (NoGood noGood : stackOfNoGoods) {
			if (assignment.isUndefined(noGood)) {
				int mostActiveAtom = getMostActiveChoosableAtom(noGood);
				if (mostActiveAtom != DEFAULT_CHOICE_ATOM) {
					return mostActiveAtom;
				}
			}
		}
		return DEFAULT_CHOICE_ATOM;
	}
	
	@Override
	public boolean chooseSign(int atom) {
		if (!isAtom(atom)) {
			throw new IllegalArgumentException("Atom must be a positive integer.");
		}

		if (assignment.getTruth(atom) == ThriceTruth.MBT) {
			return true;
		}

		int positiveCounter = signCounters.getOrDefault(+atom, DEFAULT_SIGN_COUNTER);
		int negativeCounter = signCounters.getOrDefault(-atom, DEFAULT_SIGN_COUNTER);

		if (positiveCounter > negativeCounter) {
			return false;
		} else if (negativeCounter > positiveCounter) {
			return true;
		} else {
			return rand.nextBoolean();
		}
	}

	private void pushToStack(NoGood noGood) {
		if (noGood != null) {
			stackOfNoGoods.push(noGood);
		}
	}

	private void incrementActivityCounter(int literal) {
		activityCounters.compute(atomOf(literal), (k, v) -> (v == null ? DEFAULT_ACTIVITY : v) + 1);
	}
	
	private void incrementSignCounter(Integer literal) {
		signCounters.compute(atomOf(literal), (k, v) -> (v == null ? DEFAULT_SIGN_COUNTER : v) + 1);
	}

	private void decayAllIfTimeHasCome() {
		stepsSinceLastDecay++;
		if (stepsSinceLastDecay >= decayAge) {
			// Decay all:
			activityCounters.replaceAll((k, v) -> v * decayFactor);
			stepsSinceLastDecay = 0;
		}
	}

	/**
	 * Gets the most recent conflict that is still violated.
	 * @return the violated nogood closest to the top of the stack of nogoods.
	 */
	NoGood getCurrentTopClause() {
		for (NoGood noGood : stackOfNoGoods) {
			if (assignment.isUndefined(noGood)) {
				return noGood;
			}
		}
		return null;
	}
	
	/**
	 * If {@code noGood != null}, returns the most active unassigned literal from {@code noGood}. Else, returns the most active atom of all the known atoms.
	 * @param noGood
	 * @return
	 */
	private int getMostActiveChoosableAtom(NoGood noGood) {
		if (noGood != null) {
			return getMostActiveChoosableAtom(noGood.stream().boxed());
		} else {
			return getMostActiveChoosableAtom(activityCounters.keySet().stream());
		}
	}
	
	private int getMostActiveChoosableAtom(Stream<Integer> streamOfLiterals) {
		return streamOfLiterals
			.map(Literals::atomOf)
			.filter(this::isUnassigned)
			.filter(isAtomActiveChoicePoint)
			.max(Comparator.comparingDouble(this::getActivity))
			.orElse(DEFAULT_CHOICE_ATOM);
	}

	private boolean isUnassigned(int atom) {
		ThriceTruth truth = assignment.getTruth(atom);
		return truth != FALSE && truth != TRUE; // do not use assignment.isAssigned(atom) because we may also choose MBTs
	}
}
