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

import at.ac.tuwien.kr.alpha.common.AtomTranslator;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Literals.*;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;

class BasicNoGoodStore implements NoGoodStore<ThriceTruth> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicNoGoodStore.class);

	private final AtomTranslator translator;
	private final Assignment<ThriceTruth> assignment;
	private final Map<Integer, Watches<BinaryWatch, WatchedNoGood>> watches = new HashMap<>();

	private NoGood violated;

	BasicNoGoodStore(Assignment<ThriceTruth> assignment, Grounder translator) {
		this.assignment = assignment;
		//this.assignmentIterator = assignment.iterator();
		this.translator = translator;
	}

	BasicNoGoodStore(Assignment<ThriceTruth> assignment) {
		this(assignment, null);
	}

	@Override
	public void backtrack() {
		violated = null;
		assignment.backtrack();
		if (LOGGER.isTraceEnabled()) {
			new WatchedNoGoodsChecker().doWatchesCheck();
		}
	}

	void clear() {
		assignment.clear();
		watches.clear();
	}

	private void setViolated(final NoGood noGood) {
		violated = noGood;
	}

	private ConflictCause setViolatedFromAssignment() {
		return new ConflictCause(assignment.getNoGoodViolatedByAssign(), assignment.getGuessViolatedByAssign());
	}

	private Watches<BinaryWatch, WatchedNoGood> watches(int literal) {
		return watches.computeIfAbsent(atomOf(literal), k -> new Watches<>());
	}

	private void addPosNegWatch(WatchedNoGood wng, int pointer) {
		final int literal = wng.getLiteral(wng.getPointer(pointer));
		watches(literal).n.get(literal).add(wng);
	}

	private void addAlphaWatch(WatchedNoGood wng) {
		watches(wng.getLiteralAtAlpha()).n.getAlpha().add(wng);
	}

	@Override
	public ConflictCause add(int id, NoGood noGood) {
		LOGGER.trace("Adding {}", noGood);
		if (noGood.size() == 1) {
			return addUnary(noGood);
		} else if (noGood.size() == 2) {
			return addAndWatchBinary(id, noGood);
		} else {
			return addAndWatch(noGood);
		}
	}

	@Override
	public NoGood getViolatedNoGood() {
		return violated;
	}

	/**
	 * Takes a noGood containing only a single literal and translates it into an assignment (because it
	 * is trivially unit). Still, a check for conflict is performed.
	 */
	private ConflictCause addUnary(final NoGood noGood) {
		if (noGood.hasHead()) {
			if (!assignStrongComplement(0, noGood, 0)) {
				return setViolatedFromAssignment();
			}
		} else {
			if (!assignWeakComplement(0, noGood, 0)) {
				return setViolatedFromAssignment();
			}
		}
		return null;
	}

	private ConflictCause addAndWatchBinary(final int id, final NoGood noGood) {
		// Shorthands for viewing the nogood as { a, b }.
		final int a = noGood.getLiteral(0);
		final int b = noGood.getLiteral(1);

		// Ignore NoGoods of the form { -a, a }.
		if (a != b && atomOf(a) == atomOf(b)) {
			return null;
		}

		boolean isViolatedA = assignment.containsWeakComplement(a);
		boolean isViolatedB = assignment.containsWeakComplement(b);

		Assignment.Entry entryA = assignment.get(atomOf(a));
		Assignment.Entry entryB = assignment.get(atomOf(b));

		// Check for violation.
		if (isViolatedA && isViolatedB && entryA.getDecisionLevel() == entryB.getDecisionLevel()) {
			return new ConflictCause(noGood, null);
		}

		// If one literal is violated on lower decision level than the other is assigned or the other is unassigned, the NoGood propagates.
		int propagatedLiteral = -1;
		Assignment.Entry propagatee = null;
		int propagationDecisionLevel = -1;
		if (isViolatedA && (entryB == null || entryA.getDecisionLevel() < entryB.getDecisionLevel())) {
			// Literal a is violated and propagates b.
			propagationDecisionLevel = entryA.getDecisionLevel();
			propagatedLiteral = 1;
			propagatee = entryA;
		}
		if (isViolatedB && (entryA == null || entryB.getDecisionLevel() < entryA.getDecisionLevel())) {
			// Literal b is violated and propagates a.
			propagationDecisionLevel = entryB.getDecisionLevel();
			propagatedLiteral = 0;
			propagatee = entryB;
		}

		// If binary NoGood propagates, assign corresponding literal and respect eventual head.
		if (propagatedLiteral != -1) {
			if (noGood.hasHead() && noGood.getHead() == propagatedLiteral && !MBT.equals(propagatee.getTruth())) {
				if (!assignStrongComplement(propagatedLiteral, noGood, propagationDecisionLevel)) {
					return setViolatedFromAssignment();
				}
			} else {
				if (!assignWeakComplement(propagatedLiteral, noGood, propagationDecisionLevel)) {
					return setViolatedFromAssignment();
				}
			}
		}

		// Set up watches, so that in case either a or b is assigned,
		// an assignment for the respective other can be generated
		// by unit propagation.
		watches(a).b.get(a).add(new BinaryWatch(noGood, 1));
		watches(b).b.get(b).add(new BinaryWatch(noGood, 0));

		// If the nogood has a head literal, take extra care as it
		// might propagate TRUE (and not only FALSE or MBT, which
		// are accounted for above).
		if (noGood.hasHead()) {
			final int head = noGood.getHead();
			final int bodyLiteral = noGood.getLiteral(head == 0 ? 1 : 0);

			// Set up watch.
			if (!isNegated(bodyLiteral)) {
				watches(bodyLiteral).b.getAlpha().add(new BinaryWatch(noGood, head));
			}
		}
		return null;
	}


	/**
	 * Adds a noGood to the store and performs the following:
	 *  * If <code>noGood</code> is violated, returns a ConflictCause indicating the cause of the failure, the NoGood is not added.
	 *  * If <code>noGood</code> is unit, propagate, and add appropriate watches.
	 *  * If <code>noGood</code> has at least two unassigned literals, add appropriate watches.
	 * @param noGood
	 * @return
	 */
	private ConflictCause addAndWatch(final NoGood noGood) {
		// A NoGood when added can be one of the following:
		// 1) it is violated (there are no unassigned literals, and all are assigned as occurring in the noGood).
		// 2) it is satisfied (there is one literal assigned to the complement of how it occurs in the noGood).
		// 2 a) it is unit on a lower-than-current decision level.
		// 2 b) it is just satisfied and not unit.
		// 3) it propagates weakly (there is exactly one unassigned literal for propagation to FALSE or MBT).
		// 4) it propagates strongly (to TRUE, by all positive occurrences being assigned TRUE and the head being either unassigned or assigned MBT).
		// 5) it is silent / none of the above (there are more or equal to two unassigned literals).
		int posFirstUnassigned = -1;
		int posSecondUnassigned = -1;
		int posSatisfyingLiteral = -1;
		int posSecondSatisfyingLiteral = -1;
		int posPositiveMBTAssignedLiteralExceptHead = -1;
		int posHighestDecisionLevel = -1;
		int highestDecisionLevel = -1;
		int highestDecisionLevelExceptSatisfyingLiteral = -1;
		int posHighestDecisionLevelExceptSatisfyingLiteral = -1;
		int secondHighestPriority = -1;
		int posSecondHighestPriority = -1;
		int posPositiveUnassigned = -1;
		int posPositiveTrueHighestDecisionLevel = -1;
		int positiveTrueHighestDecisionLevel = -1;

		// Used to detect always-satisfied NoGoods of form { L, -L, ... }.
		Map<Integer, Boolean> occurringLiterals = new HashMap<>();

		// Iterate whole noGood and set above pointers.
		for (int i = 0; i < noGood.size(); i++) {
			final int literal = noGood.getLiteral(i);
			final int atom = atomOf(literal);
			Assignment.Entry entry = assignment.get(atom);

			// Check if NoGood can never be violated (atom occurring positive and negative)
			if (occurringLiterals.containsKey(atom)) {
				if (occurringLiterals.get(atom) != isNegated(literal)) {
					// NoGood cannot be violated or propagate, ignore it.
					LOGGER.debug("Added NoGood can never propagate or be violated, ignoring it. NoGood is: " + noGood);
					return null;
				}
			} else {
				occurringLiterals.put(atom, isNegated(literal));
			}

			// Inspect literal and potentially assigned values.
			if (entry == null) {
				// Literal is unassigned, record in first/second unassigned pointer.
				if (posFirstUnassigned == -1) {
					posFirstUnassigned = i;
				} else if (posSecondUnassigned == -1) {
					posSecondUnassigned = i;
				}
				// Record if it occurs positively in the noGood.
				if (!isNegated(literal)) {
					posPositiveUnassigned = i;
				}
			} else {
				// Literal is assigned

				// Check if literal satisfies the noGood.
				if (entry.getTruth().toBoolean() != isPositive(literal)) {
					if (posSatisfyingLiteral == -1) {
						posSatisfyingLiteral = i;
					} else {
						posSecondSatisfyingLiteral = i;
					}
				} else {
					// Literal is not satisfying, record its decision level.
					if (entry.getDecisionLevel() > highestDecisionLevelExceptSatisfyingLiteral) {
						highestDecisionLevelExceptSatisfyingLiteral = entry.getDecisionLevel();
						posHighestDecisionLevelExceptSatisfyingLiteral = i;
					}
				}
				// Check if literal is MBT and not the head.
				if (MBT.equals(entry.getTruth()) && i != noGood.getHead() && !isNegated(literal)) {
					posPositiveMBTAssignedLiteralExceptHead = i;
				}

				int dl = entry.getDecisionLevel();

				// Check if literal has highest decision level (so far).
				if (dl >= highestDecisionLevel) {
					// Move former highest down to second highest.
					secondHighestPriority = highestDecisionLevel;
					posSecondHighestPriority = posHighestDecisionLevel;
					// Record highest decision level.
					highestDecisionLevel = dl;
					posHighestDecisionLevel = i;
					continue;
				}
				// Check if literal has second highest decision level (only reached if smaller than highest decision level).
				if (dl > secondHighestPriority) {
					secondHighestPriority = dl;
					posSecondHighestPriority = i;
				}
				// Check if literal is positive, assigned TRUE and has highest decision level
				if (TRUE.equals(entry.getTruth()) && dl > positiveTrueHighestDecisionLevel) {
					positiveTrueHighestDecisionLevel = dl;
					posPositiveTrueHighestDecisionLevel = i;
				}
			}
		}

		// The alpha pointer points at a positive literal that is either unassigned or MBT;
		// if all positive literals are assigned TRUE, it points to the one with highest decision level;
		// if the noGood has no head, it is -1.
		int potentialAlphaPointer = -1;
		if (noGood.hasHead()) {
			potentialAlphaPointer = posPositiveMBTAssignedLiteralExceptHead != -1 ? posPositiveMBTAssignedLiteralExceptHead : posPositiveUnassigned;
			if (potentialAlphaPointer == -1) {
				// All positives are assigned true, point to highest one
				potentialAlphaPointer = posPositiveTrueHighestDecisionLevel;
			}
		}

		// Match cases 1) - 5) above and process accordingly. Note: the order of cases below matters.
		if (posFirstUnassigned != -1 && posSecondUnassigned != -1) {
			// Case 5)
			setWatches(noGood, posFirstUnassigned, posSecondUnassigned, potentialAlphaPointer);
		} else if (posFirstUnassigned == -1 && posSatisfyingLiteral == -1) {
			// Case 1)
			return new ConflictCause(noGood, null);
		} else if (posSatisfyingLiteral != -1) {
			// Case 2)
			if (posSecondSatisfyingLiteral == -1 && posFirstUnassigned == -1) {
				// Case 2a)
				// There is only one literal satisfying the NoGood and no literal is unassigned, it is unit (potentially on a lower decision level).
				if (noGood.hasHead() && posSatisfyingLiteral == noGood.getHead() && posPositiveMBTAssignedLiteralExceptHead == -1) {
					if (!assignStrongComplement(posSatisfyingLiteral, noGood, highestDecisionLevelExceptSatisfyingLiteral)) {
						return setViolatedFromAssignment();
					}
				} else {
					if (!assignWeakComplement(posSatisfyingLiteral, noGood, highestDecisionLevelExceptSatisfyingLiteral)) {
						return setViolatedFromAssignment();
					}
				}
				setWatches(noGood, posSatisfyingLiteral, posHighestDecisionLevelExceptSatisfyingLiteral, potentialAlphaPointer);
			} else {
				// Case 2b)
				// At least one literal satisfies the NoGood, second pointer to unassigned literals (if any) or the second highest one.
				int posUnassignedOrSecondHighestDecisionLevel = posFirstUnassigned != -1 ? posFirstUnassigned : posSecondHighestPriority;
				setWatches(noGood, posHighestDecisionLevel, posUnassignedOrSecondHighestDecisionLevel, potentialAlphaPointer);
			}
		} else if (noGood.hasHead() && posPositiveMBTAssignedLiteralExceptHead == -1 && (posFirstUnassigned == noGood.getHead() || posFirstUnassigned == -1)) {
			// Case 4)
			if (!assignStrongComplement(noGood.getHead(), noGood, highestDecisionLevel)) {
				return setViolatedFromAssignment();
			}
			setWatches(noGood, posFirstUnassigned, posHighestDecisionLevel, potentialAlphaPointer);
		} else if (posFirstUnassigned != -1 && posSecondUnassigned == -1) {
			// Case 3)
			if (!assignWeakComplement(posFirstUnassigned, noGood, highestDecisionLevel)) {
				return setViolatedFromAssignment();
			}
			setWatches(noGood, posFirstUnassigned, posHighestDecisionLevel, potentialAlphaPointer);
		} else {
			// Should never be reached.
			throw new RuntimeException("Bug in algorithm, added NoGood not matched by any cases. Forgotten case? NoGood is: " + noGood);
		}

		return null;
	}

	private void setWatches(NoGood noGood, int a, int b, int alpha) {
		final WatchedNoGood wng = new WatchedNoGood(noGood, a, b, alpha);

		for (int i = 0; i < 2; i++) {
			addPosNegWatch(wng, i);
		}

		// Only look at the alpha pointer if it points at a legal index. It might not
		// in case the nogood has no head or no positive literal.
		if (alpha != -1) {
			addAlphaWatch(wng);
		}
	}

	/**
	 * Ensures the following invariant for watches:
	 * (Inv): For every NoGood it either holds that both watched literals are unassigned, or for one watch holds: it
	 *        points to a literal that is assigned a complementary truth value at decision level d and the other
	 *        watched literal is assigned at decision level d' such that d <= d'.
	 *  The second condition ensures that after backtracking the NoGood either is still satisfied or both watches
	 *  point to unassigned literals. Observe that for a complementary truth value of TRUE the potential previous
	 *  assignment to MBT counts for the decision level d.
	 * @param atom the atom whose watches have to be checked.
	 * @param truth the assigned truth value to identify the list of watches that is checked.
	 * @return false iff some induced assignment is conflicting.
	 */
	private boolean ensureWatchInvariantAfterAssignAtLowerDL(int atom, ThriceTruth truth) {
		// We assume that unit-propagation has been done already, both watches point to already assigned literals.
		final Watches<BinaryWatch, WatchedNoGood> w = watches(atom);
		final Assignment.Entry atomEntry = assignment.get(atom);
		// The decision level is that of the entry except if MBT was assigned and the current assignment is TRUE, then the previous (MBT) is used.
		final int atomDecisionLevel = MBT.equals(truth) && TRUE.equals(atomEntry.getTruth()) ? atomEntry.getPrevious().getDecisionLevel() : atomEntry.getDecisionLevel();
		// Check all NoGoods that are watched on atom.
		for (BinaryWatch watch : w.b.get(truth)) {
			// For binary watches, the otherLiteral must be assigned complementary.
			final NoGood binaryNoGood = watch.getNoGood();
			final int otherLiteral = binaryNoGood.getLiteral(watch.getOtherLiteralIndex());
			final int otherAtom = atomOf(otherLiteral);
			final Assignment.Entry otherEntry = assignment.get(otherAtom);
			if (otherEntry == null) {
				throw new RuntimeException("Processing re-assignment of an entry, but encountered a NoGood where unit propagation was not done. Should not happen.");
			}
			// Ensure that the assignment of the otherAtom is at the same-or-lower decision level as the atom.
			if (otherEntry.getDecisionLevel() > atomDecisionLevel) {
				// Assign the otherAtom also at lower decision level.
				propagateAssigned = true;
				if (binaryNoGood.hasHead() && binaryNoGood.getHead() == watch.getOtherLiteralIndex()
					&& (FALSE.equals(truth) || TRUE.equals(truth))) {
					if (!assignStrongComplement(watch.getOtherLiteralIndex(), binaryNoGood, atomDecisionLevel)) {
						return false;
					}
				} else {
					if (!assignWeakComplement(watch.getOtherLiteralIndex(), binaryNoGood, atomDecisionLevel)) {
						return false;
					}
				}
			}
		}

		// Check all n-ary NoGoods watched on atom
		noGoodLoop:
		for (Iterator<WatchedNoGood> iterator = w.n.get(truth).iterator(); iterator.hasNext();) {
			// Since unit-propagation was done on this NoGood, both watches point to assigned literals and all literals are assigned already.
			final WatchedNoGood noGood = iterator.next();
			final int otherPointer = atomOf(noGood.getLiteralAtPointer(0)) == atom ? 1 : 0;
			final int posOtherLiteral = noGood.getPointer(otherPointer);
			final int atomPointer = 1 - otherPointer;
			final int otherLiteral = noGood.getLiteralAtPointer(otherPointer);

			final Assignment.Entry otherEntry = assignment.get(atomOf(otherLiteral));
			final int otherLiteralDecisionLevel = otherEntry.getDecisionLevel();

			// Skip if other assignment is not on lower decision level.
			if (otherLiteralDecisionLevel <= atomDecisionLevel) {
				continue;
			}

			// Find highest decision level to re-point the watch to.
			int highestDecisionLevel = atomDecisionLevel;
			int posHighestDecisionLevel = -1;
			boolean containsMbt = MBT.equals(atomEntry.getTruth());
			for (int i = 0; i < noGood.size(); i++) {
				// Skip both currently watched literals.
				if (i == posOtherLiteral || i == noGood.getPointer(atomPointer)) {
					continue;
				}

				Assignment.Entry entry = assignment.get(noGood.getAtom(i));
				if (entry == null) {
					throw new RuntimeException("Found unassigned literal while moving watches for previously unit NoGood. Should not happen.");
				}
				if (entry.getDecisionLevel() > highestDecisionLevel) {
					highestDecisionLevel = entry.getDecisionLevel();
					posHighestDecisionLevel = i;
				}
				if (MBT.equals(entry.getTruth())) {
					containsMbt = true;
				}

				// If the decision level of the literal is higher than that of the otherLiteral, watch this literal.
				if (highestDecisionLevel >= otherLiteralDecisionLevel) {
					noGood.setPointer(atomPointer, i);
					iterator.remove();
					addPosNegWatch(noGood, atomPointer);
					continue noGoodLoop;	// continue with the next watched NoGood.
				}
			}
			// No literal with decision level higher than that of the otherLiteral was found.

			// Move watches to literal with highest decision level (ignore if atom still is highest)
			if (posHighestDecisionLevel != -1) {
				noGood.setPointer(atomPointer, posHighestDecisionLevel);
				iterator.remove();
				addPosNegWatch(noGood, atomPointer);
			}
			// Re-assign the otherLiteral at the now-highest decision level.
			if (containsMbt) {
				propagateAssigned = true;
				if (!assignWeakComplement(posOtherLiteral, noGood, highestDecisionLevel)) {
					return false;
				}
			} else {
				if (!assignStrongComplement(posOtherLiteral, noGood, highestDecisionLevel)) {
					return false;
				}
			}
		}

		// If TRUE is re-assigned, also check alpha pointers.
		if (TRUE.equals(truth)) {
			// Check binary NoGoods.
			for (BinaryWatch watch : w.b.getAlpha()) {
				final Assignment.Entry otherEntry = assignment.get(watch.getNoGood().getAtom(watch.getOtherLiteralIndex()));
				final int otherDecisionLevel = otherEntry.getDecisionLevel();
				if (otherDecisionLevel > atomDecisionLevel) {
					propagateAssigned = true;
					if (!assignStrongComplement(watch.getOtherLiteralIndex(), watch.getNoGood(), atomDecisionLevel)) {
						return false;
					}

				}
			}

			// Check n-ary watched NoGoods.
			alphaNoGoodLoop:
			for (Iterator<WatchedNoGood> iterator = w.n.getAlpha().iterator(); iterator.hasNext();) {
				final WatchedNoGood noGood = iterator.next();
				final Assignment.Entry headEntry = assignment.get(noGood.getAtom(noGood.getHead()));

				if (headEntry == null || !TRUE.equals(headEntry.getTruth())) {
					throw new RuntimeException("Processing Re-assignment of entry for TRUE, but encountered a NoGood where alpha propagation was not done. Should not happen.");
				}

				final int headDecisionLevel = headEntry.getDecisionLevel();

				// Skip NoGood if head is not assigned on higher decision level.
				if (headDecisionLevel <= atomDecisionLevel) {
					continue;
				}

				int highestDecisionLevel = atomDecisionLevel;
				int posHighestAlphaLiteral = -1;
				for (int i = 0; i < noGood.size(); i++) {
					if (i == noGood.getHead() || i == noGood.getAlphaPointer()) {
						continue;
					}

					int otherLiteral = noGood.getLiteral(i);
					Assignment.Entry otherEntry = assignment.get(atomOf(otherLiteral));
					if (otherEntry == null) {
						throw new RuntimeException("Found unassigned literal while moving watches for previously unit NoGood. Should not happen.");
					}
					// Record decision level and potential candidate for the alpha pointer.
					int otherDecisionLevel = otherEntry.getDecisionLevel();
					if (otherDecisionLevel > highestDecisionLevel) {
						highestDecisionLevel = otherDecisionLevel;
						if (isPositive(otherLiteral)) {
							posHighestAlphaLiteral = i;
						}
					}
					if (isPositive(otherLiteral) && otherDecisionLevel >= headDecisionLevel) {
						// There is a positive literal with high-enough decision level; point the alpha pointer to it.
						noGood.setAlphaPointer(i);
						iterator.remove();
						addAlphaWatch(noGood);
						continue alphaNoGoodLoop;
					}
				}

				// Could not just move the alpha pointer, re-assign head at lower decision level and move alpha pointer to highest positive literal.
				noGood.setAlphaPointer(posHighestAlphaLiteral);
				iterator.remove();
				addAlphaWatch(noGood);
				propagateAssigned = true;
				if (!assignStrongComplement(noGood.getHead(), noGood, highestDecisionLevel)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean assignWeakComplement(final int literalIndex, final NoGood impliedBy, int decisionLevel) {
		final int literal = impliedBy.getLiteral(literalIndex);
		final int atom = atomOf(literal);
		ThriceTruth truth = isNegated(literal) ? MBT : FALSE;
		return assignTruth(atom, truth, impliedBy, decisionLevel);
	}


	private boolean assignStrongComplement(final int literalIndex, final NoGood impliedBy, int decisionLevel) {
		final int literal = impliedBy.getLiteral(literalIndex);
		final int atom = atomOf(literal);
		ThriceTruth truth = isNegated(literal) ? TRUE : FALSE;
		return assignTruth(atom, truth, impliedBy, decisionLevel);
	}

	private boolean assignTruth(int atom, ThriceTruth truth, NoGood impliedBy, int decisionLevel) {
		if (!assignment.assign(atom, truth, impliedBy, decisionLevel)) {
			setViolated(assignment.getNoGoodViolatedByAssign());
			return false;
		}
		return true;
	}

	private boolean propagateAssigned;

	@Override
	public boolean propagate() {
		boolean propagated = false;
		Queue<Assignment.Entry<ThriceTruth>> assignmentsToProcess = assignment.getAssignmentsToProcess();

		while (!assignmentsToProcess.isEmpty()) {
			final Assignment.Entry<ThriceTruth> entry = assignmentsToProcess.remove();
			final int atom = entry.getAtom();

			LOGGER.trace("Looking for propagation from {} with {}.", atom, entry);

			final ThriceTruth value = entry.getTruth();

			final Assignment.Entry<ThriceTruth> previous = entry.getPrevious();
			final ThriceTruth prevValue = previous != null ? previous.getTruth() : null;
			propagateAssigned = false;

			if (entry.isReassignAtLowerDecisionLevel()) {
				if (entry.getDecisionLevel() == assignment.getDecisionLevel()) {
					throw new RuntimeException("Assignment entry is for current decision level but marked as reassign at lower one. Should not happen.");
				}
				LOGGER.debug("Processing assignment for lower decision level (ensuring watches are correct).");
				if (!ensureWatchInvariantAfterAssignAtLowerDL(atom, value)) {
					return propagated | propagateAssigned;
				}
				LOGGER.debug("Finished processing assignment for lower decision level (ensuring watches are correct).");
			}

			if (value == MBT || value == FALSE) {
				if (!propagateUnassigned(atom, value)) {
					LOGGER.trace("Halting propagation. Current assignment: {}.", assignment);
					return propagated | propagateAssigned;
				}
			} else if (value == TRUE) {
				if (!MBT.equals(prevValue)) {
					if (!propagateUnassigned(atom, MBT)) {
						LOGGER.trace("Halting propagation. Current assignment: {}.", assignment);
						return propagated | propagateAssigned;
					}
				}
				if (!propagateAssigned(atom)) {
					LOGGER.trace("Halting propagation. Current assignment: {}.", assignment);
					return propagated | propagateAssigned;
				}
			}

			if (propagateAssigned) {
				LOGGER.trace("Assignment after propagation of {}: {}", atom, assignment);
			} else {
				LOGGER.trace("Assignment did not change after checking {}", atom);
			}

			propagated |= propagateAssigned;
		}
		if (LOGGER.isTraceEnabled()) {
			new WatchedNoGoodsChecker().doWatchesCheck();
		}
		return propagated;
	}

	private boolean propagateUnassigned(final int atom, final ThriceTruth value) {
		final Watches<BinaryWatch, WatchedNoGood> w = watches(atom);
		Assignment.Entry atomEntry = assignment.get(atom);
		final int atomDecisionLevel = atomEntry.getDecisionLevel();

		// If the atom is TRUE and preciously was MBT, we use the MBT decision level for assigning weak complements.
		final int atomMBTDecisionLevel = (TRUE.equals(value) && atomEntry.getPrevious() != null) ? atomEntry.getPrevious().getDecisionLevel() : atomDecisionLevel;

		// First iterate through all binary NoGoods, as they are trivial:
		// If one of the two literals is assigned, the NoGood must be
		// unit and an assignment can be synthesized.
		for (BinaryWatch watch : w.b.get(value)) {
			NoGood binaryNoGood = watch.getNoGood();
			// If NoGood has head, head is the literal to assign, and the value of the currently assigned atom is FALSE, then set the head to TRUE.
			if (binaryNoGood.hasHead() && binaryNoGood.getHead() == watch.getOtherLiteralIndex() && FALSE.equals(value)) {
				if (!assignStrongComplement(watch.getOtherLiteralIndex(), binaryNoGood, atomDecisionLevel)) {
					return false;
				}
			} else {
				// Ordinary case, propagate to MBT/FALSE
				if (!assignWeakComplement(watch.getOtherLiteralIndex(), binaryNoGood, atomMBTDecisionLevel)) {
					return false;
				}
			}

			propagateAssigned = true;
		}

		for (Iterator<WatchedNoGood> iterator = w.n.get(value).iterator(); iterator.hasNext();) {
			final WatchedNoGood noGood = iterator.next();
			final int assignedPointer = noGood.getAtom(noGood.getPointer(0)) == atom ?  0 : 1;
			final int assignedIndex = noGood.getPointer(assignedPointer);
			final int otherIndex = noGood.getPointer(assignedPointer == 0 ? 1 : 0);
			Assignment.Entry otherEntry = assignment.get(noGood.getAtom(otherIndex));
			int highestDecisionLevel = atomDecisionLevel;
			int posHighestDecisionLevel = -1;
			int highestMBTDecisionLevel = atomMBTDecisionLevel;
			// If value is MBT, this is one occurrence of a positive literal assigned MBT.
			boolean containsMBT = MBT.equals(value);

			for (int offset = 1; offset < noGood.size(); offset++) {
				final int index = (assignedIndex + offset) % noGood.size();

				if (index == otherIndex) {
					continue;
				}

				final int literalAtIndex = noGood.getLiteral(index);

				if (!containsMBT && isPositive(literalAtIndex) && MBT.equals(assignment.getTruth(atomOf(literalAtIndex)))) {
					containsMBT = true;
				}

				if (!assignment.isAssigned(atomOf(literalAtIndex)) || !assignment.containsWeakComplement(literalAtIndex)) {
					noGood.setPointer(assignedPointer, index);
					break;
				}
				Assignment.Entry assignmentLiteralAtIndex = assignment.get(atomOf(literalAtIndex));
				int assignmentLiteralAtIndexDecisionLevel = assignmentLiteralAtIndex != null ? assignmentLiteralAtIndex.getDecisionLevel() : -1;
				int assignmentLiteralAtIndexMBTDecisionLevel = (assignmentLiteralAtIndex != null && TRUE.equals(assignmentLiteralAtIndex.getTruth())
					&& assignmentLiteralAtIndex.getPrevious() != null) ? assignmentLiteralAtIndex.getPrevious().getDecisionLevel() : -1;
				if (assignmentLiteralAtIndexDecisionLevel != -1) {
					if (assignmentLiteralAtIndexDecisionLevel > highestDecisionLevel) {
						highestDecisionLevel = assignmentLiteralAtIndexDecisionLevel;
						posHighestDecisionLevel = index;
					}
					if (assignmentLiteralAtIndexMBTDecisionLevel != -1) {
						// If literal is TRUE and was MBT, record if MBT has a currently highest decision level.
						if (assignmentLiteralAtIndexMBTDecisionLevel > highestMBTDecisionLevel) {
							highestMBTDecisionLevel = assignmentLiteralAtIndexMBTDecisionLevel;
						}
					} else {
						// Literal is either FALSE or MBT or TRUE (without being MBT before), record its decision level if it is currently highest.
						if (assignmentLiteralAtIndexDecisionLevel > highestMBTDecisionLevel) {
							highestMBTDecisionLevel = assignmentLiteralAtIndexDecisionLevel;
						}
					}

				}
			}

			// Move pointer if there is another atom at the same-or-higher decision level as the other (implied) atom.
			if (otherEntry != null && posHighestDecisionLevel != -1 && highestDecisionLevel >= otherEntry.getDecisionLevel()) {
				noGood.setPointer(assignedPointer, posHighestDecisionLevel);
			}

			// Propagate in case the pointer could not be moved.
			if (noGood.getPointer(assignedPointer) == assignedIndex) {
				// Assign to TRUE (or FALSE) in case no MBT was found and otherIndex is head.
				if (otherIndex == noGood.getHead() && !containsMBT) {
					if (!assignStrongComplement(otherIndex, noGood, highestDecisionLevel)) {
						return false;
					}
				} else {
					// Assign MBT (or FALSE)
					if (!assignWeakComplement(otherIndex, noGood, highestMBTDecisionLevel)) {
						return false;
					}
				}

				propagateAssigned = true;
				continue;
			}

			// Now that the pointer points at a different atom, we have to rewire the
			// pointers inside our watching structure.
			// Remove the NoGood from the current watchlist and add it to the watches for the
			// atom the pointer points at now.
			iterator.remove();
			addPosNegWatch(noGood, assignedPointer);
		}
		return true;
	}

	private boolean propagateAssigned(final int atom) {
		final Watches<BinaryWatch, WatchedNoGood> w = watches(atom);

		for (BinaryWatch watch : w.b.getAlpha()) {
			int decisionLevel = assignment.get(atom).getDecisionLevel();
			if (!assignStrongComplement(watch.getOtherLiteralIndex(), watch.getNoGood(), decisionLevel)) {
				return false;
			}

			propagateAssigned = true;
		}

		for (Iterator<WatchedNoGood> iterator = w.n.getAlpha().iterator(); iterator.hasNext();) {
			final WatchedNoGood noGood = iterator.next();

			int bestIndex = -1;
			boolean unit = true;
			int highestDecisionLevel = -1;
			for (int offset = 1; offset < noGood.size(); offset++) {
				final int index = (noGood.getAlphaPointer() + offset) % noGood.size();
				final int literalAtIndex = noGood.getLiteral(index);

				// The alpha pointer must never point at the head, because the head is
				// its "counterpart" that will propagate once the alpha pointer cannot
				// be moved elsewhere.
				if (index == noGood.getHead()) {
					continue;
				}

				final boolean literalAtIndexContained = assignment.contains(literalAtIndex);
				if (literalAtIndexContained) {
					// Record highest decision level if literal is assigned in order for propagation.
					int literalDecisionLevel = assignment.get(atomOf(literalAtIndex)).getDecisionLevel();
					if (literalDecisionLevel > highestDecisionLevel) {
						highestDecisionLevel = literalDecisionLevel;
					}
				}

				// If there is a literal that is not contained in the assignment (and that is not the
				// head, which was excluded above), the nogood is not unit.
				if (!literalAtIndexContained) {
					unit = false;

					// Looking for a new position for the alpha pointer, we have to place it on a
					// positive literal that is not (strictly) contained in the assignment.
					// That is, the atom should be either unassigned or assigned to MBT to qualify
					// as a location for the alpha pointer.
					if (!isNegated(literalAtIndex)) {
						bestIndex = index;
					}
				}

				if (!unit && bestIndex != -1) {
					break;
				}
			}

			// If we did not find anything with priority, we cannot move the pointer. So propagate
			// (meaning assign TRUE to the head)!
			if (unit) {
				if (assignment.contains(noGood.getLiteral(noGood.getHead()))) {
					continue;
				}

				if (!assignStrongComplement(noGood.getHead(), noGood, highestDecisionLevel)) {
					return false;
				}

				propagateAssigned = true;
				continue;
			}

			if (bestIndex == -1) {
				continue;
			}

			// The pointer can be moved to thirdPointer.
			noGood.setAlphaPointer(bestIndex);
			iterator.remove();
			addAlphaWatch(noGood);
		}
		return true;
	}

	private static final class BinaryWatch {
		private final NoGood noGood;
		private final int otherLiteralIndex;

		private BinaryWatch(NoGood noGood, int otherLiteralIndex) {
			this.noGood = noGood;
			this.otherLiteralIndex = otherLiteralIndex;
		}

		private NoGood getNoGood() {
			return noGood;
		}

		private int getOtherLiteralIndex() {
			return otherLiteralIndex;
		}
	}

	/**
	 * A simple data structure to encapsulate watched delegate by truth value tailored to {@link ThriceTruth}. It
	 * holds three separate sets that are used to refer to propagation based on assignment of one of the three truth
	 * values.
	 * @param <T> type used for referencing.
	 */
	private class ThriceSet<T> {
		private final Set<T> pos = new HashSet<>();
		private final Set<T> neg = new HashSet<>();
		private final Set<T> alpha = new HashSet<>();

		public Set<T> get(int literal) {
			return get(!isNegated(literal));
		}

		public Set<T> get(ThriceTruth truth) {
			return get(truth.toBoolean());
		}

		public Set<T> getAlpha() {
			return alpha;
		}

		private Set<T> get(boolean truth) {
			return truth ? pos : neg;
		}
	}

	/**
	 * A simple data structure to encapsulate watched delegate for an atom. It will hold two {@link ThriceSet}s, one
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
		private final ThriceSet<B> b = new ThriceSet<>();
		private final ThriceSet<N> n = new ThriceSet<>();
	}


	/**
	 * This class provides helper methods to detect NoGoods that are not properly watched by the NoGoodStore.
	 * This should be only used during debugging since the checking is costly.
	 */
	private class WatchedNoGoodsChecker {

		public void doWatchesCheck() {
			LOGGER.trace("Checking watch invariant.");
			// Check all watched NoGoods, if their pointers adhere to the watch-pointer invariant.
			for (Map.Entry<Integer, BasicNoGoodStore.Watches<BasicNoGoodStore.BinaryWatch, WatchedNoGood>> atomWatchesEntry : watches.entrySet()) {
				int atom = atomWatchesEntry.getKey();
				checkWatchesInvariant(atom, atomWatchesEntry.getValue(), MBT);
				checkWatchesInvariant(atom, atomWatchesEntry.getValue(), FALSE);
				checkAlphaWatchesInvariant(atom, atomWatchesEntry.getValue());
			}
			LOGGER.trace("Checking watch invariant: all good.");
		}

		private void checkAlphaWatchesInvariant(int atom, BasicNoGoodStore.Watches<BasicNoGoodStore.BinaryWatch, WatchedNoGood> watches) {
			Assignment.Entry atomEntry = assignment.get(atom);
			int atomDecisionLevel = atomEntry != null ? atomEntry.getDecisionLevel() : -1;
			for (BasicNoGoodStore.BinaryWatch binaryWatch : watches.b.getAlpha()) {
				int otherAtom = binaryWatch.getNoGood().getAtom(binaryWatch.getOtherLiteralIndex());
				Assignment.Entry otherEntry = assignment.get(otherAtom);
				int otherDecisionLevel = otherEntry != null ? otherEntry.getDecisionLevel() : -1;
				if (atomEntry == null && otherEntry == null) {
					continue;
				}
				if (atomDecisionLevel == otherDecisionLevel && atomDecisionLevel != -1) {
					continue;
				}
				if (isNoGoodSatisfied(binaryWatch.getNoGood())) {
					continue;
				}
				throw new RuntimeException("Watch invariant violated. Should not happen.");
			}
			for (WatchedNoGood watchedNoGood : watches.n.getAlpha()) {
				// Ensure both watches either unassigned, or one satisfies NoGood, or both are on highest decision level.
				int otherPointer = atom ==  watchedNoGood.getAtom(watchedNoGood.getPointer(1)) ? 0 : 1;
				int otherAtom = watchedNoGood.getAtom(watchedNoGood.getPointer(otherPointer));
				Assignment.Entry otherEntry = assignment.get(otherAtom);
				int otherDecisionLevel = otherEntry != null ? otherEntry.getDecisionLevel() : -1;
				if (atomEntry == null && otherEntry == null) {
					continue;
				}
				if (atomDecisionLevel == otherDecisionLevel && atomDecisionLevel != -1) {
					continue;
				}
				if (isNoGoodSatisfied(watchedNoGood)) {
					continue;
				}
				throw new RuntimeException("Watch invariant violated. Should not happen.");
			}
		}

		private boolean isNoGoodSatisfied(NoGood noGood) {
			for (Integer literal : noGood) {
				Assignment.Entry entry = assignment.get(atomOf(literal));
				if (entry == null) {
					continue;
				}
				if (isNegated(literal) != entry.getTruth().equals(FALSE)) {
					return true;
				}
			}
			return false;
		}

		private void checkWatchesInvariant(int atom, BasicNoGoodStore.Watches<BasicNoGoodStore.BinaryWatch, WatchedNoGood> watches, ThriceTruth truth) {
			Assignment.Entry atomEntry = assignment.get(atom);
			int atomDecisionLevel = atomEntry != null ? atomEntry.getDecisionLevel() : -1;
			for (BasicNoGoodStore.BinaryWatch binaryWatch : watches.b.get(truth)) {
				// Ensure both watches either unassigned, or one satisfies NoGood, or both are on highest decision level.
				int otherAtom = binaryWatch.getNoGood().getAtom(binaryWatch.getOtherLiteralIndex());
				Assignment.Entry otherEntry = assignment.get(otherAtom);
				int otherDecisionLevel = otherEntry != null ? otherEntry.getDecisionLevel() : -1;
				if (atomEntry == null && otherEntry == null) {
					continue;
				}
				if (atomDecisionLevel == otherDecisionLevel && atomDecisionLevel != -1) {
					continue;
				}
				if (isNoGoodSatisfied(binaryWatch.getNoGood())) {
					continue;
				}
				throw new RuntimeException("Watch invariant violated. Should not happen.");
			}
			for (WatchedNoGood watchedNoGood : watches.n.get(truth)) {
				// Ensure both watches either unassigned, or one satisfies NoGood, or both are on highest decision level.
				int otherPointer = atom ==  watchedNoGood.getAtom(watchedNoGood.getPointer(1)) ? 0 : 1;
				int otherAtom = watchedNoGood.getAtom(watchedNoGood.getPointer(otherPointer));
				Assignment.Entry otherEntry = assignment.get(otherAtom);
				int otherDecisionLevel = otherEntry != null ? otherEntry.getDecisionLevel() : -1;
				if (atomEntry == null && otherEntry == null) {
					continue;
				}
				if (atomDecisionLevel == otherDecisionLevel && atomDecisionLevel != -1) {
					continue;
				}
				if (isNoGoodSatisfied(watchedNoGood)) {
					continue;
				}
				throw new RuntimeException("Watch invariant violated. Should not happen.");
			}
		}
	}
}