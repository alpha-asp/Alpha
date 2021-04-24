package at.ac.tuwien.kr.alpha.commons.rules;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.literals.ASPCore2Literal;
import at.ac.tuwien.kr.alpha.api.programs.transforms.ASPCore2RuleVisitor;
import at.ac.tuwien.kr.alpha.api.rules.BasicRule;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;

public class BasicRuleImpl extends AbstractRule<NormalHead, ASPCore2Literal> implements BasicRule{

	BasicRuleImpl(NormalHead head, List<ASPCore2Literal> body) {
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
