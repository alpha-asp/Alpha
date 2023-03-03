package at.ac.tuwien.kr.alpha.api.programs.tests;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;

public interface Assertion {
	
	enum Mode {
		FOR_ALL("forAll"),
		FOR_SOME("forSome");

		private final String text;

		Mode(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}

	}

	Mode getMode();

	ASPCore2Program getVerifier();


}
