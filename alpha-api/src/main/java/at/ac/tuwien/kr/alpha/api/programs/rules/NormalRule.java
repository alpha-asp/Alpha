package at.ac.tuwien.kr.alpha.api.programs.rules;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.NormalHead;

/**
 * A {@link Rule} with a {@link NormalHead}.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface NormalRule extends Rule<NormalHead> {

	default BasicAtom getHeadAtom() {
		return this.isConstraint() ? null : this.getHead().getAtom();
	}

}
