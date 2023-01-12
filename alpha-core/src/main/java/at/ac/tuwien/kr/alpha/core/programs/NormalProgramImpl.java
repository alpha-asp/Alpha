package at.ac.tuwien.kr.alpha.core.programs;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.core.rules.NormalRuleImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * A program that only contains NormalRules.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class NormalProgramImpl extends AbstractProgram<NormalRule> implements NormalProgram {

	public NormalProgramImpl(List<NormalRule> rules, List<Atom> facts, InlineDirectives inlineDirectives, boolean containsWeakConstraints) {
		super(rules, facts, inlineDirectives, containsWeakConstraints);
	}

	public static NormalProgramImpl fromInputProgram(ASPCore2Program inputProgram) {
		List<NormalRule> normalRules = new ArrayList<>();
		for (Rule<Head> r : inputProgram.getRules()) {
			normalRules.add(NormalRuleImpl.fromBasicRule(r));
		}
		return new NormalProgramImpl(normalRules, inputProgram.getFacts(), inputProgram.getInlineDirectives(), inputProgram.containsWeakConstraints());
	}

}
