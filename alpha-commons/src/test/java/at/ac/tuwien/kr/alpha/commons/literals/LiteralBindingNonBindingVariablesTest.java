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
package at.ac.tuwien.kr.alpha.commons.literals;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.externals.IntPredicateInterpretation;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;

/**
 * Tests the behaviour of {@link Literal#getBindingVariables()} and {@link Literal#getNonBindingVariables()}
 * on classes implementing {@link Atom}.
 *
 */
public class LiteralBindingNonBindingVariablesTest {

	@Test
	public void testPositiveBasicLiteral() {
		// literal := q(X, Y)
		Literal literal = Literals.fromAtom(Atoms.newBasicAtom(Predicates.getPredicate("q", 2), Terms.newVariable("X"), Terms.newVariable("Y")), true);
		assertEquals(false, literal.isNegated());
		expectVariables(literal.getBindingVariables(), "X", "Y");
		expectVariables(literal.getNonBindingVariables());
	}

	@Test
	public void testNegativeBasicLiteral() {
		// literal := not r(X, Y)
		Literal literal = Literals.fromAtom(Atoms.newBasicAtom(Predicates.getPredicate("r", 2), Terms.newVariable("X"), Terms.newVariable("Y")), false);
		assertEquals(true, literal.isNegated());
		expectVariables(literal.getBindingVariables());
		expectVariables(literal.getNonBindingVariables(), "X", "Y");
	}

	@Test
	public void testPositiveComparisonLiteral_EQ_LeftAssigning() {
		// literal := Y = 5
		Literal literal = Literals.fromAtom(Atoms.newComparisonAtom(Terms.newVariable("Y"), Terms.newConstant(5), ComparisonOperators.EQ), true);
		assertEquals(false, literal.isNegated());
		expectVariables(literal.getBindingVariables(), "Y");
		expectVariables(literal.getNonBindingVariables());
	}

	@Test
	public void testNegativeComparisonLiteral_EQ_LeftAssigning() {
		// literal := not Y = 5
		Literal literal = Literals.fromAtom(Atoms.newComparisonAtom(Terms.newVariable("Y"), Terms.newConstant(5), ComparisonOperators.EQ), false);
		assertEquals(true, literal.isNegated());
		expectVariables(literal.getBindingVariables());
		expectVariables(literal.getNonBindingVariables(), "Y");
	}

	@Test
	public void testPositiveComparisonLiteral_EQ_RightAssigning() {
		// literal := 5 = Y
		Literal literal = Literals.fromAtom(Atoms.newComparisonAtom(Terms.newConstant(5), Terms.newVariable("Y"), ComparisonOperators.EQ), true);
		assertEquals(false, literal.isNegated());
		expectVariables(literal.getBindingVariables(), "Y");
		expectVariables(literal.getNonBindingVariables());
	}

	@Test
	public void testNegativeComparisonLiteral_EQ_RightAssigning() {
		// literal := 5 = Y
		Literal literal = Literals.fromAtom(Atoms.newComparisonAtom(Terms.newConstant(5), Terms.newVariable("Y"), ComparisonOperators.EQ), false);
		assertEquals(true, literal.isNegated());
		expectVariables(literal.getBindingVariables());
		expectVariables(literal.getNonBindingVariables(), "Y");
	}

// TODO why do we have this if it's disabled?
//	@Test
//	@Disabled("Literals of this kind are compiled away by VariableEqualityRemoval")
//	public void testPositiveComparisonLiteral_EQ_Bidirectional() {
//		Rule<Head> rule = parser.parse("p(X) :- q(X,Y), X = Y.").getRules().get(0);
//		Literal literal = rule.getBody().stream().filter((lit) -> lit.getPredicate() == ComparisonOperators.EQ.toPredicate()).findFirst().get();
//		assertEquals(false, literal.isNegated());
//		expectVariables(literal.getBindingVariables());
//		expectVariables(literal.getNonBindingVariables(), "X", "Y");
//	}

	@Test
	public void testNegativeComparisonLiteral_EQ_Bidirectional() {
		// literal := not X = Y
		Literal literal = Literals.fromAtom(Atoms.newComparisonAtom(Terms.newVariable("X"), Terms.newVariable("Y"), ComparisonOperators.EQ), false);
		assertEquals(true, literal.isNegated());
		expectVariables(literal.getBindingVariables());
		expectVariables(literal.getNonBindingVariables(), "X", "Y");
	}

	@Test
	public void testPositiveComparisonLiteral_NEQ_LeftAssigning() {
		// literal := Y != 5
		Literal literal = Literals.fromAtom(Atoms.newComparisonAtom(Terms.newVariable("Y"), Terms.newConstant(5), ComparisonOperators.NE), true);
		assertEquals(false, literal.isNegated());
		expectVariables(literal.getBindingVariables());
		expectVariables(literal.getNonBindingVariables(), "Y");
	}

	@Test
	public void testNegativeComparisonLiteral_NEQ_LeftAssigning() {
		// literal := not Y != 5
		Literal literal = Literals.fromAtom(Atoms.newComparisonAtom(Terms.newVariable("Y"), Terms.newConstant(5), ComparisonOperators.NE), false);
		assertEquals(true, literal.isNegated());
		expectVariables(literal.getBindingVariables(), "Y");
		expectVariables(literal.getNonBindingVariables());
	}

	@Test
	public void testPositiveComparisonLiteral_NEQ_RightAssigning() {
		// literal := 5 != Y
		Literal literal = Literals.fromAtom(Atoms.newComparisonAtom(Terms.newConstant(5), Terms.newVariable("Y"), ComparisonOperators.NE), true);
		assertEquals(false, literal.isNegated());
		expectVariables(literal.getBindingVariables());
		expectVariables(literal.getNonBindingVariables(), "Y");
	}

	@Test
	public void testNegativeComparisonLiteral_NEQ_RightAssigning() {
		// literal := not 5 != Y
		Literal literal = Literals.fromAtom(Atoms.newComparisonAtom(Terms.newConstant(5), Terms.newVariable("Y"), ComparisonOperators.NE), false);
		assertEquals(true, literal.isNegated());
		expectVariables(literal.getBindingVariables(), "Y");
		expectVariables(literal.getNonBindingVariables());
	}

	@Test
	public void testPositiveComparisonLiteral_NEQ_Bidirectional() {
		// literal := X != Y
		Literal literal = Literals.fromAtom(Atoms.newComparisonAtom(Terms.newVariable("X"), Terms.newVariable("Y"), ComparisonOperators.NE), true);
		assertEquals(false, literal.isNegated());
		expectVariables(literal.getBindingVariables());
		expectVariables(literal.getNonBindingVariables(), "X", "Y");
	}

	@Test
	@Disabled("Literals of this kind are compiled away by VariableEqualityRemoval")
	public void testNegativeComparisonLiteral_NEQ_Bidirectional() {
		// literal := not X != Y
		Literal literal = Literals.fromAtom(Atoms.newComparisonAtom(Terms.newVariable("X"), Terms.newVariable("Y"), ComparisonOperators.NE), false);
		assertEquals(true, literal.isNegated());
		expectVariables(literal.getBindingVariables(), "X", "Y");
		expectVariables(literal.getNonBindingVariables());
	}

	@Test
	public void testPositiveExternalLiteral() {
		// literal := &ext[Y](X)
		List<Term> extInput = new ArrayList<>();
		List<Term> extOutput = new ArrayList<>();
		extInput.add(Terms.newVariable("Y"));
		extOutput.add(Terms.newVariable("X"));
		Literal literal = Literals.fromAtom(Atoms.newExternalAtom(Predicates.getPredicate("ext", 2), new IntPredicateInterpretation(i -> i > 0), extInput, extOutput), true);
		assertEquals(false, literal.isNegated());
		expectVariables(literal.getBindingVariables(), "X");
		expectVariables(literal.getNonBindingVariables(), "Y");
	}

	@Test
	public void testNegativeExternalLiteral() {
		// literal := not &ext[Y](X)
		List<Term> extInput = new ArrayList<>();
		List<Term> extOutput = new ArrayList<>();
		extInput.add(Terms.newVariable("Y"));
		extOutput.add(Terms.newVariable("X"));
		Literal literal = Literals.fromAtom(Atoms.newExternalAtom(Predicates.getPredicate("ext", 2), new IntPredicateInterpretation(i -> i > 0), extInput, extOutput), false);
		assertEquals(true, literal.isNegated());
		expectVariables(literal.getBindingVariables());
		expectVariables(literal.getNonBindingVariables(), "X", "Y");
	}

	private void expectVariables(Collection<VariableTerm> variables, String... expectedVariableNames) {
		Set<String> setActualVariableNames = variables.stream().map(VariableTerm::toString).collect(Collectors.toSet());
		Set<String> setExpectedVariableNames = Arrays.stream(expectedVariableNames).collect(Collectors.toSet());
		assertEquals(setExpectedVariableNames, setActualVariableNames);
	}

}
