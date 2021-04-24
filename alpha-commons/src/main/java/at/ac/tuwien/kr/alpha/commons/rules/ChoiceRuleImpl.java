package at.ac.tuwien.kr.alpha.commons.rules;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.literals.ASPCore2Literal;
import at.ac.tuwien.kr.alpha.api.programs.transforms.ASPCore2RuleVisitor;
import at.ac.tuwien.kr.alpha.api.rules.ChoiceRule;
import at.ac.tuwien.kr.alpha.api.rules.heads.ChoiceHead;

class ChoiceRuleImpl extends AbstractRule<ChoiceHead, ASPCore2Literal> implements ChoiceRule{

	ChoiceRuleImpl(ChoiceHead head, List<ASPCore2Literal> body) {
		super(head, body);
	}

	@Override
	public Atom getHeadAtom() {
		throw new UnsupportedOperationException("This is bullshit!");
	}

	@Override
	public void accept(ASPCore2RuleVisitor visitor) {
		visitor.visit(this);
	}
	
}
