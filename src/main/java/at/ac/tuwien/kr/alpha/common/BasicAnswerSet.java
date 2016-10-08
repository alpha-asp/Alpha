package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.Util;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.appendDelimited;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BasicAnswerSet implements AnswerSet {
	private final List<Predicate> predicates;
	private final Map<Predicate, Set<PredicateInstance>> predicateInstances;

	public BasicAnswerSet(List<Predicate> predicates, Map<Predicate, Set<PredicateInstance>> predicateInstances) {
		this.predicates = predicates;
		this.predicateInstances = predicateInstances;
	}

	public List<Predicate> getPredicates() {
		return predicates;
	}

	@Override
	public Set<PredicateInstance> getPredicateInstances(Predicate predicate) {
		return predicateInstances.get(predicate);
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder("{ ");
		for (int i = 0; i < predicates.size(); i++) {
			sb.append(i != 0 ? ", "  : "");

			Predicate predicate = predicates.get(i);
			Set<PredicateInstance> instances = getPredicateInstances(predicate);

			if (instances.isEmpty()) {
				sb.append(predicate.getPredicateName());
				continue;
			}

			appendDelimited(sb, instances);
		}
		sb.append(" }");
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BasicAnswerSet)) return false;

		BasicAnswerSet that = (BasicAnswerSet) o;

		if (!predicates.equals(that.predicates)) return false;
		return predicateInstances.equals(that.predicateInstances);
	}

	@Override
	public int hashCode() {
		return  31 * predicates.hashCode() + predicateInstances.hashCode();
	}
}
