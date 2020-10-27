package at.ac.tuwien.kr.alpha.common.program;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.AtomImpl;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.NormalRule;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;

/**
 * A program that only contains NormalRules.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class NormalProgram extends AbstractProgram<NormalRule> {

	public NormalProgram(List<NormalRule> rules, List<? extends AtomImpl> facts, InlineDirectives inlineDirectives) {
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
