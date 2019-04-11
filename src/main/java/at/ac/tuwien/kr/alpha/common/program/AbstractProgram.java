package at.ac.tuwien.kr.alpha.common.program;

import java.util.Collections;
import java.util.List;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.rule.AbstractRule;
import at.ac.tuwien.kr.alpha.common.rule.head.Head;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;

public abstract class AbstractProgram<R extends AbstractRule<? extends Head>> {

	private final List<R> rules;
	private final List<Atom> facts;
	private final InlineDirectives inlineDirectives;

	public AbstractProgram(List<R> rules, List<Atom> facts, InlineDirectives inlineDirectives) {
		this.rules = rules;
		this.facts = facts;
		this.inlineDirectives = inlineDirectives;
	}

	public List<R> getRules() {
		return Collections.unmodifiableList(this.rules);
	}

	public List<Atom> getFacts() {
		return Collections.unmodifiableList(this.facts);
	}

	public InlineDirectives getInlineDirectives() {
		return this.inlineDirectives;
	}

	@Override
	public String toString() {
		final String ls = System.lineSeparator();
		final String result = Util.join("", this.facts, "." + ls, "." + ls);
		if (this.rules.isEmpty()) {
			return result;
		}
		return Util.join(result, this.rules, ls, ls);
	}

}
