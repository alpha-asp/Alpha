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

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

	private final WritableAssignment assignment;
	private final Map<Integer, Watches<BinaryWatch, WatchedNoGood>> watches = new LinkedHashMap<>();

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
			return addAndWatchBinary(noGood);
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

	private ConflictCause addAndWatch(final NoGood noGood) {
		// Collect potential watch candidates.
		int posWeakUnassigned1 = -1;
		int posWeakUnassigned2 = -1;
		int posStrongUnassigned = -1;
		int posSatisfiedLiteral1 = -1;
		int posSatisfiedLiteral2 = -1;
		int posWeakHighestAssigned = -1;
		int weakDecisionLevelHighestAssigned = -1;
		int posStrongHighestAssigned = -1;
		int strongDecisionLevelHighestAssigned = -1;
		boolean containsPositiveAssignedMBT = false;
		Assignment.Entry satisfiedLiteralEntry = null;

		// Used to detect always-satisfied NoGoods of form { L, -L, ... }.
		Map<Integer, Boolean> occurringLiterals = new HashMap<>();

		// Iterate noGood and record satisfying/unassigned/etc positions.
		for (int i = 0; i < noGood.size(); i++) {
			int literal = noGood.getLiteral(i);
			Assignment.Entry literalEntry = assignment.get(atomOf(literal));

			// Check if NoGood can never be violated (atom occurring positive and negative)
			if (occurringLiterals.containsKey(atomOf(literal))) {
				if (occurringLiterals.get(atomOf(literal)) != isNegated(literal)) {
					// NoGood cannot be violated or propagate, ignore it.
					LOGGER.debug("Added NoGood can never propagate or be violated, ignoring it. NoGood is: " + noGood);
					return null;
				}
			} else {
				occurringLiterals.put(atomOf(literal), isNegated(literal));
			}

			// Check weak unassigned.
			if (literalEntry == null) {
				if (posWeakUnassigned1 == -1) {
					posWeakUnassigned1 = i;
				} else {
					posWeakUnassigned2 = i;
				}
			}
			// Check strong unassigned for nogoods with head.
			if (noGood.hasHead() && i != HEAD && (literalEntry == null || literalEntry.getTruth().isMBT())) {
				posStrongUnassigned = i;
			}
			// Check satisfaction
			if (literalEntry != null && literalEntry.getTruth().toBoolean() != isPositive(literal)) {
				if (posSatisfiedLiteral1 == -1) {
					posSatisfiedLiteral1 = i;
					satisfiedLiteralEntry = literalEntry;
				} else {
					posSatisfiedLiteral2 = i;
				}
			}
			// Check violation.
			if (literalEntry != null && literalEntry.getTruth().toBoolean() == isPositive(literal)) {
				if (literalEntry.getWeakDecisionLevel() > weakDecisionLevelHighestAssigned) {
					weakDecisionLevelHighestAssigned = literalEntry.getWeakDecisionLevel();
					posWeakHighestAssigned = i;
				}
				if (!literalEntry.getTruth().isMBT()	// Ensure strong violation.
					&& literalEntry.getStrongDecisionLevel() > strongDecisionLevelHighestAssigned) {
					strongDecisionLevelHighestAssigned = literalEntry.getStrongDecisionLevel();
					posStrongHighestAssigned = i;
				}
				if (literalEntry.getTruth().isMBT()) {
					containsPositiveAssignedMBT = true;
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
				if (satisfiedLiteralEntry.getWeakDecisionLevel() > weakDecisionLevelHighestAssigned) {
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
			if (posStrongUnassigned != -1) {
				// Unassigned or MBT-assigned positive literal exists
				wng.setAlphaPointer(posStrongUnassigned);
			} else if (posSatisfiedLiteral1 == -1 && !containsPositiveAssignedMBT) {
				// Strongly unit.
				ConflictCause conflictCause = assignStrongComplement(noGood, strongDecisionLevelHighestAssigned);
				if (conflictCause != null) {
					return conflictCause;
				}
				wng.setAlphaPointer(posStrongHighestAssigned);
			} else if (posSatisfiedLiteral1 == HEAD && posSatisfiedLiteral2 == -1 && posStrongUnassigned == -1) {
				// The head is the only satisfying literal, hence it might propagate on lower decision level.
				if (satisfiedLiteralEntry.getStrongDecisionLevel() > strongDecisionLevelHighestAssigned) {
					ConflictCause conflictCause = assignStrongComplement(noGood, strongDecisionLevelHighestAssigned);
					if (conflictCause != null) {
						return conflictCause;
					}
				}
			}
		}

		// Set watches and register at watches structure:
		if (wng.getAlphaPointer() != -1) {
			// Only look at the alpha pointer if it points at a legal index. It might not
			// in case the noGood has no head or no positive literal.
			addAlphaWatch(wng);
		}
		// Set ordinary watches.
		addOrdinaryWatch(wng, 0);
		addOrdinaryWatch(wng, 1);
		return null;
	}

	private ConflictCause addAndWatchBinary(final NoGood noGood) {
		// Shorthands for viewing the nogood as { a, b }.
		final int a = noGood.getLiteral(0);
		final int b = noGood.getLiteral(1);
		final int atomA = atomOf(a);
		final int atomB = atomOf(b);

		// Ignore NoGoods of the form { -a, a }.
		if (a != b && atomA == atomB) {
			return null;
		}

		final Assignment.Entry entryA = assignment.get(atomA);
		final Assignment.Entry entryB = assignment.get(atomB);

		final boolean isViolatedA = entryA != null && isPositive(a) == entryA.getTruth().toBoolean();
		final boolean isViolatedB = entryB != null && isPositive(b) == entryB.getTruth().toBoolean();

		// Check for violation.
		if (isViolatedA && isViolatedB) {
			return new ConflictCause(noGood);
		}

		// If one literal is violated the NoGood propagates.
		boolean doesPropagate = isViolatedA || isViolatedB;
		int propagateePos = isViolatedA ? 1 : 0;
		Assignment.Entry violatedEntry = isViolatedA ? entryA : entryB;
		if (doesPropagate) {
			// Propagate weakly.
			ConflictCause conflictCause = assignWeakComplement(propagateePos, noGood, violatedEntry.getWeakDecisionLevel());
			if (conflictCause != null) {
				return conflictCause;
			}
			// Propagate strongly if applicable.
			if (noGood.hasHead() && HEAD == propagateePos && MBT != violatedEntry.getTruth()) {
				conflictCause = assignStrongComplement(noGood, violatedEntry.getStrongDecisionLevel());
				if (conflictCause != null) {
					return conflictCause;
				}
			}
		}

		// Set up watches, so that in case either a or b is assigned,
		// an assignment for the respective other can be generated
		// by unit propagation.
		watches(a).binary.getOrdinary(isPositive(a)).add(new BinaryWatch(noGood, 1));
		watches(b).binary.getOrdinary(isPositive(b)).add(new BinaryWatch(noGood, 0));

		// If the nogood has a head literal, take extra care as it
		// might propagate TRUE (and not only FALSE or MBT, which
		// are accounted for above).
		if (noGood.hasHead()) {
			final int bodyLiteral = noGood.getLiteral(1);

			// Set up watch.
			watches(bodyLiteral).binary.getAlpha(isPositive(bodyLiteral)).add(new BinaryWatch(noGood, HEAD));
		}
		return null;
	}

	private ConflictCause assignWeakComplement(final int literalIndex, final NoGood impliedBy, int decisionLevel) {
		final int literal = impliedBy.getLiteral(literalIndex);
		ThriceTruth truth = isNegated(literal) ? MBT : FALSE;
		return assignTruth(atomOf(literal), truth, impliedBy, decisionLevel);
	}

	private ConflictCause assignStrongComplement(final NoGood impliedBy, int decisionLevel) {
		return assignTruth(impliedBy.getAtom(HEAD), TRUE, impliedBy, decisionLevel);
	}

	private ConflictCause assignTruth(int atom, ThriceTruth truth, NoGood impliedBy, int decisionLevel) {
		// Skip assignment if it is the same as the current one and the current one has lower decision level.
		Assignment.Entry currentAssignment = assignment.get(atom);
		if (currentAssignment != null && currentAssignment.getDecisionLevel() <= decisionLevel && (truth.equals(currentAssignment.getTruth()))) {
			return null;
		}
		ConflictCause cause = assignment.assign(atom, truth, impliedBy, decisionLevel);
		if (cause == null) {
			didPropagate = true;
		}
		return cause;
	}

	/**
	 * Propagates from Unassigned to MBT/FALSE.
	 * @param entry the assignment that triggers the propagation.
	 */
	private ConflictCause propagateWeakly(final Assignment.Entry entry) {
		// If assignment is for TRUE, previous MBT exists, and this is no reassignment, nothing changes for weak propagation.
		if (entry.getPrevious() != null && !entry.isReassignAtLowerDecisionLevel()) {
			return null;
		}

		Watches<BinaryWatch, WatchedNoGood> watchesOfAssignedAtom = watches(entry.getAtom());

		// Process binary watches.
		for (BinaryWatch binaryWatch : watchesOfAssignedAtom.binary.getOrdinary(entry.getTruth().toBoolean())) {
			ConflictCause conflictCause = assignWeakComplement(binaryWatch.getOtherLiteralIndex(), binaryWatch.getNoGood(), entry.getDecisionLevel());
			if (conflictCause != null) {
				return conflictCause;
			}
		}

		int assignedDecisionLevel = entry.getWeakDecisionLevel();

		// Check all watched multi-ary NoGoods.
		Iterator<WatchedNoGood> watchIterator = watchesOfAssignedAtom.multary.getOrdinary(entry.getTruth().toBoolean()).iterator();
		while (watchIterator.hasNext()) {
			final WatchedNoGood watchedNoGood = watchIterator.next();

			final int assignedPointer = watchedNoGood.getLiteralAtPointer(0) == entry.getLiteral() ? 0 : 1;
			final int otherPointer = 1 - assignedPointer;

			final int assignedIndex = watchedNoGood.getPointer(assignedPointer);
			final int otherIndex = watchedNoGood.getPointer(otherPointer);

			boolean isNoGoodSatisfiedByOtherWatch = false;
			Assignment.Entry otherEntry = assignment.get(atomOf(watchedNoGood.getLiteral(otherIndex)));
			// Check if the other watch already satisfies the noGood.
			if (otherEntry != null && otherEntry.getTruth().toBoolean() != isPositive(watchedNoGood.getLiteral(otherIndex))) {
				isNoGoodSatisfiedByOtherWatch = true;
			}

			int highestDecisionLevel = assignedDecisionLevel;
			int pointerCandidateIndex = assignedIndex;
			boolean foundPointerCandidate = false;

			// Find new literal to watch.
			for (int i = 0; i < watchedNoGood.size(); i++) {
				if (i == assignedIndex || i == otherIndex) {
					continue;
				}
				int currentLiteral = watchedNoGood.getLiteral(i);
				Assignment.Entry currentEntry = assignment.get(atomOf(currentLiteral));

				// Break if: 1) current literal is unassigned, 2) satisfies the nogood, or
				// 3) the nogood is satisfied by the other watch and the current literal has decision level greater than the satisfying literal.
				if (currentEntry == null
					|| currentEntry.getTruth().toBoolean() != isPositive(currentLiteral)
					|| (isNoGoodSatisfiedByOtherWatch && currentEntry.getWeakDecisionLevel() >= otherEntry.getWeakDecisionLevel())
					) {
					foundPointerCandidate = true;
					pointerCandidateIndex = i;
					break;
				}

				// Record literal if it has highest decision level so far.
				int currentDecisionLevel = currentEntry.getWeakDecisionLevel();
				if (currentDecisionLevel > highestDecisionLevel) {
					highestDecisionLevel = currentDecisionLevel;
					pointerCandidateIndex = i;
				}
			}

			if (foundPointerCandidate) {
				// Move pointer to new literal.
				watchedNoGood.setPointer(assignedPointer, pointerCandidateIndex);
				watchIterator.remove();
				addOrdinaryWatch(watchedNoGood, assignedPointer);
			} else {
				// NoGood is unit, propagate (on potentially lower decision level).
				// Note: Violation is detected by Assignment.
				ConflictCause conflictCause = assignWeakComplement(otherIndex, watchedNoGood, highestDecisionLevel);

				if (conflictCause != null) {
					return conflictCause;
				}

				// Move assigned watch to now-highest position (if it changed).
				if (pointerCandidateIndex != assignedIndex) {
					watchedNoGood.setPointer(assignedPointer, pointerCandidateIndex);
					watchIterator.remove();
					addOrdinaryWatch(watchedNoGood, assignedPointer);
				}
			}
		}
		return null;
	}

	private ConflictCause propagateStrongly(final Assignment.Entry entry) {
		// Nothing needs to be done in case MBT is assigned since MBT cannot trigger strong unit-propagation.
		if (entry.getTruth().isMBT()) {
			return null;
		}

		// Process binary watches.
		Watches<BinaryWatch, WatchedNoGood> watchesOfAssignedAtom = watches(entry.getAtom());
		for (BinaryWatch binaryWatch : watchesOfAssignedAtom.binary.getAlpha(entry.getTruth().toBoolean())) {
			if (binaryWatch.getOtherLiteralIndex() != HEAD) {
				throw oops("Binary watch for nogood with head does not point at head");
			}

			ConflictCause conflictCause = assignStrongComplement(binaryWatch.getNoGood(), entry.getDecisionLevel());
			if (conflictCause != null) {
				return conflictCause;
			}
		}

		int assignedDecisionLevel = entry.getStrongDecisionLevel();

		Iterator<WatchedNoGood> watchIterator = watchesOfAssignedAtom.multary.getAlpha(entry.getTruth().toBoolean()).iterator();
		while (watchIterator.hasNext()) {
			final WatchedNoGood watchedNoGood = watchIterator.next();

			if (!watchedNoGood.hasHead()) {
				throw oops("Strong propagation encountered NoGood without head");
			}

			final int assignedIndex = watchedNoGood.getAlphaPointer();

			boolean isNoGoodSatisfiedByHead = false;
			Assignment.Entry headEntry = assignment.get(watchedNoGood.getAtom(HEAD));
			// Check if the other watch already satisfies the noGood.
			if (headEntry != null && TRUE.equals(headEntry.getTruth()) && !isPositive(watchedNoGood.getLiteral(HEAD))) {
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
				Assignment.Entry currentEntry = assignment.get(atomOf(currentLiteral));

				// Break if: 1) current literal is unassigned (or MBT), 2) satisfies the nogood, or
				// 3) the nogood is satisfied by the head and the current literal has decision level greater than the head literal.
				if (currentEntry == null || currentEntry.getTruth().isMBT()
					|| currentEntry.getTruth().toBoolean() != isPositive(currentLiteral)
					|| (isNoGoodSatisfiedByHead && currentEntry.getStrongDecisionLevel() >= headEntry.getStrongDecisionLevel())
					) {
					foundPointerCandidate = true;
					pointerCandidateIndex = i;
					break;
				}

				// Record literal if it has highest decision level so far.
				int currentDecisionLevel = currentEntry.getStrongDecisionLevel();
				if (currentDecisionLevel > highestDecisionLevel) {
					highestDecisionLevel = currentDecisionLevel;
					pointerCandidateIndex = i;
				}
			}

			if (foundPointerCandidate) {
				// Move pointer to new literal.
				watchedNoGood.setAlphaPointer(pointerCandidateIndex);
				watchIterator.remove();
				addAlphaWatch(watchedNoGood);
				continue;
			}

			// NoGood is unit, propagate (on potentially lower decision level).
			ConflictCause conflictCause = assignStrongComplement(watchedNoGood, highestDecisionLevel);
			if (conflictCause != null) {
				return conflictCause;
			}

			// Move assigned watch to now-highest position (if it changed).
			if (pointerCandidateIndex != assignedIndex) {
				watchedNoGood.setAlphaPointer(pointerCandidateIndex);
				watchIterator.remove();
				addAlphaWatch(watchedNoGood);
			}
		}
		return null;
	}

	@Override
	public ConflictCause propagate() {
		didPropagate = false;

		Queue<? extends Assignment.Entry> assignmentsToProcess = assignment.getAssignmentsToProcess();
		while (!assignmentsToProcess.isEmpty()) {
			final Assignment.Entry currentEntry = assignmentsToProcess.remove();
			LOGGER.trace("Propagation processing entry: {}={}", currentEntry.getAtom(), currentEntry);

			ConflictCause conflictCause = propagateWeakly(currentEntry);
			if (conflictCause != null) {
				LOGGER.trace("Halting propagation due to conflict. Current assignment: {}.", assignment);
				return conflictCause;
			}

			conflictCause = propagateStrongly(currentEntry);
			if (conflictCause != null) {
				LOGGER.trace("Halting propagation due to conflict. Current assignment: {}.", assignment);
				return conflictCause;
			}
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

	/**
	 * A simple data structure to encapsulate watched delegate by truth value tailored to {@link ThriceTruth}. It
	 * holds three separate sets that are used to refer to propagation based on assignment of one of the three truth
	 * values.
	 * @param <T> type used for referencing.
	 */
	private class WatchLists<T> {
		private final Set<T> positive = new LinkedHashSet<>();
		private final Set<T> negative = new LinkedHashSet<>();
		private final Set<T> alphaPositive = new LinkedHashSet<>();
		private final Set<T> alphaNegative = new LinkedHashSet<>();

		private Set<T> getAlpha(boolean polarity) {
			return polarity ? alphaPositive : alphaNegative;
		}
		private Set<T> getOrdinary(boolean polarity) {
			return polarity ? positive : negative;
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
		final WatchLists<B> binary = new WatchLists<>();
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
			return entry == null ? Integer.MAX_VALUE : entry.getPrevious() != null ? entry.getPrevious().getDecisionLevel() : entry.getDecisionLevel();
		}

		int strongDecisionLevel(Assignment.Entry entry) {
			return entry == null || entry.getTruth().isMBT() ? Integer.MAX_VALUE : entry.getDecisionLevel();
		}

		private void checkAlphaWatchesInvariant(int atom, NoGoodStoreAlphaRoaming.Watches<NoGoodStoreAlphaRoaming.BinaryWatch, WatchedNoGood> watches, boolean truth) {
			Assignment.Entry atomEntry = assignment.get(atom);
			int atomLiteral = truth ? atom : -atom;
			boolean atomSatisfies = atomEntry != null && isPositive(atomLiteral) != atomEntry.getTruth().toBoolean();
			int atomDecisionLevel = strongDecisionLevel(atomEntry);
			for (NoGoodStoreAlphaRoaming.BinaryWatch binaryWatch : watches.binary.getAlpha(truth)) {
				int headLiteral = binaryWatch.getNoGood().getLiteral(binaryWatch.getOtherLiteralIndex());
				Assignment.Entry headEntry = assignment.get(atomOf(headLiteral));
				boolean headViolates = headEntry != null && isPositive(headLiteral) == headEntry.getTruth().toBoolean();
				int headDecisionLevel = strongDecisionLevel(headEntry);
				if (watchInvariant(atomSatisfies, atomDecisionLevel, headLiteral, headDecisionLevel, headEntry)
					|| headViolates) {	// Head "pointer" is never moved and violation is checked by weak propagation, hence a violated head is okay.
					continue;
				}
				throw oops("Watch invariant (alpha) violated");
			}
			for (WatchedNoGood watchedNoGood : watches.multary.getAlpha(truth)) {
				int headLiteral = watchedNoGood.getLiteral(HEAD);
				Assignment.Entry headEntry = assignment.get(atomOf(headLiteral));
				boolean headViolates = headEntry != null && isPositive(headLiteral) == headEntry.getTruth().toBoolean();
				int headDecisionLevel = strongDecisionLevel(headEntry);
				if (watchInvariant(atomSatisfies, atomDecisionLevel, headLiteral, headDecisionLevel, headEntry)
					|| headViolates) {	// Head "pointer" is never moved and violation is checked by weak propagation, hence a violated head is okay.
					continue;
				}
				throw oops("Watch invariant (alpha) violated");
			}
		}

		private void checkOrdinaryWatchesInvariant(int atom, Watches<BinaryWatch, WatchedNoGood> watches, boolean truth) {
			Assignment.Entry atomEntry = assignment.get(atom);
			int atomLiteral = truth ? atom : -atom;
			boolean atomSatisfies = atomEntry != null && isPositive(atomLiteral) != atomEntry.getTruth().toBoolean();
			int atomDecisionLevel = weakDecisionLevel(atomEntry);
			for (BinaryWatch binaryWatch : watches.binary.getOrdinary(truth)) {
				// Ensure both watches are either unassigned, or one satisfies NoGood, or both are on highest decision level.
				int otherLiteral = binaryWatch.getNoGood().getLiteral(binaryWatch.getOtherLiteralIndex());
				Assignment.Entry otherEntry = assignment.get(atomOf(otherLiteral));
				int otherDecisionLevel = weakDecisionLevel(otherEntry);
				if (watchInvariant(atomSatisfies, atomDecisionLevel, otherLiteral, otherDecisionLevel, otherEntry)) {
					continue;
				}
				throw oops("Watch invariant violated");
			}
			for (WatchedNoGood watchedNoGood : watches.multary.getOrdinary(truth)) {
				// Ensure both watches are either unassigned, or one satisfies NoGood, or both are on highest decision level.
				int otherPointer = atom ==  watchedNoGood.getAtom(watchedNoGood.getPointer(1)) ? 0 : 1;
				int otherLiteral = watchedNoGood.getLiteral(watchedNoGood.getPointer(otherPointer));
				Assignment.Entry otherEntry = assignment.get(atomOf(otherLiteral));
				int otherDecisionLevel = weakDecisionLevel(otherEntry);
				if (watchInvariant(atomSatisfies, atomDecisionLevel, otherLiteral, otherDecisionLevel, otherEntry)) {
					continue;
				}
				throw oops("Watch invariant violated");
			}
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