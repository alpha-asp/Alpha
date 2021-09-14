package at.ac.tuwien.kr.alpha.api.rules.heads;

import at.ac.tuwien.kr.alpha.api.programs.atoms.ActionAtom;

public interface ActionHead extends NormalHead {
	
	@Override
	ActionAtom getAtom();

}
