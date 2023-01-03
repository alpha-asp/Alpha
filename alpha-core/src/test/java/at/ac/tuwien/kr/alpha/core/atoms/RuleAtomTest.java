package at.ac.tuwien.kr.alpha.core.atoms;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.substitutions.BasicSubstitution;
import at.ac.tuwien.kr.alpha.commons.substitutions.Instance;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;
import at.ac.tuwien.kr.alpha.core.rules.CompiledRule;
import at.ac.tuwien.kr.alpha.core.rules.InternalRule;
import at.ac.tuwien.kr.alpha.core.rules.NormalRuleImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Copyright (c) 2022, the Alpha Team.
 */
public class RuleAtomTest {
	private static final ProgramParser PARSER = new ProgramParserImpl();

	private static final VariableTerm X = Terms.newVariable("X");
	private static final VariableTerm Y = Terms.newVariable("Y");
	private static final Predicate PREDICATE = Predicates.getPredicate("p", 1);
	private static final BasicAtom PX = Atoms.newBasicAtom(PREDICATE, X);
	private static final BasicAtom PY = Atoms.newBasicAtom(PREDICATE, Y);

	@Test
	public void substitutionObtainableFromRuleAtom() {
		Rule<Head> rule = PARSER.parse("q(X) :- p(X,Y).").getRules().get(0);
		CompiledRule nonGroundRule = InternalRule.fromNormalRule(NormalRuleImpl.fromBasicRule(rule));
		// Build substitution X -> a, Y -> b.
		Substitution substitution = BasicSubstitution.specializeSubstitution(
			PY, new Instance(Terms.newSymbolicConstant("b")), BasicSubstitution.specializeSubstitution(
			PX, new Instance(Terms.newSymbolicConstant("a")), BasicSubstitution.EMPTY_SUBSTITUTION));

		RuleAtom ruleAtom = new RuleAtom(nonGroundRule, substitution);
		RuleAtom.RuleAtomData substitutionFromRuleAtom = (RuleAtom.RuleAtomData) ((ConstantTerm<?>) ruleAtom.getTerms().get(0)).getObject();
		assertEquals(substitution, substitutionFromRuleAtom.getSubstitution());
	}

	@Test
	public void substitutionWithFunctionTermsObtainableFromRuleAtom() {
		Rule<Head> rule = PARSER.parse("q(X) :- p(X,Y).").getRules().get(0);
		CompiledRule nonGroundRule = InternalRule.fromNormalRule(NormalRuleImpl.fromBasicRule(rule));
		// Build substitution X -> b(a,a), Y -> b(b(a,a),b(a,a)).
		BasicAtom atomForSpecialize = Atoms.newBasicAtom(Predicates.getPredicate("p", 2), X, Y);
		ConstantTerm<String> aTerm = Terms.newSymbolicConstant("a");
		Instance instanceForSpecialize = new Instance(Terms.newFunctionTerm("b", aTerm, aTerm),
			Terms.newFunctionTerm("b",
				Terms.newFunctionTerm("b", aTerm, aTerm),
				Terms.newFunctionTerm("b", aTerm, aTerm)));
		Substitution substitution = BasicSubstitution.specializeSubstitution(
			atomForSpecialize, instanceForSpecialize, BasicSubstitution.EMPTY_SUBSTITUTION);

		RuleAtom ruleAtom = new RuleAtom(nonGroundRule, substitution);
		RuleAtom.RuleAtomData substitutionFromRuleAtom = (RuleAtom.RuleAtomData) ((ConstantTerm<?>) ruleAtom.getTerms().get(0)).getObject();
		assertEquals(substitution, substitutionFromRuleAtom.getSubstitution());
	}
}