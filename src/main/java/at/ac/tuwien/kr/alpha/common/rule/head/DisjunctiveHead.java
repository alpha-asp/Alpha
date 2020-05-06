package at.ac.tuwien.kr.alpha.common.rule.head;

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

	@Override
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.disjunctiveAtoms == null) ? 0 : this.disjunctiveAtoms.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DisjunctiveHead)) {
			return false;
		}
		DisjunctiveHead other = (DisjunctiveHead) obj;
		if (this.disjunctiveAtoms == null) {
			if (other.disjunctiveAtoms != null) {
				return false;
			}
		} else if (!this.disjunctiveAtoms.equals(other.disjunctiveAtoms)) {
			return false;
		}
		return true;
	}

}
