/*
 * Copyright (c) 2018, 2020, the Alpha Team.
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

package at.ac.tuwien.kr.alpha.grounder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

public class UnifierTest extends SubstitutionTest {

	@Test
	public void extendUnifier() {
		VariableTerm varX = VariableTerm.getInstance("X");
		VariableTerm varY = VariableTerm.getInstance("Y");
		Unifier sub1 = new Unifier();
		sub1.put(varX, varY);
		Unifier sub2 = new Unifier();
		sub2.put(varY, ConstantTerm.getInstance("a"));

		sub1.extendWith(sub2);
		BasicAtom atom1 = parseAtom("p(X)");

		Atom atomSubstituted = atom1.substitute(sub1);
		assertEquals(ConstantTerm.getInstance("a"), atomSubstituted.getTerms().get(0));
	}

	@Test
	public void mergeUnifierIntoLeft() {
		VariableTerm varX = VariableTerm.getInstance("X");
		VariableTerm varY = VariableTerm.getInstance("Y");
		VariableTerm varZ = VariableTerm.getInstance("Z");
		Term constA = ConstantTerm.getInstance("a");
		Unifier left = new Unifier();
		left.put(varX, varY);
		left.put(varZ, varY);
		Unifier right = new Unifier();
		right.put(varX, constA);
		Unifier merged = Unifier.mergeIntoLeft(left, right);
		assertEquals(constA, merged.eval(varY));
		assertEquals(constA, merged.eval(varZ));
	}

	private BasicAtom parseAtom(String atom) {
		ProgramParser programParser = new ProgramParser();
		InputProgram program = programParser.parse(atom + ".");
		return (BasicAtom) program.getFacts().get(0);
	}
}
