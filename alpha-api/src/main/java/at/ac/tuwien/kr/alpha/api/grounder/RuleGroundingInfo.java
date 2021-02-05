package at.ac.tuwien.kr.alpha.api.grounder;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.program.Literal;

public interface RuleGroundingInfo {
	
	boolean hasFixedInstantiation();
	
	// TODO this should be an Optional
	RuleGroundingOrder getFixedGroundingOrder();
	
	List<Literal> getStartingLiterals();
	
	RuleGroundingOrder orderStartingFrom(Literal startingLiteral);
	
	void computeGroundingOrders();

}
