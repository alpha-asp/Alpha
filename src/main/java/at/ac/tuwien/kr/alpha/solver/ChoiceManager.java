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

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

/**
 * This class provides functionality for choice point management, detection of active choice points, etc.
 * Copyright (c) 2017, the Alpha Team.
 */
public class ChoiceManager implements Checkable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChoiceManager.class);
	private final WritableAssignment assignment;
	private final Stack<Choice> choiceStack;

	// Active choice points and all atoms that influence a choice point (enabler, disabler, choice atom itself).
	private final Set<ChoicePoint> activeChoicePoints = new LinkedHashSet<>();
	private final Map<Integer, ChoicePoint> influencers = new HashMap<>();

	// The total number of modifications this ChoiceManager received (avoids re-computation in ChoicePoints).
	private long modCount;

	private final NoGoodStore store;

	private boolean checksEnabled;
	private final DebugWatcher debugWatcher;

	private int choices;
	private int backtracks;
	private int backtracksWithinBackjumps;
	private int backjumps;

	public ChoiceManager(WritableAssignment assignment, NoGoodStore store) {
		this(assignment, store, false);
	}

	public ChoiceManager(WritableAssignment assignment, NoGoodStore store, boolean checksEnabled) {
		this.store = store;
		this.checksEnabled = checksEnabled;
		this.assignment = assignment;
		modCount = 0;
		this.choiceStack = new Stack<>();

		if (checksEnabled) {
			debugWatcher = new DebugWatcher();
		} else {
			debugWatcher = null;
		}
		assignment.setCallback(this);
	}

	NoGood computeEnumeration() {
		int[] enumerationLiterals = new int[choiceStack.size()];
		int enumerationPos = 0;
		for (Choice e : choiceStack) {
			enumerationLiterals[enumerationPos++] = atomToLiteral(e.getAtom(), e.getValue());
		}
		return new NoGood(enumerationLiterals);
	}

	@Override
	public void setChecksEnabled(boolean checksEnabled) {
		this.checksEnabled = checksEnabled;
	}

	public void callbackOnChanged(int atom) {
		LOGGER.trace("Callback received on influencer atom: {}", atom);
		modCount++;
		ChoicePoint choicePoint = influencers.get(atom);
		choicePoint.recomputeActive();
	}

	public int getBackjumps() {
		return backjumps;
	}

	/**
	 * Returns the total number of backtracks.
	 * 
	 * The number of backtracks excluding those within backjumps is {@link #getBacktracks()} minus {@link #getBacktracksWithinBackjumps()}.
	 * 
	 * @return the total number of backtracks
	 */
	public int getBacktracks() {
		return backtracks;
	}

	/**
	 * Returns the number of backtracks made within backjumps.
	 * 
	 * @return the number of backtracks made within backjumps.
	 */
	public int getBacktracksWithinBackjumps() {
		return backtracksWithinBackjumps;
	}

	public int getChoices() {
		return choices;
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
				LOGGER.debug("Activating choice point for atom {}", this.atom);
			} else {
				if (wasActive) {
					activeChoicePoints.remove(this);
					LOGGER.debug("Deactivating choice point for atom {}", this.atom);
				}
			}
		}

		@Override
		public String toString() {
			return String.valueOf(atom);
		}
	}

	public void updateAssignments() {
		LOGGER.trace("Updating assignments of ChoiceManager.");
		if (checksEnabled) {
			checkActiveChoicePoints();
		}
	}

	public void choose(Choice choice) {
		LOGGER.debug("Choice {} is {}@{}", choices, choice, assignment.getDecisionLevel());
		if (!choice.isBacktracked()) {
			choices++;
		}

		if (assignment.choose(choice.getAtom(), choice.getValue()) != null) {
			throw oops("Picked choice is incompatible with current assignment");
		}

		if (debugWatcher != null) {
			debugWatcher.runWatcher();
		}

		choiceStack.push(choice);
	}

	public void backjump(int target) {
		if (target < 0) {
			throw oops("Backjumping to decision level less than 0");
		}

		backjumps++;
		LOGGER.debug("Backjumping to decision level {}.", target);

		// Remove everything above the target level, but keep the target level unchanged.
		int currentDecisionLevel = assignment.getDecisionLevel();
		assignment.backjump(target);
		while (currentDecisionLevel-- > target) {
			final Choice choice = choiceStack.pop();
			backtracksWithinBackjumps++;
			backtracks++;
			modCount++;
			LOGGER.debug("Backjumping removed choice {}", choice);
		}
	}

	/**
	 * Fast backtracking will backtrack but not give any information about which choice was backtracked. This is
	 * handy in cases where higher level backtracking mechanisms already know what caused the backtracking and what
	 * to do next.
	 *
	 * In order to analyze the choice that was backtracked, use the more expensive {@link #backtrackSlow()}.
	 */
	public void backtrackFast() {
		backtrack();

		final Choice choice = choiceStack.pop();

		LOGGER.debug("Backtracked (fast) to level {} from choice {}", assignment.getDecisionLevel(), choice);
	}

	/**
	 * Slow backtracking will take more time, but allow to analyze the {@link Assignment.Entry} corresponding to the
	 * choice that is being backtracked. Higher level backtracking mechanisms can use this information to change
	 * their plans.
	 *
	 * @return the assignment entry of the choice being backtracked, or {@code null} if the choice cannot be
	 *         backtracked any futher (it already is a backtracking choice)
	 */
	public Assignment.Entry backtrackSlow() {
		final Choice choice = choiceStack.pop();
		final Assignment.Entry lastChoiceEntry = assignment.get(choice.getAtom());

		backtrack();
		LOGGER.debug("Backtracked (slow) to level {} from choice {}", assignment.getDecisionLevel(), choice);

		if (choice.isBacktracked()) {
			return null;
		}

		return lastChoiceEntry;
	}

	/**
	 * This method implements the backtracking "core" that will be executed for both slow and fast backtracking.
	 * It backtracks the NoGoodStore and recomputes choice points.
	 */
	private void backtrack() {
		store.backtrack();
		backtracks++;
		modCount++;
	}

	void addChoiceInformation(Pair<Map<Integer, Integer>, Map<Integer, Integer>> choiceAtoms) {
		// Assumption: we get all enabler/disabler pairs in one call.
		Map<Integer, Integer> enablers = choiceAtoms.getLeft();
		Map<Integer, Integer> disablers = choiceAtoms.getRight();
		for (Map.Entry<Integer, Integer> atomToEnabler : enablers.entrySet()) {
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
	}

	public boolean isActiveChoiceAtom(int atom) {
		if (checksEnabled) {
			checkActiveChoicePoints();
		}
		ChoicePoint choicePoint = influencers.get(atom);
		return choicePoint != null && choicePoint.isActive && choicePoint.atom == atom;
	}

	public int getNextActiveChoiceAtom() {
		if (checksEnabled) {
			checkActiveChoicePoints();
		}
		return activeChoicePoints.size() > 0 ? activeChoicePoints.iterator().next().atom : 0;
	}

	public boolean isAtomChoice(int atom) {
		ChoicePoint choicePoint = influencers.get(atom);
		return choicePoint != null && choicePoint.atom == atom;
	}

	private void checkActiveChoicePoints() {
		HashSet<ChoicePoint> actualActiveChoicePoints = new HashSet<>();
		for (ChoicePoint choicePoint : influencers.values()) {
			ThriceTruth enablerTruth = assignment.getTruth(choicePoint.enabler);
			ThriceTruth disablerTruth = assignment.getTruth(choicePoint.disabler);
			boolean isActive = enablerTruth == TRUE
				&& (disablerTruth == null || !disablerTruth.toBoolean());
			ThriceTruth atomTruth = assignment.getTruth(choicePoint.atom);
			boolean isNotChosen = atomTruth == null || atomTruth == MBT;
			if (isActive && isNotChosen) {
				actualActiveChoicePoints.add(choicePoint);
			}
		}
		if (!actualActiveChoicePoints.equals(activeChoicePoints)) {
			throw oops("ChoiceManger internal checker detected wrong activeChoicePoints");
		}
		LOGGER.trace("Checking internal choice manger: all ok.");
	}

	/**
	 * A helper class for halting the debugger when certain assignments occur on the choice stack.
	 *
	 * Example usage (called from DefaultSolver):
	 * choiceStack = new ChoiceStack(grounder, true);
	 * choiceStack.getDebugWatcher().watchAssignments("_R_(0,_C:red_V:7)=TRUE", "_R_(0,_C:green_V:8)=TRUE", "_R_(0,_C:red_V:9)=TRUE", "_R_(0,_C:red_V:4)=TRUE");
	 */
	class DebugWatcher {
		ArrayList<String> toWatchFor = new ArrayList<>();

		private void runWatcher() {
			String current = choiceStack.stream().map(Choice::toString).collect(Collectors.joining(", "));
			boolean contained = true;
			for (String s : toWatchFor) {
				if (!current.contains(s)) {
					contained = false;
					break;
				}
			}
			if (contained && toWatchFor.size() != 0) {
				LOGGER.debug("Marker hit.");	// Set debug breakpoint here to halt when desired assignment occurs.
			}
		}

		/**
		 * Registers atom assignments to watch for.
		 * @param toWatch one or more strings as they occur in ChoiceStack.toString()
		 */
		public void watchAssignments(String... toWatch) {
			toWatchFor = new ArrayList<>();
			Collections.addAll(toWatchFor, toWatch);
		}
	}
}
