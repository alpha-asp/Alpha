package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTermImpl;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.TermImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

public class AnswerSetBuilder {
	private boolean firstInstance = true;
	private String predicateSymbol;
	private PredicateImpl predicate;
	private SortedSet<PredicateImpl> predicates = new TreeSet<>();
	private SortedSet<Atom> instances = new TreeSet<>();
	private Map<PredicateImpl, SortedSet<Atom>> predicateInstances = new HashMap<>();

	public AnswerSetBuilder() {
	}

	public AnswerSetBuilder(AnswerSetBuilder copy) {
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
			predicate = PredicateImpl.getInstance(predicateSymbol, 0);
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

	public AnswerSetBuilder predicate(String predicateSymbol) {
		if (this.predicateSymbol != null) {
			flush();
		}
		this.predicateSymbol = predicateSymbol;
		return this;
	}

	@SafeVarargs
	public final <T extends Comparable<T>> AnswerSetBuilder instance(final T... terms) {
		if (firstInstance) {
			firstInstance = false;
			predicate = PredicateImpl.getInstance(predicateSymbol, terms.length);
			predicates.add(predicate);
		}

		// Note that usage of terms does not pollute the heap,
		// since we are only reading, not writing.
		List<TermImpl> termList = Stream
			.of(terms)
			.map(ConstantTermImpl::getInstance)
			.collect(Collectors.toList());

		instances.add(new BasicAtom(predicate, termList));
		return this;
	}

	public AnswerSetBuilder symbolicInstance(String... terms) {
		if (firstInstance) {
			firstInstance = false;
			predicate = PredicateImpl.getInstance(predicateSymbol, terms.length);
			predicates.add(predicate);
		}

		List<TermImpl> termList = Stream.of(terms).map(ConstantTermImpl::getSymbolicInstance).collect(Collectors.toList());
		instances.add(new BasicAtom(predicate, termList));
		return this;
	}

	public BasicAnswerSet build() {
		flush();
		return new BasicAnswerSet(predicates, predicateInstances);
	}
}
