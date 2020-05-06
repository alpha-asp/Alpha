/**
 * Copyright (c) 2019, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.common.program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.externals.Externals;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;

/**
 * Alpha-internal representation of an ASP program, i.e., a set of ASP rules.
 * 
 * Copyright (c) 2017-2019, the Alpha Team.
 */
public class InputProgram extends AbstractProgram<BasicRule> {

	public static final InputProgram EMPTY = new InputProgram(Collections.emptyList(), Collections.emptyList(), new InlineDirectives());

	public InputProgram(List<BasicRule> rules, List<Atom> facts, InlineDirectives inlineDirectives) {
		super(rules, facts, inlineDirectives);
	}

	public InputProgram() {
		super(new ArrayList<>(), new ArrayList<>(), new InlineDirectives());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(InputProgram prog) {
		return new Builder(prog);
	}

	/**
	 * Builder to be used instead of accumulate(), ensuring that an @{link AbstractProgram} is immutable
	 */
	public static class Builder {

		private List<BasicRule> rules = new ArrayList<>();
		private List<Atom> facts = new ArrayList<>();
		private InlineDirectives inlineDirectives = new InlineDirectives();

		public Builder(InputProgram prog) {
			this.addRules(prog.getRules());
			this.addFacts(prog.getFacts());
			this.addInlineDirectives(prog.getInlineDirectives());
		}

		public Builder() {

		}

		public Builder addRules(List<BasicRule> rules) {
			this.rules.addAll(rules);
			return this;
		}

		public Builder addRule(BasicRule r) {
			this.rules.add(r);
			return this;
		}

		public Builder addFacts(List<Atom> facts) {
			this.facts.addAll(facts);
			return this;
		}

		public Builder addFact(Atom fact) {
			this.facts.add(fact);
			return this;
		}

		public <T extends Comparable<T>> Builder addExternalFacts(Class<T> clazz, Collection<T> extFacts) {
			return this.addFacts(Externals.asFacts(clazz, extFacts));
		}

		public Builder addInlineDirectives(InlineDirectives inlineDirectives) {
			this.inlineDirectives.accumulate(inlineDirectives);
			return this;
		}

		public Builder accumulate(InputProgram prog) {
			return this.addRules(prog.getRules()).addFacts(prog.getFacts()).addInlineDirectives(prog.getInlineDirectives());
		}

		public InputProgram build() {
			return new InputProgram(this.rules, this.facts, this.inlineDirectives);
		}
	}

	public boolean containsAggregates() {
		for (BasicRule r : this.getRules()) {
			for (Literal l : r.getBody()) {
				if (l instanceof AggregateLiteral) {
					return true;
				}
			}
		}
		return false;
	}

}
