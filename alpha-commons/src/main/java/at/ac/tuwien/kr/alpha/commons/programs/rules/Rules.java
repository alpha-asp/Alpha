package at.ac.tuwien.kr.alpha.commons.programs.rules;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.commons.programs.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.commons.util.Util;

public final class Rules {

	private Rules() {
		throw new AssertionError("Cannot instantiate utility class!");
	}

	public static Rule<Head> newRule(Head head, List<Literal> body) {
		return new BasicRule(head, body);
	}

	public static Rule<Head> newRule(Head head, Literal... body) {
		List<Literal> bodyLst = new ArrayList<>();
		for (Literal lit : body) {
			bodyLst.add(lit);
		}
		return new BasicRule(head, bodyLst);
	}

	public static NormalRule newNormalRule(NormalHead head, List<Literal> body) {
		return new NormalRuleImpl(head, body);
	}

	public static NormalRule newNormalRule(NormalHead head, Literal... body) {
		List<Literal> bodyLst = new ArrayList<>();
		for (Literal lit : body) {
			bodyLst.add(lit);
		}
		return new NormalRuleImpl(head, bodyLst);
	}

	public static NormalRule toNormalRule(Rule<Head> rule) {
		BasicAtom headAtom = null;
		if (!rule.isConstraint()) {
			if (!(rule.getHead() instanceof NormalHead)) {
				throw Util.oops("Trying to construct a NormalRule from rule with non-normal head! Head type is: " + rule.getHead().getClass().getSimpleName());
			}
			headAtom = ((NormalHead) rule.getHead()).getAtom();
		}
		return new NormalRuleImpl(headAtom != null ? Heads.newNormalHead(headAtom) : null, new ArrayList<>(rule.getBody()));
	}

}
