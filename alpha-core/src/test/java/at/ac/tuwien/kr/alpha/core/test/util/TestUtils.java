package at.ac.tuwien.kr.alpha.core.test.util;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.Program;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.NoGood;
import at.ac.tuwien.kr.alpha.core.solver.Antecedent;
import at.ac.tuwien.kr.alpha.test.AnswerSetsParser;

public class TestUtils {

	public static void fillAtomStore(AtomStore atomStore, int numberOfAtomsToFill) {
		Predicate predA = Predicates.getPredicate("a", 1);
		for (int i = 0; i < numberOfAtomsToFill; i++) {
			atomStore.putIfAbsent(Atoms.newBasicAtom(predA, Terms.newConstant(i)));
		}
	}
	
	/**
	 * Tests whether two Antecedent objects have the same reason literals (irrespective of their order).
	 * Note that both Antecedents are assumed to contain no duplicate literals.
	 * @param l left Antecedent.
	 * @param r right Antecedent
	 * @return true iff both Antecedents contain the same literals.
	 */
	public static boolean antecedentsEquals(Antecedent l, Antecedent r) {
		if (l == r) {
			return true;
		}
		if (l != null && r != null && l.getReasonLiterals().length == r.getReasonLiterals().length) {
			HashSet<Integer> lSet = new HashSet<>();
			for (int literal : l.getReasonLiterals()) {
				lSet.add(literal);
			}
			for (int literal : r.getReasonLiterals()) {
				if (!lSet.contains(literal)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public static void printNoGoods(AtomStore atomStore, Collection<NoGood> noGoods) {
		System.out.println(noGoods.stream().map(atomStore::noGoodToString).collect(Collectors.toSet()));
	}
	
	public static void assertAnswerSetsEqual(Set<AnswerSet> expected, Set<AnswerSet> actual) {
		if (expected == null) {
			if (actual != null) {
				throw new AssertionError("Expected answer sets are null, but actual are not!");
			}
		}
		try {
			assertEquals(expected, actual);
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

	public static void assertFactsContainedInProgram(Program<?> prog, Atom... facts) {
		for (Atom fact : facts) {
			assertTrue(prog.getFacts().contains(fact));
		}
	}

	public static BasicAtom basicAtomWithStringTerms(String predicate, String... terms) {
		Predicate pred = Predicates.getPredicate(predicate, terms.length);
		List<Term> trms = new ArrayList<>();
		for (String str : terms) {
			trms.add(Terms.newConstant(str));
		}
		return Atoms.newBasicAtom(pred, trms);
	}

	public static BasicAtom basicAtomWithSymbolicTerms(String predicate, String... constantSymbols) {
		Predicate pred = Predicates.getPredicate(predicate, constantSymbols.length);
		List<Term> trms = new ArrayList<>();
		for (String str : constantSymbols) {
			trms.add(Terms.newSymbolicConstant(str));
		}
		return Atoms.newBasicAtom(pred, trms);
	}

}
