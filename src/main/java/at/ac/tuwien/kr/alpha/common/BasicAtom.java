package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BasicAtom implements Atom {
	private final Predicate predicate;
	private final List<Term> terms;
	private final boolean ground;

	public BasicAtom(Predicate predicate, List<Term> terms) {
		this.predicate = predicate;
		this.terms = terms;

		boolean ground = true;
		for (Term term : terms) {
			if (!term.isGround()) {
				ground = false;
				break;
			}
		}
		this.ground = ground;
	}

	public BasicAtom(Predicate predicate, Term... terms) {
		this(predicate, Arrays.asList(terms));
	}

	public BasicAtom(Predicate predicate) {
		this(predicate, Collections.emptyList());
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	@Override
	public List<Term> getTerms() {
		return terms;
	}

	public boolean isGround() {
		return ground;
	}

	public boolean isInternal() {
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		BasicAtom that = (BasicAtom) o;

		if (!predicate.equals(that.predicate)) {
			return false;
		}

		return terms.equals(that.terms);
	}

	@Override
	public int hashCode() {
		return 31 * predicate.hashCode() + terms.hashCode();
	}

	public List<VariableTerm> getOccurringVariables() {
		LinkedList<VariableTerm> occurringVariables = new LinkedList<>();
		for (Term term : terms) {
			occurringVariables.addAll(term.getOccurringVariables());
		}
		return occurringVariables;
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return new BasicAtom(predicate, terms.stream().map(t -> {
			return t.substitute(substitution);
		}).collect(Collectors.toList()));
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(predicate.getPredicateName());
		sb.append("(");
		Util.appendDelimited(sb, terms);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int compareTo(Atom o) {
		if (this.terms.size() != o.getTerms().size()) {
			return this.terms.size() - o.getTerms().size();
		}

		int result = this.predicate.compareTo(o.getPredicate());

		if (result != 0) {
			return result;
		}

		for (int i = 0; i < terms.size(); i++) {
			result = terms.get(i).compareTo(o.getTerms().get(i));
			if (result != 0) {
				return result;
			}
		}

		return 0;
	}
}