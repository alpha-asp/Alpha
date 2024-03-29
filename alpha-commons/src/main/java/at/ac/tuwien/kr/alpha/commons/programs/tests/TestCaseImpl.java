package at.ac.tuwien.kr.alpha.commons.programs.tests;

import java.util.List;
import java.util.Set;
import java.util.function.IntPredicate;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.tests.Assertion;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestCase;
import at.ac.tuwien.kr.alpha.commons.util.Util;

class TestCaseImpl implements TestCase {

	private final String name;
	private final IntPredicate answerSetCountVerifier;
	private final Set<BasicAtom> input;
	private final List<Assertion> assertions;

	TestCaseImpl(final String name, final IntPredicate answerSetCountVerifier, final Set<BasicAtom> input, final List<Assertion> assertions) {
		this.name = name;
		this.answerSetCountVerifier = answerSetCountVerifier;
		this.input = input;
		this.assertions = assertions;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IntPredicate getAnswerSetCountVerifier() {
		return answerSetCountVerifier;
	}

	@Override
	public Set<BasicAtom> getInput() {
		return input;
	}

	@Override
	public List<Assertion> getAssertions() {
		return assertions;
	}

	public String toString() {
		String ls = System.lineSeparator();
		String inputString = input.isEmpty() ? "" : Util.join("{", input, ls, "}");
		String assertionsString = assertions.isEmpty() ? "" : Util.join(ls, assertions, ls, ls);
		return "#test " + name + "(expect: " + answerSetCountVerifier.toString() + ") { " + ls + "given " + inputString + assertionsString + "}";
	}

}
