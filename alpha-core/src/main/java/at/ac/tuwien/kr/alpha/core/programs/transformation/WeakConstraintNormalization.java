package at.ac.tuwien.kr.alpha.core.programs.transformation;


import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.core.atoms.WeakConstraintAtom;
import at.ac.tuwien.kr.alpha.core.programs.InputProgram;
import at.ac.tuwien.kr.alpha.core.rules.BasicRule;
import at.ac.tuwien.kr.alpha.core.rules.WeakConstraint;

import java.util.ArrayList;

/**
 * Rewrites weak constraints into normal rules with special head atom.
 *
 * Copyright (c) 2020, the Alpha Team.
 */
public class WeakConstraintNormalization extends ProgramTransformation<ASPCore2Program, ASPCore2Program> {
	@Override
	public ASPCore2Program apply(ASPCore2Program inputProgram) {
		InputProgram.Builder builder = new InputProgram.Builder();
		builder.addFacts(inputProgram.getFacts());
		builder.addInlineDirectives(inputProgram.getInlineDirectives());

		for (Rule<Head> rule : inputProgram.getRules()) {
			if (!(rule instanceof WeakConstraint)) {
				builder.addRule(rule);
				continue;
			}
			// Create rule with special head atom.
			WeakConstraint weakConstraint = (WeakConstraint)rule;
			Head weakConstraintHead = Heads.newNormalHead(WeakConstraintAtom.getInstance(weakConstraint.getWeight(), weakConstraint.getLevel(), weakConstraint.getTermList()));
			BasicRule rewrittenWeakConstraint = new BasicRule(weakConstraintHead, new ArrayList<>(rule.getBody()));
			builder.addRule(rewrittenWeakConstraint);
		}
		return builder.build();
	}
}
