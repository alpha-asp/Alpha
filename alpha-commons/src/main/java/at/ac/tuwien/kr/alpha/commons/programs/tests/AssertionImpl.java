package at.ac.tuwien.kr.alpha.commons.programs.tests;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.tests.Assertion;

class AssertionImpl implements Assertion {

	private final Mode mode;
	private final ASPCore2Program verifier;

	AssertionImpl(final Mode mode, final ASPCore2Program verifier) {
		this.mode = mode;
		this.verifier = verifier;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public ASPCore2Program getVerifier() {
		return verifier;
	}

	@Override
	public String toString() {
		return  "assert" + mode.toString() + " {" + System.lineSeparator() + verifier.toString() + "}";
	}

}
