package at.ac.tuwien.kr.alpha.common;

import org.junit.Test;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.ConstantTerm.getInstance;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BasicAnswerSetTest {
	@Test
	public void areAnswerSetsEqual() throws Exception {
		BasicPredicate a = new BasicPredicate("a", 0);
		BasicPredicate foo = new BasicPredicate("foo", 1);
		SortedSet<Predicate> fooAndA = new TreeSet<>(asList(foo, a));

		BasicPredicate q = new BasicPredicate("q", 0);
		BasicPredicate p = new BasicPredicate("p", 1);
		SortedSet<Predicate> qAndP = new TreeSet<>(asList(q, p));

		ConstantTerm bar = getInstance("bar");
		ConstantTerm baz = getInstance("baz");

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
			new BasicAtom(foo, getInstance("batsinga"))
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