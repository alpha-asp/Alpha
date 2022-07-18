package at.ac.tuwien.kr.alpha.core.solver;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;

public class AtomizedChoice {
	private final Atom atom;
	private final boolean truthValue;
	private final boolean backtracked;

	public AtomizedChoice(Atom atom, boolean truthValue, boolean backtracked) {
		this.atom = atom;
		this.truthValue = truthValue;
		this.backtracked = backtracked;
	}

	public Atom getAtom() {
		return atom;
	}

	public boolean getTruthValue() {
		return truthValue;
	}

	public boolean isBacktracked() {
		return backtracked;
	}
}
