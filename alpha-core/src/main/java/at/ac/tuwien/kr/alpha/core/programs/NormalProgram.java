package at.ac.tuwien.kr.alpha.core.programs;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.programs.Program;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.rules.Head;
import at.ac.tuwien.kr.alpha.api.rules.NormalHead;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.core.rules.NormalRule;

/**
 * A program that only contains NormalRules.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class NormalProgram extends AbstractProgram<Rule<NormalHead>> implements Program<Rule<NormalHead>> {

	public NormalProgram(List<Rule<NormalHead>> rules, List<Atom> facts, InlineDirectives inlineDirectives) {
		super(rules, facts, inlineDirectives);
	}

	public static NormalProgram fromInputProgram(ASPCore2Program inputProgram) {
		List<Rule<NormalHead>> normalRules = new ArrayList<>();
		for (Rule<Head> r : inputProgram.getRules()) {
			normalRules.add(NormalRule.fromBasicRule(r));
		}
		return new NormalProgram(normalRules, inputProgram.getFacts(), inputProgram.getInlineDirectives());
	}

}
