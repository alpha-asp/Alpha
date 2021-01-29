package at.ac.tuwien.kr.alpha.api.rules;

import at.ac.tuwien.kr.alpha.api.program.Atom;

public interface NormalHead extends Head{

	Atom getAtom();
	
	boolean isGround();
	
}
