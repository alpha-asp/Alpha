package at.ac.tuwien.kr.alpha.api.rules.heads;

import java.util.List;
import java.util.function.Function;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.terms.Term;

/**
 * A choice head as defined by the ASP Core 2 Standard.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface ChoiceHead extends Head {

	Term getLowerBound();

	Term getUpperBound();
	
	ComparisonOperator getLowerOperator();
	
	ComparisonOperator getUpperOperator();

	List<ChoiceElement> getChoiceElements();

	public static interface ChoiceElement {

		BasicAtom getChoiceAtom();

		List<Literal> getConditionLiterals();

	}

	ChoiceHead renameVariables(Function<String, String> mapping);

}
