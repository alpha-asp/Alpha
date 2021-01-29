package at.ac.tuwien.kr.alpha.test.util;

import static java.util.Collections.emptySet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.junit.Assert;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.program.Atom;
import at.ac.tuwien.kr.alpha.api.program.Predicate;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.core.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.core.common.CorePredicate;
import at.ac.tuwien.kr.alpha.core.common.terms.CoreConstantTerm;
import at.ac.tuwien.kr.alpha.core.programs.AbstractProgram;
import at.ac.tuwien.kr.alpha.core.util.AnswerSetsParser;

public class TestUtils {

	public static void assertAnswerSetsEqual(Set<AnswerSet> expected, Set<AnswerSet> actual) {
		if (expected == null) {
			if (actual != null) {
				throw new AssertionError("Expected answer sets are null, but actual are not!");
			}
		}
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			Set<AnswerSet> expectedMinusActual = new LinkedHashSet<>(expected);
			expectedMinusActual.removeAll(actual);
			Set<AnswerSet> actualMinusExpected = new LinkedHashSet<>(actual);
			actualMinusExpected.removeAll(expected);
			String setDiffs = "Expected and actual answer sets do not agree, differences are:\nExpected \\ Actual:\n" + expectedMinusActual
					+ "\nActual \\ Expected:\n" + actualMinusExpected;
			throw new AssertionError(setDiffs + e.getMessage(), e);
		}
	}

	public static void assertAnswerSetsEqual(String[] expected, Set<AnswerSet> actual) {
		if (expected.length == 0) {
			TestUtils.assertAnswerSetsEqual(emptySet(), actual);
			return;
		}
		StringJoiner joiner = new StringJoiner("} {", "{", "}");
		Arrays.stream(expected).forEach(joiner::add);
		TestUtils.assertAnswerSetsEqual(AnswerSetsParser.parse(joiner.toString()), actual);
	}

	public static void assertAnswerSetsEqual(String expectedAnswerSet, Set<AnswerSet> actual) {
		TestUtils.assertAnswerSetsEqual(AnswerSetsParser.parse("{ " + expectedAnswerSet + " }"), actual);
	}

	public static void assertAnswerSetsEqualWithBase(String base, String[] expectedAnswerSets, Set<AnswerSet> actual) {
		base = base.trim();
		if (!base.endsWith(",")) {
			base += ", ";
		}

		for (int i = 0; i < expectedAnswerSets.length; i++) {
			expectedAnswerSets[i] = base + expectedAnswerSets[i];
			// Remove trailing ",".
			expectedAnswerSets[i] = expectedAnswerSets[i].trim();
			if (expectedAnswerSets[i].endsWith(",")) {
				expectedAnswerSets[i] = expectedAnswerSets[i].substring(0, expectedAnswerSets[i].length() - 1);
			}
		}
		TestUtils.assertAnswerSetsEqual(expectedAnswerSets, actual);
	}

	public static void assertFactsContainedInProgram(AbstractProgram<?> prog, Atom... facts) {
		for (Atom fact : facts) {
			Assert.assertTrue(prog.getFacts().contains(fact));
		}
	}

	public static Atom basicAtomWithStringTerms(String predicate, String... terms) {
		Predicate pred = CorePredicate.getInstance(predicate, terms.length);
		List<Term> trms = new ArrayList<>();
		for (String str : terms) {
			trms.add(CoreConstantTerm.getInstance(str));
		}
		return new BasicAtom(pred, trms);
	}

	public static Atom basicAtomWithSymbolicTerms(String predicate, String... constantSymbols) {
		Predicate pred = CorePredicate.getInstance(predicate, constantSymbols.length);
		List<Term> trms = new ArrayList<>();
		for (String str : constantSymbols) {
			trms.add(CoreConstantTerm.getSymbolicInstance(str));
		}
		return new BasicAtom(pred, trms);
	}

}
