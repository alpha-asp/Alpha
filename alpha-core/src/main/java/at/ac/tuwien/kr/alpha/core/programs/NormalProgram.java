package at.ac.tuwien.kr.alpha.core.programs;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.core.atoms.CoreAtom;
import at.ac.tuwien.kr.alpha.core.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.core.rules.BasicRule;
import at.ac.tuwien.kr.alpha.core.rules.NormalRule;

/**
 * A program that only contains NormalRules.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class NormalProgram extends AbstractProgram<NormalRule> {

	public NormalProgram(List<NormalRule> rules, List<CoreAtom> facts, InlineDirectives inlineDirectives) {
		super(rules, facts, inlineDirectives);
	}

	public static NormalProgram fromInputProgram(InputProgram inputProgram) {
		List<NormalRule> normalRules = new ArrayList<>();
		for (BasicRule r : inputProgram.getRules()) {
			normalRules.add(NormalRule.fromBasicRule(r));
		}
		return new NormalProgram(normalRules, inputProgram.getFacts(), inputProgram.getInlineDirectives());
	}

}
