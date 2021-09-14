package at.ac.tuwien.kr.alpha.api.rules.heads;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;

public interface NormalHead extends Head{

	BasicAtom getAtom();
	
	boolean isGround();
	
}
