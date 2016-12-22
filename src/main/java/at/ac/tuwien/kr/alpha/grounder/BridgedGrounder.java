package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.ImmutableAssignment;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;

import java.util.HashSet;
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

	protected Set<NoGood> collectExternal(ImmutableAssignment assignment, AtomStore atomStore) {
		Set<NoGood> collectedNoGoods = new HashSet<>();

		for (Bridge bridge : bridges) {
			collectedNoGoods.addAll(bridge.getNoGoods(assignment, atomStore));
		}

		return collectedNoGoods;
	}
}
