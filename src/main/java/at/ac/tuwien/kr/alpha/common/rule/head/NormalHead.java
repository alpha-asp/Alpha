package at.ac.tuwien.kr.alpha.common.rule.head;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;

/**
 * Copyright (c) 2019, the Alpha Team.
 */
public class NormalHead extends Head {

	private final Atom atom;

	public NormalHead(Atom atom) {
		this.atom = atom;
	}

	// TODO ideally this should be inherited from Head, but need to check how to handle choiceHeads
	public boolean isGround() {
		return this.atom.isGround();
	}

	@Override
	public boolean isNormal() {
		return true;
	}

	public Atom getAtom() {
		return this.atom;
	}

	@Override
	public String toString() {
		return this.atom.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.atom == null) ? 0 : this.atom.hashCode());
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
		if (!(obj instanceof NormalHead)) {
			return false;
		}
		NormalHead other = (NormalHead) obj;
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
