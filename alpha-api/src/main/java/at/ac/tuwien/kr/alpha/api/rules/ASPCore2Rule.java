package at.ac.tuwien.kr.alpha.api.rules;

import at.ac.tuwien.kr.alpha.api.programs.literals.ASPCore2Literal;
import at.ac.tuwien.kr.alpha.api.programs.transforms.ASPCore2RuleVisitor;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;

public interface ASPCore2Rule<H extends Head> extends Rule<H, ASPCore2Literal> {

	void accept(ASPCore2RuleVisitor visitor);
	
}
