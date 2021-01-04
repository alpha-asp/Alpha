package at.ac.tuwien.kr.alpha.api.program;

import java.util.Set;

import at.ac.tuwien.kr.alpha.api.rules.ChoiceHead;
import at.ac.tuwien.kr.alpha.api.rules.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.api.rules.NormalHead;
import at.ac.tuwien.kr.alpha.api.rules.Rule;

public interface InputProgram extends Program{
	
	InlineDirectives getInlineDirectives();
	
	Set<Rule<ChoiceHead>> getChoiceRules();
	
	Set<Rule<DisjunctiveHead>> getDisjunctiveRules();
	
	Set<Rule<NormalHead>> getNormalRules();

}
