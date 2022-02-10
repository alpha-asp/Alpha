package at.ac.tuwien.kr.alpha.api.programs.actions;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;

/**
 * An action that gets executed as part of an action rule in an evolog program firing.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
@FunctionalInterface
public interface Action {

	/**
	 * @param input a list of (ground) terms constituting the input of the action
	 * @return a function term representing the result of executing the action
	 */
	FunctionTerm execute(List<Term> input);

}
