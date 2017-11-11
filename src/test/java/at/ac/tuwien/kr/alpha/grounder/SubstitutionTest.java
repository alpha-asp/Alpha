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

import at.ac.tuwien.kr.alpha.common.terms.Constant;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.Variable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class SubstitutionTest {
	private static Constant<?> a = Constant.getSymbolicInstance("a");
	private static Constant<?> b = Constant.getSymbolicInstance("b");
	private static Constant<?> c = Constant.getSymbolicInstance("c");

	private static Variable x = Variable.getInstance("X");
	private static Variable y = Variable.getInstance("Y");

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

		assertEquals(c, substitution.apply(x));
		assertEquals(a, substitution.apply(y));
	}
}