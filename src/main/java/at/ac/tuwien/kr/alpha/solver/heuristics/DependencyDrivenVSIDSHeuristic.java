/**
 * Copyright (c) 2018 Siemens AG
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.heuristics.activity.BodyActivityProvider;
import at.ac.tuwien.kr.alpha.solver.learning.GroundConflictNoGoodLearner.ConflictAnalysisResult;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;

/**
 * The BerkMin variants {@link BerkMin} and {@link BerkMinLiteral} suffer from the fact that choice points
 * comprise only a small portion of all the literals occurring in nogoods and therefore do not influence
 * activity and sign counters as much as other atoms.
 * The basic idea of {@link DependencyDrivenVSIDSHeuristic} is therefore to find <i>dependent</i> atoms that can
 * enrich the information available for a choice point. Intuitively, all atoms occurring in the head or the
 * body of a rule depend on a choice point representing the body of this rule.
 * <p/>
 * In contrast to {@link GeneralizedDependencyDrivenHeuristic}, this heuristic is based on ideas from VSIDS instead of BerkMin.
 * This implementation is inspired by the VSIDS implementation in <a href="https://github.com/potassco/clasp">clasp</a>.
 * Therefore, for example, decay is not realized by decreasing activity scores with age, but by
 * steadily increasing the increment added to the score of active atoms
 * (such that the activity score of older atoms does not have to be changed later).
 * <p/>
 * The implementation is simplified in some ways, e.g. it is not possible to specify a frequency in which the activity
 * increment is updated (which corresponds to decay), but it is updated after every conflict.
 * <p/>
 * Copyright (c) 2018 Siemens AG
 */
public class DependencyDrivenVSIDSHeuristic implements ActivityBasedBranchingHeuristic {
	protected static final Logger LOGGER = LoggerFactory.getLogger(DependencyDrivenVSIDSHeuristic.class);

	public static final int DEFAULT_SIGN_COUNTER = 0;

	public static final int DEFAULT_DECAY_FREQUENCY = 1;
	public static final double DEFAULT_DECAY_FACTOR = 1 / 0.92;

	protected final Assignment assignment;
	protected final ChoiceManager choiceManager;
	protected final Random rand;

	protected final HeapOfActiveChoicePoints heapOfActiveChoicePoints;
	protected final Map<Integer, Integer> signCounters = new HashMap<>();

	private final Collection<NoGood> bufferedNoGoods = new ArrayList<>();

	private int nChoicesTrue;
	private int nChoicesFalse;
	private int nChoicesRand;

	/**
	 * Maps rule heads to atoms representing corresponding bodies.
	 */
	protected final MultiValuedMap<Integer, Integer> headToBodies = new HashSetValuedHashMap<>();

	public DependencyDrivenVSIDSHeuristic(Assignment assignment, ChoiceManager choiceManager, int decayAge, double decayFactor, Random random) {
		this.assignment = assignment;
		this.choiceManager = choiceManager;
		this.heapOfActiveChoicePoints = new HeapOfActiveChoicePoints(decayAge, decayFactor, choiceManager);
		this.rand = random;
	}

	public DependencyDrivenVSIDSHeuristic(Assignment assignment, ChoiceManager choiceManager, Random random) {
		this(assignment, choiceManager, DEFAULT_DECAY_FREQUENCY, DEFAULT_DECAY_FACTOR, random);
	}

	@Override
	public void violatedNoGood(NoGood violatedNoGood) {
	}

	@Override
	public void analyzedConflict(ConflictAnalysisResult analysisResult) {
		// TODO: under which conditions shall activities of literals in noGoodsResponsibleForConflict be incremented?
		//		for (NoGood noGood : analysisResult.noGoodsResponsibleForConflict) {
		//			for (Integer literal : noGood) {
		//				heapOfActiveChoicePoints.incrementActivity(atomOf(literal));
		//			}
		//		}
		for (Integer literal : analysisResult.learnedNoGood) {
			incrementSignCounter(literal);
			heapOfActiveChoicePoints.incrementActivity(atomOf(literal));
		}
		heapOfActiveChoicePoints.decayIfTimeHasCome();
	}

	@Override
	public void newNoGood(NoGood newNoGood) {
		this.bufferedNoGoods.add(newNoGood);
	}

	@Override
	public void newNoGoods(Collection<NoGood> newNoGoods) {
		this.bufferedNoGoods.addAll(newNoGoods);
	}

	private void ingestBufferedNoGoods() {
		for (NoGood newNoGood : bufferedNoGoods) {
			heapOfActiveChoicePoints.initActity(newNoGood);
			// TODO: increment sign counters only for learnt nogoods or also for static ones?
			// for (Integer literal : newNoGood) {
			// incrementSignCounter(literal);
			// }
		}
		bufferedNoGoods.clear();
	}

	/**
	 * {@link DependencyDrivenVSIDSHeuristic} manages a stack of nogoods in the fashion of {@link BerkMin}
	 * and starts by looking at the most active atom <code>a</code> in the nogood currently at the top of the stack.
	 * If <code>a</code> is an active choice point (i.e. representing the body of an applicable rule), it is immediately chosen;
	 * else the most active choice point dependent on <code>a</code> is.
	 * If there is no such atom, we continue further down the stack.
	 * When choosing between dependent atoms, a {@link BodyActivityProvider} is employed to define the activity of a choice point.
	 */
	@Override
	public int chooseLiteral() {
		int atom = chooseAtom();
		if (atom == DEFAULT_CHOICE_ATOM) {
			return DEFAULT_CHOICE_LITERAL;
		}
		boolean sign = chooseSign(atom);
		return atomToLiteral(atom, sign);
	}

	protected int chooseAtom() {
		ingestBufferedNoGoods();
		Integer mostActiveAtom;
		while ((mostActiveAtom = heapOfActiveChoicePoints.getMostActiveAtom()) != null) {
			if (choiceManager.isActiveChoiceAtom(mostActiveAtom)) {
				return mostActiveAtom;
			}
		}
		return DEFAULT_CHOICE_ATOM;
	}

	protected boolean chooseSign(int atom) {
		atom = getAtomForChooseSign(atom);

		if (assignment.getTruth(atom) == ThriceTruth.MBT) {
			return true;
		}

		// TODO: make one sign counter out of two (maintain only balance as in clasp)
		int positiveCounter = signCounters.getOrDefault(atomToLiteral(atom, true),  DEFAULT_SIGN_COUNTER);
		int negativeCounter = signCounters.getOrDefault(atomToLiteral(atom, false), DEFAULT_SIGN_COUNTER);

		if (LOGGER.isDebugEnabled() && (nChoicesFalse + nChoicesTrue + nChoicesRand) % 100 == 0) {
			LOGGER.debug("chooseSign stats: f={}, t={}, r={}", nChoicesFalse, nChoicesTrue, nChoicesRand);
			LOGGER.debug("chooseSign stats: positiveCounter={}, negativeCounter={}", positiveCounter, negativeCounter);
		}

		if (positiveCounter > negativeCounter) {
			nChoicesFalse++;
			return false;
		} else {
			nChoicesTrue++;
			return true;
		}
	}

	protected int getAtomForChooseSign(int atom) {
		// TODO: bodyAtomToHeadAtom is not used at the moment
		Integer head = choiceManager.getHeadDerivedByChoiceAtom(atom);
		if (head != null) {
			atom = head; // head atom can give more relevant information than atom representing rule body
		}
		return atom;
	}

	protected void incrementSignCounter(Integer literal) {
		signCounters.compute(literal, (k, v) -> (v == null ? DEFAULT_SIGN_COUNTER : v) + 1);
	}

	@Override
	public double getActivity(int literal) {
		return heapOfActiveChoicePoints.getActivity(literal);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
