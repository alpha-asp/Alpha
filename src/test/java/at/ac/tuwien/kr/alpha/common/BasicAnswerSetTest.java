package at.ac.tuwien.kr.alpha.common;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BasicAnswerSetTest {
	private final List<AnswerSet> answerSets;

	public BasicAnswerSetTest() {
		BasicPredicate a = new BasicPredicate("a", 0);
		BasicPredicate foo = new BasicPredicate("foo", 1);
		Set<Predicate> fooAndA = new HashSet<>(Arrays.asList(foo, a));

		BasicPredicate q = new BasicPredicate("q", 0);
		BasicPredicate p = new BasicPredicate("p", 1);
		Set<Predicate> qAndP = new HashSet<>(Arrays.asList(q, p));

		ConstantTerm bar = ConstantTerm.getInstance("bar");
		ConstantTerm baz = ConstantTerm.getInstance("baz");

		Map<Predicate, Set<BasicAtom>> inst1 = new HashMap<>();
		inst1.put(a, new HashSet<>(Collections.singletonList(new BasicAtom(a))));
		inst1.put(foo, new HashSet<>(Arrays.asList(new BasicAtom[] {
			new BasicAtom(foo, bar),
			new BasicAtom(foo, baz)
		})));
		// as1 = { a, foo(bar), foo(baz) }

		Map<Predicate, Set<BasicAtom>> inst2 = new HashMap<>();
		inst2.put(a, new HashSet<>(Collections.singletonList(new BasicAtom(a))));
		inst2.put(foo, new HashSet<>(Arrays.asList(new BasicAtom[] {
			new BasicAtom(foo, baz),
			new BasicAtom(foo, bar)
		})));
		// as1 = { a, foo(baz), foo(bar) }

		Map<Predicate, Set<BasicAtom>> inst3 = new HashMap<>();
		inst3.put(q, new HashSet<>(Collections.singletonList(new BasicAtom(q))));
		inst3.put(p, new HashSet<>(Arrays.asList(
			new BasicAtom(p, bar),
			new BasicAtom(p, baz)
		)));
		// as3 = { q, p(bar), p(baz) }

		Map<Predicate, Set<BasicAtom>> inst4 = new HashMap<>();
		inst4.put(a, new HashSet<>(Collections.singletonList(new BasicAtom(a))));
		inst4.put(foo, new HashSet<>(Arrays.asList(
			new BasicAtom(foo, bar),
			new BasicAtom(foo, baz),
			new BasicAtom(foo, ConstantTerm.getInstance("batsinga"))
		)));
		// as4 = { a, foo(bar), foo(baz), foo(batsinga) }

		answerSets = Arrays.asList(
			new BasicAnswerSet(fooAndA, inst1),
			new BasicAnswerSet(fooAndA, inst2),
			new BasicAnswerSet(qAndP, inst3),
			new BasicAnswerSet(fooAndA, inst4)
		);
	}

	@Test
	public void areAnswerSetsEqual() throws Exception {
		assertEquals(answerSets.get(0), answerSets.get(1));
		assertEquals(answerSets.get(1), answerSets.get(0));

		assertNotEquals(answerSets.get(0), answerSets.get(2));
		assertNotEquals(answerSets.get(0), answerSets.get(3));
	}
}