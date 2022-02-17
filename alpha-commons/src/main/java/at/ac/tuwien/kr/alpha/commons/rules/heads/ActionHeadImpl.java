package at.ac.tuwien.kr.alpha.commons.rules.heads;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.actions.Action;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.rules.RuleInstantiator;
import at.ac.tuwien.kr.alpha.api.rules.heads.ActionHead;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;

class ActionHeadImpl implements ActionHead {

	private final BasicAtom atom;
	private final Action action;
	private final List<Term> actionInputTerms;
	private final VariableTerm actionOutputTerm;

	ActionHeadImpl(BasicAtom atom, Action action, List<Term> actionInputTerms, VariableTerm actionOutputTerm) {
		this.atom = atom;
		this.action = action;
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
	public Action getAction() {
		return action;
	}

	@Override
	public List<Term> getActionInputTerms() {
		return actionInputTerms;
	}

	@Override
	public VariableTerm getActionOutputTerm() {
		return actionOutputTerm;
	}

	@Override
	public ActionHead renameVariables(Function<String, String> mapping) {
		// TODO Auto-generated method stub
		return null;
	}

}
