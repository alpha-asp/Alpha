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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;

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

	protected final Map<Integer, Double> activityCounters = new HashMap<>();
	protected final PriorityQueue<AtomActivity> heap = new PriorityQueue<>(new AtomActivityComparator().reversed());

	private int decayAge;
	private double decayFactor;
	private int stepsSinceLastDecay;
	private double currentActivityIncrement = 1.0;

	/**
	 * @param decayFactor
	 * @param decayAge
	 * 
	 */
	public HeapOfActiveAtoms(int decayAge, double decayFactor) {
		this.decayAge = decayAge;
		this.decayFactor = decayFactor;
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

	public double getActivity(int literal) {
		return activityCounters.getOrDefault(atomOf(literal), DEFAULT_ACTIVITY);
	}

	/**
	 * Sets the factor by which all counters are multiplied to decay after {@link #getDecayAge()}.
	 */
	public void setDecayFactor(double decayFactor) {
		this.decayFactor = decayFactor;
	}

	/**
	 * TODO: docs
	 * TODO: actually this does not decay but increase activity for future atoms (maybe rename?)
	 * TODO: really public?
	 */
	public void decayIfTimeHasCome() {
		stepsSinceLastDecay++;
		if (stepsSinceLastDecay >= decayAge) {
			currentActivityIncrement /= decayFactor; // TODO: is this correct?
			stepsSinceLastDecay = 0;
		}
		// TODO: "reset" if values get too high
	}

	/**
	 * TODO: docs
	 * 
	 * @return
	 */
	public Integer getMostActiveAtom() {
		AtomActivity polled = heap.poll();
		return polled != null ? polled.atom : null;
	}

	/**
	 * TODO: docs
	 */
	public void incrementActivity(int atom) {
		double newActivity = activityCounters.compute(atom, (k, v) -> (v == null ? DEFAULT_ACTIVITY : v) + currentActivityIncrement);
		addToHeap(atom, newActivity);
		LOGGER.trace("Activity of atom {} increased to {}", atom, newActivity);
	}

	protected void addToHeap(int atom, double activity) {
		heap.add(new AtomActivity(atom, activity));
	}

	private static class AtomActivity {
		int atom;
		double activity;

		private AtomActivity(int atom, double activity) {
			super();
			this.atom = atom;
			this.activity = activity;
		}

		@Override
		public String toString() {
			return atom + ":" + activity;
		}
	}

	private static class AtomActivityComparator implements Comparator<AtomActivity> {

		@Override
		public int compare(AtomActivity aa1, AtomActivity aa2) {
			return Double.compare(aa1.activity, aa2.activity);
		}

	}

}
