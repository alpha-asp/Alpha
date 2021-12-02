package at.ac.tuwien.kr.alpha.api.rules.heads;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;

/**
 * The "standard case" of a rule head, representing the head of a rule which derives a new {@link BasicAtom}.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface NormalHead extends Head {

	BasicAtom getAtom();

	boolean isGround();

}
