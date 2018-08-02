/**
 * Copyright (c) 2017-2018, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.solver;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

/**
 * Manages influence of atoms on the activity of certain other atoms. Can be used for either choice points or heuristic atoms.
 *
 */
public class ChoiceInfluenceManager implements Checkable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ChoiceInfluenceManager.class);
	
	// Active choice points and all atoms that influence a choice point (enabler, disabler, choice atom itself).
	private final Set<ChoicePoint> activeChoicePoints = new LinkedHashSet<>();
	private final Set<Integer> activeChoicePointsAtoms = new LinkedHashSet<>();
	private final Map<Integer, ChoicePoint> influencers = new HashMap<>();

	private final WritableAssignment assignment;
	private final AtomicLong modCount;

	private boolean checksEnabled;
	private boolean checksNecessary; 

	/**
	 * @param assignment 
	 * @param modCount 
	 * @param modifiedInDecisionLevel 
	 * 
	 */
	public ChoiceInfluenceManager(WritableAssignment assignment, AtomicLong modCount, boolean checksEnabled) {
		this.assignment = assignment;
		this.modCount = modCount;
		this.checksEnabled = checksEnabled;
	}

	void addInformation(Pair<Map<Integer, Integer>, Map<Integer, Integer>> choiceAtoms) {
		// Assumption: we get all enabler/disabler pairs in one call.
		Map<Integer, Integer> enablers = choiceAtoms.getLeft();
		Map<Integer, Integer> disablers = choiceAtoms.getRight();
		for (Map.Entry<Integer, Integer> atomToEnabler : enablers.entrySet()) {
			addInformation(atomToEnabler, disablers);
		}
	}

	void addInformation(Map.Entry<Integer, Integer> atomToEnabler, Map<Integer, Integer> disablers) {
		// Construct and record ChoicePoint.
		Integer atom = atomToEnabler.getKey();
		if (atom == null) {
			throw oops("Incomplete choice point description found (no atom)");
		}
		if (influencers.get(atom) != null) {
			throw oops("Received choice information repeatedly");
		}
		Integer enabler = atomToEnabler.getValue();
		Integer disabler = disablers.get(atom);
		if (enabler == null || disabler == null) {
			throw oops("Incomplete choice point description found (no enabler or disabler)");
		}
		assignment.registerCallbackOnChange(atom);
		assignment.registerCallbackOnChange(enabler);
		assignment.registerCallbackOnChange(disabler);
		ChoicePoint choicePoint = new ChoicePoint(atom, enabler, disabler);
		influencers.put(atom, choicePoint);
		influencers.put(enabler, choicePoint);
		influencers.put(disabler, choicePoint);
		choicePoint.recomputeActive();
	}
	
	void recomputeActive(int atom) {
		ChoicePoint choicePoint = influencers.get(atom);
		if (choicePoint != null) {
			choicePoint.recomputeActive();
		}
	}

	void checkActiveChoicePoints() {
		if (!checksNecessary) {
			return;
		}
		checksNecessary = false;
		
		HashSet<ChoicePoint> actualActiveChoicePoints = new HashSet<>();
		for (ChoicePoint choicePoint : influencers.values()) {
			if (checkActiveChoicePoint(choicePoint)) {
				actualActiveChoicePoints.add(choicePoint);
			}
		}
		if (!actualActiveChoicePoints.equals(activeChoicePoints)) {
			throw oops("ChoiceInfluenceManager internal checker detected wrong activeChoicePoints");
		}
		LOGGER.trace("Checking internal choice manger: all ok.");
	}
	
	private boolean checkActiveChoicePoint(ChoicePoint choicePoint) {
		ThriceTruth enablerTruth = assignment.getTruth(choicePoint.enabler);
		ThriceTruth disablerTruth = assignment.getTruth(choicePoint.disabler);
		boolean isActive = enablerTruth == TRUE
			&& (disablerTruth == null || !disablerTruth.toBoolean());
		ThriceTruth atomTruth = assignment.getTruth(choicePoint.atom);
		boolean isNotChosen = atomTruth == null || atomTruth == MBT;
		return isActive && isNotChosen;
	}

	public Set<Integer> getAllActiveInfluencedAtoms() {
		if (checksEnabled) {
			checkActiveChoicePoints();
		}
		return Collections.unmodifiableSet(activeChoicePointsAtoms);
	}

	public boolean isActive(int atom) {
		if (checksEnabled) {
			checkActiveChoicePoints();
		}
		ChoicePoint choicePoint = influencers.get(atom);
		return choicePoint != null && choicePoint.isActive && choicePoint.atom == atom;
	}

	public int getNextActiveAtomOrDefault(int defaultAtom) {
		if (checksEnabled) {
			checkActiveChoicePoints();
		}
		return activeChoicePointsAtoms.size() > 0 ? activeChoicePointsAtoms.iterator().next() : defaultAtom;
	}

	public boolean isAtomInfluenced(int atom) {
		ChoicePoint choicePoint = influencers.get(atom);
		return choicePoint != null && choicePoint.atom == atom;
	}

	@Override
	public void setChecksEnabled(boolean checksEnabled) {
		this.checksEnabled = checksEnabled;
	}

	public void callbackOnChanged(int atom) {
		LOGGER.trace("Callback received on influencer atom: {}", atom);
		modCount.incrementAndGet();
		ChoicePoint choicePoint = influencers.get(atom);
		if (choicePoint != null) {
			choicePoint.recomputeActive();
		}
	}

	private class ChoicePoint {
		final Integer atom;
		final int enabler;
		final int disabler;
		boolean isActive;

		private ChoicePoint(Integer atom, Integer enabler, int disabler) {
			this.atom = atom;
			this.enabler = enabler;
			this.disabler = disabler;
		}

		private boolean isActiveChoicePoint() {
			ThriceTruth enablerTruth = assignment.getTruth(enabler);
			ThriceTruth disablerTruth = assignment.getTruth(disabler);
			return  enablerTruth == TRUE
				&& (disablerTruth == null || !disablerTruth.toBoolean());
		}

		private boolean isNotChosen() {
			ThriceTruth atomTruth = assignment.getTruth(atom);
			return atomTruth == null || atomTruth == MBT;
		}

		void recomputeActive() {
			LOGGER.trace("Recomputing activity of atom {}.", atom);
			final boolean wasActive = isActive;
			isActive = isNotChosen() && isActiveChoicePoint();
			if (isActive) {
				activeChoicePoints.add(this);
				activeChoicePointsAtoms.add(atom);
				LOGGER.debug("Activating choice point for atom {}", this.atom);
			} else {
				if (wasActive) {
					activeChoicePoints.remove(this);
					activeChoicePointsAtoms.remove(atom);
					LOGGER.debug("Deactivating choice point for atom {}", this.atom);
				}
			}
			checksNecessary = checksEnabled;
		}

		@Override
		public String toString() {
			return String.valueOf(atom);
		}
	}

}
