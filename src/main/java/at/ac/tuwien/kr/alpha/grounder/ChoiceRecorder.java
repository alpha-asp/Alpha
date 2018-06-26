package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.NoGood;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.common.Literals.negateLiteral;
import static at.ac.tuwien.kr.alpha.grounder.atoms.ChoiceAtom.off;
import static at.ac.tuwien.kr.alpha.grounder.atoms.ChoiceAtom.on;
import static java.util.Collections.emptyList;

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

	public List<NoGood> generate(final List<Integer> posLiterals, final List<Integer> negLiterals, final int bodyRepresentingLiteral) {
		// Obtain an ID for this new choice.
		final int choiceId = ID_GENERATOR.getNextId();

		final List<NoGood> noGoods = generateNeg(choiceId, negLiterals, bodyRepresentingLiteral);
		noGoods.add(generatePos(choiceId, posLiterals, bodyRepresentingLiteral));

		return noGoods;
	}

	private NoGood generatePos(final int choiceId, List<Integer> posLiterals, final int bodyRepresentingLiteral) {
		final int choiceOnLiteral = atomToLiteral(atomStore.add(on(choiceId)));
		newChoiceAtoms.getLeft().put(atomOf(bodyRepresentingLiteral), atomOf(choiceOnLiteral));

		return NoGood.fromBody(posLiterals, emptyList(), choiceOnLiteral);
	}

	private List<NoGood> generateNeg(final int choiceId, List<Integer> negLiterals, final int bodyRepresentingLiteral)  {
		final int choiceOffLiteral = atomToLiteral(atomStore.add(off(choiceId)));
		newChoiceAtoms.getRight().put(atomOf(bodyRepresentingLiteral), atomOf(choiceOffLiteral));

		final List<NoGood> noGoods = new ArrayList<>(negLiterals.size() + 1);
		for (Integer negLiteral : negLiterals) {
			// Choice is off if any of the negative atoms is assigned true,
			// hence we add one nogood for each such atom.
			noGoods.add(NoGood.headFirst(negateLiteral(choiceOffLiteral), negLiteral));
		}
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
