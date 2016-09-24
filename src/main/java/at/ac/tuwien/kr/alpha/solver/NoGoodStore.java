package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.nogood.WatchedNoGood;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

class NoGoodStore {
	private final Assignment<ThriceTruth> assignment;

	private final Map<Integer, Watches<Integer, WatchedNoGood>> watches = new HashMap<>();

	private final Queue<Integer> cache = new ArrayDeque<>();

	private int decisionLevel;

	NoGoodStore(Assignment<ThriceTruth> assignment) {
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

	public void add(NoGood noGood) {
		if (noGood.size() == 1) {
			addUnary(noGood);
		} else if (noGood.size() == 2) {
			addBinary(noGood);
		} else {
			addWatched(noGood);
		}
	}

	private void addUnary(final NoGood noGood) {
		final int literal = noGood.getLiteral(0);

		if (assignment.contains(literal)) {
			throw new ConflictingNoGoodException(noGood);
		}

		assign(literal, isNegated(literal) ? noGood.hasHead() ? TRUE : MBT : FALSE);
	}

	private void addBinary(final NoGood noGood) {
		final int a = noGood.getLiteral(0);
		final int b = noGood.getLiteral(1);

		if (!assignment.isAssigned(atomOf(a)) && !assignment.isAssigned(atomOf(b))) {
			watches(a).binary.get(isNegated(a) ? FALSE : MBT).add(b);
			watches(b).binary.get(isNegated(b) ? FALSE : MBT).add(a);
		} else {
			eat(noGood);
		}
	}

	private boolean eat(final NoGood noGood) {
		int lastUnassignedLiteral = 0;
		boolean isUnit = false;
		boolean isViolated = true;

		for (Integer literal : noGood) {
			ThriceTruth value = assignment.get(atomOf(literal));
			if (value == null) {
				isUnit = lastUnassignedLiteral == 0;
				lastUnassignedLiteral = literal;
				isViolated = false;
			} else if (!assignment.contains(literal)) {
				isUnit = false;
				isViolated = false;
			}
		}

		if (isViolated) {
			throw new ConflictingNoGoodException(noGood);
		}

		if (isUnit) {
			assign(lastUnassignedLiteral, isNegated(lastUnassignedLiteral) ? MBT : FALSE);
			return true;
		}

		return false;
	}

	private WatchedNoGood addWatched(final NoGood noGood) {
		if (eat(noGood)) {
			return null;
		}

		Comparator<Integer> comp = Comparator.comparingInt(assignment::toPriority);

		int a = 1;
		int b = 0;

		for (int i = 0; i < noGood.size(); i++) {
			final int atom = noGood.getAtom(i);
			if (comp.compare(atom, noGood.getAtom(a)) > 0) {
				b = a;
				a = i;
			} else if (comp.compare(atom, noGood.getAtom(b)) > 0) {
				b = i;
			}
		}

		// 1. Find two indices s.t.
		// 1.a. if there are more than one unassigned literals (atoms), choose any two of them.
		// 1.b. if there is only one unassigned literal:
		// 1.b.I Either the noGood is unit (propagate).
		// 1.b.II Or, the assignment satisfies the nogood, then choose:
		//        the two with the highest decision level for their assignments. Improvement: Define an ordering
		//        so that we choose the literal that satisfies the NoGood and has the smallest decision level
		//        for it's assignment and the one with the highest decision level.
		// 1.c. if there is no unassigned literal, check if the NoGood is violated
		// 1.c.I if the noGood is violated, start conflict analysis and backtracking.
		// 1.c.II else, proceed as in 1.b.II

		// TODO: Set c.
		WatchedNoGood wng = new WatchedNoGood(noGood, a, b);
		addByIndex(wng, a);
		addByIndex(wng, b);
		return wng;
	}

	private void addByIndex(WatchedNoGood watchedNoGood, int i) {
		final int literal = watchedNoGood.getLiteral(i);
		watches(literal).nary.get(isNegated(literal) ? FALSE : MBT).add(watchedNoGood);
	}

	public void assign(int literal, ThriceTruth value) {
		final int atom = atomOf(literal);
		assignment.assign(atom, value, decisionLevel);
		cache.add(atom);
	}

	public void propagate() {
		while (!cache.isEmpty()) {
			final int atom = cache.poll();
			final ThriceTruth value = assignment.get(atom);

			if (value == MBT || value == FALSE) {
				propagateMBT(atom, value);
			} else if (value == TRUE) {
				propagateTrue(atom, value);
			}
		}
	}

	private void propagateMBT(final int atom, final ThriceTruth value) {
		final Watches<Integer, WatchedNoGood> w = watches(atom);

		for (Integer literal : w.binary.get(value)) {
			assign(literal, isNegated(literal) ? MBT : FALSE);
		}

		for (WatchedNoGood noGood : w.nary.get(value)) {
			final int assigned = noGood.getAtom(noGood.getPointer(0)) == atom ?  0 : 1;
			final int other = assigned == 0 ? 1 : 0;
			final int otherLiteral = noGood.getLiteral(noGood.getPointer(other));

			if (!assignment.isAssigned(atomOf(otherLiteral))) {
				assign(otherLiteral, isNegated(otherLiteral) ? MBT : FALSE);
				continue;
			}
/*
			Comparator<Integer> comp = Comparator.comparingInt(assignment::toPriority);

			int max = 0;
			for (int i = 1; i < noGood.size(); i++) {
				final int otherAtom = noGood.getAtom(i);
				if (i == noGood.getPointer(other)) {
					continue;
				}
				if (comp.compare(otherAtom, noGood.getAtom(max)) > 0) {
					max = i;
				}
			}

			if (assignment.get(noGood.getAtom(max)) != null) {
				//
				continue;
			}

			noGood.setPointer(assigned, max);*/
		}
	}

	private void propagateTrue(final int atom, final ThriceTruth value) {
	}

	private static class ThriceSet<T> {
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
	private static class Watches<B, N> {
		final ThriceSet<B> binary = new ThriceSet<>();
		final ThriceSet<N> nary = new ThriceSet<>();
	}
}