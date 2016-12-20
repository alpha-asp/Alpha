package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AtomTranslator;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;
import static at.ac.tuwien.kr.alpha.common.Literals.isPositive;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;

class BasicNoGoodStore implements NoGoodStore<ThriceTruth> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicNoGoodStore.class);

	private final AtomTranslator translator;
	private final Assignment assignment;
	private final Map<Integer, Watches<BinaryWatch, WatchedNoGood>> watches = new HashMap<>();

	private NoGood violated;

	BasicNoGoodStore(Assignment assignment, Grounder translator) {
		this.assignment = assignment;
		//this.assignmentIterator = assignment.iterator();
		this.translator = translator;
	}

	BasicNoGoodStore(Assignment assignment) {
		this(assignment, null);
	}

	@Override
	public void backtrack() {
		violated = null;
		assignment.backtrack();
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
		watches(a).b.get(isNegated(a) ? FALSE : MBT).add(new BinaryWatch(id, 1, noGood));
		watches(b).b.get(isNegated(b) ? FALSE : MBT).add(new BinaryWatch(id, 0, noGood));

		// If the nogood has a head literal, take extra care as it
		// might propagate TRUE (and not only FALSE or MBT, which
		// are accounted for above).
		if (noGood.hasHead()) {
			final int head = noGood.getHead();
			final int bodyLiteral = noGood.getLiteral(head == 0 ? 1 : 0);

			ThriceTruth complementValue;
			if (!isNegated(bodyLiteral)) {
				complementValue = TRUE;
			} else {
				complementValue = FALSE;
			}
			// Set up watch.
			watches(bodyLiteral).b.get(complementValue).add(new BinaryWatch(id, head, noGood));
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
		int secondHighestDecisionLevel = -1;
		int posSecondHighestDecisionLevel = -1;
		int posPositiveUnassigned = -1;
		int posPositiveTrueHighestDecisionLevel = -1;
		int positiveTrueHighestDecisionLevel = -1;

		Map<Integer, Boolean> occurringLiterals = new HashMap<>();	// Used to detect always-satisfied NoGoods of form { L, -L, ... }.

		// Iterate whole noGood and set above pointers.
		for (int i = 0; i < noGood.size(); i++) {
			final int literal = noGood.getLiteral(i);
			Assignment.Entry entry = assignment.get(atomOf(literal));

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

			// Inspect literal and potentially assigned values.
			if (entry == null) {
				// Literal is unassigned, record in first/second unassigned pointer.
				if (posFirstUnassigned == -1) {
					posFirstUnassigned = i;
				} else {
					if (posSecondUnassigned == -1) {
						posSecondUnassigned = i;
					}
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
				// Check if literal has highest decision level (so far).
				if (entry.getDecisionLevel() >= highestDecisionLevel) {
					// Move former highest down to second highest.
					secondHighestDecisionLevel = highestDecisionLevel;
					posSecondHighestDecisionLevel = posHighestDecisionLevel;
					// Record highest decision level.
					highestDecisionLevel = entry.getDecisionLevel();
					posHighestDecisionLevel = i;
					continue;
				}
				// Check if literal has second highest decision level (only reached if smaller than highest decision level).
				if (entry.getDecisionLevel() > secondHighestDecisionLevel) {
					secondHighestDecisionLevel = entry.getDecisionLevel();
					posSecondHighestDecisionLevel = i;
				}
				// Check if literal is positive, assigned TRUE and has highest decision level
				if (TRUE.equals(entry.getTruth()) && entry.getDecisionLevel() > positiveTrueHighestDecisionLevel) {
					positiveTrueHighestDecisionLevel = entry.getDecisionLevel();
					posPositiveTrueHighestDecisionLevel = i;
				}
			}
		}

		// The MBT pointer points at a positive literal that is either unassigned or MBT;
		// if all positive literals are assigned TRUE, it points to the one with highest decision level;
		// if the noGood has no head, it is -1.
		int potentialMbtPointer = -1;
		if (noGood.hasHead()) {
			potentialMbtPointer = posPositiveMBTAssignedLiteralExceptHead != -1 ? posPositiveMBTAssignedLiteralExceptHead : posPositiveUnassigned;
			if (potentialMbtPointer == -1) {
				// All positives are assigned true, point to highest one
				potentialMbtPointer = posPositiveTrueHighestDecisionLevel;
			}
		}


		// Match cases 1) - 5) above and process accordingly. Note: the order of cases below matters.
		if (posFirstUnassigned != -1 && posSecondUnassigned != -1) {
			// Case 5)
			int pointers[] = {posFirstUnassigned, posSecondUnassigned, potentialMbtPointer};
			setWatches(noGood, pointers);
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
				int pointers[] = {posSatisfyingLiteral, posHighestDecisionLevelExceptSatisfyingLiteral, potentialMbtPointer};
				setWatches(noGood, pointers);
			} else {
				// Case 2b)
				int posSecondHighestDecisionLevelOrUnassigned = posSecondHighestDecisionLevel == -1 ? posFirstUnassigned : posSecondHighestDecisionLevel;
				int pointers[] = {posHighestDecisionLevel, posSecondHighestDecisionLevelOrUnassigned, potentialMbtPointer};
				setWatches(noGood, pointers);
			}
		} else if (noGood.hasHead() && posPositiveMBTAssignedLiteralExceptHead == -1 && (posFirstUnassigned == noGood.getHead() || posFirstUnassigned == -1)) {
			// Case 4)
			if (!assignStrongComplement(noGood.getHead(), noGood, highestDecisionLevel)) {
				return setViolatedFromAssignment();
			}
			int pointers[] = {posFirstUnassigned, posHighestDecisionLevel, potentialMbtPointer};
			setWatches(noGood, pointers);
		} else if (posFirstUnassigned != -1 && posSecondUnassigned == -1) {
			// Case 3)
			if (!assignWeakComplement(posFirstUnassigned, noGood, highestDecisionLevel)) {
				return setViolatedFromAssignment();
			}
			int pointers[] = {posFirstUnassigned, posHighestDecisionLevel, potentialMbtPointer};
			setWatches(noGood, pointers);
		} else {
			// Should never be reached.
			throw new RuntimeException("Bug in algorithm, added NoGood not matched by any cases. Forgotten case? NoGood is: " + noGood);
		}

		return null;
	}

	private void setWatches(NoGood noGood, int[] pointers) {
		final WatchedNoGood wng = new WatchedNoGood(noGood, pointers);

		for (int i = 0; i < 2; i++) {
			final int literal = noGood.getLiteral(pointers[i]);
			watches(literal).n.get(isNegated(literal) ? FALSE : MBT).add(wng);
		}

		// Only look at the third pointer if it points at a legal index. It might not
		// in case the nogood has no head or no positive literal.
		if (pointers[2] != -1) {
			watches(noGood.getLiteral(pointers[2])).n.get(TRUE).add(wng);
		}
	}


	private boolean assignWeakComplement(final int literalIndex, final NoGood impliedBy, int decisionLevel) {
		final int literal = impliedBy.getLiteral(literalIndex);
		final int atom = atomOf(literal);
		if (!assignment.assign(atom, isNegated(literal) ? MBT : FALSE, impliedBy, decisionLevel)) {
			setViolated(assignment.getNoGoodViolatedByAssign());
			return false;
		}
		return true;
	}

	private boolean assignStrongComplement(final int literalIndex, final NoGood impliedBy, int decisionLevel) {
		final int literal = impliedBy.getLiteral(literalIndex);
		final int atom = atomOf(literal);
		if (!assignment.assign(atom, isNegated(literal) ? TRUE : FALSE, impliedBy, decisionLevel)) {
			setViolated(assignment.getNoGoodViolatedByAssign());
			return false;
		}
		return true;
	}

	private boolean assign(final NoGood noGood, final int index, final ThriceTruth negated) {
		int literal = noGood.getLiteral(index);
		if (!assignment.assign(atomOf(literal), isNegated(literal) ? negated : FALSE, noGood)) {
			setViolated(assignment.getNoGoodViolatedByAssign());
			return false;
		}
		return true;
	}

	@Override
	public boolean propagate() {
		boolean propagated = false;
		Queue<Assignment.Entry> assignmentsToProcess = assignment.getAssignmentsToProcess();

		while (!assignmentsToProcess.isEmpty()) {
		//while (assignmentIterator.hasNext()) {
			final Assignment.Entry entry = assignmentsToProcess.remove(); //assignmentIterator.next();
			final int atom = entry.getAtom();

			LOGGER.trace("Looking for propagation from {}", atom);

			final ThriceTruth value = entry.getTruth();

			final Assignment.Entry previous = entry.getPrevious();
			final ThriceTruth prevValue = previous != null ? previous.getTruth() : null;

			boolean atomPropagated = false;

			if (value == MBT || value == FALSE) {
				atomPropagated = propagateMBT(atom, value);
				propagated |= atomPropagated;
				if (violated != null) {
					return propagated;
				}
			} else if (value == TRUE) {
				if (!MBT.equals(prevValue)) {
					atomPropagated = propagateMBT(atom, MBT);
					propagated |= atomPropagated;
					if (violated != null) {
						return propagated;
					}
				}
				atomPropagated |= propagateTrue(atom);
				propagated |= atomPropagated;
				if (violated != null) {
					return propagated;
				}
			}

			if (atomPropagated) {
				LOGGER.trace("Assignment after propagation of {}: {}", atom, assignment);
			} else {
				LOGGER.trace("Assignment did not change after checking {}", atom);
			}

			propagated |= atomPropagated;
		}
		return propagated;
	}

	private boolean propagateMBT(final int atom, final ThriceTruth value) {
		boolean propagated = false;
		final Watches<BinaryWatch, WatchedNoGood> w = watches(atom);
		final int atomDecisionLevel = assignment.get(atom).getDecisionLevel();

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
				if (!assignWeakComplement(watch.getOtherLiteralIndex(), binaryNoGood, atomDecisionLevel)) {
					return false;
				}
			}

			propagated = true;
		}

		for (Iterator<WatchedNoGood> iterator = w.n.get(value).iterator(); iterator.hasNext();) {
			final WatchedNoGood noGood = iterator.next();
			final int assignedPointer = noGood.getAtom(noGood.getPointer(0)) == atom ?  0 : 1;
			final int assignedIndex = noGood.getPointer(assignedPointer);
			final int otherIndex = noGood.getPointer(assignedPointer == 0 ? 1 : 0);
			int highestDecisionLevel = atomDecisionLevel;
			boolean containsMBT = MBT.equals(value);	// If value is MBT, this is one occurrence of a positive literal assigned MBT.

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
				Assignment.Entry assignmentLiteralAtIndex = assignment.get(literalAtIndex);
				if (assignmentLiteralAtIndex != null && assignmentLiteralAtIndex.getDecisionLevel() > highestDecisionLevel) {
					highestDecisionLevel = assignmentLiteralAtIndex.getDecisionLevel();
				}
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
					if (!assignWeakComplement(otherIndex, noGood, highestDecisionLevel)) {
						return false;
					}
				}

				propagated = true;
				continue;
			}

			// Now that the pointer points at a different atom, we have to rewire the
			// pointers inside our watching structure.
			// Remove the NoGood from the current watchlist and add it to the watches for the
			// atom the pointer points at now.
			iterator.remove();
			final int repointedLiteral = noGood.getLiteral(noGood.getPointer(assignedPointer));
			watches(repointedLiteral).n.get(isNegated(repointedLiteral) ? FALSE : MBT).add(noGood);
		}
		return propagated;
	}

	private boolean propagateTrue(final int atom) {
		boolean propagated = false;
		final Watches<BinaryWatch, WatchedNoGood> w = watches(atom);

		for (BinaryWatch watch : w.b.get(TRUE)) {
			final int otherLiteralIndex = watch.getOtherLiteralIndex();
			final NoGood noGood = watch.getNoGood();

			if (!assign(noGood, otherLiteralIndex, TRUE)) {
				return false;
			}

			propagated = true;
		}

		for (Iterator<WatchedNoGood> iterator = w.n.get(TRUE).iterator(); iterator.hasNext();) {
			final WatchedNoGood noGood = iterator.next();

			int bestIndex = -1;
			int priority = -1;

			boolean unit = true;
			for (int offset = 1; offset < noGood.size(); offset++) {
				final int index = (noGood.getPointer(2) + offset) % noGood.size();
				final int literalAtIndex = noGood.getLiteral(index);

				if (index == noGood.getHead()) {
					continue;
				}

				if (!assignment.contains(literalAtIndex)) {
					unit = false;
				}

				if (!isNegated(literalAtIndex) && !assignment.contains(literalAtIndex)) {
					final int itemPriority = toPriority(noGood.getAtom(index));
					if (itemPriority > priority) {
						bestIndex = index;
						priority = itemPriority;
					}
				}
			}

			// If we did not find anything with priority, we cannot move the pointer. So propagate
			// (meaning assign TRUE to the head)!
			if (unit) {
				if (assignment.contains(noGood.getLiteral(noGood.getHead()))) {
					continue;
				}

				if (!assign(noGood, noGood.getHead(), TRUE)) {
					return false;
				}

				propagated = true;
				continue;
			}

			if (priority == -1) {
				continue;
			}

			// The pointer can be moved to thirdPointer.
			noGood.setPointer(2, bestIndex);
			iterator.remove();
			watches(noGood.getLiteral(noGood.getPointer(2))).n.get(TRUE).add(noGood);
		}
		return propagated;
	}

	private int toPriority(int atom) {
		final Assignment.Entry entry = assignment.get(atom);
		if (entry == null) {
			return Integer.MAX_VALUE;
		}
		return entry.getDecisionLevel();
	}

	private static final class BinaryWatch {
		private final int id;
		private final int otherLiteralIndex;
		private final NoGood noGood;

		private BinaryWatch(int id, int otherLiteralIndex, NoGood noGood) {
			this.id = id;
			this.otherLiteralIndex = otherLiteralIndex;
			this.noGood = noGood;
		}

		private int getId() {
			return id;
		}

		private int getOtherLiteralIndex() {
			return otherLiteralIndex;
		}

		public NoGood getNoGood() {
			return noGood;
		}
	}

	/**
	 * A simple data structure to encapsulate watched objects by truth value tailored to {@link ThriceTruth}. It
	 * holds three separate sets that are used to refer to propagation based on assignment of one of the three truth
	 * values.
	 * @param <T> type used for referencing.
	 */
	private class ThriceSet<T> {
		private final Set<T> pos = new HashSet<>();
		private final Set<T> neg = new HashSet<>();
		private final Set<T> mbt = new HashSet<>();

		public Set<T> get(ThriceTruth truth) {
			return truth != FALSE ? truth != MBT ? pos : mbt : neg;
		}
	}

	/**
	 * A simple data structure to encapsulate watched objects for an atom. It will hold two {@link ThriceSet}s, one
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
}