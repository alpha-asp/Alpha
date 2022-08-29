package at.ac.tuwien.kr.alpha.core.actions;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.terms.Term;

public class ActionWitness {

	private final int ruleId;
	private final Substitution groundSubstitution;
	private final String actionName;
	private final List<Term> actionInput;
	private final Term actionResult;

	public ActionWitness(int ruleId, Substitution groundSubstitution, String actionName, List<Term> actionInput, Term actionResult) {
		this.ruleId = ruleId;
		this.groundSubstitution = groundSubstitution;
		this.actionName = actionName;
		this.actionInput = actionInput;
		this.actionResult = actionResult;
	}

	public int getRuleId() {
		return ruleId;
	}

	public Substitution getGroundSubstitution() {
		return groundSubstitution;
	}

	public String getActionName() {
		return actionName;
	}

	public List<Term> getActionInput() {
		return actionInput;
	}

	public Term getActionResult() {
		return actionResult;
	}
	
}
