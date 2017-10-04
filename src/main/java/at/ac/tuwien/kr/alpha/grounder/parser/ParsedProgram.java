/**
 * Copyright (c) 2016-2017, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.predicates.ExternalEvaluable;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;

import java.util.*;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedProgram extends CommonParsedObject {
	public static final ParsedProgram EMPTY = new ParsedProgram(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

	public List<ParsedRule> rules;
	public List<ParsedFact> facts;
	public List<ParsedConstraint> constraints;

	private ParsedProgram(List<ParsedRule> rules, List<ParsedFact> facts, List<ParsedConstraint> constraints) {
		this.rules = rules;
		this.facts = facts;
		this.constraints = constraints;
	}

	public ParsedProgram(Collection<? extends CommonParsedObject> objects) {
		this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

		objects.forEach(o -> o.addTo(this));
	}

	public boolean addRule(ParsedRule rule) {
		return rules.add(rule);
	}

	public boolean addFact(ParsedFact fact) {
		return facts.add(fact);
	}

	public boolean addConstraint(ParsedConstraint constraint) {
		return constraints.add(constraint);
	}

	public void accumulate(ParsedProgram program) {
		rules.addAll(program.rules);
		facts.addAll(program.facts);
		constraints.addAll(program.constraints);
	}

	public Program toProgram() {
		return this.toProgram(Collections.emptyMap());
	}

	public Program toProgram(Map<String, ExternalEvaluable> externals)  {
		List<Atom> facts = new ArrayList<>(this.facts.size());
		List<NonGroundRule> rules = new ArrayList<>(this.rules.size());
		List<NonGroundRule> constraints = new ArrayList<>(this.constraints.size());

		for (ParsedFact fact : this.facts) {
			facts.add(fact.getFact().toAtom(externals));
		}

		for (ParsedRule rule : this.rules) {
			rules.add(rule.toNonGroundRule(externals));
		}

		for (ParsedConstraint rule : this.constraints) {
			constraints.add(rule.toNonGroundRule(externals));
		}

		return new Program(facts, rules, constraints);
	}
}
