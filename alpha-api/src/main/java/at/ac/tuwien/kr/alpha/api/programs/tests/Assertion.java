package at.ac.tuwien.kr.alpha.api.programs.tests;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;

public interface Assertion {
	
	enum Mode {
		FOR_ALL,
		FOR_SOME;
	}

	Mode getMode();

	ASPCore2Program getVerifier();

}
