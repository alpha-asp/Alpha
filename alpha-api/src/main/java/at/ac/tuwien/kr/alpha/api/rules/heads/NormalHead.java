package at.ac.tuwien.kr.alpha.api.rules.heads;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;

public interface NormalHead extends InstantiableHead {

	BasicAtom getAtom();

	boolean isGround();

}
