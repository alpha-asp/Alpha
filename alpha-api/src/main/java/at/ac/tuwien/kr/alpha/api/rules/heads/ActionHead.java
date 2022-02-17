package at.ac.tuwien.kr.alpha.api.rules.heads;

import java.util.List;
import java.util.function.Function;

import at.ac.tuwien.kr.alpha.api.programs.actions.Action;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;

public interface ActionHead extends NormalHead {

	Action getAction();
	
	List<Term> getActionInputTerms();
	
	VariableTerm getActionOutputTerm();

	ActionHead renameVariables(Function<String, String> mapping);

}
