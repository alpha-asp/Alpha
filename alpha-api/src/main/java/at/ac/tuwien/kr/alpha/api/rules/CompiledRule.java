package at.ac.tuwien.kr.alpha.api.rules;

import java.util.List;
import java.util.Set;

import at.ac.tuwien.kr.alpha.api.grounder.RuleGroundingInfo;
import at.ac.tuwien.kr.alpha.api.program.Literal;
import at.ac.tuwien.kr.alpha.api.program.Predicate;

public interface CompiledRule extends Rule<NormalHead> {
	
	int getRuleId();
	
	List<Predicate> getOccurringPredicates();
	
	Set<Literal> getPositiveBody();
	
	Set<Literal> getNegativeBody();
	
	RuleGroundingInfo getGroundingInfo();
	
	CompiledRule renameVariables(String str);

}
