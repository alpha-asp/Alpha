package at.ac.tuwien.kr.alpha.commons.programs.rules.heads;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.ChoiceHead;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.ChoiceHead.ChoiceElement;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.commons.programs.rules.heads.ChoiceHeadImpl.ChoiceElementImpl;

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
	
}
