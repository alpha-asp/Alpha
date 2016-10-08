package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;

class BasicNoGoodStore implements NoGoodStore<ThriceTruth> {
	private final Assignment assignment;

	private final Map<Integer, Watches<Integer, WatchedNoGood>> watches = new HashMap<>();

	private final Queue<Integer> cache = new ArrayDeque<>();

	private Map<Integer, ThriceTruth> changedAssignments = new HashMap<>();

	private int decisionLevel;

	BasicNoGoodStore(Assignment assignment) {
		this.assignment = assignment;
		this.decisionLevel = 0;
	}

	public void setDecisionLevel(int decisionLevel) {
		this.decisionLevel = decisionLevel;
	}

	void clear() {
		assignment.clear();
		cache.clear();
		watches.clear();
	}

	private Watches<Integer, WatchedNoGood> watches(int literal) {
		return watches.computeIfAbsent(atomOf(literal), k -> new Watches<>());
	}

	@Override
	public void add(int id, NoGood noGood) {
		if (noGood.size() == 1) {
			addUnary(noGood);
		} else {
			if (noGood.size() == 2) {
				addAndWatchBinary(noGood);
			} else {
				addAndWatch(noGood);
			}
		}
	}

	@Override
	public boolean isNoGoodViolated() {
		return false;
	}

	/**
	 * Takes a noGood containing only a single literal and translates it into an assignment (because it
	 * is trivially unit). Still, a check for conflict is performed.
	 */
	private void addUnary(final NoGood noGood) {
		final int literal = noGood.getLiteral(0);

		// TODO: Do not throw exception but start backtracking.
		if (assignment.contains(literal)) {
			throw new ConflictingNoGoodException(noGood);
		}

		assign(literal, isNegated(literal) ? noGood.hasHead() ? TRUE : MBT : FALSE);
	}

	private void addAndWatchBinary(final NoGood noGood) {
		final int a = noGood.getLiteral(0);
		final int b = noGood.getLiteral(1);

		if (assignment.contains(a) && assignment.contains(b)) {
			throw new ConflictingNoGoodException(noGood);
		}

		watches(a).b.get(isNegated(a) ? FALSE : MBT).add(b);
		watches(b).b.get(isNegated(b) ? FALSE : MBT).add(a);

		if (noGood.hasHead()) {
			final int head = noGood.getHead();
			final int body = head == 0 ? 1 : 0;

			if (!isNegated(body)) {
				watches(noGood.getLiteral(body)).b.get(TRUE).add(noGood.getAtom(head));

				if (TRUE.equals(assignment.getTruth(noGood.getAtom(body)))) {
					final int headLiteral = noGood.getLiteral(head);
					if (!assignment.isAssigned(atomOf(headLiteral))) {
						assign(headLiteral, isNegated(headLiteral) ? TRUE : FALSE);
					}
					return;
				}
			}
		}

		boolean aAssigned = assignment.isAssigned(atomOf(noGood.getLiteral(0)));
		boolean bAssigned = assignment.isAssigned(atomOf(noGood.getLiteral(1)));

		if (aAssigned ^ bAssigned) {
			int unassignedLiteral = aAssigned ? b : a;
			int assignedLiteral = aAssigned ? a : b;
			if (isNegated(assignedLiteral) == FALSE.equals(assignment.getTruth(atomOf(assignedLiteral)))) {
				assign(unassignedLiteral, isNegated(unassignedLiteral) ? MBT : FALSE);
			}
		}
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
	private void addAndWatch(final NoGood noGood) {
		boolean isViolated = true;
		int[] unassigned = new int[] {-1, -1};
		int positive = -1;
		int priority = -1;

		for (int i = 0; i < noGood.size(); i++) {
			final int literal = noGood.getLiteral(i);
			final ThriceTruth value = assignment.getTruth(atomOf(literal));

			if (!isNegated(literal) && noGood.hasHead() && noGood.getHead() != i) {
				int candidatePriority = toPriority(noGood.getAtom(i));
				if (noGood.hasHead() && i != noGood.getHead() && candidatePriority > priority) {
					positive = i;
					priority = candidatePriority;
				}
			}

			if (value == null) {
				if (unassigned[0] == -1) {
					unassigned[0] = i;
				} else if (unassigned[1] == -1) {
					unassigned[1] = i;
				}

				// A noGood that has an unassigned literal cannot be violated.
				isViolated = false;
			} else {
				// The literal we're looking at is assigned.
				if (!assignment.contains(literal)) {
					isViolated = false;
				}
			}
		}

		if (isViolated) {
			// TODO: Do not throw exception but start backtracking.
			throw new ConflictingNoGoodException(noGood);
		}

		if (positive != -1 && noGood.hasHead() && TRUE.equals(assignment.getTruth(noGood.getAtom(positive)))) {
			final int head = noGood.getLiteral(noGood.getHead());
			if (!assignment.isAssigned(atomOf(head))) {
				assign(head, isNegated(head) ? TRUE : FALSE);
			}
		} else if (unassigned[0] != -1 && unassigned[1] == -1) {
			int unassignedLiteral = noGood.getLiteral(unassigned[0]);
			assign(unassignedLiteral, isNegated(unassignedLiteral) ? MBT : FALSE);
		}

		if (unassigned[0] == -1) {
			unassigned[0] = 0;
		}

		if (unassigned[1] == -1) {
			unassigned[1] = noGood.size() / 2;
		}

		watch(new WatchedNoGood(noGood, unassigned[0], unassigned[1], positive));
	}

	/**
	 * Adds the passed {@link WatchedNoGood} to {@link #watches} for the atoms pointed at respectively.
	 * @param wng the watched nogood to add to {@link #watches}.
	 */
	private void watch(final WatchedNoGood wng) {
		for (int i = 0; i < 2; i++) {
			final int literal = wng.getLiteral(wng.getPointer(i));
			watches(literal).n.get(isNegated(literal) ? FALSE : MBT).add(wng);
		}
		// Only look at the third pointer if it points at a legal index. It might not
		// in case the nogood has no head or no positive literal.
		if (wng.getPointer(2) != -1) {
			watches(wng.getLiteral(wng.getPointer(2))).n.get(TRUE).add(wng);
		}
	}

	/**
	 * Passes on the assignment of a given literal and value to {@link #assignment} and adds the corresponding atom
	 * to {@link #cache} for checking later on.
	 * @param literal the literal (or atom, for this matter) to be assigned.
	 * @param value the value to be assigned to the atom
	 */
	@Override
	public void assign(final int literal, final ThriceTruth value) {
		final int atom = atomOf(literal);
		assignment.assign(atom, value, decisionLevel);
		cache.add(atom);
		changedAssignments.put(atom, value);
	}

	/**
	 * Cleans up {@link #cache} and checks all new assignments for possible unit propagations in watched nogoods.
	 */
	@Override
	public boolean propagate() {
		boolean propagated = false;
		while (!cache.isEmpty()) {
			final int atom = cache.poll();
			final ThriceTruth value = assignment.getTruth(atom);

			if (value == MBT || value == FALSE) {
				propagated |= propagateMBT(atom, value);
			} else if (value == TRUE) {
				propagated |= propagateTrue(atom);
			}
		}
		return propagated;
	}

	@Override
	public Map<Integer, ThriceTruth> getChangedAssignments() {
		Map<Integer, ThriceTruth> result = Collections.unmodifiableMap(new HashMap<>(changedAssignments));
		changedAssignments.clear();
		return result;
	}

	private boolean propagateMBT(final int atom, final ThriceTruth value) {
		boolean propagated = false;
		final Watches<Integer, WatchedNoGood> w = watches(atom);

		// First iterate through all binary NoGoods, as they are trivial:
		// If one of the two literals is assigned, the NoGood must be
		// unit and an assignment can be synthesized.
		final Set<Integer> binaries = w.b.get(value);
		for (Integer literal : binaries) {
			propagated = true;
			if (!assignment.isAssigned(atomOf(literal))) {
				assign(literal, isNegated(literal) ? MBT : FALSE);
			}
		}

		for (Iterator<WatchedNoGood> iterator = w.n.get(value).iterator(); iterator.hasNext();) {
			final WatchedNoGood noGood = iterator.next();
			final int assignedPointer = noGood.getAtom(noGood.getPointer(0)) == atom ?  0 : 1;
			final int assignedIndex = noGood.getPointer(assignedPointer);
			final int otherIndex = noGood.getPointer(assignedPointer == 0 ? 1 : 0);

			boolean moved = false;
			for (int offset = 1; offset < noGood.size(); offset++) {
				final int index = (assignedIndex + offset) % noGood.size();
				final int otherAtom = noGood.getAtom(index);
				if (index == otherIndex) {
					continue;
				}
				if (!assignment.isAssigned(otherAtom)) {
					moved = true;
					noGood.setPointer(assignedPointer, index);
					break;
				}
			}

			// Propagate in case the pointer could not be moved.
			if (!moved) {
				final int otherLiteral = noGood.getLiteral(otherIndex);
				assign(otherLiteral, isNegated(otherLiteral) ? MBT : FALSE);
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
		final Watches<Integer, WatchedNoGood> w = watches(atom);

		final Set<Integer> binaries = w.b.get(TRUE);
		for (Integer literal : binaries) {
			propagated = true;
			ThriceTruth currentAssignment = assignment.getTruth(atomOf(literal));

			if (isNegated(literal) && currentAssignment == null) {
				assign(literal, FALSE);
			} else if (!isNegated(literal) && (MBT.equals(currentAssignment) || currentAssignment == null)) {
				assign(literal, TRUE);
			}
		}

		final Set<WatchedNoGood> naries = w.n.get(TRUE);
		for (Iterator<WatchedNoGood> iterator = naries.iterator(); iterator.hasNext();) {
			final WatchedNoGood noGood = iterator.next();

			int thirdPointer = noGood.getPointer(2);
			int priority = -1;

			for (int offset = 1; offset < noGood.size(); offset++) {
				final int index = (noGood.getPointer(2) + offset) % noGood.size();
				if (index == noGood.getHead()) {
					continue;
				}
				final ThriceTruth assignmentAtIndex = assignment.getTruth(noGood.getAtom(index));
				if (isNegated(noGood.getLiteral(index))) {
					continue;
				}
				if (assignmentAtIndex == TRUE) {
					continue;
				}
				final int itemPriority = toPriority(noGood.getAtom(index));
				if (itemPriority > priority) {
					thirdPointer = index;
					priority = itemPriority;
				}
			}

			// If we did not find anything with priority, we cannot move the pointer. So propagate
			// (meaning assign TRUE to the head)!
			if (priority == -1) {
				assign(noGood.getLiteral(noGood.getHead()), TRUE);
				propagated = true;
				continue;
			}

			// The pointer can be moved to thirdPointer.
			noGood.setPointer(2, thirdPointer);
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

	/**
	 * A simple data structure to encapsulate watched objects by truth value tailored to {@link ThriceTruth}. It
	 * holds three separate sets that are used to refer to propagation based on assignment of one of the three truth
	 * values.
	 * @param <T> type used for referencing.
	 */
	private class ThriceSet<T> {
		private Set<T> pos = new HashSet<>();
		private Set<T> neg = new HashSet<>();
		private Set<T> mbt = new HashSet<>();

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
	private class Watches<B, N> {
		final ThriceSet<B> b = new ThriceSet<>();
		final ThriceSet<N> n = new ThriceSet<>();
	}
}