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
	private final Iterator<Assignment.Entry> assignmentIterator;

	private NoGood violated;

	BasicNoGoodStore(Assignment assignment, Grounder translator) {
		this.assignment = assignment;
		this.assignmentIterator = assignment.iterator();
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

	@Override
	public boolean isEmpty() {
		return watches.isEmpty();
	}

	private Watches<BinaryWatch, WatchedNoGood> watches(int literal) {
		return watches.computeIfAbsent(atomOf(literal), k -> new Watches<>());
	}

	@Override
	public boolean add(int id, NoGood noGood) {
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
	private boolean addUnary(final NoGood noGood) {
		final int literal = noGood.getLiteral(0);
		if (!assignment.assign(atomOf(literal), isNegated(literal) ? noGood.hasHead() ? TRUE : MBT : FALSE, noGood)) {
			setViolated(noGood);
			return false;
		}
		return true;
	}

	private boolean addAndWatchBinary(final int id, final NoGood noGood) {
		// Shorthands for viewing the nogood as { a, b }.
		final int a = noGood.getLiteral(0);
		final int b = noGood.getLiteral(1);

		if (a != b && atomOf(a) == atomOf(b)) {
			return true;
		}

		// Check for violation.
		if (assignment.containsRelaxed(a) && assignment.containsRelaxed(b)) {
			setViolated(noGood);
			return false;
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
			Set<BinaryWatch> w;
			if (!isNegated(bodyLiteral)) {
				w = watches(bodyLiteral).b.getAlpha();
			} else {
				w = watches(bodyLiteral).b.get(FALSE);
			}
			w.add(new BinaryWatch(noGood, head));

			// If the body already is assigned TRUE or FALSE,
			// directly perform propagation.
			if (TRUE.equals(assignment.getTruth(atomOf(bodyLiteral))) && !isNegated(bodyLiteral)
				|| FALSE.equals(assignment.getTruth(atomOf(bodyLiteral))) && isNegated(bodyLiteral)) {
				if (!assign(noGood, noGood.getHead(), TRUE)) {
					return false;
				}
			}
		}

		// At this point it is clear that the nogood is not violated
		// and all watches are set up. Note that in case we were lucky
		// and directly propagated TRUE for the head, we'll not get
		// here.
		// What's left now is to check whether "ordinary" propagation
		// (to MBT/FALSE) can be done.

		final boolean aAssigned = assignment.isAssigned(atomOf(a));
		final boolean bAssigned = assignment.isAssigned(atomOf(b));

		// If exactly one literal is assigned, maybe we can obtain
		// assignment for the other.
		if (aAssigned ^ bAssigned) {
			// First check whether the assigned literal is really
			// contained in the assignment (otherwise the nogood
			// cannot be unit) and then just go ahead and assign.
			if (assignment.containsRelaxed(noGood, aAssigned ? 0 : 1) && !assign(noGood, aAssigned ? 1 : 0, MBT)) {
				setViolated(noGood);
				return false;
			}
		}

		return true;
	}

	private boolean addAndWatch(final NoGood noGood) {
		// A NoGood when added can be one of the following:
		// 1) it is violated (there are no unassigned literals, and all are assigned as occurring in the noGood).
		// 2) it is satisfied (there is one literal assigned to the complement of how it occurs in the noGood).
		// 3) it propagates weakly (there is exactly one unassigned literal for propagation to FALSE or MBT).
		// 4) it propagates strongly (to TRUE, by all positive occurrences being assigned TRUE and the head being either unassigned or assigned MBT).
		// 5) it is silent / none of the above (there are more or equal to two unassigned literals).
		int posFirstUnassigned = -1;
		int posSecondUnassigned = -1;
		int posSatisfyingLiteral = -1;
		int posPositiveMBTAssignedLiteralExceptHead = -1;
		int posHighestDecisionLevel = -1;
		int highestDecisionLevel = -1;
		int secondHighestPriority = -1;
		int posSecondHighestPriority = -1;
		int posPositiveUnassigned = -1;
		int posPositiveTrueHighestPriority = -1;
		int positiveTrueHighestPriority = -1;

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
					return true;
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
					posSatisfyingLiteral = i;
				}
				// Check if literal is MBT and not the head.
				if (MBT.equals(entry.getTruth()) && i != noGood.getHead() && !isNegated(literal)) {
					posPositiveMBTAssignedLiteralExceptHead = i;
				}

				int priority = toPriority(entry);

				// Check if literal has highest decision level (so far).
				if (priority >= highestDecisionLevel) {
					// Move former highest down to second highest.
					secondHighestPriority = highestDecisionLevel;
					posSecondHighestPriority = posHighestDecisionLevel;
					// Record highest decision level.
					highestDecisionLevel = priority;
					posHighestDecisionLevel = i;
					continue;
				}
				// Check if literal has second highest decision level (only reached if smaller than highest decision level).
				if (priority > secondHighestPriority) {
					secondHighestPriority = priority;
					posSecondHighestPriority = i;
				}
				// Check if literal is positive, assigned TRUE and has highest decision level
				if (TRUE.equals(entry.getTruth()) && priority > positiveTrueHighestPriority) {
					positiveTrueHighestPriority = priority;
					posPositiveTrueHighestPriority = i;
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
				potentialAlphaPointer = posPositiveTrueHighestPriority;
			}
		}

		// Match cases 1) - 5) above and process accordingly. Note: the order of cases below matters.
		if (posFirstUnassigned != -1 && posSecondUnassigned != -1) {
			// Case 5)
			setWatches(noGood, posFirstUnassigned, posSecondUnassigned, potentialAlphaPointer);
		} else if (posFirstUnassigned == -1 && posSatisfyingLiteral == -1) {
			// Case 1)
			setViolated(noGood);
			return false;
		} else if (posSatisfyingLiteral != -1) {
			// Case 2)
			setWatches(noGood, posHighestDecisionLevel, posSecondHighestPriority, potentialAlphaPointer);
		} else if (noGood.hasHead() && posPositiveMBTAssignedLiteralExceptHead == -1 && (posFirstUnassigned == noGood.getHead() || posFirstUnassigned == -1)) {
			// Case 4)
			assignSubDl(noGood, noGood.getHead(), TRUE, highestDecisionLevel);
			setWatches(noGood, posFirstUnassigned, posHighestDecisionLevel, potentialAlphaPointer);
		} else if (posFirstUnassigned != -1 && posSecondUnassigned == -1) {
			// Case 3)
			assignSubDl(noGood, posFirstUnassigned, MBT, highestDecisionLevel);
			setWatches(noGood, posFirstUnassigned, posHighestDecisionLevel, potentialAlphaPointer);
		} else {
			// Should never be reached.
			throw new RuntimeException("Bug in algorithm, added NoGood not matched by any cases. Forgotten case? NoGood is: " + noGood);
		}

		return true;
	}

	private void setWatches(NoGood noGood, int a, int b, int alpha) {
		final WatchedNoGood wng = new WatchedNoGood(noGood, a, b, alpha);

		for (int i = 0; i < 2; i++) {
			final int literal = wng.getLiteral(wng.getPointer(i));
			watches(literal).n.get(literal).add(wng);
		}

		// Only look at the alpha pointer if it points at a legal index. It might not
		// in case the nogood has no head or no positive literal.
		if (alpha != -1) {
			watches(wng.getLiteral(alpha)).n.getAlpha().add(wng);
		}
	}

	/**
	 * Adds a noGood to the store and performs following precautions:
	 *  * If <code>noGood</code> is violated, return false, NoGood is not added.
	 *  * If <code>noGood</code> is unit, propagate
	 *  * If <code>noGood</code> has at least two unassigned literals, add appropriate watches.
	 *  * If <code>noGood</code> is eligible for propagating <code>TRUE</code> add appropriate watches.
	 * @param noGood
	 * @return
	 */
	private boolean addAndWatchOld(final NoGood noGood) {
		// Do one iteration over the nogood in order to find out whether it (1.) is violated
		// or (2.) might propagate. The second condition is slightly differs from "unit".
		// Even if all literals are assigned, there might be a propagation to TRUE. However,
		// there must be at most one unassigned literal and all other literals must be
		// contained in the assignment.
		boolean isViolated = true;
		boolean propagatesMbt = true;
		boolean propagatesTrue = true;

		Map<Integer, Boolean> mapRep = new HashMap<>();

		// Along the way keep pointers to the first two
		// unassigned literals encountered and the index
		// and priority for placing the third pointer in
		// order to propagate TRUE.
		int[] pointers = new int[]{-1, -1, -1};
		int priority = -1;

		for (int i = 0; i < noGood.size(); i++) {
			final int literal = noGood.getLiteral(i);

			if (mapRep.containsKey(atomOf(literal))) {
				if (mapRep.get(atomOf(literal)) != isNegated(literal)) {
					return true;
				}
			} else {
				mapRep.put(atomOf(literal), isNegated(literal));
			}

			// Third pointer comes into play if there's a head literal. It may
			// not point at the head literal, though. Also it must not be negated.
			if (noGood.hasHead() && noGood.getHead() != i && !isNegated(literal)) {
				int candidatePriority = toPriority(noGood.getAtom(i));
				if (candidatePriority > priority) {
					pointers[2] = i;
					priority = candidatePriority;
				}
			}

			final ThriceTruth value = assignment.getTruth(atomOf(literal));

			// Once we found a literal that is not contained in the assignment, we
			// can be sure that the nogood is not violated.
			if (!(isNegated(literal) ? FALSE : TRUE).equals(value)) {
				isViolated = false;
				if (value != null) {
					// If the literal also is assigned, we can further
					// deduce that it will not propagate.
					propagatesTrue = false;

					// Regular unit propagation is has weaker constraints,
					// MBT is allowed for positive literals, so check this
					// separately.
					if (value.toBoolean() == isNegated(literal)) {
						propagatesMbt = false;
					}
				}
			}

			// Look closely at all unassigned literals and record their index.
			if (value == null) {
				if (pointers[0] == -1) {
					pointers[0] = i;
				} else if (pointers[1] == -1) {
					pointers[1] = i;

					// In case there are two unassigned literals,
					// the nogood will not propagate.
					propagatesMbt = false;
					propagatesTrue = false;
				}
			}
		}

		if (isViolated) {
			setViolated(noGood);
			return false;
		}

		// "propagates" makes sure that there is at most one unassigned literal, and all other literals are
		// contained in the assignment.
		if (propagatesTrue && (pointers[0] == noGood.getHead() || pointers[0] == -1) && pointers[2] != -1) {
			// In case there is no unassigned literal, or the only unassigned literal is the head,
			// and furthermore the third pointer points at a TRUE atom, propagate TRUE for the head.
			if (!assign(noGood, noGood.getHead(), TRUE)) {
				return false;
			}
		} else if (propagatesMbt && pointers[0] != -1 && pointers[1] == -1) {
			// If there is exactly one unassigned literal, perform regular unit propagation.
			if (!assign(noGood, pointers[0], MBT)) {
				return false;
			}
		}

		// Now fix up pointers in case no unassigned literals were found.
		// Here we need to assign to literals in the highest decisionLevel, otherwise after backtracking the pointers may point at assigned literals while the NoGood is actually unit.
		if (pointers[0] == -1) {
			pointers[0] = 0;
		}

		if (pointers[1] == -1) {
			pointers[1] = (pointers[0] + noGood.size() / 2) % noGood.size();
		}

		final WatchedNoGood wng = new WatchedNoGood(noGood, pointers[0], pointers[1], pointers[2]);

		for (int i = 0; i < 2; i++) {
			final int literal = noGood.getLiteral(pointers[i]);
			watches(literal).n.get(literal).add(wng);
		}

		// Only look at the third pointer if it points at a legal index. It might not
		// in case the nogood has no head or no positive literal.
		if (pointers[2] != -1) {
			watches(noGood.getLiteral(pointers[2])).n.getAlpha().add(wng);
		}
		return true;
	}

	private boolean assignSubDl(final NoGood noGood, final int index, final ThriceTruth negated, int decisionLevel) {
		int literal = noGood.getLiteral(index);
		if (!assignment.assignSubDL(atomOf(literal), isNegated(literal) ? negated : FALSE, noGood, decisionLevel)) {
			setViolated(noGood);
			return false;
		}
		return true;
	}

	private boolean assign(final NoGood noGood, final int index, final ThriceTruth negated) {
		int literal = noGood.getLiteral(index);
		if (!assignment.assign(atomOf(literal), isNegated(literal) ? negated : FALSE, noGood)) {
			setViolated(noGood);
			return false;
		}
		return true;
	}

	@Override
	public boolean propagate() {
		boolean propagated = false;

		while (assignmentIterator.hasNext()) {
			final Assignment.Entry entry = assignmentIterator.next();
			final int atom = entry.getAtom();

			LOGGER.trace("Looking for propagation from {}", atom);

			final ThriceTruth value = entry.getTruth();

			final Assignment.Entry previous = entry.getPrevious();
			final ThriceTruth prevValue = previous != null ? previous.getTruth() : null;

			boolean atomPropagated = false;

			if (value == MBT || value == FALSE) {
				atomPropagated = propagateUnassigned(atom, value);
				if (violated != null) {
					return false;
				}
			} else if (value == TRUE) {
				if (!MBT.equals(prevValue)) {
					atomPropagated = propagateUnassigned(atom, MBT);
					if (violated != null) {
						return false;
					}
				}
				atomPropagated |= propagateAssigned(atom);
				if (violated != null) {
					return false;
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

	private boolean propagateUnassigned(final int atom, final ThriceTruth value) {
		boolean propagated = false;
		final Watches<BinaryWatch, WatchedNoGood> w = watches(atom);

		// First iterate through all binary NoGoods, as they are trivial:
		// If one of the two literals is assigned, the NoGood must be
		// unit and an assignment can be synthesized.
		for (BinaryWatch watch : w.b.get(value)) {
			NoGood binaryNoGood = watch.getNoGood();
			// If NoGood has head, head is the literal to assign, and the value of the currently assigned atom is FALSE, then set the head to TRUE.
			if (binaryNoGood.hasHead() && binaryNoGood.getHead() == watch.getOtherLiteralIndex() && FALSE.equals(value)) {
				if (!assign(binaryNoGood, watch.getOtherLiteralIndex(), TRUE)) {
					return false;
				}
			} else {
				// Ordinary case, propagate to MBT/FALSE
				if (!assign(binaryNoGood, watch.getOtherLiteralIndex(), MBT)) {
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

				if (!assignment.isAssigned(atomOf(literalAtIndex)) || !assignment.containsRelaxed(literalAtIndex)) {
					noGood.setPointer(assignedPointer, index);
					break;
				}
			}

			// Propagate in case the pointer could not be moved.
			if (noGood.getPointer(assignedPointer) == assignedIndex) {
				// Assign to TRUE (or FALSE) in case no MBT was found and otherIndex is head.
				if (otherIndex == noGood.getHead() && !containsMBT) {
					if (!assign(noGood, otherIndex, TRUE)) {
						return false;
					}
				} else {
					// Assign MBT (or FALSE)
					if (!assign(noGood, otherIndex, MBT)) {
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
			watches(repointedLiteral).n.get(repointedLiteral).add(noGood);
		}
		return propagated;
	}

	private boolean propagateAssigned(final int atom) {
		boolean propagated = false;
		final Watches<BinaryWatch, WatchedNoGood> w = watches(atom);

		for (BinaryWatch watch : w.b.getAlpha()) {
			final int otherLiteralIndex = watch.getOtherLiteralIndex();
			final NoGood noGood = watch.getNoGood();

			if (!assign(noGood, otherLiteralIndex, TRUE)) {
				return false;
			}

			propagated = true;
		}

		for (Iterator<WatchedNoGood> iterator = w.n.getAlpha().iterator(); iterator.hasNext();) {
			final WatchedNoGood noGood = iterator.next();

			int bestIndex = -1;
			int priority = -1;

			boolean unit = true;
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

				// If there is a literal that is not contained in the assignment (and that is not the
				// head, which was excluded above), the nogood is not unit.
				if (!literalAtIndexContained) {
					unit = false;
				}

				// Looking for a new position for the alpha pointer, we have to place it on a
				// positive literal that is not (strictly) contained in the assignment.
				// That is, the atom should be either unassigned or assigned to MBT to qualify
				// as a location for the alpha pointer.
				if (!isNegated(literalAtIndex) && !literalAtIndexContained) {
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
			noGood.setAlphaPointer(bestIndex);
			iterator.remove();
			watches(noGood.getLiteral(noGood.getAlphaPointer())).n.getAlpha().add(noGood);
		}
		return propagated;
	}

	private int toPriority(Assignment.Entry entry) {
		return entry.getDecisionLevel();
	}

	private int toPriority(int atom) {
		final Assignment.Entry entry = assignment.get(atom);
		if (entry == null) {
			return Integer.MAX_VALUE;
		}
		return toPriority(entry);
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
	 * A simple data structure to encapsulate watched objects by truth value tailored to {@link ThriceTruth}. It
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