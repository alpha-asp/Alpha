package at.ac.tuwien.kr.alpha.api.programs.tests;

import java.util.Set;
import java.util.function.IntPredicate;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;

public interface TestCase {
	
	String getName();

	IntPredicate getAnswerSetCountVerifier();

	Set<BasicAtom> getInput();

	Set<Assertion> getAssertions();

}
