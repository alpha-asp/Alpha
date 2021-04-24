package at.ac.tuwien.kr.alpha.commons.rules;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.literals.ASPCore2Literal;
import at.ac.tuwien.kr.alpha.api.rules.BasicRule;
import at.ac.tuwien.kr.alpha.api.rules.ChoiceRule;
import at.ac.tuwien.kr.alpha.api.rules.heads.ChoiceHead;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;

public final class Rules {
	
	private Rules() {
		throw new AssertionError("Cannot instantiate utility class!");
	}
	
	public static ChoiceRule newChoiceRule(ChoiceHead head, List<ASPCore2Literal> body) {
		return new ChoiceRuleImpl(head, body);
	}
	
	public static BasicRule newBasicRule(NormalHead head, List<ASPCore2Literal> body) {
		return new BasicRuleImpl(head, body);
	}
	
}
