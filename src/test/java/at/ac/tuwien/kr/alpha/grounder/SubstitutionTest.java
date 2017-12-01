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
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class SubstitutionTest {
	private static ConstantTerm<?> a = ConstantTerm.getSymbolicInstance("a");
	private static ConstantTerm<?> b = ConstantTerm.getSymbolicInstance("b");
	private static ConstantTerm<?> c = ConstantTerm.getSymbolicInstance("c");

	private static VariableTerm x = VariableTerm.getInstance("X");
	private static VariableTerm y = VariableTerm.getInstance("Y");

	@Test
	public void unifyTermsSimpleBinding() throws Exception {
		Substitution substitution = new Substitution();
		substitution.unifyTerms(y, a);
		assertEquals(a, substitution.eval(y));
	}

	@Test
	public void unifyTermsFunctionTermBinding() throws Exception {
		Substitution substitution = new Substitution();
		substitution.put(y, a);

		FunctionTerm groundFunctionTerm = FunctionTerm.getInstance("f", b, c);
		Term nongroundFunctionTerm = FunctionTerm.getInstance("f", b, x);

		substitution.unifyTerms(nongroundFunctionTerm, groundFunctionTerm);

		assertEquals(c, substitution.eval(x));
		assertEquals(a, substitution.eval(y));
	}

	@Test
	public void equalizingSubstitution() {
		BasicAtom atom1 = parseAtom("p(X,Y)");
		BasicAtom atom2 = parseAtom("p(A,B)");
		assertNotEquals(null, Substitution.findEqualizingSubstitution(atom1, atom2));
		assertNotEquals(null, Substitution.findEqualizingSubstitution(atom2, atom1));

		BasicAtom atom3 = parseAtom("p(X,Y)");
		BasicAtom atom4 = parseAtom("p(a,f(X))");
		assertNotEquals(null, Substitution.findEqualizingSubstitution(atom3, atom4));
		assertEquals(null, Substitution.findEqualizingSubstitution(atom4, atom3));

		BasicAtom atom5 = parseAtom("p(X,X)");
		BasicAtom atom6 = parseAtom("p(a,Y)");
		assertEquals(null, Substitution.findEqualizingSubstitution(atom5, atom6));
		assertEquals(null, Substitution.findEqualizingSubstitution(atom6, atom5));
	}

	private BasicAtom parseAtom(String atom) {
		ProgramParser programParser = new ProgramParser();
		Program program = programParser.parse(atom + ".");
		return (BasicAtom) program.getFacts().get(0);
	}
}