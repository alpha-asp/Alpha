/**
 * Copyright (c) 2018 Siemens AG
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
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertTrue;

public class AnnotationGroundingTest {
	
	private final ProgramParser parser = new ProgramParser();

	/**
	 * Tests if literals that would not be grounded otherwise are grounded because they occur in a heuristic condition
	 */
	@Test
	public void testConditionIsGrounded() {
		String testProgram = "n(1). n(2). o(2)."
				+ "{p(N)} :- n(N)."
				+ "{q(N)} :- n(N)."
				+ "x(N) :- n(N), not o(N). [1@1 : p(Np2), not q(Np2), Np2=N+2]";
		Program parsedProgram = parser.parse(testProgram);
		Grounder grounder = new NaiveGrounder(parsedProgram);
		getNoGoods(grounder);
		checkAtomInAtomStore(atom("p", 3), grounder);
		checkAtomInAtomStore(atom("q", 3), grounder);
	}

	private Atom atom(String predicate, int term) {
		return new BasicAtom(Predicate.getInstance(predicate, 1), ConstantTerm.getInstance(term));
	}

	private Collection<NoGood> getNoGoods(Grounder grounder) {
		return grounder.getNoGoods(null).values();
	}

	private void checkAtomInAtomStore(Atom atom, Grounder grounder) {
		assertTrue(atom.toString(), grounder.getAtomStore().contains(atom));
	}
}
