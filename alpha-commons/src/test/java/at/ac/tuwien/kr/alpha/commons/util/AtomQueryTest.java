package at.ac.tuwien.kr.alpha.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AtomQuery;
import at.ac.tuwien.kr.alpha.api.programs.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.commons.AnswerSetBuilder;
import at.ac.tuwien.kr.alpha.commons.AnswerSets;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;

public class AtomQueryTest {

	@Test
	public void matchPredicate() {
		AnswerSetBuilder bld = new AnswerSetBuilder();
		//@formatter:off
		bld.predicate("p")
				.symbolicInstance("a")
				.symbolicInstance("b")
			.predicate("q")
				.symbolicInstance("x");
		//@formatter:on
		AnswerSet as = bld.build();
		List<Atom> queryResult = as.query(Atoms.query(Predicates.getPredicate("p", 1)));
		assertEquals(2, queryResult.size());
		for (Atom a : queryResult) {
			assertTrue(a.getPredicate().equals(Predicates.getPredicate("p", 1)));
		}
	}

	@Test
	public void matchSymbolicConstant() {
		AnswerSetBuilder bld = new AnswerSetBuilder();
		bld.predicate("p")
				.symbolicInstance("a")
				.instance("a");
		AnswerSet as = bld.build();
		AtomQuery constantQuery = Atoms.query(Predicates.getPredicate("p", 1))
				.withConstantEquals(0, "a");
		List<Atom> queryResult = as.query(constantQuery);
		assertEquals(1, queryResult.size());
	}

	@Test
	public void matchString() {
		AnswerSetBuilder bld = new AnswerSetBuilder();
		bld.predicate("p")
				.symbolicInstance("a")
				.instance("a");
		AnswerSet as = bld.build();
		AtomQuery stringQuery = Atoms.query(Predicates.getPredicate("p", 1))
				.withStringEquals(0, "a");
		List<Atom> queryResult = as.query(stringQuery);
		assertEquals(1, queryResult.size());
	}

	@Test
	public void matchEvenIntegers() {
		AnswerSetBuilder bld = new AnswerSetBuilder();
		bld.predicate("p")
				.instance(1).instance(2).instance(3).instance(4).instance(5)
				.instance("bla").symbolicInstance("blubb");
		AnswerSet as = bld.build();
		java.util.function.Predicate<Term> isInteger = (term) -> {
			if (!(term instanceof ConstantTerm<?>)) {
				return false;
			}
			String strValue = ((ConstantTerm<?>) term).getObject().toString();
			return strValue.matches("[0-9]+");
		};
		AtomQuery evenIntegers = Atoms.query(Predicates.getPredicate("p", 1))
				.withFilter(0, isInteger.and(
						(term) -> Integer.valueOf(((ConstantTerm<?>) term).getObject().toString()) % 2 == 0));
		List<Atom> queryResult = as.query(evenIntegers);
		assertEquals(2, queryResult.size());
		for (Atom atom : queryResult) {
			ConstantTerm<?> term = (ConstantTerm<?>) atom.getTerms().get(0);
			assertTrue(Integer.valueOf(term.getObject().toString()) % 2 == 0);
		}
	}

	@Test
	public void matchXWithFuncTerm() {
		Predicate p = Predicates.getPredicate("p", 2);
		Atom a1 = Atoms.newBasicAtom(p, Terms.newSymbolicConstant("x"), Terms.newFunctionTerm("f", Terms.newSymbolicConstant("x")));
		Atom a2 = Atoms.newBasicAtom(p, Terms.newSymbolicConstant("y"), Terms.newFunctionTerm("f", Terms.newSymbolicConstant("y")));
		Atom a3 = Atoms.newBasicAtom(p, Terms.newSymbolicConstant("y"), Terms.newFunctionTerm("f", Terms.newSymbolicConstant("x")));
		Atom a4 = Atoms.newBasicAtom(p, Terms.newSymbolicConstant("x"), Terms.newFunctionTerm("f", Terms.newSymbolicConstant("y")));
		Atom a5 = Atoms.newBasicAtom(p, Terms.newSymbolicConstant("x"), Terms.newFunctionTerm("f"));
		SortedSet<Predicate> predicates = new TreeSet<>();
		predicates.add(p);
		Map<Predicate, SortedSet<Atom>> instances = new HashMap<>();
		SortedSet<Atom> ps = new TreeSet<>();
		ps.add(a1);
		ps.add(a2);
		ps.add(a3);
		ps.add(a4);
		ps.add(a5);
		instances.put(p, ps);
		AnswerSet as = AnswerSets.newAnswerSet(predicates, instances);
		AtomQuery query = Atoms.query(Predicates.getPredicate("p", 2)).withConstantEquals(0, "x").withFunctionTerm(1, "f", 1);
		List<Atom> queryResult = as.query(query);
		assertEquals(2, queryResult.size());
	}

	@Test
	public void matchTerm() {
		AnswerSetBuilder bld = new AnswerSetBuilder();
		bld.predicate("p")
				.instance(1).instance(2).instance(3).instance(4).instance(5)
				.instance("bla").symbolicInstance("blubb");
		AnswerSet as = bld.build();

		AtomQuery equalTerm = Atoms.query(Predicates.getPredicate("p", 1)).withTermEquals(0, Terms.newConstant(1));
		List<Atom> queryResult = as.query(equalTerm);
		assertEquals(1, queryResult.size());
		Atom retrievedAtom = queryResult.get(0);
		assertTrue(retrievedAtom.getTerms().get(0).equals(Terms.newConstant(1)));
	}

}
