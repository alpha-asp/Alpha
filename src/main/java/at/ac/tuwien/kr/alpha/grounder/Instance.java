package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.Term;

import java.util.Arrays;

/**
 * An instance is a positional association of terms, e.g., representing a variable substitution, or a ground instance of
 * a predicate.
 * Copyright (c) 2016, the Alpha Team.
 */
public class Instance {
	public final Term[] terms;

	public Instance(Term... terms) {
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

		Instance instance = (Instance) o;

		return Arrays.equals(terms, instance.terms);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(terms);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("(");
		Util.appendDelimited(sb, Arrays.asList(terms));
		sb.append(")");
		return sb.toString();
	}
}
