/**
 * Copyright (c) 2018 Siemens AG
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.rule.impl.NormalRule;
import at.ac.tuwien.kr.alpha.common.rule.impl.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Test;

import java.util.List;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link NoGoodGenerator}
 */
public class NoGoodGeneratorTest {
	private static final ProgramParser PARSER = new ProgramParser();
	
	private static final ConstantTerm<?> A = ConstantTerm.getSymbolicInstance("a");
	private static final ConstantTerm<?> B = ConstantTerm.getSymbolicInstance("b");

	private static final VariableTerm X = VariableTerm.getInstance("X");
	private static final VariableTerm Y = VariableTerm.getInstance("Y");
	
	/**
	 * Calls {@link NoGoodGenerator#collectNegLiterals(NormalRule, Substitution)},
	 * which puts the atom occuring negatively in a rule into the atom store.
	 * It is then checked whether the atom in the atom store is positive.
	 */
	@Test
	public void collectNeg_ContainsOnlyPositiveLiterals() {
		Program program = PARSER.parse("p(a,b). "
				+ "q(a,b) :- not nq(a,b). "
				+ "nq(a,b) :- not q(a,b).");
		
		BasicRule rule = program.getRules().get(1);
		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore);
		NoGoodGenerator noGoodGenerator = ((NaiveGrounder)grounder).noGoodGenerator;
		NormalRule nonGroundRule = NormalRule.fromBasicRule(rule);
		Substitution substitution = new Substitution();
		substitution.unifyTerms(X, A);
		substitution.unifyTerms(Y, B);
		List<Integer> collectedNeg = noGoodGenerator.collectNegLiterals(nonGroundRule, substitution);
		assertEquals(1, collectedNeg.size());
		String negAtomString = atomStore.atomToString(atomOf(collectedNeg.get(0)));
		assertEquals("q(a, b)", negAtomString);
	}

}
