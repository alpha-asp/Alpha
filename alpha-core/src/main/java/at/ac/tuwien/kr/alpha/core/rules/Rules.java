package at.ac.tuwien.kr.alpha.core.rules;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import at.ac.tuwien.kr.alpha.api.program.Atom;
import at.ac.tuwien.kr.alpha.api.program.Literal;
import at.ac.tuwien.kr.alpha.api.rules.ChoiceHead;
import at.ac.tuwien.kr.alpha.api.rules.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.api.rules.NormalHead;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.core.rules.heads.DisjunctiveHeadImpl;
import at.ac.tuwien.kr.alpha.core.rules.heads.NormalHeadImpl;

public class Rules {

	public static Rule<DisjunctiveHead> newDisjunctiveRule(List<Atom> disjunctiveAtoms, Set<Literal> body) {
		return new RuleImpl<>(new DisjunctiveHeadImpl(disjunctiveAtoms), body);
	}

	// TODO factory method for choice heads
	public static Rule<ChoiceHead> newChoiceRule(ChoiceHead head, Set<Literal> body) {
		return new RuleImpl<>(head, body);
	}

	public static Rule<NormalHead> newNormalRule(Atom headAtom, Set<Literal> body) {
		return new RuleImpl<>(new NormalHeadImpl(Optional.of(headAtom)), body);
	}

	public static Rule<NormalHead> newConstraint(Set<Literal> body) {
		return new RuleImpl<>(new NormalHeadImpl(Optional.empty()), body);
	}

}
