package at.ac.tuwien.kr.alpha.api.programs.transforms;

import at.ac.tuwien.kr.alpha.api.rules.ASPCore2Rule;
import at.ac.tuwien.kr.alpha.api.rules.BasicRule;
import at.ac.tuwien.kr.alpha.api.rules.ChoiceRule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;

public interface ASPCore2RuleVisitor {

	void visit(ASPCore2Rule<? extends Head> rule);

	void visit(ChoiceRule rule);

	void visit(BasicRule rule);

}
