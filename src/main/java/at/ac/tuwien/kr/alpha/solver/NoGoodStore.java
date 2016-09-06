package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.nogood.WatchedMBTNoGood;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;

class NoGoodStore {
	private final Assignment<ThriceTruth> assignment;

	private final Set<NoGood> unaries = new HashSet<>();
	private final Map<Integer, Collection<NoGood>> binaries = new HashMap<>();
	private final Map<Integer, WatchedNoGoods> naries = new HashMap<>();

	private final Queue<NoGood> unariesToCheck = new ArrayDeque<>();
	private final Queue<NoGood> binariesToCheck = new ArrayDeque<>();
	private final Queue<WatchedMBTNoGood> nariesToCheck = new ArrayDeque<>();

	NoGoodStore(Assignment<ThriceTruth> assignment) {
		this.assignment = assignment;
	}

	void clear() {
		assignment.clear();

		unaries.clear();
		binaries.clear();
		naries.clear();

		unariesToCheck.clear();
		binariesToCheck.clear();
		nariesToCheck.clear();
	}

	public void add(NoGood noGood) {
		if (noGood.size() == 1) {
			unaries.add(noGood);
			unariesToCheck.add(noGood);
		} else if (noGood.size() == 2) {
			addBinary(noGood.getLiteral(0), noGood);
			addBinary(noGood.getLiteral(1), noGood);
			binariesToCheck.add(noGood);
		} else {
			nariesToCheck.add(addWatched(noGood));
		}
	}

	private void addBinary(int literal, NoGood noGood) {
		binaries.computeIfAbsent(atomOf(literal), k ->  new LinkedList<>()).add(noGood);
	}

	private WatchedMBTNoGood addWatched(NoGood noGood) {
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
		WatchedMBTNoGood wng = new WatchedMBTNoGood(noGood, a, b, 0);
		addByIndex(wng, a);
		addByIndex(wng, b);
		return wng;
	}

	private void addByIndex(WatchedMBTNoGood watchedNoGood, int i) {
		final int literal = watchedNoGood.getNoGood().getLiteral(i);

		// TODO: Correctly assign truth.
		final ThriceTruth truth = ThriceTruth.FALSE;

		naries.computeIfAbsent(literal, k -> new WatchedNoGoods()).get(truth).add(watchedNoGood);
	}

	private void assign(int atom, ThriceTruth value, int decisionLevel) {
		// TODO: Pass decision level.
		assignment.assign(atom, value, decisionLevel);

		// TODO: Optimize w.r.t. value.
		if (binaries.containsKey(atom)) {
			binariesToCheck.addAll(binaries.get(atom));
		}

		if (naries.containsKey(atom)) {
			nariesToCheck.addAll(naries.get(atom).get(value));
		}
	}

	public void propagate(int decisionLevel) {
		while (!unariesToCheck.isEmpty()) {
			final NoGood noGood = unariesToCheck.poll();
			final int literal = noGood.getLiteral(0);
			if (noGood.hasHead()) {
				// This is a fact.
				assign(atomOf(literal), isNegated(literal) ? ThriceTruth.TRUE : ThriceTruth.FALSE, decisionLevel);
			} else {
				assign(atomOf(literal), isNegated(literal) ? ThriceTruth.MBT : ThriceTruth.FALSE, decisionLevel);
			}
		}

		while (!binariesToCheck.isEmpty()) {
			final NoGood noGood = binariesToCheck.poll();

			final int firstLiteral = noGood.getLiteral(0);
			final int secondLiteral = noGood.getLiteral(1);

			final int firstAtom = atomOf(firstLiteral);
			final int secondAtom = atomOf(secondLiteral);

			final boolean firstAssigned = assignment.isAssigned(firstAtom);
			final boolean secondAssigned = assignment.isAssigned(secondAtom);

			boolean someAssigned = assignment.isAssigned(firstAtom) || assignment.isAssigned(secondAtom);

			if (!someAssigned || firstAssigned == secondAssigned) {
				continue;
			}

			if (!firstAssigned) {
				assign(firstAtom, isNegated(firstLiteral) ? ThriceTruth.MBT : ThriceTruth.FALSE, decisionLevel);
			} else {
				assign(secondAtom, isNegated(secondLiteral) ? ThriceTruth.MBT : ThriceTruth.FALSE, decisionLevel);
			}
		}

		while (!nariesToCheck.isEmpty()) {
			// Check if NoGood is unit.
			// If it is unit, find what atom will be assigned.
			// Alter the assignment of the atom to the value that was derived.
			// Find the corresponding WatchedNoGoods for the atom and assignment in
			// naries and add all of the to checkingQueue.

			final WatchedMBTNoGood noGood = nariesToCheck.poll();

			final int firstLiteral = noGood.getNoGood().getLiteral(noGood.getA());
			final int secondLiteral = noGood.getNoGood().getLiteral(noGood.getB());

			final int firstAtom = atomOf(noGood.getNoGood().getLiteral(noGood.getA()));
			final int secondAtom = atomOf(noGood.getNoGood().getLiteral(noGood.getB()));

			final boolean firstAssigned = assignment.isAssigned(firstAtom);
			final boolean secondAssigned = assignment.isAssigned(secondAtom);

			boolean someAssigned = assignment.isAssigned(firstAtom) || assignment.isAssigned(secondAtom);

			if (!someAssigned || firstAssigned == secondAssigned) {
				continue;
			}

			if (!firstAssigned) {
				assign(firstAtom, isNegated(firstLiteral) ? ThriceTruth.MBT : ThriceTruth.FALSE, decisionLevel);
			} else {
				assign(secondAtom, isNegated(secondLiteral) ? ThriceTruth.MBT : ThriceTruth.FALSE, decisionLevel);
			}
		}
	}

	private static class WatchedNoGoods {
		private Set<WatchedMBTNoGood> positive = new HashSet<>();
		private Set<WatchedMBTNoGood> negative = new HashSet<>();
		private Set<WatchedMBTNoGood> mustBeTrue = new HashSet<>();

		public Set<WatchedMBTNoGood> get(ThriceTruth truth) {
			switch (truth) {
				case FALSE:
					return negative;
				case TRUE:
					return positive;
				case MBT:
					return mustBeTrue;
			}
			throw new IllegalArgumentException("invalid truth value");
		}
	}
}