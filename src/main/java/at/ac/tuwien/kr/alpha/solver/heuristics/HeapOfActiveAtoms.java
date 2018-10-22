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

import at.ac.tuwien.kr.alpha.common.Literals;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.BinaryNoGoodPropagationEstimation;
import at.ac.tuwien.kr.alpha.solver.ChoiceInfluenceManager;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static java.lang.Math.max;

/**
 * Manages a heap of atoms that are assigned an activity, such that the most active atom
 * resides at the top of the heap.
 * In contrast to standard heuristics like VSIDS, activities are not periodically decayed but
 * the increment added when increasing activities is constantly increased itself, which has the
 * same effect.
 *
 */
public class HeapOfActiveAtoms {
	protected static final Logger LOGGER = LoggerFactory.getLogger(HeapOfActiveAtoms.class);

	public static final double DEFAULT_ACTIVITY = 0.0;
	public static final double DEFAULT_INCREMENT_FACTOR = 1.0;
	private static final double NORMALIZATION_THRESHOLD = 1E100;

	protected final Map<Integer, Double> activityScores = new HashMap<>();
	protected final PriorityQueue<Integer> heap = new PriorityQueue<>(new AtomActivityComparator().reversed());

	protected ChoiceManager choiceManager;
	private int decayFrequency;
	private double decayFactor;
	private int stepsSinceLastDecay;
	private double currentActivityIncrement = 1.0;
	private double incrementFactor = DEFAULT_INCREMENT_FACTOR; // TODO: make configurable
	
	private final MOMs moms;

	/**
	 * @param decayFactor
	 * @param decayAge
	 * 
	 */
	public HeapOfActiveAtoms(int decayAge, double decayFactor, ChoiceManager choiceManager) {
		this.decayFrequency = decayAge;
		this.decayFactor = decayFactor;
		this.choiceManager = choiceManager;
		this.choiceManager.addChoicePointActivityListener(new ChoicePointActivityListener());
		BinaryNoGoodPropagationEstimation bnpEstimation = choiceManager.getBinaryNoGoodPropagationEstimation();
		this.moms = bnpEstimation == null ? null : new MOMs(bnpEstimation);
	}

	public double getActivity(int literal) {
		return activityScores.getOrDefault(atomOf(literal), DEFAULT_ACTIVITY);
	}

	/**
	 * Gets the number of steps after which activity scores are decayed.
	 */
	public int getDecayFrequency() {
		return decayFrequency;
	}

	/**
	 * @see #getDecayFrequency()
	 */
	public void setDecayFrequency(int decayAge) {
		this.decayFrequency = decayAge;
	}

	/**
	 * Gets the factor by which the activity increment is multiplied every {@link #getDecayFrequency()} steps.
	 */
	public double getDecayFactor() {
		return decayFactor;
	}

	/**
	 * @see #getDecayFactor()
	 */
	public void setDecayFactor(double decayFactor) {
		this.decayFactor = decayFactor;
	}

	/**
	 * Updates the current activity increment by multiplying it with the decay factor, if time for decay has come.
	 * 
	 * Time for decay has come if the number of conflicts since the last decay has reached the decay frequency.
	 * 
	 * TODO: actually this does not decay but increase activity for future atoms (maybe rename?)
	 * TODO: really public?
	 */
	public void decayIfTimeHasCome() {
		stepsSinceLastDecay++;
		if (stepsSinceLastDecay >= decayFrequency) {
			currentActivityIncrement *= decayFactor;
			stepsSinceLastDecay = 0;
		}
	}

	/**
	 * Computes and stores initial activity values for the atoms occurring in the given nogoods.
	 */
	public void initActity(Collection<NoGood> newNoGoods) {
		// TODO: do this only for static nogoods (?)
		if (moms != null) {
			initActivityMOMs(newNoGoods);
		} else {
			initActivityNaive(newNoGoods);
		}
	}

	private void initActivityMOMs(Collection<NoGood> newNoGoods) {
		LOGGER.debug("Initializing activity scores with MOMs");
		Map<Integer, Double> newActivityScores = new HashMap<>();
		double maxScore = 0.0;
		Set<Integer> atoms = Literals.getAtoms(newNoGoods); // TODO: might be inefficient
		for (Integer atom : atoms) {
			// TODO: make this more performant by converting activityScores to an array and only respecting new atoms outside the former array bounds
			if (!activityScores.containsKey(atom)) { // TODO: && unassigned (?)
				double score = moms.getScore(atom);
				if (score != 0.0) {
					maxScore = max(score, maxScore);
					newActivityScores.put(atom, score);
				}
			}
		}
		for (Entry<Integer, Double> newAtomActivity : newActivityScores.entrySet()) {
			Integer atom = newAtomActivity.getKey();
			double normalizedScore = newAtomActivity.getValue() / maxScore;
			incrementActivity(atom, normalizedScore);
		}
	}

	private void initActivityNaive(Collection<NoGood> newNoGoods) {
		LOGGER.debug("Initializing activity scores naively");
		for (NoGood newNoGood : newNoGoods) {
			for (Integer literal : newNoGood) {
				incrementActivity(atomOf(literal));
			}
		}
	}

	/**
	 * Returns the atom with the highest activity score and removes it from the heap.
	 */
	public Integer getMostActiveAtom() {
		return heap.poll();
	}

	/**
	 * Increments the activity of the given atom
	 * 
	 * by adding to it the current activity increment times the increment factor.
	 * If the new value exceeds a certain threshold, all activity scores are normalized.
	 */
	public void incrementActivity(int atom) {
		incrementActivity(atom, currentActivityIncrement);
	}
	
	private void incrementActivity(int atom, double increment) {
		// newActivity := oldActivity + (increment * incrementFactor)
		double newActivity = activityScores.compute(atom, (k, v) -> (v == null ? DEFAULT_ACTIVITY : v) + (increment * incrementFactor));
		LOGGER.trace("Activity of atom {} increased to {}", atom, newActivity);
		
		if (newActivity > NORMALIZATION_THRESHOLD) {
			normalizeActivityScores();
		} else {
			heap.add(atom); // ignores the fact that atom may already be in the heap for performance reasons (may be revised in future)
		}
	}

	/**
	 * Makes all activity scores smaller if they get too high.
	 * 
	 * Avoids <a href="https://en.wikipedia.org/wiki/Denormal_number">denormal numbers</a> similarly as done in clasp.
	 */
	private void normalizeActivityScores() {
		LOGGER.debug("Normalizing activity scores");
		final double min = Double.MIN_VALUE * NORMALIZATION_THRESHOLD;
		currentActivityIncrement /= NORMALIZATION_THRESHOLD;
		for (Integer atom : activityScores.keySet()) {
			activityScores.compute(atom, (k, v) -> (v + min) / NORMALIZATION_THRESHOLD);
		}
		heap.clear();
		heap.addAll(activityScores.keySet());
	}

	private class AtomActivityComparator implements Comparator<Integer> {

		@Override
		public int compare(Integer a1, Integer a2) {
			return Double.compare(activityScores.get(a1), activityScores.get(a2));
		}

	}

	private class ChoicePointActivityListener implements ChoiceInfluenceManager.ActivityListener {

		@Override
		public void callbackOnChanged(int atom, boolean active) {
			if (active && choiceManager.isActiveChoiceAtom(atom)) {
				Double activity = activityScores.get(atom);
				if (activity != null) {
					/* if activity is null, probably the atom is still being buffered
					   by DependencyDrivenVSIDSHeuristic and will get an initial activity
					   when the buffer is ingested */
					heap.add(atom);
				}
			}
		}
	}

}
