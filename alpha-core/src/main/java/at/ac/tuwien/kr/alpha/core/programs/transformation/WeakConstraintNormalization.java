package at.ac.tuwien.kr.alpha.core.programs.transformation;


import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.commons.programs.Programs;
import at.ac.tuwien.kr.alpha.commons.programs.rules.Rules;
import at.ac.tuwien.kr.alpha.commons.programs.rules.WeakConstraint;
import at.ac.tuwien.kr.alpha.commons.programs.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.core.atoms.WeakConstraintAtom;

import java.util.ArrayList;

/**
 * Rewrites weak constraints into normal rules with special head atom.
 *
 * Copyright (c) 2020, the Alpha Team.
 */
public class WeakConstraintNormalization extends ProgramTransformation<ASPCore2Program, ASPCore2Program> {
	@Override
	public ASPCore2Program apply(ASPCore2Program inputProgram) {
		Programs.ASPCore2ProgramBuilder builder = Programs.builder();
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
			Rule<Head> rewrittenWeakConstraint = Rules.newRule(weakConstraintHead, new ArrayList<>(rule.getBody()));
			builder.addRule(rewrittenWeakConstraint);
		}
		return builder.build();
	}
}
