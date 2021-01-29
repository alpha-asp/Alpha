package at.ac.tuwien.kr.alpha.common;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.program.Atom;
import at.ac.tuwien.kr.alpha.api.program.Predicate;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.core.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.core.common.BasicAnswerSet;
import at.ac.tuwien.kr.alpha.core.common.CorePredicate;
import at.ac.tuwien.kr.alpha.core.common.terms.CoreConstantTerm;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BasicAnswerSetTest {
	@Test
	public void areAnswerSetsEqual() throws Exception {
		Predicate a = CorePredicate.getInstance("a", 0);
		Predicate foo = CorePredicate.getInstance("foo", 1);
		SortedSet<Predicate> fooAndA = new TreeSet<>(asList(foo, a));

		Predicate q = CorePredicate.getInstance("q", 0);
		Predicate p = CorePredicate.getInstance("p", 1);
		SortedSet<Predicate> qAndP = new TreeSet<>(asList(q, p));

		ConstantTerm<?> bar = CoreConstantTerm.getInstance("bar");
		ConstantTerm<?> baz = CoreConstantTerm.getInstance("baz");

		Map<Predicate, SortedSet<Atom>> inst1 = new HashMap<>();
		inst1.put(a, new TreeSet<>(singleton(new BasicAtom(a))));
		inst1.put(foo, new TreeSet<>(asList(
			new BasicAtom(foo, bar),
			new BasicAtom(foo, baz)
		)));
		// as1 = { a, foo(bar), foo(baz) }

		Map<Predicate, SortedSet<Atom>> inst2 = new HashMap<>();
		inst2.put(a, new TreeSet<>(singleton(new BasicAtom(a))));
		inst2.put(foo, new TreeSet<>(asList(
			new BasicAtom(foo, baz),
			new BasicAtom(foo, bar)
		)));
		// as1 = { a, foo(baz), foo(bar) }

		Map<Predicate, SortedSet<Atom>> inst3 = new HashMap<>();
		inst3.put(q, new TreeSet<>(singleton(new BasicAtom(q))));
		inst3.put(p, new TreeSet<>(asList(
			new BasicAtom(p, bar),
			new BasicAtom(p, baz)
		)));
		// as3 = { q, p(bar), p(baz) }

		Map<Predicate, SortedSet<Atom>> inst4 = new HashMap<>();
		inst4.put(a, new TreeSet<>(singleton(new BasicAtom(a))));
		inst4.put(foo, new TreeSet<>(asList(
			new BasicAtom(foo, bar),
			new BasicAtom(foo, baz),
			new BasicAtom(foo, CoreConstantTerm.getInstance("batsinga"))
		)));
		// as4 = { a, foo(bar), foo(baz), foo(batsinga) }

		final List<AnswerSet> answerSets = asList(
			new BasicAnswerSet(fooAndA, inst1),
			new BasicAnswerSet(fooAndA, inst2),
			new BasicAnswerSet(qAndP, inst3),
			new BasicAnswerSet(fooAndA, inst4)
		);

		assertEquals(answerSets.get(0), answerSets.get(1));
		assertEquals(answerSets.get(1), answerSets.get(0));

		assertNotEquals(answerSets.get(0), answerSets.get(2));
		assertNotEquals(answerSets.get(0), answerSets.get(3));
	}
}