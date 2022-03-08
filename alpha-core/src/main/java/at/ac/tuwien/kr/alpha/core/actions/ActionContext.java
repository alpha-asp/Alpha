package at.ac.tuwien.kr.alpha.core.actions;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.terms.Term;

public interface ActionContext {

	ActionWitness execute(String actionName, int sourceRuleId, Substitution sourceRuleInstance, List<Term> inputTerms);
	
}
