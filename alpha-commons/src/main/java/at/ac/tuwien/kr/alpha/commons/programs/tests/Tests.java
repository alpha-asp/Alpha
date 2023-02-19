package at.ac.tuwien.kr.alpha.commons.programs.tests;

import java.util.Set;
import java.util.function.IntPredicate;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.tests.Assertion;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestCase;

public final class Tests {

	private Tests() {
		throw new AssertionError("Cannot instantiate utility class!");
	}
	
	public static TestCase newTestCase(final String name, final IntPredicate answerSetCountVerifier, final Set<BasicAtom> input, final Set<Assertion> assertions) {
		return new TestCaseImpl(name, answerSetCountVerifier, input, assertions);
	}

	public static Assertion newAssertion(final Assertion.Mode mode, final ASPCore2Program verifier) {
		return new AssertionImpl(mode, verifier);
	}

}
