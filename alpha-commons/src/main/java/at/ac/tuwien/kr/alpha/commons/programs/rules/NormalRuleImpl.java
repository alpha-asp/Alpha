package at.ac.tuwien.kr.alpha.commons.programs.rules;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.NormalHead;

/**
 * A rule that has a normal head, i.e. just one head atom, no disjunction or choice heads allowed.
 * Currently, any constructs such as aggregates, intervals, etc. in the rule body are allowed.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
class NormalRuleImpl extends AbstractRule<NormalHead> implements NormalRule {

	NormalRuleImpl(NormalHead head, List<Literal> body) {
		super(head, body);
	}

}
