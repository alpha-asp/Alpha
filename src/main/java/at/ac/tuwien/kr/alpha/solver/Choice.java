package at.ac.tuwien.kr.alpha.solver;

class Choice {
	private final int atom;
	private final boolean value;
	private final boolean backtracked;

	Choice(int atom, boolean value, boolean backtracked) {
		this.atom = atom;
		this.value = value;
		this.backtracked = backtracked;
	}

	public int getAtom() {
		return atom;
	}

	public boolean getValue() {
		return value;
	}

	public boolean isBacktracked() {
		return backtracked;
	}

	@Override
	public String toString() {
		return atom + "=" + (value ? "TRUE" : "FALSE");
	}
}
