package at.ac.tuwien.kr.alpha.api.programs.rules.heads;

import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.api.programs.terms.VariableTerm;

import java.util.List;

public interface ActionHead extends NormalHead {

	String getActionName();

	List<Term> getActionInputTerms();

	VariableTerm getActionOutputTerm();

}
