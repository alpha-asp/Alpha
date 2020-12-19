package at.ac.tuwien.kr.alpha.core.common;

import static java.util.Collections.singletonList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import at.ac.tuwien.kr.alpha.common.terms.CoreConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.CoreTerm;
import at.ac.tuwien.kr.alpha.core.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.core.atoms.CoreAtom;

public class AnswerSetBuilder {
	private boolean firstInstance = true;
	private String predicateSymbol;
	private CorePredicate predicate;
	private SortedSet<CorePredicate> predicates = new TreeSet<>();
	private SortedSet<CoreAtom> instances = new TreeSet<>();
	private Map<CorePredicate, SortedSet<CoreAtom>> predicateInstances = new HashMap<>();

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
			predicate = CorePredicate.getInstance(predicateSymbol, 0);
			predicates.add(predicate);
			predicateInstances.put(predicate, new TreeSet<>(singletonList(new BasicAtom(predicate))));
		} else {
			SortedSet<CoreAtom> atoms = predicateInstances.get(predicate);
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
			predicate = CorePredicate.getInstance(predicateSymbol, terms.length);
			predicates.add(predicate);
		}

		// Note that usage of terms does not pollute the heap,
		// since we are only reading, not writing.
		List<CoreTerm> termList = Stream
			.of(terms)
			.map(CoreConstantTerm::getInstance)
			.collect(Collectors.toList());

		instances.add(new BasicAtom(predicate, termList));
		return this;
	}

	public AnswerSetBuilder symbolicInstance(String... terms) {
		if (firstInstance) {
			firstInstance = false;
			predicate = CorePredicate.getInstance(predicateSymbol, terms.length);
			predicates.add(predicate);
		}

		List<CoreTerm> termList = Stream.of(terms).map(CoreConstantTerm::getSymbolicInstance).collect(Collectors.toList());
		instances.add(new BasicAtom(predicate, termList));
		return this;
	}

	public BasicAnswerSet build() {
		flush();
		return new BasicAnswerSet(predicates, predicateInstances);
	}
}
