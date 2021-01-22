package at.ac.tuwien.kr.alpha.core.programs;

import java.util.Collections;
import java.util.Set;

import at.ac.tuwien.kr.alpha.api.program.Atom;
import at.ac.tuwien.kr.alpha.api.program.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.program.Program;
import at.ac.tuwien.kr.alpha.core.rules.AbstractRule;
import at.ac.tuwien.kr.alpha.core.rules.heads.Head;
import at.ac.tuwien.kr.alpha.core.util.Util;

/**
 * The parent type for all kinds of programs. Defines a program's basic structure (facts + rules + inlineDirectives)
 *
 * @param <R> the type of rule a program permits. This needs to be determined by implementations based on which syntax constructs an
 *            implementation permits
 *            Copyright (c) 2019-2021, the Alpha Team.
 */
public abstract class AbstractProgram<R extends AbstractRule<? extends Head>> implements Program {

	private final Set<Atom> facts;
	private final InlineDirectives inlineDirectives;

	protected AbstractProgram(Set<Atom> facts, InlineDirectives inlineDirectives) {
		this.facts = Collections.unmodifiableSet(facts);
		this.inlineDirectives = inlineDirectives;
	}

	@Override
	public Set<Atom> getFacts() {
		return this.facts;
	}

	@Override
	public String toString() {
		final String ls = System.lineSeparator();
		final String result = this.getFacts().isEmpty() ? "" : Util.join("", this.getFacts(), "." + ls, "." + ls);
		if (this.getRules().isEmpty()) {
			return result;
		}
		return Util.join(result, this.getRules(), ls, ls);
	}

}
