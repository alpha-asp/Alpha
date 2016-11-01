package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;

class BasicNoGoodStore implements NoGoodStore<ThriceTruth> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicNoGoodStore.class);

	private final Grounder grounder;
	private final Assignment assignment;
	private final Queue<Integer> cache = new ArrayDeque<>();
	private final Map<Integer, Watches<BinaryWatch, WatchedNoGood>> watches = new HashMap<>();
	private final Map<Integer, NoGood> binaries = new HashMap<>();

	private Map<Integer, ThriceTruth> changedAssignments = new HashMap<>();
	private NoGood violated;
	private int decisionLevel;

	BasicNoGoodStore(Assignment assignment, Grounder grounder) {
		this.assignment = assignment;
		this.decisionLevel = 0;
		this.grounder = grounder;
	}

	BasicNoGoodStore(Assignment assignment) {
		this(assignment, null);
	}

	public void setDecisionLevel(int decisionLevel) {
		if (decisionLevel < this.decisionLevel) {
			// When backtracking, forget any violation.
			violated = null;
		}
		this.decisionLevel = decisionLevel;
	}

	void clear() {
		assignment.clear();
		cache.clear();
		watches.clear();
	}

	@Override
	public Map<Integer, ThriceTruth> getChangedAssignments() {
		Map<Integer, ThriceTruth> result = Collections.unmodifiableMap(changedAssignments);
		changedAssignments = new HashMap<>();
		return result;
	}

	@Override
	public boolean isEmpty() {
		return watches.isEmpty() && cache.isEmpty() && binaries.isEmpty();
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
		if (!assign(literal, isNegated(literal) ? noGood.hasHead() ? TRUE : MBT : FALSE)) {
			violated = noGood;
			return false;
		}
		return true;
	}

	private boolean addAndWatchBinary(final int id, final NoGood noGood) {
		// Shorthands for viewing the nogood as { a, b }.
		final int a = noGood.getLiteral(0);
		final int b = noGood.getLiteral(1);

		// Check for violation.
		if (assignment.containsRelaxed(a) && assignment.containsRelaxed(b)) {
			violated = noGood;
			return false;
		}

		// Store this nogood for referencing it by ID later.
		binaries.put(id, noGood);

		// Set up watches, so that in case either a or b is assigned,
		// an assignment for the respective other can be generated
		// by unit propagation.
		watches(a).b.get(isNegated(a) ? FALSE : MBT).add(new BinaryWatch(id, 1));
		watches(b).b.get(isNegated(b) ? FALSE : MBT).add(new BinaryWatch(id, 0));

		// If the nogood has a head literal, take extra care as it
		// might propagate TRUE (and not only FALSE or MBT, which
		// are accounted for above).
		if (noGood.hasHead()) {
			final int head = noGood.getHead();
			final int bodyLiteral = noGood.getLiteral(head == 0 ? 1 : 0);

			// If the body literal is negated, TRUE will never
			// be propagated, so ly take a closer look if it's
			// not.
			if (!isNegated(bodyLiteral)) {
				// Set up a watch in case the body part is assigned to TRUE,
				// because then the head might also propagate to TRUE.
				watches(bodyLiteral).b.get(TRUE).add(new BinaryWatch(id, head));

				// If the body already is assigned TRUE,
				// directly perform propagation.
				if (TRUE.equals(assignment.getTruth(atomOf(bodyLiteral)))) {
					if (!assign(noGood, noGood.getHead(), TRUE)) {
						return false;
					}
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
				violated = noGood;
				return false;
			}
		}

		return true;
	}

	/**
	 * Adds a noGood to the store and performs following precautions:
	 *  * If <code>noGood</code> is violated, start backtracking
	 *  * If <code>noGood</code> is unit, propagate
	 *  * If <code>noGood</code> has at least two unassigned literals, add appropriate watches.
	 *  * If <code>noGood</code> is eligible for propagating <code>TRUE</code> add appropriate watches.
	 * @param noGood
	 * @return
	 */
	private boolean addAndWatch(final NoGood noGood) {
		// Do one iteration over the nogood in order to find out whether it (1.) is violated
		// or (2.) might propagate. The second condition is slightly differs from "unit".
		// Even if all literals are assigned, there might be a propagation to TRUE. However,
		// there must be at most one unassigned literal and all other literals must be
		// contained in the assignment.
		boolean isViolated = true;
		boolean propagatesMbt = true;
		boolean propagatesTrue = true;

		// Along the way keep pointers to the first two
		// unassigned literals encountered and the index
		// and priority for placing the third pointer in
		// order to propagate TRUE.
		int[] pointers = new int[]{-1, -1, -1};
		int priority = -1;

		for (int i = 0; i < noGood.size(); i++) {
			final int literal = noGood.getLiteral(i);

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
			violated = noGood;
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
		if (pointers[0] == -1) {
			pointers[0] = 0;
		}

		if (pointers[1] == -1) {
			pointers[1] = (pointers[0] + noGood.size() / 2) % noGood.size();
		}

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

		return true;
	}

	/**
	 * Passes on the assignment of a given literal and value to {@link #assignment} and adds the corresponding atom
	 * to {@link #cache} for checking later on.
	 * @param literal the literal (or atom, for this matter) to be assigned.
	 * @param value the value to be assigned to the atom
	 */
	@Override
	public boolean assign(final int literal, final ThriceTruth value) {
		final int atom = atomOf(literal);
		final ThriceTruth current = assignment.getTruth(atom);

		// NOTE(flowlo): There's a similar check in BasicAssignment.
		// Maybe move cache bookkeeping to the assignment?
		if (current != null && (current.equals(value) || (TRUE.equals(current) && MBT.equals(value)))) {
			return true;
		}

		if (assignment.assign(atom, value, decisionLevel)) {
			cache.add(atom);
			changedAssignments.put(atom, value);
			return true;
		}
		return false;
	}

	private boolean assign(final NoGood noGood, final int index, final ThriceTruth negated) {
		int literal = noGood.getLiteral(index);
		if (!assign(literal, isNegated(literal) ? negated : FALSE)) {
			violated = noGood;
			return false;
		}
		return true;
	}

	/**
	 * Cleans up {@link #cache} and checks all new assignments for possible unit propagations in watched nogoods.
	 */
	@Override
	public boolean propagate() {
		boolean propagated = false;
		if (!cache.isEmpty()) {
			LOGGER.trace("Assignment before propagation: {}", assignment);
		}
		while (!cache.isEmpty()) {
			final int atom = cache.poll();
			final ThriceTruth value = assignment.getTruth(atom);
			boolean atomPropagated = false;

			if (value == MBT || value == FALSE) {
				atomPropagated = propagateMBT(atom, value);
				if (violated != null) {
					return false;
				}
			} else if (value == TRUE) {
				atomPropagated = propagateMBT(atom, MBT);
				if (violated != null) {
					return false;
				}
				atomPropagated |= propagateTrue(atom);
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

	private boolean propagateMBT(final int atom, final ThriceTruth value) {
		boolean propagated = false;
		final Watches<BinaryWatch, WatchedNoGood> w = watches(atom);

		// First iterate through all binary NoGoods, as they are trivial:
		// If one of the two literals is assigned, the NoGood must be
		// unit and an assignment can be synthesized.
		for (BinaryWatch watch : w.b.get(value)) {
			if (!assign(binaries.get(watch.getId()), watch.getOtherLiteralIndex(), MBT)) {
				return false;
			}

			propagated = true;
		}

		for (Iterator<WatchedNoGood> iterator = w.n.get(value).iterator(); iterator.hasNext();) {
			final WatchedNoGood noGood = iterator.next();
			final int assignedPointer = noGood.getAtom(noGood.getPointer(0)) == atom ?  0 : 1;
			final int assignedIndex = noGood.getPointer(assignedPointer);
			final int otherIndex = noGood.getPointer(assignedPointer == 0 ? 1 : 0);

			for (int offset = 1; offset < noGood.size(); offset++) {
				final int index = (assignedIndex + offset) % noGood.size();

				if (index == otherIndex) {
					continue;
				}

				final int literalAtIndex = noGood.getLiteral(index);

				if (!assignment.isAssigned(atomOf(literalAtIndex)) || !assignment.containsRelaxed(literalAtIndex)) {
					noGood.setPointer(assignedPointer, index);
					break;
				}
			}

			// Propagate in case the pointer could not be moved.
			if (noGood.getPointer(assignedPointer) == assignedIndex) {
				if (!assign(noGood, otherIndex, MBT)) {
					return false;
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
			final NoGood noGood = binaries.get(watch.getId());

			if (!assignment.containsRelaxed(noGood, otherLiteralIndex == 1 ? 0 : 1)) {
				continue;
			}

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

				if (!isNegated(literalAtIndex) && !TRUE.equals(assignment.getTruth(atomOf(literalAtIndex)))) {
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

		private BinaryWatch(int id, int otherLiteralIndex) {
			this.id = id;
			this.otherLiteralIndex = otherLiteralIndex;
		}

		private int getId() {
			return id;
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

	/* When you find yourself going down the dark road, take these as your vicious companions.

	private String atomToString(int atom) {
		if (grounder != null) {
			return grounder.atomIdToString(atom);
		}
		return String.valueOf(atom);
	}

	private String literalToString(int literal) {
		return (isNegated(literal) ? "-" : "+") + atomToString(atomOf(literal));
	}

	private <T extends NoGood> String noGoodToString(T noGood) {
		StringBuilder sb = new StringBuilder("{");

		for (Iterator<Integer> iterator = noGood.iterator(); iterator.hasNext();) {
			sb.append(literalToString(iterator.next()));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");
		return sb.toString();
	}
	 */
}