package at.ac.tuwien.kr.alpha.core.actions;

import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.api.grounder.Substitution;

import java.util.List;

public interface ActionExecutionService {

	/**
	 * Executes the action with the given name using the provided input terms.
	 * If the action has been executed before with the same parameters, a cached
	 * result is returned.
	 * The general contract is that an action function is only called once per
	 * unique firing ground instance of an action rule
	 * in order to ensure that, regardless of how often a solver evaluates an action
	 * rule, the associated side effect is
	 * only executed once.
	 *
	 * @param actionName         the name of the action function to call
	 * @param sourceRuleId       the id of the rule which is fired to cause the
	 *                           action execution
	 * @param sourceRuleInstance the ground substitution used in firing the source
	 *                           rule
	 * @param inputTerms         the input terms for the action (a subset of
	 *                           substition "sourceRuleInstance")
	 * @return an ActionWitness wrapping the input to and output of the executed
	 *         action function.
	 */
	ActionWitness execute(String actionName, int sourceRuleId, Substitution sourceRuleInstance, List<Term> inputTerms);

}
