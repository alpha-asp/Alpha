package at.ac.tuwien.kr.alpha.core.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.rules.CompiledRule;
import at.ac.tuwien.kr.alpha.core.rules.InternalRule;
import at.ac.tuwien.kr.alpha.core.rules.NormalRuleImpl;
import at.ac.tuwien.kr.alpha.core.rules.Rules;

/**
 * Copyright (c) 2018 - 2021, the Alpha Team.
 */
public class RuleTest {

	@Test
	public void renameVariables() {
		// rule := p(X,Y) :- a, f(Z) = 1, q(X,g(Y),Z), dom(A).
		BasicAtom headAtom = Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newVariable("Y"));
		BasicAtom bodyAtom1 = Atoms.newBasicAtom(Predicates.getPredicate("a", 0));
		ComparisonAtom bodyAtom2 = Atoms.newComparisonAtom(Terms.newFunctionTerm("f", Terms.newVariable("Z")), Terms.newConstant(1), ComparisonOperators.EQ);
		BasicAtom bodyAtom3 = Atoms.newBasicAtom(Predicates.getPredicate("q", 3),
				Terms.newVariable("X"), Terms.newFunctionTerm("g", Terms.newVariable("Y")), Terms.newVariable("Z"));
		BasicAtom bodyAtom4 = Atoms.newBasicAtom(Predicates.getPredicate("dom", 1), Terms.newVariable("A"));
		CompiledRule rule = InternalRule.fromNormalRule(NormalRuleImpl.fromBasicRule(
				Rules.newRule(headAtom, bodyAtom1.toLiteral(), bodyAtom2.toLiteral(), bodyAtom3.toLiteral(), bodyAtom4.toLiteral())));

		// ruleWithRenamedVars := p(X_13, Y_13) :- a, f(Z_13) = 1, q(X_13, g(Y_13), Z_13), dom(A_13).
		BasicAtom headAtomRenamed = Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X_13"), Terms.newVariable("Y_13"));
		BasicAtom bodyAtom1Renamed = Atoms.newBasicAtom(Predicates.getPredicate("a", 0));
		ComparisonAtom bodyAtom2Renamed = Atoms.newComparisonAtom(Terms.newFunctionTerm("f", Terms.newVariable("Z_13")), Terms.newConstant(1),
				ComparisonOperators.EQ);
		BasicAtom bodyAtom3Renamed = Atoms.newBasicAtom(Predicates.getPredicate("q", 3),
				Terms.newVariable("X_13"), Terms.newFunctionTerm("g", Terms.newVariable("Y_13")), Terms.newVariable("Z_13"));
		BasicAtom bodyAtom4Renamed = Atoms.newBasicAtom(Predicates.getPredicate("dom", 1), Terms.newVariable("A_13"));
		CompiledRule ruleWithRenamedVars = InternalRule.fromNormalRule(NormalRuleImpl.fromBasicRule(
				Rules.newRule(headAtomRenamed, bodyAtom1Renamed.toLiteral(), bodyAtom2Renamed.toLiteral(), bodyAtom3Renamed.toLiteral(),
						bodyAtom4Renamed.toLiteral())));

		assertEquals(ruleWithRenamedVars, rule.renameVariables("_13"));
	}

	@Test
	public void testRulesEqual() {
		// r1 := p(X, Y) :- bla(X), blub(Y), foo(X, Y), not bar(X).
		Rule<Head> r1 = Rules.newRule(Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newVariable("Y")),
				Atoms.newBasicAtom(Predicates.getPredicate("bla", 1), Terms.newVariable("X")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("blub", 1), Terms.newVariable("Y")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("foo", 2), Terms.newVariable("X"), Terms.newVariable("Y")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("bar", 1), Terms.newVariable("X")).toLiteral(false));
		// r2 := p(X, Y) :- bla(X), blub(Y), foo(X, Y), not bar(X).
		Rule<Head> r2 = Rules.newRule(Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newVariable("Y")),
				Atoms.newBasicAtom(Predicates.getPredicate("bla", 1), Terms.newVariable("X")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("blub", 1), Terms.newVariable("Y")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("foo", 2), Terms.newVariable("X"), Terms.newVariable("Y")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("bar", 1), Terms.newVariable("X")).toLiteral(false));
		// r3 := p(X, Y) :- bla(X), blub(X), foo(X, X), not bar(X).
		Rule<Head> r3 = Rules.newRule(Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newVariable("Y")),
				Atoms.newBasicAtom(Predicates.getPredicate("bla", 1), Terms.newVariable("X")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("blub", 1), Terms.newVariable("X")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("foo", 2), Terms.newVariable("X"), Terms.newVariable("X")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("bar", 1), Terms.newVariable("X")).toLiteral(false));
		assertTrue(r1.equals(r2));
		assertTrue(r2.equals(r1));
		assertTrue(r1.hashCode() == r2.hashCode());
		assertFalse(r1.equals(r3));
		assertFalse(r3.equals(r1));
		assertTrue(r1.hashCode() != r3.hashCode());
		assertFalse(r2.equals(r3));
		assertFalse(r3.equals(r2));
		assertTrue(r2.hashCode() != r3.hashCode());
	}

}