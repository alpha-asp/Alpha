package at.ac.tuwien.kr.alpha.core.programs;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.commons.rules.Rules;

/**
 * A program that only contains NormalRules.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class NormalProgramImpl extends AbstractProgram<NormalRule> implements NormalProgram {

	public NormalProgramImpl(List<NormalRule> rules, List<Atom> facts, InlineDirectives inlineDirectives) {
		super(rules, facts, inlineDirectives);
	}

	public static NormalProgramImpl fromInputProgram(InputProgram inputProgram) {
		List<NormalRule> normalRules = new ArrayList<>();
		for (Rule<Head> r : inputProgram.getRules()) {
			if (!r.isConstraint() && !(r.getHead() instanceof NormalHead)) {
				throw new IllegalArgumentException("Cannot create normal rule for unsupported head type " + r.getHead().getClass().getSimpleName());
			}
			normalRules.add(Rules.newNormalRule((NormalHead) r.getHead(), r.getBody()));
		}
		return new NormalProgramImpl(normalRules, inputProgram.getFacts(), inputProgram.getInlineDirectives());
	}

}
