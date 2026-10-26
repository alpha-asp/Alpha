package at.ac.tuwien.kr.alpha.api.programs.actions;

import at.ac.tuwien.kr.alpha.api.programs.terms.ActionResultTerm;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;

import java.util.List;

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
	ActionResultTerm<?> execute(List<Term> input);

}
