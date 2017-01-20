/**
 * Copyright (c) 2017 Siemens AG
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
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.solver.*;
import at.ac.tuwien.kr.alpha.solver.GroundConflictNoGoodLearner.ConflictAnalysisResult;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.*;
import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.common.Atoms.isAtom;
import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

/**
 * A new branching heuristic designed specifically for the lazy-grounding solver Alpha.
 * Inspired by BerkMin, as described in:
 * Goldberg, E.; Novikov, Y. (2002): BerkMin: A fast and robust SAT-solver.
 * In : Design, Automation and Test in Europe Conference and Exhibition, 2002. Proceedings. IEEE, pp. 142-149.
 * 
 * Copyright (c) 2017 Siemens AG
 */
public class AlphaHeuristic implements BranchingHeuristic {
	
	public static final double DEFAULT_ACTIVITY = 0.0;
	public static final int DEFAULT_SIGN_COUNTER = 0;
	public static final int DEFAULT_CHOICE_ATOM = 0;

	public static final int DEFAULT_DECAY_AGE = 10;
	public static final double DEFAULT_DECAY_FACTOR = 0.25;

	protected final Assignment assignment;
	protected final ChoiceManager choiceManager;
	protected final Random rand;
	protected final Grounder grounder; // TODO: this is a temporary workaround (see https://github.com/AntoniusW/Alpha/issues/39)

	private Map<Integer, Double> activityCounters = new HashMap<>();
	private Map<Integer, Integer> signCounters = new HashMap<>();
	private Deque<NoGood> stackOfNoGoods = new ArrayDeque<>();
	private int decayAge;
	private double decayFactor;
	private int stepsSinceLastDecay;

	/**
	 * Maps body-representing atoms to rule heads.
	 */
	private Map<Integer, Integer> bodyToHead = new HashMap<>();

	/**
	 * Maps body-representing atoms to literals occuring in the rule body.
	 */
	private Map<Integer, Set<Integer>> bodyToLiterals = new HashMap<>();

	/**
	 * Maps atoms to atoms representing bodies of rules in which the former atoms occur (in the head or the body).
	 */
	private MultiValuedMap<Integer, Integer> atomsToBodies = new HashSetValuedHashMap<>();

	public AlphaHeuristic(Grounder grounder, Assignment assignment, ChoiceManager choiceManager, int decayAge, double decayFactor, Random random) {
		this.grounder = grounder;
		this.assignment = assignment;
		this.choiceManager = choiceManager;
		this.decayAge = decayAge;
		this.decayFactor = decayFactor;
		this.rand = random;
	}

	public AlphaHeuristic(Grounder grounder, Assignment assignment, ChoiceManager choiceManager, Random random) {
		this(grounder, assignment, choiceManager, DEFAULT_DECAY_AGE, DEFAULT_DECAY_FACTOR, random);
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
		handleSpecialNoGood(newNoGood);
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
	 * TODO: doc
	 */
	@Override
	public int chooseAtom() {
		for (NoGood noGood : stackOfNoGoods) {
			int mostActiveAtom = getMostActiveAtom(noGood);
			Collection<Integer> bodies = atomsToBodies.get(mostActiveAtom);
			Optional<Integer> mostActiveBody = bodies.stream().filter(this::isUnassigned).filter(choiceManager::isActiveChoiceAtom)
					.max(Comparator.comparingDouble(this::getBodyActivity));
			if (mostActiveBody.isPresent()) {
				return mostActiveBody.get();
			}
		}
		return DEFAULT_CHOICE_ATOM;
	}
	
	private double getBodyActivity(int bodyRepresentingAtom) {
		return bodyToLiterals.get(bodyRepresentingAtom).stream().mapToDouble(this::getActivity).sum();
		// TODO: other aggregate functions apart from sum
		// TODO: make more performant by counting when bodies arrive
	}

	@Override
	public boolean chooseSign(int atom) {
		if (!isAtom(atom)) {
			throw new IllegalArgumentException("Atom must be a positive integer.");
		}

		Integer head = bodyToHead.get(atom);
		if (head != null) {
			atom = head; // head atom can give more relevant information than atom representing rule body
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

	private void handleSpecialNoGood(NoGood noGood) {
		// if (noGood.isBodyNotHead(choiceManager::isAtomChoice)) {
		// TODO: this is a temporary workaround (see https://github.com/AntoniusW/Alpha/issues/39)
		if (noGood.isBodyNotHead(grounder::isAtomChoicePoint)) {
			int headIndex = noGood.getHead();
			int bodyIndex = headIndex != 0 ? 0 : 1;
			int body = noGood.getAtom(bodyIndex);
			int head = noGood.getAtom(headIndex);
			bodyToHead.put(body, head);
			atomsToBodies.put(head, body);
			// } else if (noGood.isBodyElementsNotBody(choiceManager::isAtomChoice)) {
			// TODO: this is a temporary workaround (see https://github.com/AntoniusW/Alpha/issues/39)
		} else if (noGood.isBodyElementsNotBody(grounder::isAtomChoicePoint)) {
			Set<Integer> literals = new HashSet<>();
			int bodyAtom = 0;
			for (int i = 0; i < noGood.size(); i++) {
				if (i == noGood.getHead()) {
					bodyAtom = noGood.getAtom(i);
				} else {
					int literal = noGood.getLiteral(i);
					literals.add(literal);
					if (bodyAtom != 0) {
						atomsToBodies.put(atomOf(literal), bodyAtom);
					} else {
						// TODO
					}
				}
				// TODO: make more performant (maybe head could always come first in NoGood?)
			}
			assert bodyAtom != 0;
			bodyToLiterals.put(bodyAtom, literals);
		}
	}

	private void pushToStack(NoGood noGood) {
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
	
	protected void incrementSignCounter(Integer literal) {
		signCounters.compute(literal, (k, v) -> (v == null ? DEFAULT_SIGN_COUNTER : v) + 1);
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

	private boolean isUnassigned(int atom) {
		ThriceTruth truth = assignment.getTruth(atom);
		return truth != FALSE && truth != TRUE; // do not use assignment.isAssigned(atom) because we may also choose MBTs
	}

	private int getMostActiveAtom(NoGood noGood) {
		if (noGood != null) {
			return getMostActiveAtom(noGood.stream().boxed());
		} else {
			return getMostActiveAtom(activityCounters.keySet().stream());
		}
	}

	private int getMostActiveAtom(Stream<Integer> streamOfLiterals) {
		return streamOfLiterals.map(Literals::atomOf).max(Comparator.comparingDouble(this::getActivity)).orElse(DEFAULT_CHOICE_ATOM);
		// TODO: exploit synergy with getMostActiveChoosableAtom
	}
}
