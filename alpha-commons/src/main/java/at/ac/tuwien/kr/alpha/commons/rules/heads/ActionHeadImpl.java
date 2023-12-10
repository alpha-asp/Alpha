package at.ac.tuwien.kr.alpha.commons.rules.heads;

import java.util.Collections;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.rules.RuleInstantiator;
import at.ac.tuwien.kr.alpha.api.rules.heads.ActionHead;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import org.apache.commons.lang3.StringUtils;

class ActionHeadImpl implements ActionHead {

	private final BasicAtom atom;
	private final String actionName;
	private final List<Term> actionInputTerms;
	private final VariableTerm actionOutputTerm;

	ActionHeadImpl(BasicAtom atom, String actionName, List<Term> actionInputTerms, VariableTerm actionOutputTerm) {
		this.atom = atom;
		this.actionName = actionName;
		this.actionInputTerms = Collections.unmodifiableList(actionInputTerms);
		this.actionOutputTerm = actionOutputTerm;
	}

	@Override
	public BasicAtom getAtom() {
		return atom;
	}

	@Override
	public boolean isGround() {
		// TODO: an action head is conceptually a basic one with an (interpreted) function term
		return false;
	}

	@Override
	public BasicAtom instantiate(RuleInstantiator instantiator, Substitution substitution) {
		return instantiator.instantiate(this, substitution);
	}

	@Override
	public String getActionName() {
		return actionName;
	}

	@Override
	public List<Term> getActionInputTerms() {
		return actionInputTerms;
	}

	@Override
	public VariableTerm getActionOutputTerm() {
		return actionOutputTerm;
	}

	public String toString() {
		return atom.toString() + " : @" + actionName + "(" + StringUtils.join(actionInputTerms, ", ") + ") = " + actionOutputTerm;
	}

}
