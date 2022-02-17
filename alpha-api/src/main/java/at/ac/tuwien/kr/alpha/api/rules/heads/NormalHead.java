package at.ac.tuwien.kr.alpha.api.rules.heads;

import java.util.function.Function;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;

/**
 * The "standard case" of a rule head, representing the head of a rule which derives a new {@link BasicAtom}.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface NormalHead extends InstantiableHead {

	BasicAtom getAtom();

	boolean isGround();

	NormalHead renameVariables(Function<String, String> mapping);

}
