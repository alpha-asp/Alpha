package at.ac.tuwien.kr.alpha.core.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.actions.Action;
import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;

public class ActionContextImpl implements ActionContext {

	// TODO initialize from outside
	private final Map<String, Action> actions;
	private final Map<ActionInput, ActionWitness> actionRecord = new HashMap<>();

	public ActionContextImpl(Map<String, Action> actions) {
		this.actions = actions;
	}

	@Override
	public ActionWitness execute(String actionName, int sourceRuleId, Substitution sourceRuleInstance, List<Term> inputTerms) {
		// TODO maybe we want to stuff the whole action head plus substitution in here and do the whole instantiation here?
		ActionInput actInput = new ActionInput(actionName, sourceRuleId, sourceRuleInstance, inputTerms);
		return actionRecord.computeIfAbsent(actInput, this::execute);
	}

	private ActionWitness execute(ActionInput input) {
		Action action = actions.get(input.name);
		FunctionTerm result = action.execute(input.inputTerms);
		return new ActionWitness(input.sourceRule, input.instance, input.name, input.inputTerms, result);
	}

	private static class ActionInput {

		private final String name;
		private final int sourceRule;
		private final Substitution instance;
		private final List<Term> inputTerms;

		public ActionInput(String name, int sourceRule, Substitution instance, List<Term> inputTerms) {
			this.name = name;
			this.sourceRule = sourceRule;
			this.instance = instance;
			this.inputTerms = inputTerms;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((inputTerms == null) ? 0 : inputTerms.hashCode());
			result = prime * result + ((instance == null) ? 0 : instance.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + sourceRule;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ActionInput other = (ActionInput) obj;
			if (inputTerms == null) {
				if (other.inputTerms != null) {
					return false;
				}
			} else if (!inputTerms.equals(other.inputTerms)) {
				return false;
			}
			if (instance == null) {
				if (other.instance != null) {
					return false;
				}
			} else if (!instance.equals(other.instance)) {
				return false;
			}
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			if (sourceRule != other.sourceRule) {
				return false;
			}
			return true;
		}

	}

}
