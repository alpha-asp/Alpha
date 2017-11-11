package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.grounder.atoms.ChoiceAtom;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class ChoiceRecorder {
	private static final IntIdGenerator ID_GENERATOR = new IntIdGenerator();

	private final AtomStore atomStore;
	private Pair<Map<Integer, Integer>, Map<Integer, Integer>> newChoiceAtoms = new ImmutablePair<>(new LinkedHashMap<>(), new LinkedHashMap<>());

	public ChoiceRecorder(AtomStore atomStore) {
		this.atomStore = atomStore;
	}

	public Pair<Map<Integer, Integer>, Map<Integer, Integer>> getAndReset() {
		Pair<Map<Integer, Integer>, Map<Integer, Integer>> currentChoiceAtoms = newChoiceAtoms;
		newChoiceAtoms = new ImmutablePair<>(new LinkedHashMap<>(), new LinkedHashMap<>());
		return currentChoiceAtoms;
	}

	public List<NoGood> generate(List<Integer> pos, List<Integer> neg, int bodyAtom) {
		List<NoGood> noGoods = new ArrayList<>(neg.size() + 1);
		Map<Integer, Integer> newChoiceOn = newChoiceAtoms.getLeft();
		Map<Integer, Integer> newChoiceOff = newChoiceAtoms.getRight();
		// Choice is on the body representing atom

		// ChoiceOn if all positive body atoms are satisfied
		int[] choiceOnLiterals = new int[pos.size() + 1];
		int i = 1;
		for (Integer atomId : pos) {
			choiceOnLiterals[i++] = atomId;
		}

		int choiceId = ID_GENERATOR.getNextId();
		Atom choiceOnAtom = ChoiceAtom.on(choiceId);
		int choiceOnAtomIdInt = atomStore.add(choiceOnAtom);
		choiceOnLiterals[0] = -choiceOnAtomIdInt;
		// Add corresponding NoGood and ChoiceOn
		// ChoiceOn and ChoiceOff NoGoods avoid MBT and directly set to true, hence the rule head pointer.
		noGoods.add(NoGood.headFirst(choiceOnLiterals));
		newChoiceOn.put(bodyAtom, choiceOnAtomIdInt);

		// ChoiceOff if some negative body atom is contradicted
		Atom choiceOffAtom = ChoiceAtom.off(choiceId);
		int choiceOffAtomIdInt = atomStore.add(choiceOffAtom);
		for (Integer negAtomId : neg) {
			// Choice is off if any of the negative atoms is assigned true, hence we add one NoGood for each such atom.
			noGoods.add(NoGood.headFirst(-choiceOffAtomIdInt, negAtomId));
		}
		newChoiceOff.put(bodyAtom, choiceOffAtomIdInt);

		return noGoods;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[enablers: ");
		for (Map.Entry<Integer, Integer> enablers : newChoiceAtoms.getLeft().entrySet()) {
			sb.append(enablers.getKey()).append("/").append(enablers.getValue()).append(", ");
		}
		sb.append(" disablers: ");
		for (Map.Entry<Integer, Integer> disablers : newChoiceAtoms.getRight().entrySet()) {
			sb.append(disablers.getKey()).append("/").append(disablers.getValue());
		}
		return sb.append("]").toString();
	}
}
