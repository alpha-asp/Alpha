package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.Util.join;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BasicAtom implements Literal {
	private final Predicate predicate;
	private final List<Term> terms;
	private final boolean ground;
	private final boolean negated;

	public BasicAtom(Predicate predicate, List<Term> terms, boolean negated) {
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
		this.negated = negated;
	}

	/**
	 * Creates a positive BasicAtom over predicate and terms.
	 * @param predicate
	 * @param terms
	 */
	public BasicAtom(Predicate predicate, List<Term> terms) {
		this(predicate, terms, false);
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

	@Override
	public boolean isGround() {
		return ground;
	}

	@Override
	public boolean isNegated() {
		return negated;
	}

	@Override
	public List<VariableTerm> getBindingVariables() {
		if (negated) {
			// Negative literal has no binding variables.
			return Collections.emptyList();
		}
		LinkedList<VariableTerm> bindingVariables = new LinkedList<>();
		for (Term term : terms) {
			bindingVariables.addAll(term.getOccurringVariables());
		}
		return bindingVariables;
	}

	@Override
	public List<VariableTerm> getNonBindingVariables() {
		if (!negated) {
			// Positive literal has only binding variables.
			return Collections.emptyList();
		}
		LinkedList<VariableTerm> nonbindingVariables = new LinkedList<>();
		for (Term term : terms) {
			nonbindingVariables.addAll(term.getOccurringVariables());
		}
		return nonbindingVariables;
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return new BasicAtom(predicate, terms.stream()
			.map(t -> t.substitute(substitution))
			.collect(Collectors.toList()), negated);
	}

	public Atom getPositiveVersion() {
		return new BasicAtom(predicate, terms, false);
	}

	@Override
	public String toString() {
		final String prefix = (negated ? "not " : "") + predicate.getName();
		if (terms.isEmpty()) {
			return prefix;
		}

		return join(prefix + "(", terms, ")");
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		BasicAtom that = (BasicAtom) o;

		return predicate.equals(that.predicate) && terms.equals(that.terms);
	}

	@Override
	public int hashCode() {
		return 31 * predicate.hashCode() + terms.hashCode();
	}
}
