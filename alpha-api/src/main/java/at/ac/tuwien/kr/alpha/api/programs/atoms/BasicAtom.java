package at.ac.tuwien.kr.alpha.api.programs.atoms;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.VariableNormalizableAtom;

public interface BasicAtom extends Atom, VariableNormalizableAtom {
	
	@Override
	BasicAtom substitute(Substitution substitution);

}
