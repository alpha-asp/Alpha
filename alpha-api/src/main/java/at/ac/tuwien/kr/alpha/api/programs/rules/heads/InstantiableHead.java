package at.ac.tuwien.kr.alpha.api.programs.rules.heads;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.rules.RuleInstantiator;

public interface InstantiableHead extends Head {

	BasicAtom instantiate(RuleInstantiator instantiator, Substitution substitution);

}
