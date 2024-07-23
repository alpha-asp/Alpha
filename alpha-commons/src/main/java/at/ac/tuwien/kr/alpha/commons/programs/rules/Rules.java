package at.ac.tuwien.kr.alpha.commons.programs.rules;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.commons.util.Util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public final class Rules {

	private Rules() {
		throw new AssertionError("Cannot instantiate utility class!");
	}

	public static Rule<Head> newRule(Head head, Set<Literal> body) {
		return new BasicRule(head, body);
	}

	public static Rule<Head> newRule(Head head, Literal... body) {
		Set<Literal> bodyLst = new LinkedHashSet<>(Arrays.asList(body));
		return new BasicRule(head, bodyLst);
	}

	public static NormalRule newNormalRule(NormalHead head, Set<Literal> body) {
		return new NormalRuleImpl(head, body);
	}

	public static NormalRule newNormalRule(NormalHead head, Literal... body) {
		Set<Literal> bodyLst = new LinkedHashSet<>(Arrays.asList(body));
		return new NormalRuleImpl(head, bodyLst);
	}

	public static NormalRule toNormalRule(Rule<Head> rule) {
		BasicAtom headAtom = null;
		if (!rule.isConstraint()) {
			if (!(rule.getHead() instanceof NormalHead)) {
				throw Util.oops("Trying to construct a NormalRule from rule with non-normal head! Head type is: " + rule.getHead().getClass().getSimpleName());
			}

		}
		return newNormalRule(rule.isConstraint() ? null : (NormalHead) rule.getHead(), new LinkedHashSet<>(rule.getBody()));
	}

}
