package at.ac.tuwien.kr.alpha.common;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class PredicateInstance {

	public final Predicate predicate;
	public final Term[] termList;

	public PredicateInstance(Predicate predicate, Term[] termList) {
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
		int result = predicate.hashCode();
		result = 31 * result + Arrays.hashCode(termList);
		return result;
	}

	public List<VariableTerm> getOccurringVariables() {
		LinkedList<VariableTerm> occurringVariables = new LinkedList<>();
		for (Term term : termList) {
			occurringVariables.addAll(term.getOccurringVariables());
		}
		return occurringVariables;
	}
}
