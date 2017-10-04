package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
			this.predicateInstances = copy.predicateInstances.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> new TreeSet<>(e.getValue())));
		}

		private void flush() {
			if (firstInstance) {
				predicate = new BasicPredicate(predicateSymbol, 0);
				predicates.add(predicate);
				predicateInstances.put(predicate, new TreeSet<>(singletonList(new BasicAtom(predicate))));
			} else {
				SortedSet<Atom> atoms = predicateInstances.get(predicate);
				if (atoms == null) {
					predicateInstances.put(predicate, new TreeSet<>(instances));
				} else {
					atoms.addAll(instances);
				}
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

		public Builder instanceObj(Object... terms) {
			if (firstInstance) {
				firstInstance = false;
				predicate = new BasicPredicate(predicateSymbol, terms.length);
				predicates.add(predicate);
			}

			List<Term> termList = Stream.of(terms).map(ConstantTerm::getInstance).collect(Collectors.toList());
			instances.add(new BasicAtom(predicate, termList));
			return this;
		}

		public Builder instance(String... terms) {
			if (firstInstance) {
				firstInstance = false;
				predicate = new BasicPredicate(predicateSymbol, terms.length);
				predicates.add(predicate);
			}

			List<Term> termList = Stream.of(terms).map(BasicAnswerSet.Builder::parseFunctionTermSimple).collect(Collectors.toList());
			instances.add(new BasicAtom(predicate, termList));
			return this;
		}

		private static Term parseFunctionTermSimple(String functionTerm) {
			String funcTerm = functionTerm.replaceAll("\\s+", "");	// remove all whitespace.
			Pattern funcTermPattern = Pattern.compile("(\\w*)\\((.*)\\)");
			Matcher funcTermMatcher = funcTermPattern.matcher(funcTerm);
			if (funcTermMatcher.matches()) {
				String functionSymbol = funcTermMatcher.group(1);
				String functionTermlist = funcTermMatcher.group(2);
				ArrayList<Term> termlist = new ArrayList<>();
				for (String subTerm : functionTermlist.split(",")) {
					termlist.add(parseFunctionTermSimple(subTerm));
				}
				return FunctionTerm.getInstance(functionSymbol, termlist);
			} else {
				// Function term does not match, input is a constant.
				return ConstantTerm.getInstance(functionTerm);
			}
		}

		public BasicAnswerSet build() {
			flush();
			return new BasicAnswerSet(predicates, predicateInstances);
		}
	}
}