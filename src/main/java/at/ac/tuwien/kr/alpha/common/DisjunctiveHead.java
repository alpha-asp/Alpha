package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;

import java.util.List;

import static at.ac.tuwien.kr.alpha.Util.join;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class DisjunctiveHead extends Head {
	public final List<Atom> disjunctiveAtoms;

	public DisjunctiveHead(List<Atom> disjunctiveAtoms) {
		this.disjunctiveAtoms = disjunctiveAtoms;
		if (disjunctiveAtoms != null && disjunctiveAtoms.size() > 1) {
			throw new UnsupportedOperationException("Disjunction in rule heads is not yet supported");
		}
	}

	public boolean isNormal() {
		return disjunctiveAtoms != null && disjunctiveAtoms.size() <= 1;
	}

	@Override
	public String toString() {
		if (isNormal()) {
			return disjunctiveAtoms.get(0).toString();
		}
		return join("", disjunctiveAtoms, " | ", "");
	}

	public boolean isGround() {
		for (Atom atom : disjunctiveAtoms) {
			if (!atom.isGround()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Atom> getAtoms() {
		return this.disjunctiveAtoms;
	}
}
