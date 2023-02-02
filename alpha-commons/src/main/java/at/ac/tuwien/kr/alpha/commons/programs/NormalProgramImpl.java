package at.ac.tuwien.kr.alpha.commons.programs;

import at.ac.tuwien.kr.alpha.api.programs.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.rules.NormalRule;

import java.util.List;

/**
 * A program that only contains NormalRules.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
class NormalProgramImpl extends AbstractProgram<NormalRule> implements NormalProgram {

	NormalProgramImpl(List<NormalRule> rules, List<Atom> facts, InlineDirectives inlineDirectives, boolean containsWeakConstraints) {
		super(rules, facts, inlineDirectives, containsWeakConstraints);
	}

}
