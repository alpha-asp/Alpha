package at.ac.tuwien.kr.alpha.core.actions;

import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.api.grounder.Substitution;

import java.util.List;

public interface ActionExecutionService {

	ActionWitness execute(String actionName, int sourceRuleId, Substitution sourceRuleInstance, List<Term> inputTerms);
	
}
