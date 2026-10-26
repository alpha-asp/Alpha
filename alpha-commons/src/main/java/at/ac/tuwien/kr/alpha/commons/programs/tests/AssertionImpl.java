package at.ac.tuwien.kr.alpha.commons.programs.tests;

import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.tests.Assertion;

class AssertionImpl implements Assertion {

	private final Mode mode;
	private final InputProgram verifier;

	AssertionImpl(final Mode mode, final InputProgram verifier) {
		this.mode = mode;
		this.verifier = verifier;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public InputProgram getVerifier() {
		return verifier;
	}

	@Override
	public String toString() {
		return  "assert" + mode.toString() + " {" + System.lineSeparator() + verifier.toString() + "}";
	}

}
