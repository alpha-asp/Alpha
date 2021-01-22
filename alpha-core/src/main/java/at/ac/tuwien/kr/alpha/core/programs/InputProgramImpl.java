/**
 * Copyright (c) 2019, the Alpha Team.
 * All rights reserved.
 * <p>
 * Additional changes made by Siemens.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
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
package at.ac.tuwien.kr.alpha.core.programs;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;

import at.ac.tuwien.kr.alpha.api.program.Atom;
import at.ac.tuwien.kr.alpha.api.program.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.program.InputProgram;
import at.ac.tuwien.kr.alpha.api.rules.ChoiceHead;
import at.ac.tuwien.kr.alpha.api.rules.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.api.rules.Head;
import at.ac.tuwien.kr.alpha.api.rules.NormalHead;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.core.atoms.CoreAtom;
import at.ac.tuwien.kr.alpha.core.rules.BasicRule;

/**
 * Alpha-internal representation of an ASP program, i.e., a set of ASP rules.
 * <p>
 * Copyright (c) 2017-2021, the Alpha Team.
 */
public class InputProgramImpl extends AbstractProgram<BasicRule> implements InputProgram {

	public static final InputProgramImpl EMPTY = null; // TODO

	private final Set<Rule<DisjunctiveHead>> disjunctiveRules;
	private final Set<Rule<ChoiceHead>> choiceRules;
	private final Set<Rule<NormalHead>> normalRules;

	public InputProgramImpl(Set<Atom> facts, Set<Rule<DisjunctiveHead>> disjunctiveRules, Set<Rule<ChoiceHead>> choiceRules, Set<Rule<NormalHead>> normalRules,
			InlineDirectives inlineDirectives) {
		super(facts, inlineDirectives);
		this.disjunctiveRules = Collections.unmodifiableSet(disjunctiveRules);
		this.choiceRules = Collections.unmodifiableSet(choiceRules);
		this.normalRules = Collections.unmodifiableSet(normalRules);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(InputProgramImpl prog) {
		return new Builder(prog);
	}

	/**
	 * Builder for more complex program construction scenarios, ensuring that an {@link InputProgramImpl} is immutable
	 */
	public static class Builder {

		private Set<Rule<DisjunctiveHead>> disjunctiveRules = new LinkedHashSet<>();
		private Set<Rule<ChoiceHead>> choiceRules = new LinkedHashSet<>();
		private Set<Rule<NormalHead>> normalRules = new LinkedHashSet<>();
		private Set<Atom> facts = new LinkedHashSet<>();
		private InlineDirectives inlineDirectives = null; // TODO do inline directives properly

		public Builder(InputProgramImpl prog) {
			this.addDisjunctiveRules(prog.getDisjunctiveRules());
			this.addChoiceRules(prog.getChoiceRules());
			this.addNormalRules(prog.getNormalRules());
			this.addFacts(prog.getFacts());
			this.addInlineDirectives(prog.getInlineDirectives());
		}

		public Builder() {

		}

		public Builder addDisjunctiveRules(Set<Rule<DisjunctiveHead>> rules) {
			this.disjunctiveRules.addAll(rules);
			return this;
		}

		public Builder addChoiceRules(Set<Rule<ChoiceHead>> rules) {
			this.choiceRules.addAll(rules);
			return this;
		}

		public Builder addNormalRules(Set<Rule<NormalHead>> rules) {
			this.normalRules.addAll(rules);
			return this;
		}

		public Builder addDisjunctiveRule(Rule<DisjunctiveHead> r) {
			this.disjunctiveRules.add(r);
			return this;
		}

		public Builder addChoiceRule(Rule<ChoiceHead> r) {
			this.choiceRules.add(r);
			return this;
		}

		public Builder addNormalRule(Rule<NormalHead> r) {
			this.normalRules.add(r);
			return this;
		}

		public Builder addFacts(Set<Atom> facts) {
			this.facts.addAll(facts);
			return this;
		}

		public Builder addFact(CoreAtom fact) {
			this.facts.add(fact);
			return this;
		}

		public Builder addInlineDirectives(InlineDirectives inlineDirectives) {
			// this.inlineDirectives.accumulate(inlineDirectives); TODO
			return this;
		}

		public Builder accumulate(InputProgramImpl prog) {
			this.addDisjunctiveRules(prog.getDisjunctiveRules());
			this.addChoiceRules(prog.getChoiceRules());
			this.addNormalRules(prog.getNormalRules());
			this.addFacts(prog.getFacts());
			this.addInlineDirectives(prog.getInlineDirectives());
			return this;
		}

		public InputProgramImpl build() {
			return new InputProgramImpl(this.facts, this.disjunctiveRules, this.choiceRules, this.normalRules, this.inlineDirectives);
		}
	}

	@Override
	public Set<Rule<? extends Head>> getRules() {
		return SetUtils.union(SetUtils.union(this.disjunctiveRules, this.choiceRules), this.normalRules);
	}

	@Override
	public at.ac.tuwien.kr.alpha.api.program.InlineDirectives getInlineDirectives() {
		return this.getInlineDirectives();
	}

	@Override
	public Set<Rule<ChoiceHead>> getChoiceRules() {
		return this.getChoiceRules();
	}

	@Override
	public Set<Rule<DisjunctiveHead>> getDisjunctiveRules() {
		return this.getDisjunctiveRules();
	}

	@Override
	public Set<Rule<NormalHead>> getNormalRules() {
		return this.getNormalRules();
	}

}
