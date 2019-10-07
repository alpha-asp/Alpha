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
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.BinaryNoGoodPropagationEstimation;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.learning.GroundConflictNoGoodLearner.ConflictAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;

/**
 * This implementation is like {@link VSIDS} but uses the saved phase for the truth of the chosen atom.
 *
 * Copyright (c) 2019, the Alpha Team.
 */
public class VSIDSWithPhaseSaving implements ActivityBasedBranchingHeuristic {
	protected static final Logger LOGGER = LoggerFactory.getLogger(VSIDSWithPhaseSaving.class);

	private static final int DEFAULT_DECAY_PERIOD = 1;

	/**
	 * The default factor by which VSID's activity increment will be multiplied when the decay period has expired.
	 * The value is taken from clasp's tweety configuration which clasp uses by default.
	 */
	private static final double DEFAULT_DECAY_FACTOR = 1 / 0.92;

	protected final Assignment assignment;
	protected final ChoiceManager choiceManager;
	private final HeapOfActiveAtoms heapOfActiveAtoms;
	private final Collection<NoGood> bufferedNoGoods = new ArrayList<>();

	private double activityDecrease;
	private long numThrownAway;

	private VSIDSWithPhaseSaving(Assignment assignment, ChoiceManager choiceManager, HeapOfActiveAtoms heapOfActiveAtoms, BinaryNoGoodPropagationEstimation.Strategy momsStrategy) {
		this.assignment = assignment;
		this.choiceManager = choiceManager;
		this.heapOfActiveAtoms = heapOfActiveAtoms;
		this.heapOfActiveAtoms.setMOMsStrategy(momsStrategy);
	}

	private VSIDSWithPhaseSaving(Assignment assignment, ChoiceManager choiceManager, int decayPeriod, double decayFactor, BinaryNoGoodPropagationEstimation.Strategy momsStrategy) {
		this(assignment, choiceManager, new HeapOfActiveAtoms(decayPeriod, decayFactor, choiceManager),  momsStrategy);
	}

	VSIDSWithPhaseSaving(Assignment assignment, ChoiceManager choiceManager, BinaryNoGoodPropagationEstimation.Strategy momsStrategy) {
		this(assignment, choiceManager, DEFAULT_DECAY_PERIOD, DEFAULT_DECAY_FACTOR,  momsStrategy);
	}

	@Override
	public void violatedNoGood(NoGood violatedNoGood) {
	}

	@Override
	public void analyzedConflict(ConflictAnalysisResult analysisResult) {
		ingestBufferedNoGoods();	//analysisResult may contain new atoms whose activity must be initialized
		for (int resolutionAtom : analysisResult.resolutionAtoms) {
			heapOfActiveAtoms.incrementActivity(resolutionAtom);
		}
		if (analysisResult.learnedNoGood != null) {
			for (int literal : analysisResult.learnedNoGood) {
				heapOfActiveAtoms.incrementActivity(atomOf(literal));
			}
		}
		heapOfActiveAtoms.decayIfTimeHasCome();
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
		heapOfActiveAtoms.newNoGoods(bufferedNoGoods);
		bufferedNoGoods.clear();
	}

	public double getActivityDecrease() {
		return activityDecrease;
	}

	public long getNumThrownAway() {
		return numThrownAway;
	}

	/**
	 * {@link VSIDSWithPhaseSaving} works like {@link VSIDS} for selecting an atom but uses the saved phase to
	 * determine the truth value to choose.
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

	private int chooseAtom() {
		ingestBufferedNoGoods();
		Integer mostActiveAtom;
		double maxActivity = 0.0f;
		while ((mostActiveAtom = heapOfActiveAtoms.getMostActiveAtom()) != null) {
			double activity = heapOfActiveAtoms.getActivity(atomToLiteral(mostActiveAtom));
			if (activity > maxActivity) {
				maxActivity = activity;
			}
			if (choiceManager.isActiveChoiceAtom(mostActiveAtom)) {
				if (maxActivity > activity) {
					double lostActitivyNormalized = (maxActivity - activity) / heapOfActiveAtoms.getCurrentActivityIncrement();
					activityDecrease += lostActitivyNormalized;
				}
				return mostActiveAtom;
			}
			numThrownAway++;
		}
		return DEFAULT_CHOICE_ATOM;
	}

	/**
	 * Chooses a sign (truth value) to assign to the given atom;
	 * uses the last value (saved phase) to determine its truth value.
	 * 
	 * @param atom
	 *          the chosen atom
	 * @return the truth value to assign to the given atom
	 */
	private boolean chooseSign(int atom) {
		if (assignment.getTruth(atom) == ThriceTruth.MBT) {
			return true;
		}
		return assignment.getLastValue(atom);
	}

	public void growForMaxAtomId(int maxAtomId) {
		heapOfActiveAtoms.growForMaxAtomId(maxAtomId);
	}

	@Override
	public double getActivity(int literal) {
		return heapOfActiveAtoms.getActivity(literal);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
