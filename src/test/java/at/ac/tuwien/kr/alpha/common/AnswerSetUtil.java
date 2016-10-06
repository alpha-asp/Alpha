package at.ac.tuwien.kr.alpha.common;

import java.util.*;

/**
 * Contains utility functions for comparing AnswerSets.
 * Copyright (c) 2016, the Alpha Team.
 */
public class AnswerSetUtil {

	/**
	 * Returns true if both AnswerSets are the same.
	 * @param answerSet1 the first AnswerSet.
	 * @param answerSet2 the second AnswerSet.
	 * @return true iff (answerSet1 = answerSet2) in the mathematical sense.
	 */
	public static boolean areAnswerSetsEqual(AnswerSet answerSet1, AnswerSet answerSet2) {
		HashSet<Predicate> predicateList1 = new HashSet<>(answerSet1.getPredicateList());
		HashSet<Predicate> predicateList2 = new HashSet<>(answerSet2.getPredicateList());

		if (!predicateList1.equals(predicateList2)) {
			return false;
		}

		for (Predicate predicate : predicateList1) {
			HashSet<PredicateInstance> predicateInstances1 = new HashSet<>(answerSet1.getPredicateInstances(predicate));
			HashSet<PredicateInstance> predicateInstances2 = new HashSet<>(answerSet2.getPredicateInstances(predicate));
			if (!predicateInstances1.equals(predicateInstances2)) {
				return false;
			}
		}
		return true;
	}

	public static boolean areSetsOfAnswerSetsEqual(Set<AnswerSet> setOfAnswerSets1, Set<AnswerSet> setOfAnswerSets2) {
		HashSet<AnswerSet> set1 = new HashSet<>(setOfAnswerSets1);
		HashSet<AnswerSet> set2 = new HashSet<>(setOfAnswerSets2);
		// Find and remove from set2 each answer set of set1.
		for (AnswerSet answerSet1 : set1) {
			Iterator<AnswerSet> iterator = set2.iterator();
			boolean foundMatch = false;
			while (iterator.hasNext()) {
				AnswerSet answerSet2 = iterator.next();
				if (areAnswerSetsEqual(answerSet1, answerSet2)) {
					iterator.remove();
					foundMatch = true;
				}
			}
			if (!foundMatch) {
				return false;
			}
		}
		// Both sets are equal if set2 is empty now.
		return set2.isEmpty();
	}


	/**
	 * Constructs an answer set from a list of atoms.
	 * @param atoms the atoms contained in the answer set.
	 * @return the constructed answer set.
	 */
	public static AnswerSet constructAnswerSet(List<PredicateInstance> atoms) {
		BasicAnswerSet answerSet = new BasicAnswerSet();
		HashSet<Predicate> predicates = new HashSet<>();
		HashMap<Predicate, ArrayList<PredicateInstance>> instances = new HashMap<>();
		for (PredicateInstance atom : atoms) {
			predicates.add(atom.predicate);
			instances.putIfAbsent(atom.predicate, new ArrayList<>());
			instances.get(atom.predicate).add(atom);
		}
		ArrayList<Predicate> predicateList = new ArrayList<>(predicates);
		answerSet.setPredicateList(predicateList);
		answerSet.setPredicateInstances(instances);
		return answerSet;
	}
}
