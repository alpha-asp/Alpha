/**
 * Copyright (c) 2016-2017, the Alpha Team.
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

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.arrayGrowthSize;
import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.common.Literals.*;
import static at.ac.tuwien.kr.alpha.common.NoGood.HEAD;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;

/**
 * NoGoodStore using for each NoGood three watches, two ordinary ones and an alpha watch.
 * The alpha watch may point to positive or negative literals but not the head of the NoGood.
 *
 * Note the following invariant for watches:
 * (Inv): For every NoGood it either holds that both watched literals are unassigned, or for one watch holds: it
 *        points to a literal that is assigned a satisfying truth value at decision level d and the other
 *        watched literal is assigned at decision level d' such that d <= d'.
 *  The second condition ensures that after backtracking the NoGood is still satisfied or both watches
 *  point to unassigned literals. Observe that for an assignment to TRUE the (potentially lower) decision level of MBT
 *  is taken.
 */
public class NoGoodStoreAlphaRoaming implements NoGoodStore, Checkable {
	private static final Logger LOGGER = LoggerFactory.getLogger(NoGoodStoreAlphaRoaming.class);
	private static final int UNASSIGNED = Integer.MAX_VALUE;

	private final WritableAssignment assignment;
	private final Map<Integer, Watches<BinaryWatch, WatchedNoGood>> watches = new LinkedHashMap<>();
	private BinaryWatchList[] binaryWatches = new BinaryWatchList[0];

	private boolean checksEnabled;
	private boolean didPropagate;

	public NoGoodStoreAlphaRoaming(WritableAssignment assignment, boolean checksEnabled) {
		this.assignment = assignment;
		this.checksEnabled = checksEnabled;
	}

	public NoGoodStoreAlphaRoaming(WritableAssignment assignment) {
		this(assignment, false);
	}

	void clear() {
		assignment.clear();
		watches.clear();
		binaryWatches = new BinaryWatchList[0];
	}

	@Override
	public void backtrack() {
		didPropagate = false;
		assignment.backtrack();
		if (checksEnabled) {
			if (assignment.getAssignmentsToProcess().isEmpty()) {
				new WatchedNoGoodsChecker().doWatchesCheck();
			} else {
				LOGGER.trace("Skipping watches check since there are assignments to process first.");
			}
		}
	}

	@Override
	public void growForMaxAtomId(int maxAtomId) {
		int requiredMaxSize = 2 * (maxAtomId + 2);
		if (requiredMaxSize < binaryWatches.length) {
			return;
		}
		int newCapacity = arrayGrowthSize(binaryWatches.length);
		if (newCapacity < requiredMaxSize) {
			newCapacity = requiredMaxSize;
		}
		int oldlength = binaryWatches.length;
		binaryWatches = Arrays.copyOf(binaryWatches, newCapacity);
		for (int i = oldlength; i < binaryWatches.length; i++) {
			binaryWatches[i] = new BinaryWatchList(i);
		}
	}

	private Watches<BinaryWatch, WatchedNoGood> watches(int literal) {
		final int atom = atomOf(literal);
		Watches<BinaryWatch, WatchedNoGood> result = watches.get(atom);
		if (result == null) {
			result = new Watches<>();
			this.watches.put(atom, result);
		}
		return result;
	}

	private void addOrdinaryWatch(WatchedNoGood wng, int pointer) {
		final int literal = wng.getLiteral(wng.getPointer(pointer));
		watches(literal).multary.getOrdinary(isPositive(literal)).add(wng);
	}

	private void addAlphaWatch(WatchedNoGood wng) {
		final int literal = wng.getLiteralAtAlpha();
		watches(literal).multary.getAlpha(isPositive(literal)).add(wng);
	}

	@Override
	public ConflictCause add(int id, NoGood noGood) {
		LOGGER.trace("Adding {}", noGood);

		if (noGood.isUnary()) {
			return addUnary(noGood);
		} else if (noGood.isBinary()) {
			return addAndWatchBinaryImproved(noGood);
		} else {
			return addAndWatch(noGood);
		}
	}

	/**
	 * Takes a noGood containing only a single literal and translates it into an assignment (because it
	 * is trivially unit). Still, a check for conflict is performed.
	 */
	private ConflictCause addUnary(final NoGood noGood) {
		if (noGood.hasHead()) {
			return assignStrongComplement(noGood, 0);
		} else {
			return assignWeakComplement(HEAD, noGood, 0);
		}
	}

	private static boolean isComplementaryAssigned(int literal, ThriceTruth literalTruth) {
		return literalTruth != null && literalTruth.toBoolean() != isPositive(literal);
	}

	private int strongDecisionLevel(int atom) {
		int strongDecisionLevel = assignment.getStrongDecisionLevel(atom);
		return strongDecisionLevel == -1 ? UNASSIGNED : strongDecisionLevel;
	}

	private ConflictCause addAndWatch(final NoGood noGood) {
		// Collect potential watch candidates.
		int posWeakUnassigned1 = -1;
		int posWeakUnassigned2 = -1;
		int posSatisfiedLiteral1 = -1;
		int posSatisfiedLiteral2 = -1;
		int posWeakHighestAssigned = -1;
		int weakDecisionLevelHighestAssigned = -1;
		int posStrongHighestAssigned = -1;
		int strongDecisionLevelHighestAssigned = -1;
		int posPotentialAlphaWatch = -1;
		int satisfiedLiteralWeakDecisionLevel = -1;

		// Used to detect always-satisfied NoGoods of form { L, -L, ... }.
		Map<Integer, Boolean> occurringAtomPolarity = new HashMap<>();

		// Iterate noGood and record satisfying/unassigned/etc positions.
		int headLiteral = noGood.getPositiveLiteral(HEAD);
		final ThriceTruth headTruth = noGood.hasHead() ? assignment.getTruth(atomOf(headLiteral)) : null;
		final boolean isHeadTrue = headTruth == TRUE;
		for (int i = 0; i < noGood.size(); i++) {
			final int literal = noGood.getLiteral(i);
			final int atom = atomOf(literal);
			final ThriceTruth atomTruthValue = assignment.getTruth(atom);
			final int atomWeakDecisionLevel = assignment.getWeakDecisionLevel(atom);
			final int atomStrongDecisionLevel = assignment.getStrongDecisionLevel(atom);

			// Check if NoGood can never be violated (atom occurring positive and negative)
			if (occurringAtomPolarity.containsKey(atomOf(literal))) {
				if (occurringAtomPolarity.get(atomOf(literal)) != isNegated(literal)) {
					// NoGood cannot be violated or propagate, ignore it.
					LOGGER.debug("Added NoGood can never propagate or be violated, ignoring it. NoGood is: " + noGood);
					return null;
				}
			} else {
				occurringAtomPolarity.put(atomOf(literal), isNegated(literal));
			}

			// Check weak unassigned.
			if (atomTruthValue == null) {
				if (posWeakUnassigned1 == -1) {
					posWeakUnassigned1 = i;
				} else {
					posWeakUnassigned2 = i;
				}
			}
			// Alpha watch:
			if (posPotentialAlphaWatch == -1 && noGood.hasHead() && i != HEAD) {
				// Current literal is potential alpha watch if:
				// 1) the head of the nogood is true and the literal is assigned at a higher-or-equal decision level.
				// 2) the literal is complementary assigned and thus satisfies the nogood, or
				// 3) the literal is unassigned or assigned must-be-true.
				if (isHeadTrue && strongDecisionLevel(atomOf(literal)) >= strongDecisionLevel(atomOf(headLiteral))
					|| isComplementaryAssigned(noGood.getLiteral(i), atomTruthValue)
					|| strongDecisionLevel(atomOf(literal)) == Integer.MAX_VALUE) {
					posPotentialAlphaWatch = i;
				}
			}
			// Check satisfaction
			if (atomTruthValue != null && atomTruthValue.toBoolean() != isPositive(literal)) {
				if (posSatisfiedLiteral1 == -1) {
					posSatisfiedLiteral1 = i;
					satisfiedLiteralWeakDecisionLevel = atomWeakDecisionLevel;
				} else {
					posSatisfiedLiteral2 = i;
				}
			}
			// Check violation.
			if (atomTruthValue != null && atomTruthValue.toBoolean() == isPositive(literal)) {
				if (atomWeakDecisionLevel > weakDecisionLevelHighestAssigned) {
					weakDecisionLevelHighestAssigned = atomWeakDecisionLevel;
					posWeakHighestAssigned = i;
				}
				if (!atomTruthValue.isMBT() && noGood.hasHead()	// Ensure strong violation.
					&& atomStrongDecisionLevel > strongDecisionLevelHighestAssigned) {
					strongDecisionLevelHighestAssigned = atomStrongDecisionLevel;
					posStrongHighestAssigned = i;
				}
			}
		}

		// Set ordinary and alpha watches now.
		// Compute ordinary watches:
		final WatchedNoGood wng;

		if (posWeakUnassigned1 != -1 && posWeakUnassigned2 != -1) {
			// NoGood has two unassigned literals.
			wng = new WatchedNoGood(noGood, posWeakUnassigned1, posWeakUnassigned2, -1);
		} else if (posSatisfiedLiteral1 != -1) {
			// NoGood is satisfied.
			int bestSecondPointer = posSatisfiedLiteral2 != -1 ? posSatisfiedLiteral2
						: posWeakUnassigned1 != -1 ? posWeakUnassigned1
						: posWeakHighestAssigned;
			if (posSatisfiedLiteral2 == -1 && posWeakUnassigned1 == -1) {
				// The NoGood has only one satisfied literal and is unit without it.
				// If it is unit on lower decision level than it is satisfied, it propagates the satisfying literal on lower decision level.
				if (satisfiedLiteralWeakDecisionLevel > weakDecisionLevelHighestAssigned) {
					ConflictCause conflictCause = assignWeakComplement(posSatisfiedLiteral1, noGood, weakDecisionLevelHighestAssigned);
					if (conflictCause != null) {
						return conflictCause;
					}
				}
			}
			wng = new WatchedNoGood(noGood, posSatisfiedLiteral1, bestSecondPointer, -1);
		} else if (posWeakUnassigned1 != -1) {
			// NoGood is weakly unit; propagate.
			ConflictCause conflictCause = assignWeakComplement(posWeakUnassigned1, noGood, weakDecisionLevelHighestAssigned);
			if (conflictCause != null) {
				return conflictCause;
			}
			wng = new WatchedNoGood(noGood, posWeakUnassigned1, posWeakHighestAssigned, -1);
		} else {
			// NoGood is violated.
			return new ConflictCause(noGood);
		}

		// Compute alpha watch:
		if (noGood.hasHead()) {
			if (posPotentialAlphaWatch != -1) {
				// Found potential alpha watch.
				wng.setAlphaPointer(posPotentialAlphaWatch);
			} else {
				// No potential alpha watch found: noGood must be strongly unit.
				ConflictCause conflictCause = assignStrongComplement(noGood, strongDecisionLevelHighestAssigned);
				if (conflictCause != null) {
					return conflictCause;
				}
				wng.setAlphaPointer(posStrongHighestAssigned);
			}
		}

		if (wng.getAlphaPointer() == -1 && noGood.hasHead()) {
			throw oops("Did not set alpha watch for nogood with head.");
		}

		// Register alpha watch if present.
		if (wng.getAlphaPointer() != -1) {
			addAlphaWatch(wng);
		}
		// Set ordinary watches.
		addOrdinaryWatch(wng, 0);
		addOrdinaryWatch(wng, 1);
		return null;
	}

	private ConflictCause addAndWatchBinaryImproved(final NoGood noGood) {
		// Shorthands for viewing the nogood as { a, b }.
		final int a = noGood.getLiteral(0);
		final int b = noGood.getLiteral(1);
		final int atomA = atomOf(a);
		final int atomB = atomOf(b);

		// Ignore NoGoods of the form { -a, a }.
		if (a != b && atomA == atomB) {
			return null;
		}

		// Note: it might be faster to not check explicitly for violation but wait for conflict from assign.
		final ThriceTruth atomATruthValue = assignment.getTruth(atomA);
		final ThriceTruth atomBTruthValue = assignment.getTruth(atomB);

		final boolean isViolatedA = atomATruthValue != null && isPositive(a) == atomATruthValue.toBoolean();
		final boolean isViolatedB = atomBTruthValue != null && isPositive(b) == atomBTruthValue.toBoolean();

		// Check for violation.
		if (isViolatedA && isViolatedB) {
			return new ConflictCause(noGood);
		}

		ConflictCause conflictCause = binaryWatches[a].add(noGood);
		if (conflictCause != null) {
			return conflictCause;
		}
		return binaryWatches[b].add(noGood);
	}

	private ConflictCause assignWeakComplement(final int literalIndex, final NoGood impliedBy, int decisionLevel) {
		final int literal = impliedBy.getLiteral(literalIndex);
		ThriceTruth truth = isNegated(literal) ? MBT : FALSE;
		return assignTruth(atomOf(literal), truth, impliedBy, decisionLevel);
	}

	private ConflictCause assignStrongComplement(final NoGood impliedBy, int decisionLevel) {
		return assignTruth(atomOf(impliedBy.getPositiveLiteral(HEAD)), TRUE, impliedBy, decisionLevel);
	}

	private ConflictCause assignTruth(int atom, ThriceTruth truth, NoGood impliedBy, int decisionLevel) {
		ConflictCause cause = assignment.assign(atom, truth, impliedBy, decisionLevel);
		if (cause == null) {
			didPropagate = true;
		}
		return cause;
	}

	/**
	 * Propagates from Unassigned to MBT/FALSE.
	 * @param literal the literal that triggers the propagation.
	 * @param truth the assigned truth value.
	 */
	private ConflictCause propagateWeakly(int literal, ThriceTruth truth) {
		final Watches<BinaryWatch, WatchedNoGood> watchesOfAssignedAtom = watches(literal);

		// Propagate binary watches.
		ConflictCause conflictCause = binaryWatches[literal].propagateWeakly();
		if (conflictCause != null) {
			return conflictCause;
		}

		// Check all watched multi-ary NoGoods.
		Iterator<WatchedNoGood> watchIterator = watchesOfAssignedAtom.multary.getOrdinary(truth.toBoolean()).iterator();
		watchesOfAssignedAtom.multary.clearOrdinaryWatchList(truth.toBoolean());
		while (watchIterator.hasNext()) {
			WatchedNoGood nextNoGood = watchIterator.next();
			conflictCause = processWeaklyWatchedNoGood(literal, nextNoGood);
			if (conflictCause != null) {
				// Copy over all non-treated NoGoods, so that they can be treated after backtracking.
				ArrayList<WatchedNoGood> watchlist = watchesOfAssignedAtom.multary.getOrdinary(truth.toBoolean());
				watchlist.add(nextNoGood);
				while (watchIterator.hasNext()) {
					watchlist.add(watchIterator.next());
				}
				return conflictCause;
			}
		}
		return null;
	}

	private ConflictCause processWeaklyWatchedNoGood(int assignedLiteral, WatchedNoGood watchedNoGood) {
		final int assignedPointer = watchedNoGood.getLiteralAtPointer(0) == assignedLiteral ? 0 : 1;
		final int otherPointer = 1 - assignedPointer;

		final int assignedIndex = watchedNoGood.getPointer(assignedPointer);
		final int otherIndex = watchedNoGood.getPointer(otherPointer);

		int otherAtom = atomOf(watchedNoGood.getLiteral(otherIndex));
		final ThriceTruth otherAtomTruth = assignment.getTruth(otherAtom);

		// Find new literal to watch.
		boolean foundPointerCandidate = false;
		int pointerCandidateIndex = assignedIndex;

		// Check if the other watch already satisfies the noGood.
		if (otherAtomTruth != null && otherAtomTruth.toBoolean() != isPositive(watchedNoGood.getLiteral(otherIndex))) {
			foundPointerCandidate = true;
			// Keep this watch and return early.
			// Note that other literal cannot have higher decision level than currently due to new out-of-order literals in assignment.
		} else {
			for (int j = 0; j < watchedNoGood.size(); j++) {
				int i = (j + assignedIndex) % watchedNoGood.size();        // Start iteration on currently watched index and loop around.
				if (i == assignedIndex || i == otherIndex) {
					continue;
				}
				final int currentLiteral = watchedNoGood.getLiteral(i);
				final int currentAtom = atomOf(currentLiteral);
				final ThriceTruth currentTruth = assignment.getTruth(currentAtom);

				// Break if: 1) current literal is unassigned, or 2) satisfies the nogood.
				if (currentTruth == null || currentTruth.toBoolean() != isPositive(currentLiteral)) {
					foundPointerCandidate = true;
					pointerCandidateIndex = i;
					break;
				}
			}
		}

		if (foundPointerCandidate) {
			// Move pointer to new literal.
			watchedNoGood.setPointer(assignedPointer, pointerCandidateIndex);
			addOrdinaryWatch(watchedNoGood, assignedPointer);
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Moved watch pointers of nogood:");
				logNoGoodAndAssignment(watchedNoGood, assignment);
			}
			return null;
		}
		// NoGood is unit, propagate (on potentially lower decision level).
		// Note: Violation is detected by Assignment.
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Nogood is unit:");
			logNoGoodAndAssignment(watchedNoGood, assignment);
		}
		ConflictCause conflictCause = assignWeakComplement(otherIndex, watchedNoGood, assignment.getDecisionLevel());

		// Return conflict if noGood is violated.
		if (conflictCause != null) {
			return conflictCause;
		}

		// Move assigned watch to now-highest position.
		watchedNoGood.setPointer(assignedPointer, pointerCandidateIndex);
		addOrdinaryWatch(watchedNoGood, assignedPointer);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Moved watch pointers after propagation:");
			logNoGoodAndAssignment(watchedNoGood, assignment);
		}
		return null;
	}

	private void logNoGoodAndAssignment(WatchedNoGood noGood, Assignment assignment) {
		if (!LOGGER.isTraceEnabled()) {
			return;
		}
		StringBuilder sb = new StringBuilder("Watched NoGood is: " + noGood + "\t\t Assigned: ");
		for (Integer literal : noGood) {
			Assignment.Entry assignmentEntry = assignment.get(atomOf(literal));
			sb.append(atomOf(literal));
			sb.append("=");
			sb.append(assignmentEntry);
			sb.append(", ");
		}
		LOGGER.trace(sb.toString());
	}

	private ConflictCause propagateStrongly(final Assignment.Entry entry) {
		// Nothing needs to be done in case MBT is assigned since MBT cannot trigger strong unit-propagation.
		if (entry.getTruth().isMBT()) {
			return null;
		}
		final int assignedLiteral = entry.getLiteral();

		ConflictCause conflictCause = binaryWatches[assignedLiteral].propagateStrongly();
		if (conflictCause != null) {
			return conflictCause;
		}

		Watches<BinaryWatch, WatchedNoGood> watchesOfAssignedAtom = watches(entry.getLiteral());

		int assignedDecisionLevel = entry.getStrongDecisionLevel();

		Iterator<WatchedNoGood> watchIterator = watchesOfAssignedAtom.multary.getAlpha(entry.getTruth().toBoolean()).iterator();
		watchesOfAssignedAtom.multary.clearAlphaWatchList(entry.getTruth().toBoolean());

		while (watchIterator.hasNext()) {
			final WatchedNoGood watchedNoGood = watchIterator.next();

			if (!watchedNoGood.hasHead()) {
				throw oops("Strong propagation encountered NoGood without head");
			}

			final int assignedIndex = watchedNoGood.getAlphaPointer();

			boolean isNoGoodSatisfiedByHead = false;
			int headAtom = atomOf(watchedNoGood.getLiteral(HEAD));
			ThriceTruth headAtomTruth = assignment.getTruth(headAtom);
			int headAtomStrongDecisionLevel = assignment.getStrongDecisionLevel(headAtom);
			// Check if the other watch already satisfies the noGood.
			if (headAtomTruth != null && TRUE == headAtomTruth && !isPositive(watchedNoGood.getLiteral(HEAD))) {
				isNoGoodSatisfiedByHead = true;
			}

			int highestDecisionLevel = assignedDecisionLevel;
			int pointerCandidateIndex = assignedIndex;
			boolean foundPointerCandidate = false;

			// Find new literal to watch.
			for (int i = 1; i < watchedNoGood.size(); i++) {
				if (i == assignedIndex) {
					continue;
				}
				int currentLiteral = watchedNoGood.getLiteral(i);
				int currentAtom = atomOf(currentLiteral);
				ThriceTruth currentAtomTruth = assignment.getTruth(currentAtom);
				int currentAtomStrongDecisionLevel = assignment.getStrongDecisionLevel(currentAtom);

				// Break if: 1) current literal is unassigned (or MBT), 2) satisfies the nogood, or
				// 3) the nogood is satisfied by the head and the current literal has decision level greater than the head literal.
				if (currentAtomTruth == null || currentAtomTruth.isMBT()
					|| currentAtomTruth.toBoolean() != isPositive(currentLiteral)
					|| (isNoGoodSatisfiedByHead && currentAtomStrongDecisionLevel >= headAtomStrongDecisionLevel)
					) {
					foundPointerCandidate = true;
					pointerCandidateIndex = i;
					break;
				}

				// Record literal if it has highest decision level so far.
				if (currentAtomStrongDecisionLevel > highestDecisionLevel) {
					highestDecisionLevel = currentAtomStrongDecisionLevel;
					pointerCandidateIndex = i;
				}
			}

			if (foundPointerCandidate) {
				// Move pointer to new literal.
				watchedNoGood.setAlphaPointer(pointerCandidateIndex);
				addAlphaWatch(watchedNoGood);
				continue;
			}

			// NoGood is unit, propagate (on potentially lower decision level).
			conflictCause = assignStrongComplement(watchedNoGood, highestDecisionLevel);
			if (conflictCause != null) {
				// Copy over all non-treated NoGoods, so that they can be treated after backtracking.
				ArrayList<WatchedNoGood> watchlist = watchesOfAssignedAtom.multary.getAlpha(entry.getTruth().toBoolean());
				watchlist.add(watchedNoGood);
				while (watchIterator.hasNext()) {
					watchlist.add(watchIterator.next());
				}
				return conflictCause;
			}

			// Move assigned watch to now-highest position.
			watchedNoGood.setAlphaPointer(pointerCandidateIndex);
			addAlphaWatch(watchedNoGood);
		}
		return null;
	}

	@Override
	public ConflictCause propagate() {
		didPropagate = false;

		Assignment.Pollable<? extends Assignment.Entry> assignmentsToProcess = assignment.getAssignmentsToProcess();
		while (!assignmentsToProcess.isEmpty()) {
			final Assignment.Entry currentEntry = assignmentsToProcess.peek();
			final int atom = currentEntry.getAtom();
			LOGGER.trace("Propagation processing entry: {}", currentEntry);

			// TODO: if we had a guarantee that TRUE is only assigned after MBT, then propagateWeakly could skip TRUE alltogether!
			ThriceTruth currentTruth = assignment.getTruth(atom);
			ConflictCause conflictCause = propagateWeakly(currentEntry.getLiteral(), currentTruth);
			if (conflictCause != null) {
				LOGGER.trace("Halting propagation due to conflict. Current assignment: {}.", assignment);
				return conflictCause;
			}

			conflictCause = propagateStrongly(currentEntry);
			if (conflictCause != null) {
				LOGGER.trace("Halting propagation due to conflict. Current assignment: {}.", assignment);
				return conflictCause;
			}
			assignmentsToProcess.remove();
		}
		if (checksEnabled) {
			runInternalChecks();
		}
		return null;
	}

	@Override
	public boolean didPropagate() {
		return didPropagate;
	}

	@Override
	public void setChecksEnabled(boolean checksEnabled) {
		this.checksEnabled = checksEnabled;
	}

	private static final class BinaryWatch {
		private final NoGood noGood;
		private final int otherLiteralIndex;

		private BinaryWatch(NoGood noGood, int otherLiteralIndex) {
			this.noGood = noGood;
			this.otherLiteralIndex = otherLiteralIndex;
		}

		NoGood getNoGood() {
			return noGood;
		}

		int getOtherLiteralIndex() {
			return otherLiteralIndex;
		}
	}

	private class BinaryWatchList implements ImplicationReasonProvider {
		private int[] noGoodsWithoutHead = new int[10];
		private int noGoodsWithoutHeadSize;
		private int[] noGoodsWithHead = new int[10];
		private int noGoodsWithHeadSize;
		private final int forLiteral;

		private BinaryWatchList(int forLiteral) {
			this.forLiteral = forLiteral;
		}

		ConflictCause add(NoGood noGood) {
			if (!noGood.isBinary()) {
				throw oops("Received noGood is not binary.");
			}
			if (noGood.hasHead() && noGood.getLiteral(HEAD) != forLiteral) {
				return addHeadedNoGood(noGood);
			} else {
				return addOrdinaryNoGood(noGood);
			}
		}

		private ConflictCause addHeadedNoGood(NoGood noGood) {
			if (noGoodsWithHeadSize + 1 > noGoodsWithHead.length) {
				noGoodsWithHead = Arrays.copyOf(noGoodsWithHead, Util.arrayGrowthSize(noGoodsWithHeadSize));
			}
			int otherLiteral = noGood.getLiteral(0) == forLiteral ? noGood.getLiteral(1) : noGood.getLiteral(0);
			if (isPositive(otherLiteral)) {
				throw oops("NoGood has wrong head.");
			}
			noGoodsWithHead[noGoodsWithHeadSize++] = otherLiteral;
			// Assign (weakly) otherLiteral if the newly added NoGood is unit.
			ThriceTruth literalTruth = assignment.getTruth(atomOf(forLiteral));
			if (literalTruth != null && literalTruth.toBoolean() == isPositive(forLiteral)) {
				int weakDecisionLevel = assignment.getWeakDecisionLevel(atomOf(forLiteral));
				ConflictCause conflictCause = assignment.assign(atomOf(otherLiteral), isPositive(otherLiteral) ? FALSE : MBT, this, weakDecisionLevel);
				if (conflictCause != null) {
					return conflictCause;
				}
			}
			// Assign head (strongly) if the newly added NoGood is unit.
			int strongDecisionLevel = assignment.getStrongDecisionLevel(atomOf(forLiteral));
			if (strongDecisionLevel != -1 && assignment.getTruth(atomOf(forLiteral)).toBoolean() == isPositive(forLiteral)) {
				return assignment.assign(atomOf(otherLiteral), TRUE, this, strongDecisionLevel);
			}
			return null;
		}

		private ConflictCause addOrdinaryNoGood(NoGood noGood) {
			if (noGoodsWithoutHeadSize + 1 > noGoodsWithoutHead.length) {
				noGoodsWithoutHead = Arrays.copyOf(noGoodsWithoutHead, Util.arrayGrowthSize(noGoodsWithoutHeadSize));
			}
			int otherLiteral = noGood.getLiteral(0) == forLiteral ? noGood.getLiteral(1) : noGood.getLiteral(0);
			noGoodsWithoutHead[noGoodsWithoutHeadSize++] = otherLiteral;
			// Assign otherLiteral if the newly added NoGood is unit.
			ThriceTruth literalTruth = assignment.getTruth(atomOf(forLiteral));
			if (literalTruth != null && literalTruth.toBoolean() == isPositive(forLiteral)) {
				int weakDecisionLevel = assignment.getWeakDecisionLevel(atomOf(forLiteral));
				return assignment.assign(atomOf(otherLiteral), isPositive(otherLiteral) ? FALSE : MBT, this, weakDecisionLevel);
			}
			return null;
		}

		@Override
		public NoGood getNoGood(int impliedAtom) {
			return new NoGood(impliedAtom, forLiteral);
		}
		
		ConflictCause propagateWeakly() {
			didPropagate |= noGoodsWithHeadSize > 0 || noGoodsWithoutHeadSize > 0;
			for (int i = 0; i < noGoodsWithoutHeadSize; i++) {
				final int otherLiteral = noGoodsWithoutHead[i];
				ConflictCause conflictCause = assignment.assign(atomOf(otherLiteral), isPositive(otherLiteral) ? FALSE : MBT, this);
				if (conflictCause != null) {
					return conflictCause;
				}
			}
			for (int i = 0; i < noGoodsWithHeadSize; i++) {
				final int otherLiteral = noGoodsWithHead[i];
				ConflictCause conflictCause = assignment.assign(atomOf(otherLiteral), isPositive(otherLiteral) ? FALSE : MBT, this);
				if (conflictCause != null) {
					return conflictCause;
				}
			}
			return null;
		}

		ConflictCause propagateStrongly() {
			didPropagate |= noGoodsWithHeadSize > 0;
			for (int i = 0; i < noGoodsWithHeadSize; i++) {
				final int headLiteral = noGoodsWithHead[i];
				ConflictCause conflictCause = assignment.assign(atomOf(headLiteral), TRUE, this);
				if (conflictCause != null) {
					return conflictCause;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return "BinaryWatchList(" + forLiteral + ")";
		}
	}

	/**
	 * A simple data structure to encapsulate watched delegate by truth value tailored to {@link ThriceTruth}. It
	 * holds three separate sets that are used to refer to propagation based on assignment of one of the three truth
	 * values.
	 * @param <T> type used for referencing.
	 */
	private class WatchLists<T> {
		private ArrayList<T> positive = new ArrayList<>();
		private ArrayList<T> negative = new ArrayList<>();
		private ArrayList<T> alphaPositive = new ArrayList<>();
		private ArrayList<T> alphaNegative = new ArrayList<>();

		private ArrayList<T> getAlpha(boolean polarity) {
			return polarity ? alphaPositive : alphaNegative;
		}
		private ArrayList<T> getOrdinary(boolean polarity) {
			return polarity ? positive : negative;
		}

		void clearOrdinaryWatchList(boolean polarity) {
			if (polarity) {
				positive = new ArrayList<>();
			} else {
				negative = new ArrayList<>();
			}
		}

		void clearAlphaWatchList(boolean polarity) {
			if (polarity) {
				alphaPositive = new ArrayList<>();
			} else {
				alphaNegative = new ArrayList<>();
			}
		}
	}

	/**
	 * A simple data structure to encapsulate watched delegate for an atom. It will hold two {@link WatchLists}s, one
	 * for references resulting from nogoods containing exactly two atoms, and one for larger nogoods.
	 *
	 * To enable compact storage, you will want to use a smaller/simpler type for storing references resulting from
	 * binary nogoods (such as an {@link Integer}) whereas in the more general case, you will probably refer to some
	 * sort of {@link WatchedNoGood}.
	 *
	 * @param <B> type used to resolve references resulting from binary nogoods
	 * @param <N> type used to resolve references from nogoods sizes greater than two
	 */
	private final class Watches<B, N> {
		final WatchLists<N> multary = new WatchLists<>();
	}

	public void runInternalChecks() {
		new WatchedNoGoodsChecker().doWatchesCheck();
	}

	/**
	 * This class provides helper methods to detect NoGoods that are not properly watched by this NoGoodStore.
	 * This should be used only during debugging since checking is costly.
	 */
	private class WatchedNoGoodsChecker {

		void doWatchesCheck() {
			LOGGER.trace("Checking watch invariant.");
			// Check all watched NoGoods, if their pointers adhere to the watch-pointer invariant.
			for (Map.Entry<Integer, NoGoodStoreAlphaRoaming.Watches<NoGoodStoreAlphaRoaming.BinaryWatch, WatchedNoGood>> atomWatchesEntry : watches.entrySet()) {
				int atom = atomWatchesEntry.getKey();
				Watches<BinaryWatch, WatchedNoGood> atomWatchLists = atomWatchesEntry.getValue();
				checkOrdinaryWatchesInvariant(atom, atomWatchLists, true);
				checkOrdinaryWatchesInvariant(atom, atomWatchLists, false);
				checkAlphaWatchesInvariant(atom, atomWatchLists, true);
				checkAlphaWatchesInvariant(atom, atomWatchLists, false);
			}
			LOGGER.trace("Checking watch invariant: all good.");
		}

		int weakDecisionLevel(Assignment.Entry entry) {
			return entry == null ? UNASSIGNED : entry.hasPreviousMBT() ? entry.getMBTDecisionLevel() : entry.getDecisionLevel();
		}

		int weakReplayLevel(int atom) {
			if (assignment instanceof TrailAssignment) {
				return ((TrailAssignment) assignment).getOutOfOrderDecisionLevel(atom);
			}
			return UNASSIGNED;
		}

		int trailAwareStrongDecisionLevel(int atom) {
			int trailStrongDecisionLevel = UNASSIGNED;
			if (assignment instanceof TrailAssignment) {
				trailStrongDecisionLevel = ((TrailAssignment) assignment).getOutOfOrderStrongDecisionLevel(atom);
			}
			int atomStrongDecisionLevel = strongDecisionLevel(atom);
			return Math.min(trailStrongDecisionLevel, atomStrongDecisionLevel);
		}

		private void checkAlphaWatchesInvariant(int atom, NoGoodStoreAlphaRoaming.Watches<NoGoodStoreAlphaRoaming.BinaryWatch, WatchedNoGood> watches, boolean truth) {
			Assignment.Entry atomEntry = assignment.get(atom);
			int atomLiteral = truth ? atomToLiteral(atom) : atomToNegatedLiteral(atom);
			boolean atomSatisfies = atomEntry != null && isPositive(atomLiteral) != atomEntry.getTruth().toBoolean();
			int atomDecisionLevel = strongDecisionLevel(atom);
			BinaryWatchList binaryWatchList = binaryWatches[atomLiteral];
			for (int i = 0; i < binaryWatchList.noGoodsWithHeadSize; i++) {
				int headLiteral = binaryWatchList.noGoodsWithHead[i];
				if (headLiteral == atomLiteral) {
					throw oops("Watch invariant violated: alpha watch points at head.");
				}
				Assignment.Entry headEntry = assignment.get(atomOf(headLiteral));
				boolean headViolates = headEntry != null && isPositive(headLiteral) == headEntry.getTruth().toBoolean();
				int headDecisionLevel = trailAwareStrongDecisionLevel(atomOf(headLiteral));
				if (watchInvariant(atomSatisfies, atomDecisionLevel, headLiteral, headDecisionLevel, headEntry)
					|| headViolates) {	// Head "pointer" is never moved and violation is checked by weak propagation, hence a violated head is okay.
					continue;
				}
				throw oops("Watch invariant (alpha) violated");
			}
			for (WatchedNoGood watchedNoGood : watches.multary.getAlpha(truth)) {
				int headLiteral = watchedNoGood.getLiteral(HEAD);
				if (headLiteral == atomLiteral) {
					throw oops("Watch invariant violated: alpha watch points at head.");
				}
				Assignment.Entry headEntry = assignment.get(atomOf(headLiteral));
				boolean headViolates = headEntry != null && isPositive(headLiteral) == headEntry.getTruth().toBoolean();
				int headDecisionLevel = trailAwareStrongDecisionLevel(atomOf(headLiteral));
				if (watchInvariant(atomSatisfies, atomDecisionLevel, headLiteral, headDecisionLevel, headEntry)
					|| headViolates) {	// Head "pointer" is never moved and violation is checked by weak propagation, hence a violated head is okay.
					continue;
				}
				throw oops("Watch invariant (alpha) violated");
			}
		}

		private void checkOrdinaryWatchesInvariant(int atom, Watches<BinaryWatch, WatchedNoGood> watches, boolean truth) {
			Assignment.Entry atomEntry = assignment.get(atom);
			int atomLiteral = truth ? atomToLiteral(atom) : atomToNegatedLiteral(atom);
			boolean atomSatisfies = atomEntry != null && isPositive(atomLiteral) != atomEntry.getTruth().toBoolean();
			int atomDecisionLevel = weakDecisionLevel(atomEntry);
			int atomReplayLevel = weakReplayLevel(atom);
			BinaryWatchList binaryWatchList = binaryWatches[atomLiteral];
			for (int i = 0; i < binaryWatchList.noGoodsWithoutHeadSize; i++) {
				int otherLiteral = binaryWatchList.noGoodsWithoutHead[i];
				checkBinaryWatch(atomSatisfies, atomDecisionLevel, atomReplayLevel, otherLiteral);
			}
			for (int i = 0; i < binaryWatchList.noGoodsWithHeadSize; i++) {
				int otherLiteral = binaryWatchList.noGoodsWithHead[i];
				checkBinaryWatch(atomSatisfies, atomDecisionLevel, atomReplayLevel, otherLiteral);
			}
			for (WatchedNoGood watchedNoGood : watches.multary.getOrdinary(truth)) {
				// Ensure both watches are either unassigned, or one satisfies NoGood, or both are on highest decision level.
				int otherPointer = atom ==  atomOf(watchedNoGood.getLiteral(watchedNoGood.getPointer(1))) ? 0 : 1;
				int otherLiteral = watchedNoGood.getLiteral(watchedNoGood.getPointer(otherPointer));
				int otherAtom = atomOf(otherLiteral);
				Assignment.Entry otherEntry = assignment.get(otherAtom);
				int otherDecisionLevel = weakDecisionLevel(otherEntry);
				int otherReplayLevel = weakReplayLevel(otherAtom);
				boolean otherSatisfies = otherEntry != null && isPositive(otherLiteral) != otherEntry.getTruth().toBoolean();
				if (watchInvariant(atomSatisfies, otherSatisfies, atomDecisionLevel, atomReplayLevel, otherDecisionLevel, otherReplayLevel)) {
					continue;
				}
				throw oops("Watch invariant violated");
			}
		}

		private void checkBinaryWatch(boolean atomSatisfies, int atomDecisionLevel, int atomReplayLevel, int otherLiteral) {
			int otherAtom = atomOf(otherLiteral);
			Assignment.Entry otherEntry = assignment.get(otherAtom);
			boolean otherSatisfies = otherEntry != null && isPositive(otherLiteral) != otherEntry.getTruth().toBoolean();
			int otherDecisionLevel = weakDecisionLevel(otherEntry);
			int otherReplayLevel = weakReplayLevel(otherAtom);
			if (watchInvariant(atomSatisfies, otherSatisfies, atomDecisionLevel, atomReplayLevel, otherDecisionLevel, otherReplayLevel)) {
				return;
			}
			throw oops("Watch invariant violated");
		}

		private boolean watchInvariant(boolean atomSatisfies, boolean otherAtomSatisfies, int atomDecisionLevel, int atomReplayLevel, int otherAtomDecisionLevel, int otherAtomReplayLevel) {
			if (atomDecisionLevel == UNASSIGNED && otherAtomDecisionLevel == UNASSIGNED) {
				// Both watches are unassigned.
				return true;
			}
			if ((atomSatisfies && (otherAtomDecisionLevel >= atomDecisionLevel || otherAtomDecisionLevel >= atomReplayLevel))
				|| (otherAtomSatisfies && (atomDecisionLevel >= otherAtomDecisionLevel || atomDecisionLevel >= otherAtomReplayLevel))) {
				// One watch satisfies the nogood and the other is assigned at higher decision level (or higher than the replay level of the satisfying one).
				return true;
			}
			return false;
		}

		private boolean watchInvariant(boolean atomSatisfies, int atomDecisionLevel, int otherLiteral, int otherDecisionLevel, Assignment.Entry otherEntry) {
			boolean otherSatisfies = otherEntry != null && isPositive(otherLiteral) != otherEntry.getTruth().toBoolean();
			if (atomDecisionLevel == Integer.MAX_VALUE && otherDecisionLevel == Integer.MAX_VALUE) {
				// Both watches are unassigned.
				return true;
			}
			if ((atomSatisfies && otherDecisionLevel >= atomDecisionLevel)
				|| (otherSatisfies && atomDecisionLevel >= otherDecisionLevel)) {
				// One watch satisfies the nogood and the other is assigned at higher decision level.
				return true;
			}
			return false;
		}
	}
}