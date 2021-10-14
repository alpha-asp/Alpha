/**
 * Copyright (c) 2017 Siemens AG
 * All rights reserved.
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
package at.ac.tuwien.kr.alpha.core.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.literals.Literals;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.programs.InputProgram;
import at.ac.tuwien.kr.alpha.core.rules.Rules;

public class ProgramTest {

	@Test
	public void testToString() {
		ASPCore2Program program;
		// rule := q(X) :- p(X).
		List<Literal> body = new ArrayList<>();
		body.add(Literals.fromAtom(Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X")), true));
		Rule<Head> rule = Rules.newNormalRule(
				Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newVariable("X")),
				Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X")).toLiteral());
		List<Atom> facts = new ArrayList<>();
		facts.add(Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newSymbolicConstant("a")));
		facts.add(Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newSymbolicConstant("b")));
		// program := p(a). p(b). q(X) :- p(X).
		program = InputProgram.builder().addFacts(facts).addRule(rule).build();
		assertEquals(
				"p(a)." + System.lineSeparator() +
						"p(b)." + System.lineSeparator() +
						"q(X) :- p(X)." + System.lineSeparator(),
				program.toString());
	}
}