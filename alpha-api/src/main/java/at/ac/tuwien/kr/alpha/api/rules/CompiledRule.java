package at.ac.tuwien.kr.alpha.api.rules;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.grounder.RuleGroundingInfo;
import at.ac.tuwien.kr.alpha.api.program.Predicate;

public interface CompiledRule extends Rule<NormalHead> {
	
	int getRuleId();
	
	List<Predicate> getOccurringPredicates();
	
	RuleGroundingInfo getGroundingInfo();
	
	CompiledRule renameVariables(String str);

	boolean isGround();
}
