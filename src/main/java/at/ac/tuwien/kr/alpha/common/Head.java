package at.ac.tuwien.kr.alpha.common;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;

/**
 * Represents the head of a rule, i.e., either a choice or a disjunction of
 * atoms, but not both. For normal rules the disjunction contains only one atom.
 *
 * Copyright (c) 2017, the Alpha Team.
 */
public abstract class Head {
	public abstract boolean isNormal();

	/**
	 * (Needed for dependency graph creation)
	 * 
	 * @return a list of all atoms in this rule head.
	 */
	public abstract List<Atom> getAtoms();
}
