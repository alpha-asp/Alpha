package at.ac.tuwien.kr.alpha.api.rules.heads;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;

public interface NormalHead extends Head{

	// TODO should this be a BasicAtom?
	Atom getAtom();
	
	boolean isGround();
	
}
