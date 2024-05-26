package at.ac.tuwien.kr.alpha.api.programs.rules;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.InstantiableHead;

public interface RuleInstantiator {

    BasicAtom instantiate(InstantiableHead ruleHead, Substitution substitution);

}
