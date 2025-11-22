package at.ac.tuwien.kr.alpha.api.programs.rules.heads;

import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.api.programs.terms.VariableTerm;

import java.util.List;

/**
 * Rule head implementation representing the head of an Evolog "Action Rule".
 * In addition to a regular head atom, an action head encodes an action name (the name of an action function to be called
 * when the rule fires) as well as a list input terms to said action function and one variable representing the result
 * (i.e. output term) of the action function.
 * <p>
 * Copyright (c) 2024, the Alpha Team.
 */
public interface ActionHead extends NormalHead {

	String getActionName();

	List<Term> getActionInputTerms();

	VariableTerm getActionOutputTerm();

}
