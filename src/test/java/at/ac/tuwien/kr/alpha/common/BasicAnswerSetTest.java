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

		Map<Predicate, Set<PredicateInstance>> inst1 = new HashMap<>();
		inst1.put(a, new HashSet<>(Collections.singletonList(new PredicateInstance<>(a))));
		inst1.put(foo, new HashSet<>(Arrays.asList(new PredicateInstance[] {
			new PredicateInstance<>(foo, bar),
			new PredicateInstance<>(foo, baz)
		})));
		// as1 = { a, foo(bar), foo(baz) }

		Map<Predicate, Set<PredicateInstance>> inst2 = new HashMap<>();
		inst2.put(a, new HashSet<>(Collections.singletonList(new PredicateInstance<>(a))));
		inst2.put(foo, new HashSet<>(Arrays.asList(new PredicateInstance[] {
			new PredicateInstance<>(foo, baz),
			new PredicateInstance<>(foo, bar)
		})));
		// as1 = { a, foo(baz), foo(bar) }

		Map<Predicate, Set<PredicateInstance>> inst3 = new HashMap<>();
		inst3.put(q, new HashSet<>(Collections.singletonList(new PredicateInstance<>(q))));
		inst3.put(p, new HashSet<>(Arrays.asList(
			new PredicateInstance<>(p, bar),
			new PredicateInstance<>(p, baz)
		)));
		// as3 = { q, p(bar), p(baz) }

		Map<Predicate, Set<PredicateInstance>> inst4 = new HashMap<>();
		inst4.put(a, new HashSet<>(Collections.singletonList(new PredicateInstance<>(a))));
		inst4.put(foo, new HashSet<>(Arrays.asList(
			new PredicateInstance<>(foo, bar),
			new PredicateInstance<>(foo, baz),
			new PredicateInstance<>(foo, ConstantTerm.getInstance("batsinga"))
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