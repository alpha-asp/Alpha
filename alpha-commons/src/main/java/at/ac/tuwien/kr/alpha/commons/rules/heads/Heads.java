package at.ac.tuwien.kr.alpha.commons.rules.heads;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.actions.Action;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.heads.ActionHead;
import at.ac.tuwien.kr.alpha.api.rules.heads.ChoiceHead;
import at.ac.tuwien.kr.alpha.api.rules.heads.ChoiceHead.ChoiceElement;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.rules.heads.ChoiceHeadImpl.ChoiceElementImpl;

public final class Heads {

	private Heads() {
		throw new AssertionError("Cannot instantiate utility class");
	}

	public static NormalHead newNormalHead(BasicAtom atom) {
		return new NormalHeadImpl(atom);
	}

	public static ChoiceHead newChoiceHead(List<ChoiceElement> choiceElements, Term lowerBound, ComparisonOperator lowerOp, Term upperBound,
			ComparisonOperator upperOp) {
		return new ChoiceHeadImpl(choiceElements, lowerBound, lowerOp, upperBound, upperOp);
	}

	public static ChoiceElement newChoiceElement(BasicAtom choiceAtom, List<Literal> conditionLiterals) {
		return new ChoiceElementImpl(choiceAtom, conditionLiterals);
	}

	public static ActionHead newActionHead(BasicAtom atom, Action action, List<Term> actionInputTerms, VariableTerm actionResult) {
		return new ActionHeadImpl(atom, action, actionInputTerms, actionResult);
	}
}
