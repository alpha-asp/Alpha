package at.ac.tuwien.kr.alpha.api.programs.literals;

import at.ac.tuwien.kr.alpha.api.programs.atoms.ModuleAtom;
import at.ac.tuwien.kr.alpha.api.grounder.Substitution;

public interface ModuleLiteral extends Literal{

	@Override
	ModuleAtom getAtom();

	@Override
	ModuleLiteral negate();

	@Override
	ModuleLiteral substitute(Substitution substitution);

}
