package at.ac.tuwien.kr.alpha.core.rules;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
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
		List<Literal> bodyLiterals = new ArrayList<>();
		for (Literal lit : body) {
			bodyLiterals.add(lit);
		}
		return new BasicRule(head, bodyLiterals);
	}

}
