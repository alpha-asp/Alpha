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

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.atoms.HeuristicAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link NaiveGrounder}
 */
public class NaiveGrounderTest {
	private static final ProgramParser PARSER = new ProgramParser();
	private static final VariableTerm N = VariableTerm.getInstance("N");
	private static final ConstantTerm<Integer> ONE = ConstantTerm.getInstance(1);
	
	@Before
	public void resetIdGenerator() {
		ChoiceRecorder.ID_GENERATOR.resetGenerator();
	}

	@Test
	public void testGenerateHeuristicNoGoods() {
		Program program = PARSER.parse("{ a(1) }."
				+ "{ b(N) } :- a(N)."
				+ "#heuristic b(N) : a(N), not b(N). [N@2]");
		
		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore);
		NoGoodGenerator noGoodGenerator = ((NaiveGrounder)grounder).noGoodGenerator;
		Rule rule = findHeuristicRule(program.getRules());
		NonGroundRule nonGroundRule = NonGroundRule.constructNonGroundRule(rule);
		Substitution substitution = new Substitution();
		substitution.unifyTerms(N, ONE);
		List<NoGood> generatedNoGoods = noGoodGenerator.generateNoGoodsFromGroundSubstitution(nonGroundRule, substitution);
		assertEquals(2, generatedNoGoods.size());
		assertEquals("*{-(HeuOff(\"0\")), +(b(1))}", atomStore.noGoodToString(generatedNoGoods.get(0)));
		assertEquals("*{-(HeuOn(\"0\")), +(a(1))}", atomStore.noGoodToString(generatedNoGoods.get(1)));
	}

	private Rule findHeuristicRule(List<Rule> rules) {
		for (Rule rule : rules) {
			if (rule.getHead() instanceof DisjunctiveHead && ((DisjunctiveHead)rule.getHead()).disjunctiveAtoms.get(0) instanceof HeuristicAtom) {
				return rule;
			}
		}
		return null;
	}

}
