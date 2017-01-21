package at.ac.tuwien.kr.alpha.common;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.*;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BasicAnswerSet implements AnswerSet {
	public static final BasicAnswerSet EMPTY = new BasicAnswerSet(emptySortedSet(), emptyMap());

	private final SortedSet<Predicate> predicates;
	private final Map<Predicate, SortedSet<Atom>> predicateInstances;

	public BasicAnswerSet(SortedSet<Predicate> predicates, Map<Predicate, SortedSet<Atom>> predicateInstances) {
		this.predicates = predicates;
		this.predicateInstances = predicateInstances;
	}

	@Override
	public SortedSet<Predicate> getPredicates() {
		return predicates;
	}

	@Override
	public SortedSet<Atom> getPredicateInstances(Predicate predicate) {
		return predicateInstances.get(predicate);
	}

	@Override
	public boolean isEmpty() {
		return predicates.isEmpty();
	}

	public String toString() {
		if (predicates.isEmpty()) {
			return "{}";
		}

		final StringBuilder sb = new StringBuilder("{ ");
		for (Iterator<Predicate> iterator = predicates.iterator(); iterator.hasNext();) {
			Predicate predicate = iterator.next();
			Set<Atom> instances = getPredicateInstances(predicate);

			if (instances == null || instances.isEmpty()) {
				sb.append(predicate.getPredicateName());
				continue;
			}

			for (Iterator<Atom> instanceIterator = instances.iterator(); instanceIterator.hasNext();) {
				sb.append(instanceIterator.next());
				if (instanceIterator.hasNext()) {
					sb.append(", ");
				}
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(" }");
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BasicAnswerSet)) {
			return false;
		}

		BasicAnswerSet that = (BasicAnswerSet) o;

		if (!predicates.equals(that.predicates)) {
			return false;
		}

		return predicateInstances.equals(that.predicateInstances);
	}

	@Override
	public int hashCode() {
		return  31 * predicates.hashCode() + predicateInstances.hashCode();
	}

	public static class Builder {
		private boolean firstInstance = true;
		private String predicateSymbol;
		private Predicate predicate;
		private SortedSet<Predicate> predicates = new TreeSet<>();
		private SortedSet<Atom> instances = new TreeSet<>();
		private Map<Predicate, SortedSet<Atom>> predicateInstances = new HashMap<>();

		public Builder() {
		}

		public Builder(Builder copy) {
			this.firstInstance = copy.firstInstance;
			this.predicateSymbol = copy.predicateSymbol;
			this.predicate = copy.predicate;
			this.predicates = new TreeSet<>(copy.predicates);
			this.instances = new TreeSet<>(copy.instances);
			this.predicateInstances = new HashMap<>(copy.predicateInstances);
		}

		private void flush() {
			if (firstInstance) {
				predicate = new BasicPredicate(predicateSymbol, 0);
				predicates.add(predicate);
				predicateInstances.put(predicate, new TreeSet<>(singletonList(new BasicAtom(predicate))));
			} else {
				predicateInstances.put(predicate, new TreeSet<>(instances));
			}
			firstInstance = true;
			instances.clear();
			predicate = null;
		}

		public Builder predicate(String predicateSymbol) {
			if (this.predicateSymbol != null) {
				flush();
			}
			this.predicateSymbol = predicateSymbol;
			return this;
		}

		public Builder instance(String... constantSymbols) {
			if (firstInstance) {
				firstInstance = false;
				predicate = new BasicPredicate(predicateSymbol, constantSymbols.length);
				predicates.add(predicate);
			}

			List<Term> terms = Stream.of(constantSymbols).map(ConstantTerm::getInstance).collect(Collectors.toList());
			instances.add(new BasicAtom(predicate, terms));
			return this;
		}

		public BasicAnswerSet build() {
			flush();
			return new BasicAnswerSet(predicates, predicateInstances);
		}
	}
}