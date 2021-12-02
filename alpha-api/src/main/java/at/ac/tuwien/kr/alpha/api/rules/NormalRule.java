package at.ac.tuwien.kr.alpha.api.rules;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;

/**
 * A {@link Rule} with a {@link NormalHead}.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface NormalRule extends Rule<NormalHead> {

	BasicAtom getHeadAtom();

}
