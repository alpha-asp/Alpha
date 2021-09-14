package at.ac.tuwien.kr.alpha.commons.rules.heads;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;

/**
 * Represents a normal head, i.e., a head that is an Atom.
 * Copyright (c) 2019, the Alpha Team.
 */
class NormalHeadImpl implements NormalHead {

	private final BasicAtom atom;

	NormalHeadImpl(BasicAtom atom) {
		this.atom = atom;
	}

	// Note that at some point in the future it might make sense to have this method directly in Head
	@Override
	public boolean isGround() {
		return atom.isGround();
	}

	@Override
	public BasicAtom getAtom() {
		return atom;
	}

	@Override
	public String toString() {
		return atom.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((atom == null) ? 0 : atom.hashCode());
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
		if (!(obj instanceof NormalHeadImpl)) {
			return false;
		}
		NormalHeadImpl other = (NormalHeadImpl) obj;
		if (this.atom == null) {
			if (other.atom != null) {
				return false;
			}
		} else if (!this.atom.equals(other.atom)) {
			return false;
		}
		return true;
	}

}
