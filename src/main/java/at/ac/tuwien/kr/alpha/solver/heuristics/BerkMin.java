/**
 * Copyright (c) 2016-2017 Siemens AG
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
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.learning.GroundConflictNoGoodLearner.ConflictAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.solver.Atoms.isAtom;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(BerkMin.class);
	
	static final double DEFAULT_ACTIVITY = 0.0;
	static final int DEFAULT_SIGN_COUNTER = 0;
	static final int DEFAULT_CHOICE_ATOM = 0;

	static final int DEFAULT_DECAY_AGE = 10;
	static final double DEFAULT_DECAY_FACTOR = 0.25;

	final Assignment assignment;
	final ChoiceManager choiceManager;
	final Random rand;

	public Grounder getGrounder() {
		return grounder;
	}

	private final Grounder grounder;

	private Map<Integer, Double> activityCounters = new LinkedHashMap<>();
	private Map<Integer, Integer> signCounters = new LinkedHashMap<>();

	public Map<Integer, Double> getActivityCounters() {
		return activityCounters;
	}

	public Deque<NoGood> getStackOfNoGoods() {
		return stackOfNoGoods;
	}

	private Deque<NoGood> stackOfNoGoods = new ArrayDeque<>();
	private int decayAge;
	private double decayFactor;
	private int stepsSinceLastDecay;

	BerkMin(Assignment assignment, ChoiceManager choiceManager, int decayAge, double decayFactor, Random random, Grounder grounder) {
		this.assignment = assignment;
		this.choiceManager = choiceManager;
		this.decayAge = decayAge;
		this.decayFactor = decayFactor;
		this.rand = random;
		this.grounder = grounder;
	}

	BerkMin(Assignment assignment, ChoiceManager choiceManager, Random random, Grounder grounder) {
		this(assignment, choiceManager, DEFAULT_DECAY_AGE, DEFAULT_DECAY_FACTOR, random, grounder);
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
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("NoGood responsible with {} choice points", numChoicePoints(noGood));
			}
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

	private int numChoicePoints(NoGood noGood) {
		int numChoicePoints = 0;
		for (Integer literal : noGood) {
			if (choiceManager.isAtomChoice(literal)) {
				numChoicePoints++;
			}
		}
		return numChoicePoints;
	}

	@Override
	public void newNoGoods(Collection<NoGood> newNoGoods) {
		newNoGoods.forEach(this::newNoGood);
	}

	@Override
	public double getActivity(int literal) {
		int key = atomOf(literal);
		return activityCounters.getOrDefault(key, DEFAULT_ACTIVITY);
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

	protected void pushToStack(NoGood noGood) {
		if (noGood != null) {
			stackOfNoGoods.push(noGood);
		}
	}

	private void incrementActivityCounter(int literal) {
		int atom = atomOf(literal);
		if (choiceManager.isAtomChoice(atom)) {
			activityCounters.compute(atom, (k, v) -> (v == null ? DEFAULT_ACTIVITY : v) + 1);
		}
		// TODO: check performance
		// note that here (and in incrementSignCounter) we only count atoms that are
		// choice points, which might affect performance.
		// alternative approaches:
		// 1. count everything and from time to time do a garbage collection (i.e.
		// remove atoms that are no choice points)
		// 2. make check cheaper, e.g. by using dedicated atom IDs (e.g. even
		// integers for rule bodies, uneven for other atoms)
	}
	
	private void incrementSignCounter(Integer literal) {
		if (choiceManager.isAtomChoice(atomOf(literal))) {
			signCounters.compute(literal, (k, v) -> (v == null ? DEFAULT_SIGN_COUNTER : v) + 1);
		}
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
	protected int getMostActiveChoosableAtom(NoGood noGood) {
		if (noGood != null) {
			return getMostActiveChoosableAtom(noGood.stream().boxed());
		} else {
			return getMostActiveChoosableAtom(activityCounters.keySet().stream());
		}
	}
	
	protected int getMostActiveChoosableAtom(Stream<Integer> streamOfLiterals) {
		Set<Integer> activeChoices = streamOfLiterals
			.map(Literals::atomOf)
			.filter(this::isUnassigned)
			.filter(choiceManager::isActiveChoiceAtom).collect(Collectors.toSet());

		return activeChoices.stream().max(Comparator.comparingDouble(this::getActivity))
			.orElse(DEFAULT_CHOICE_ATOM);


	}




	protected boolean isUnassigned(int atom) {
		ThriceTruth truth = assignment.getTruth(atom);
		return truth != FALSE && truth != TRUE; // do not use assignment.isAssigned(atom) because we may also choose MBTs
	}
}
