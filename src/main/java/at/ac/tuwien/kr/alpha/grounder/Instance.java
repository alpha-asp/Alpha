package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.Term;

import java.util.Arrays;
import java.util.List;

/**
 * An instance is a positional association of terms, e.g., representing a variable substitution, or a ground instance of
 * a predicate.
 * Copyright (c) 2016, the Alpha Team.
 */
public class Instance {
	public final List<Term> terms;

	public Instance(Term... terms) {
		this(Arrays.asList(terms));
	}

	public Instance(List<Term> terms) {
		this.terms = terms;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		return terms.equals(((Instance) o).terms);
	}

	@Override
	public int hashCode() {
		return terms.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("(");
		Util.appendDelimited(sb, terms);
		sb.append(")");
		return sb.toString();
	}
}