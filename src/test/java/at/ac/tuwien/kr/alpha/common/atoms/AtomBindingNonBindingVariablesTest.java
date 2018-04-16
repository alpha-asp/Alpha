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
package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.fixedinterpretations.IntPredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Tests the behaviour of {@link Atom#getBindingVariables()} and {@link Atom#getNonBindingVariables()}
 * on classes implementing {@link Atom}.
 *
 */
public class AtomBindingNonBindingVariablesTest {

	private final Map<String, PredicateInterpretation> externals = new HashMap<>();
	private final ProgramParser parser = new ProgramParser(externals);
	
	@Test
	public void testPositiveBasicAtom() {
		BasicAtom atom = (BasicAtom)parser.parse("p(X,Y).").getFacts().get(0);
		assertEquals(false, atom.isNegated());
		expectVariables(atom.getBindingVariables(), "X", "Y");
		expectVariables(atom.getNonBindingVariables());
	}
	
	@Test
	public void testNegativeBasicAtom() {
		BasicAtom atom = (BasicAtom)parser.parse("p(X,Y) :- q(X,Y), not r(X,Y).").getRules().get(0).getBody().get(1);
		assertEquals(true, atom.isNegated());
		expectVariables(atom.getBindingVariables());
		expectVariables(atom.getNonBindingVariables(), "X", "Y");
	}
	
	@Test
	public void testPositiveComparisonAtom_EQ_LeftAssigning() {
		ComparisonAtom atom = (ComparisonAtom)parser.parse("p(X) :- q(X,Y), Y = 5.").getRules().get(0).getBody().get(1);
		assertEquals(false, atom.isNegated());
		expectVariables(atom.getBindingVariables(), "Y");
		expectVariables(atom.getNonBindingVariables());
	}
	
	@Test
	public void testNegativeComparisonAtom_EQ_LeftAssigning() {
		ComparisonAtom atom = (ComparisonAtom)parser.parse("p(X) :- q(X,Y), not Y = 5.").getRules().get(0).getBody().get(1);
		assertEquals(true, atom.isNegated());
		expectVariables(atom.getBindingVariables());
		expectVariables(atom.getNonBindingVariables(), "Y");
	}
	
	@Test
	public void testPositiveComparisonAtom_EQ_RightAssigning() {
		ComparisonAtom atom = (ComparisonAtom)parser.parse("p(X) :- q(X,Y), 5 = Y.").getRules().get(0).getBody().get(1);
		assertEquals(false, atom.isNegated());
		expectVariables(atom.getBindingVariables(), "Y");
		expectVariables(atom.getNonBindingVariables());
	}
	
	@Test
	public void testNegativeComparisonAtom_EQ_RightAssigning() {
		ComparisonAtom atom = (ComparisonAtom)parser.parse("p(X) :- q(X,Y), not 5 = Y.").getRules().get(0).getBody().get(1);
		assertEquals(true, atom.isNegated());
		expectVariables(atom.getBindingVariables());
		expectVariables(atom.getNonBindingVariables(), "Y");
	}
	
	@Test
	@Ignore("Atoms of this kind are compiled away by VariableEqualityRemoval")
	public void testPositiveComparisonAtom_EQ_Bidirectional() {
		ComparisonAtom atom = (ComparisonAtom)parser.parse("p(X) :- q(X,Y), X = Y.").getRules().get(0).getBody().get(1);
		assertEquals(false, atom.isNegated());
		expectVariables(atom.getBindingVariables());
		expectVariables(atom.getNonBindingVariables(), "X", "Y");
	}
	
	@Test
	public void testNegativeComparisonAtom_EQ_Bidirectional() {
		ComparisonAtom atom = (ComparisonAtom)parser.parse("p(X) :- q(X,Y), not X = Y.").getRules().get(0).getBody().get(1);
		assertEquals(true, atom.isNegated());
		expectVariables(atom.getBindingVariables());
		expectVariables(atom.getNonBindingVariables(), "X", "Y");
	}
	
	@Test
	public void testPositiveComparisonAtom_NEQ_LeftAssigning() {
		ComparisonAtom atom = (ComparisonAtom)parser.parse("p(X) :- q(X,Y), Y != 5.").getRules().get(0).getBody().get(1);
		assertEquals(false, atom.isNegated());
		expectVariables(atom.getBindingVariables());
		expectVariables(atom.getNonBindingVariables(), "Y");
	}
	
	@Test
	public void testNegativeComparisonAtom_NEQ_LeftAssigning() {
		ComparisonAtom atom = (ComparisonAtom)parser.parse("p(X) :- q(X,Y), not Y != 5.").getRules().get(0).getBody().get(1);
		assertEquals(true, atom.isNegated());
		expectVariables(atom.getBindingVariables(), "Y");
		expectVariables(atom.getNonBindingVariables());
	}
	
	@Test
	public void testPositiveComparisonAtom_NEQ_RightAssigning() {
		ComparisonAtom atom = (ComparisonAtom)parser.parse("p(X) :- q(X,Y), 5 != Y.").getRules().get(0).getBody().get(1);
		assertEquals(false, atom.isNegated());
		expectVariables(atom.getBindingVariables());
		expectVariables(atom.getNonBindingVariables(), "Y");
	}
	
	@Test
	public void testNegativeComparisonAtom_NEQ_RightAssigning() {
		ComparisonAtom atom = (ComparisonAtom)parser.parse("p(X) :- q(X,Y), not 5 != Y.").getRules().get(0).getBody().get(1);
		assertEquals(true, atom.isNegated());
		expectVariables(atom.getBindingVariables(), "Y");
		expectVariables(atom.getNonBindingVariables());
	}
	
	@Test
	public void testPositiveComparisonAtom_NEQ_Bidirectional() {
		ComparisonAtom atom = (ComparisonAtom)parser.parse("p(X) :- q(X,Y), X != Y.").getRules().get(0).getBody().get(1);
		assertEquals(false, atom.isNegated());
		expectVariables(atom.getBindingVariables());
		expectVariables(atom.getNonBindingVariables(), "X", "Y");
	}
	
	@Test
	@Ignore("Atoms of this kind are compiled away by VariableEqualityRemoval")
	public void testNegativeComparisonAtom_NEQ_Bidirectional() {
		ComparisonAtom atom = (ComparisonAtom)parser.parse("p(X) :- q(X,Y), not X != Y.").getRules().get(0).getBody().get(1);
		assertEquals(true, atom.isNegated());
		expectVariables(atom.getBindingVariables(), "X", "Y");
		expectVariables(atom.getNonBindingVariables());
	}
	
	@Test
	public void testPositiveExternalAtom() {
		externals.put("ext", new IntPredicateInterpretation(i -> i > 0));
		ExternalAtom atom = (ExternalAtom)parser.parse("p(X) :- q(Y), &ext[Y](X).").getRules().get(0).getBody().get(1);
		assertEquals(false, atom.isNegated());
		expectVariables(atom.getBindingVariables(), "X");
		expectVariables(atom.getNonBindingVariables(), "Y");
	}
	
	@Test
	public void testNegativeExternalAtom() {
		externals.put("ext", new IntPredicateInterpretation(i -> i > 0));
		ExternalAtom atom = (ExternalAtom)parser.parse("p(X) :- q(Y), not &ext[Y](X).").getRules().get(0).getBody().get(1);
		assertEquals(true, atom.isNegated());
		expectVariables(atom.getBindingVariables());
		expectVariables(atom.getNonBindingVariables(), "X", "Y");
	}

	private void expectVariables(List<VariableTerm> variables, String... expectedVariableNames) {
		Set<String> setActualVariableNames = variables.stream().map(VariableTerm::toString).collect(Collectors.toSet());
		Set<String> setExpectedVariableNames = Arrays.stream(expectedVariableNames).collect(Collectors.toSet());
		assertEquals(setExpectedVariableNames, setActualVariableNames);
	}

}
