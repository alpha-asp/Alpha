package at.ac.tuwien.kr.alpha.common;

/**
 * Represents the head of a rule, i.e., either a choice or a disjunction of atoms, but not both.
 * For normal rules the disjunction contains only one atom.
 *
 * Copyright (c) 2017, the Alpha Team.
 */
public abstract class Head {

	public abstract boolean isNormal();
}
