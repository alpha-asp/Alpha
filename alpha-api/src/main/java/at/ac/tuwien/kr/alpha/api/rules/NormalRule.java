package at.ac.tuwien.kr.alpha.api.rules;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;

public interface NormalRule extends Rule<NormalHead> {
	
	BasicAtom getHeadAtom();

}
