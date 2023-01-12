package at.ac.tuwien.kr.alpha.core.programs;

import at.ac.tuwien.kr.alpha.api.programs.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.commons.util.Util;

import java.util.Collections;
import java.util.List;

/**
 * The parent type for all kinds of programs. Defines a program's basic structure (facts + rules + inlineDirectives)
 *
 * @param <R> the type of rule a program permits. This needs to be determined by implementations based on which syntax constructs an
 *            implementation permits
 *            Copyright (c) 2019, the Alpha Team.
 */
public abstract class AbstractProgram<R extends Rule<? extends Head>> {

	private final List<R> rules;
	private final List<Atom> facts;
	private final InlineDirectives inlineDirectives;
	private final boolean containsWeakConstraints;

	public AbstractProgram(List<R> rules, List<Atom> facts, InlineDirectives inlineDirectives, boolean containsWeakConstraints) {
		this.rules = rules;
		this.facts = facts;
		this.inlineDirectives = inlineDirectives;
		this.containsWeakConstraints = containsWeakConstraints;
	}

	public List<R> getRules() {
		return Collections.unmodifiableList(rules);
	}

	public List<Atom> getFacts() {
		return Collections.unmodifiableList(facts);
	}

	public InlineDirectives getInlineDirectives() {
		return inlineDirectives;
	}

	@Override
	public String toString() {
		final String ls = System.lineSeparator();
		final String result = facts.isEmpty() ? "" : Util.join("", facts, "." + ls, "." + ls);
		if (rules.isEmpty()) {
			return result;
		}
		return Util.join(result, rules, ls, ls);
	}

	public boolean containsWeakConstraints() {
		return containsWeakConstraints;
	}
}
