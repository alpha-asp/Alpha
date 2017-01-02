package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.ReadableAssignment;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class BridgedGrounder extends AbstractGrounder {
	protected final Bridge[] bridges;

	protected BridgedGrounder(ParsedProgram program, java.util.function.Predicate<Predicate> filter, Bridge... bridges) {
		super(program, filter);
		this.bridges = bridges;
	}

	protected BridgedGrounder(ParsedProgram program, Bridge... bridges) {
		super(program);
		this.bridges = bridges;
	}

	protected Set<NoGood> collectExternalNogoods(ReadableAssignment assignment, AtomStore atomStore, Pair<Map<Integer, Integer>, Map<Integer, Integer>> newChoiceAtoms, IntIdGenerator choiceAtomsGenerator) {
		Set<NoGood> collectedNoGoods = new HashSet<>();

		for (Bridge bridge : bridges) {
			collectedNoGoods.addAll(bridge.getNoGoods(assignment, atomStore, newChoiceAtoms, choiceAtomsGenerator));
		}

		return collectedNoGoods;
	}
}
