package at.ac.tuwien.kr.alpha.api.programs.atoms;

import at.ac.tuwien.kr.alpha.api.programs.VariableNormalizableAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.BasicLiteral;

public interface BasicAtom extends Atom, VariableNormalizableAtom {
	
	@Override
	BasicLiteral toLiteral(boolean positive);

}
