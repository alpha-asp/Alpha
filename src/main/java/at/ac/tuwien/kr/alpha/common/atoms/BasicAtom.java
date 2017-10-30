package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BasicAtom implements Literal {
	private final Predicate predicate;
	private final List<Term> terms;
	private final boolean ground;
	private final boolean isNegated;

	public BasicAtom(Predicate predicate, List<Term> terms, boolean isNegated) {
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
		this.isNegated = isNegated;
	}

	/**
	 * Creates a positive BasicAtom over predicate and terms.
	 * @param predicate
	 * @param terms
	 */
	public BasicAtom(Predicate predicate, List<Term> terms) {
		this(predicate, terms, false);
	}

	public BasicAtom(BasicAtom clone) {
		this(clone, clone.isNegated);
	}

	public BasicAtom(BasicAtom clone, boolean isNegated) {
		this.predicate = clone.getPredicate();
		this.terms = new ArrayList<>(clone.getTerms());
		this.ground = clone.ground;
		this.isNegated = isNegated;
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

	public List<VariableTerm> getBindingVariables() {
		if (isNegated) {
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
		if (!isNegated) {
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
		return new BasicAtom(predicate, terms.stream().map(t -> {
			return t.substitute(substitution);
		}).collect(Collectors.toList()));
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (isNegated) {
			sb.append("not ");
		}
		sb.append(predicate.getPredicateName());
		if (!terms.isEmpty()) {
			sb.append("(");
			Util.appendDelimited(sb, terms);
			sb.append(")");
		}
		return sb.toString();
	}

	@Override
	public boolean isNegated() {
		return isNegated;
	}

	public boolean containsIntervalTerms() {
		for (Term term : terms) {
			if (IntervalTerm.termContainsIntervalTerm(term)) {
				return true;
			}
		}
		return false;
	}
}
