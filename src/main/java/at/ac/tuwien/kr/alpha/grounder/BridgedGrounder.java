package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
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

	protected Set<NonGroundRule> collectExternalRules(Assignment assignment, AtomStore atomStore, IntIdGenerator intIdGenerator) {
		Set<NonGroundRule> collectedRules = new HashSet<>();

		for (Bridge bridge : bridges) {
			collectedRules.addAll(bridge.getRules(assignment, atomStore, intIdGenerator));
		}

		return collectedRules;
	}
}
