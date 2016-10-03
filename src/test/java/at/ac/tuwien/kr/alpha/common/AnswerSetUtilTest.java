package at.ac.tuwien.kr.alpha.common;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class AnswerSetUtilTest {

	private List<AnswerSet> constructTestingAnswerSets() {
		BasicAnswerSet as1 = new BasicAnswerSet();
		ArrayList<Predicate> preds1 = new ArrayList<>(Arrays.asList(new Predicate[] {new BasicPredicate("a", 0), new BasicPredicate("foo", 1)}));
		as1.setPredicateList(preds1);
		HashMap<Predicate, ArrayList<PredicateInstance>> inst1 = new HashMap<>();
		as1.setPredicateInstances(inst1);
		BasicPredicate predA = new BasicPredicate("a", 0);
		BasicPredicate predFoo = new BasicPredicate("foo", 1);
		inst1.put(predA, new ArrayList<>(Arrays.asList(new PredicateInstance[] {
			new PredicateInstance(predA, new Term[] {})
		})));
		inst1.put(predFoo, new ArrayList<>(Arrays.asList(new PredicateInstance[] {
			new PredicateInstance(predFoo, new Term[] {ConstantTerm.getConstantTerm("bar")}),
			new PredicateInstance(predFoo, new Term[] {ConstantTerm.getConstantTerm("baz")})
		})));
		// as1 = { a, foo(bar), foo(baz) }

		BasicAnswerSet as2 = new BasicAnswerSet();
		ArrayList<Predicate> preds2 = new ArrayList<>(Arrays.asList(new Predicate[] {new BasicPredicate("foo", 1), new BasicPredicate("a", 0)}));
		as2.setPredicateList(preds2);
		HashMap<Predicate, ArrayList<PredicateInstance>> inst2 = new HashMap<>();
		as2.setPredicateInstances(inst2);
		inst2.put(predA, new ArrayList<>(Arrays.asList(new PredicateInstance[] {
			new PredicateInstance(predA, new Term[] {})
		})));
		inst2.put(predFoo, new ArrayList<>(Arrays.asList(new PredicateInstance[] {
			new PredicateInstance(predFoo, new Term[] {ConstantTerm.getConstantTerm("baz")}),
			new PredicateInstance(predFoo, new Term[] {ConstantTerm.getConstantTerm("bar")})
		})));
		// as1 = { a, foo(baz), foo(bar) }

		BasicAnswerSet as3 = new BasicAnswerSet();
		ArrayList<Predicate> preds3 = new ArrayList<>(Arrays.asList(new Predicate[] {new BasicPredicate("q", 0), new BasicPredicate("p", 1)}));
		as3.setPredicateList(preds3);
		HashMap<Predicate, ArrayList<PredicateInstance>> inst3 = new HashMap<>();
		as3.setPredicateInstances(inst3);
		BasicPredicate predQ = new BasicPredicate("q", 0);
		BasicPredicate predP = new BasicPredicate("p", 1);
		inst3.put(predQ, new ArrayList<>(Arrays.asList(new PredicateInstance[] {
			new PredicateInstance(predQ, new Term[] {})
		})));
		inst3.put(predP, new ArrayList<>(Arrays.asList(new PredicateInstance[] {
			new PredicateInstance(predP, new Term[] {ConstantTerm.getConstantTerm("bar")}),
			new PredicateInstance(predP, new Term[] {ConstantTerm.getConstantTerm("baz")})
		})));
		// as3 = { q, p(bar), p(baz) }


		BasicAnswerSet as4 = new BasicAnswerSet();
		ArrayList<Predicate> preds4 = new ArrayList<>(Arrays.asList(new Predicate[] {new BasicPredicate("foo", 1), new BasicPredicate("a", 0)}));
		as4.setPredicateList(preds4);
		HashMap<Predicate, ArrayList<PredicateInstance>> inst4 = new HashMap<>();
		as4.setPredicateInstances(inst4);
		inst4.put(predA, new ArrayList<>(Arrays.asList(new PredicateInstance[] {
			new PredicateInstance(predA, new Term[] {})
		})));
		inst4.put(predFoo, new ArrayList<>(Arrays.asList(new PredicateInstance[] {
			new PredicateInstance(predFoo, new Term[] {ConstantTerm.getConstantTerm("bar")}),
			new PredicateInstance(predFoo, new Term[] {ConstantTerm.getConstantTerm("baz")}),
			new PredicateInstance(predFoo, new Term[] {ConstantTerm.getConstantTerm("batsinga")})
		})));
		// as4 = { a, foo(bar), foo(baz), foo(batsinga) }

		return new ArrayList<>(Arrays.asList(new AnswerSet[] {as1, as2, as3, as4}));
	}


	@Test
	public void areAnswerSetsEqual() throws Exception {
		List<AnswerSet> answerSets = constructTestingAnswerSets();

		assertTrue("Answer-set 1 and answer-set 2 must be equal", AnswerSetUtil.areAnswerSetsEqual(answerSets.get(0), answerSets.get(1)));
		assertTrue("Answer-set 1 and answer-set 2 must be equal", AnswerSetUtil.areAnswerSetsEqual(answerSets.get(1), answerSets.get(0)));

		assertFalse("Answer-set 1 and answer-set 3 must be not equal", AnswerSetUtil.areAnswerSetsEqual(answerSets.get(0), answerSets.get(2)));
		assertFalse("Answer-set 1 and answer-set 4 must be not equal", AnswerSetUtil.areAnswerSetsEqual(answerSets.get(0), answerSets.get(3)));
	}

	@Test
	public void areSetsOfAnswerSetsEqual() throws Exception {
		List<AnswerSet> answerSets = constructTestingAnswerSets();
		HashSet<AnswerSet> set1 = new HashSet<>();
		set1.add(answerSets.get(0));
		set1.add(answerSets.get(2));
		set1.add(answerSets.get(3));
		HashSet<AnswerSet> set2 = new HashSet<>();
		set2.add(answerSets.get(1)); // Answer sets 0 and 1 are equal
		set2.add(answerSets.get(2));
		set2.add(answerSets.get(3));

		assertTrue("The sets 1 and 2 of answer sets must be equal.", AnswerSetUtil.areSetsOfAnswerSetsEqual(set1, set2));

		HashSet<AnswerSet> set3 = new HashSet<>();
		set3.add(answerSets.get(2));
		HashSet<AnswerSet> set4 = new HashSet<>();
		set4.add(answerSets.get(2));
		set4.add(answerSets.get(3));

		assertFalse("The sets 3 and 4 of answer sets must be not equal.", AnswerSetUtil.areSetsOfAnswerSetsEqual(set3, set4));

		assertTrue("Empty sets of answer sets must be equal.", AnswerSetUtil.areSetsOfAnswerSetsEqual(new HashSet<>(), new HashSet<>()));
	}

}