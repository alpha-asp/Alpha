package at.ac.tuwien.kr.alpha.common;

public class OrdinaryAssignment {
	private final int atom;
	private final boolean truthValue;

	public OrdinaryAssignment(int atom, boolean truthValue) {
		this.atom = atom;
		this.truthValue = truthValue;
	}

	public int getAtom() {
		return atom;
	}

	public boolean getTruthValue() {
		return truthValue;
	}
}