package at.ac.tuwien.kr.alpha.common.rule;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.head.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;

/**
 * A rule that has a normal head, i.e. just one head atom, no disjunction or choice heads allowed Currently, any constructs such as aggregates, intervals, etc.
 * in the rule body are allowed.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class NormalRule extends AbstractRule<NormalHead> {

	public NormalRule(NormalHead head, List<Literal> body) {
		super(head, body);
	}

	public static NormalRule fromBasicRule(BasicRule rule) {
		Atom headAtom = null;
		if (!rule.isConstraint()) {
			if (!rule.getHead().isNormal()) {
				throw Util.oops("Trying to construct a NormalRule from rule with non-normal head!");
			}
			if (rule.getHead() instanceof DisjunctiveHead) {
				headAtom = ((DisjunctiveHead) rule.getHead()).disjunctiveAtoms.get(0);
			} else if (rule.getHead() instanceof NormalHead) {
				headAtom = ((NormalHead) rule.getHead()).getAtom();
			} else {
				throw Util.oops("Trying to construct a rule with unsuppported head type: " + rule.getHead().getClass().getSimpleName());
			}
		}
		return new NormalRule(headAtom != null ? new NormalHead(headAtom) : null, new ArrayList<>(rule.getBody()));
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

	public Atom getHeadAtom() {
		return this.isConstraint() ? null : this.getHead().getAtom();
	}

}
