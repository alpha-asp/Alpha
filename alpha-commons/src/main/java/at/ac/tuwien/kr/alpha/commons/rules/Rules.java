package at.ac.tuwien.kr.alpha.commons.rules;

import java.util.LinkedHashSet;
import java.util.Set;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;

public final class Rules {

	private Rules() {
		throw new AssertionError("Cannot instantiate utility class!");
	}

	public static Rule<Head> newRule(BasicAtom headAtom, Literal... body) {
		NormalHead head = Heads.newNormalHead(headAtom);
		Set<Literal> bodyLiterals = new LinkedHashSet<>();
		for (Literal lit : body) {
			bodyLiterals.add(lit);
		}
		return Rules.newRule(head, bodyLiterals);
	}

	public static Rule<Head> newRule(BasicAtom headAtom, Set<Literal> body) {
		NormalHead head = Heads.newNormalHead(headAtom);
		return Rules.newRule(head, body);
	}

	public static Rule<Head> newRule(Head head, Set<Literal> body) {
		return new BasicRule(head, body);
	}

	public static Rule<Head> newConstraint(Set<Literal> body) {
		return new BasicRule(null, body);
	}

	public static NormalRule newNormalRule(NormalHead head, Set<Literal> body) {
		return new NormalRuleImpl(head, body);
	}

}
