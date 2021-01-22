package at.ac.tuwien.kr.alpha.core.rules;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.program.Atom;
import at.ac.tuwien.kr.alpha.api.program.Literal;
import at.ac.tuwien.kr.alpha.api.rules.ChoiceHead;
import at.ac.tuwien.kr.alpha.api.rules.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.api.rules.NormalHead;
import at.ac.tuwien.kr.alpha.api.rules.Rule;

public class Rules {

	public static Rule<DisjunctiveHead> newDisjunctiveRule(List<Atom> disjunctiveAtoms, List<Literal> body) {
		return null; // TODO
	}

	public static Rule<ChoiceHead> newChoiceRule(ChoiceHead head, List<Literal> body) {
		return null; // TODO (also better params)
	}

	public static Rule<NormalHead> newNormalRule(Atom headAtom, List<Literal> body) {
		return null; // TODO
	}

	public static Rule<NormalHead> newConstraint(List<Literal> body) {
		return null; // TODO
	}

}
