package at.ac.tuwien.kr.alpha.commons.rules;

import java.util.Set;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;

/**
 * A rule that has a normal head, i.e. just one head atom, no disjunction or choice heads allowed.
 * Currently, any constructs such as aggregates, intervals, etc. in the rule body are allowed.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
class NormalRuleImpl extends AbstractRule<NormalHead> implements NormalRule {

	NormalRuleImpl(NormalHead head, Set<Literal> body) {
		super(head, body);
	}

	@Override
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
	public BasicAtom getHeadAtom() {
		return this.isConstraint() ? null : this.getHead().getAtom();
	}

}
