package at.ac.tuwien.kr.alpha.core.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;
import at.ac.tuwien.kr.alpha.core.rules.CompiledRule;
import at.ac.tuwien.kr.alpha.core.rules.InternalRule;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
// TODO this is a functional test depedendent on parser that wants to be a unit test
public class RuleTest {

	private final ProgramParserImpl parser = new ProgramParserImpl();

	@Test
	public void renameVariables() {
		// rule := p(X,Y) :- a, f(Z) = 1, q(X,g(Y),Z), dom(A).
		BasicAtom headAtom = Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newVariable("Y"));
		BasicAtom bodyAtom1 = Atoms.newBasicAtom(Predicates.getPredicate("a", 0));
		ComparisonAtom bodyAtom2 = Atoms.newComparisonAtom(Terms.newFunctionTerm("f", Terms.newVariable("Z")), Terms.newConstant(1), ComparisonOperators.EQ);
		BasicAtom bodyAtom3 = Atoms.newBasicAtom(Predicates.getPredicate("q", 3),
				Terms.newVariable("X"), Terms.newFunctionTerm("g", Terms.newVariable("Y")), Terms.newVariable("Z"));
		BasicAtom bodyAtom4 = Atoms.newBasicAtom(Predicates.getPredicate("dom", 1), Terms.newVariable("A"));
		List<Literal> ruleBody = new ArrayList<>();
		ruleBody.add(bodyAtom1.toLiteral());
		ruleBody.add(bodyAtom2.toLiteral());
		ruleBody.add(bodyAtom3.toLiteral());
		ruleBody.add(bodyAtom4.toLiteral());
		CompiledRule rule = new InternalRule(Heads.newNormalHead(headAtom), ruleBody);

		// ruleWithRenamedVars := p(X_13, Y_13) :- a, f(Z_13) = 1, q(X_13, g(Y_13), Z_13), dom(A_13).
		BasicAtom headAtomRenamed = Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X_13"), Terms.newVariable("Y_13"));
		BasicAtom bodyAtom1Renamed = Atoms.newBasicAtom(Predicates.getPredicate("a", 0));
		ComparisonAtom bodyAtom2Renamed = Atoms.newComparisonAtom(Terms.newFunctionTerm("f", Terms.newVariable("Z_13")), Terms.newConstant(1),
				ComparisonOperators.EQ);
		BasicAtom bodyAtom3Renamed = Atoms.newBasicAtom(Predicates.getPredicate("q", 3),
				Terms.newVariable("X_13"), Terms.newFunctionTerm("g", Terms.newVariable("Y_13")), Terms.newVariable("Z_13"));
		BasicAtom bodyAtom4Renamed = Atoms.newBasicAtom(Predicates.getPredicate("dom", 1), Terms.newVariable("A_13"));
		List<Literal> ruleBodyRenamed = new ArrayList<>();
		ruleBodyRenamed.add(bodyAtom1Renamed.toLiteral());
		ruleBodyRenamed.add(bodyAtom2Renamed.toLiteral());
		ruleBodyRenamed.add(bodyAtom3Renamed.toLiteral());
		ruleBodyRenamed.add(bodyAtom4Renamed.toLiteral());
		CompiledRule ruleWithRenamedVars = new InternalRule(Heads.newNormalHead(headAtomRenamed), ruleBodyRenamed);

		assertEquals(ruleWithRenamedVars, rule.renameVariables("_13"));
	}

	@Test
	public void testRulesEqual() {
		ASPCore2Program p1 = parser.parse("p(X, Y) :- bla(X), blub(Y), foo(X, Y), not bar(X).");
		Rule<Head> r1 = p1.getRules().get(0);
		// r1 := p(X, Y) :- bla(X), blub(Y), foo(X, Y), not bar(X).
		ASPCore2Program p2 = parser.parse("p(X, Y) :- bla(X), blub(Y), foo(X, Y), not bar(X).");
		Rule<Head> r2 = p2.getRules().get(0);
		ASPCore2Program p3 = parser.parse("p(X, Y) :- bla(X), blub(X), foo(X, X), not bar(X).");
		Rule<Head> r3 = p3.getRules().get(0);
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