package at.ac.tuwien.kr.alpha.commons.rules;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.literals.NormalLiteral;
import at.ac.tuwien.kr.alpha.api.programs.transforms.RuleVisitor;
import at.ac.tuwien.kr.alpha.api.rules.BasicRule;
import at.ac.tuwien.kr.alpha.api.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.commons.rules.heads.NormalHeadImpl;

/**
 * A rule that has a normal head, i.e. just one head atom, no disjunction or choice heads allowed.
 * Currently, any constructs such as aggregates, intervals, etc. in the rule body are allowed.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class NormalRuleImpl extends AbstractRule<NormalHead, NormalLiteral> implements NormalRule {

	public NormalRuleImpl(NormalHead head, List<NormalLiteral> body) {
		super(head, body);
	}

	public static NormalRuleImpl fromBasicRule(BasicRule rule) {
		Atom headAtom = null;
		if (!rule.isConstraint()) {
			headAtom = rule.getHead().getAtom();
		}
		return null;
		// TODO this method should go away - by contract, the body of a BasicRule can contain literals incompatible with NormalRule
		//return new NormalRuleImpl(headAtom != null ? new NormalHeadImpl(headAtom) : null, new ArrayList<>(rule.getBody()));
	}

	public boolean isGround() {
		if (!isConstraint() && !this.getHead().isGround()) {
			return false;
		}
		for (Literal bodyElement : this.getBody()) {
			if (!bodyElement.isGround()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Atom getHeadAtom() {
		return this.isConstraint() ? null : this.getHead().getAtom();
	}

	@Override
	public void accept(RuleVisitor visitor) {
		visitor.visit(this);
	}

}
