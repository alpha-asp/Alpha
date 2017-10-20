/**
 * Copyright (c) 2017, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static at.ac.tuwien.kr.alpha.Util.join;

/**
 * Alpha-internal representation of an ASP program, i.e., a set of ASP rules.
 * Copyright (c) 2017, the Alpha Team.
 */
public class Program {
	public static final Program EMPTY = new Program(Collections.emptyList(), Collections.emptyList(), new InlineDirectives());

	private final List<Rule> rules;
	private final List<Atom> facts;
	private final InlineDirectives inlineDirectives;

	public Program(List<Rule> rules, List<Atom> facts, InlineDirectives inlineDirectives) {
		this.rules = rules;
		this.facts = facts;
		this.inlineDirectives = inlineDirectives;
	}

	public Program() {
		this(new ArrayList<>(), new ArrayList<>());
	}

	public List<Rule> getRules() {
		return rules;
	}

	public List<Atom> getFacts() {
		return facts;
	}

	public InlineDirectives getInlineDirectives() {
		return inlineDirectives;
	}

	public void accumulate(Program program) {
		rules.addAll(program.rules);
		facts.addAll(program.facts);
		inlineDirectives.accumulate(program.inlineDirectives);
	}

	@Override
	public String toString() {
		final String ls = System.lineSeparator();
		final String result = join(
			"",
			facts,
			"." + ls,
			"." + ls
		);

		if (rules.isEmpty()) {
			return result;
		}

		return join(
			result,
			rules,
			ls,
			ls
		);
	}
}
