package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.parser.TermParser;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

public class AnswerSetBuilder {
	private boolean firstInstance = true;
	private String predicateSymbol;
	private Predicate predicate;
	private SortedSet<Predicate> predicates = new TreeSet<>();
	private SortedSet<Atom> instances = new TreeSet<>();
	private Map<Predicate, SortedSet<Atom>> predicateInstances = new HashMap<>();

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

	public AnswerSetBuilder predicate(String predicateSymbol) {
		if (this.predicateSymbol != null) {
			flush();
		}
		this.predicateSymbol = predicateSymbol;
		return this;
	}

	public <T extends Comparable<T>> AnswerSetBuilder instance(T... terms) {
		if (firstInstance) {
			firstInstance = false;
			predicate = new BasicPredicate(predicateSymbol, terms.length);
			predicates.add(predicate);
		}

		List<Term> termList = Stream.of(terms).map(ConstantTerm::getInstance).collect(Collectors.toList());
		instances.add(new BasicAtom(predicate, termList));
		return this;
	}

	public AnswerSetBuilder symbolicInstance(String... terms) {
		if (firstInstance) {
			firstInstance = false;
			predicate = new BasicPredicate(predicateSymbol, terms.length);
			predicates.add(predicate);
		}

		List<Term> termList = Stream.of(terms).map(Symbol::getInstance).map(ConstantTerm::getInstance).collect(Collectors.toList());
		instances.add(new BasicAtom(predicate, termList));
		return this;
	}

	public AnswerSetBuilder parseInstance(String... terms) {
		if (firstInstance) {
			firstInstance = false;
			predicate = new BasicPredicate(predicateSymbol, terms.length);
			predicates.add(predicate);
		}

		TermParser parser = new TermParser();

		List<Term> termList = Stream.of(terms).map(s -> {
			try {
				return parser.parse(s);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList());
		instances.add(new BasicAtom(predicate, termList));
		return this;
	}

	public BasicAnswerSet build() {
		flush();
		return new BasicAnswerSet(predicates, predicateInstances);
	}
}
