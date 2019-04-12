package at.ac.tuwien.kr.alpha.common.rule.head.impl;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.rule.head.Head;

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

}
