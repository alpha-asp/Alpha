package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.List;

/**
 * Represents an atom whose ground truth value(s) are independent of the current assignment.
 * Examples of such atoms are builtin atoms and external atoms.
 * Copyright (c) 2017, the Alpha Team.
 */
public abstract class FixedInterpretationAtom implements Literal{

	public abstract List<Substitution> getSubstitutions(Substitution partialSubstitution);
}
