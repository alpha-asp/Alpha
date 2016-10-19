package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.Util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class PredicateInstance<P extends Predicate> {
	public final P predicate;
	public final Term[] termList;

	public PredicateInstance(P predicate, Term... termList) {
		this.predicate = predicate;
		this.termList = termList;
	}

	public boolean isGround() {
		for (Term term : termList) {
			if (!term.isGround()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PredicateInstance that = (PredicateInstance) o;

		if (!predicate.equals(that.predicate)) {
			return false;
		}

		return Arrays.equals(termList, that.termList);
	}

	@Override
	public int hashCode() {
		return 31 * predicate.hashCode() + Arrays.hashCode(termList);
	}

	public List<VariableTerm> getOccurringVariables() {
		LinkedList<VariableTerm> occurringVariables = new LinkedList<>();
		for (Term term : termList) {
			occurringVariables.addAll(term.getOccurringVariables());
		}
		return occurringVariables;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(predicate.getPredicateName());
		sb.append("(");
		Util.appendDelimited(sb, Arrays.asList(termList));
		sb.append(")");
		return sb.toString();
	}
}
