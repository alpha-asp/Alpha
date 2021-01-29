package at.ac.tuwien.kr.alpha.api.rules;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.program.Atom;
import at.ac.tuwien.kr.alpha.api.program.Literal;
import at.ac.tuwien.kr.alpha.api.terms.Term;

public interface ChoiceHead extends Head {

	Term getLowerBound();

	Term getUpperBound();

	List<ChoiceElement> getChoiceElements();

	public static interface ChoiceElement {

		Atom getChoiceAtom();

		List<Literal> getConditionLiterals();

	}

}
