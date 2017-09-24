package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;

import java.util.List;

public class Program {
	private final List<Atom> facts;
	private final List<NonGroundRule> rules;
	private final List<NonGroundRule> constraints;

	public Program(List<Atom> facts, List<NonGroundRule> rules, List<NonGroundRule> constraints) {
		this.facts = facts;
		this.rules = rules;
		this.constraints = constraints;
	}

	public List<Atom> getFacts() {
		return facts;
	}

	public List<NonGroundRule> getRules() {
		return rules;
	}

	public List<NonGroundRule> getConstraints() {
		return constraints;
	}
}
