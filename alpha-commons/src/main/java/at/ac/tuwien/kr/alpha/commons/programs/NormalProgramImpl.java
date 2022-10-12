package at.ac.tuwien.kr.alpha.commons.programs;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.commons.programs.rules.Rules;

/**
 * A program that only contains NormalRules.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class NormalProgramImpl extends AbstractProgram<NormalRule> implements NormalProgram {

	public NormalProgramImpl(List<NormalRule> rules, List<Atom> facts, InlineDirectives inlineDirectives) {
		super(rules, facts, inlineDirectives);
	}

	public static NormalProgramImpl fromInputProgram(ASPCore2Program inputProgram) {
		List<NormalRule> normalRules = new ArrayList<>();
		for (Rule<Head> r : inputProgram.getRules()) {
			normalRules.add(Rules.toNormalRule(r));
		}
		return new NormalProgramImpl(normalRules, inputProgram.getFacts(), inputProgram.getInlineDirectives());
	}

}
