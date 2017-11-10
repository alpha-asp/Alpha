package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;

import java.util.Collections;
import java.util.List;

import static at.ac.tuwien.kr.alpha.Util.join;

/**
 * Alpha-internal representation of an ASP program, i.e., a set of ASP rules.
 * Copyright (c) 2017, the Alpha Team.
 */
public class Program {
	public static final Program EMPTY = new Program(Collections.emptyList(), Collections.emptyList());

	private final List<Rule> rules;
	private final List<Atom> facts;

	public Program(List<Rule> rules, List<Atom> facts) {
		this.rules = rules;
		this.facts = facts;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public List<Atom> getFacts() {
		return facts;
	}

	public void accumulate(Program program) {
		rules.addAll(program.rules);
		facts.addAll(program.facts);
	}

	@Override
	public String toString() {
		final String ls = System.lineSeparator();
		return join(
			join(
				"",
				facts,
				"." + ls,
				ls
			),
			rules,
			ls,
			""
		);
	}
}
