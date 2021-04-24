package at.ac.tuwien.kr.alpha.core.programs;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.rules.NormalRule;

/**
 * A program that only contains NormalRules.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
// TODO this implementation is obsolete!
public class NormalProgramImpl extends AbstractProgram<NormalRule> implements NormalProgram {

	public NormalProgramImpl(List<NormalRule> rules, List<Atom> facts, InlineDirectives inlineDirectives) {
		super(rules, facts, inlineDirectives);
	}

	// TODO the existence of this method is problematic.
	// ASPCore2Program by contract has constructs not representable in a NormalProgram,
	// translation my only happen through proper normalization
	// potentially move to TestUtil since this seems to be onbly used in tests
	public static NormalProgramImpl fromInputProgram(ASPCore2Program inputProgram) {
//		List<NormalRule> normalRules = new ArrayList<>();
//		for (ASPCore2Rule<? extends Head> r : inputProgram.getRules()) {
//			normalRules.add(NormalRuleImpl.fromBasicRule(r));
//		}
//		return new NormalProgramImpl(normalRules, inputProgram.getFacts(), inputProgram.getInlineDirectives());
		return null; // only to get it compiling, needs proper attention
	}

}
