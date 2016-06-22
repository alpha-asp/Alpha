package at.ac.tuwien.kr.alpha;

import java.util.Arrays;

class Atom {
	private final Predicate predicate;
	private final Term[] terms;

	private Atom(Predicate predicate, Term[] terms) {
		if (terms == null) {
			throw new NullPointerException("terms must not be null");
		}
		if (predicate == null) {
			throw new NullPointerException("predicate must not be null");
		}
		if (predicate.getArity() != terms.length) {
			throw new IllegalArgumentException("length of terms does not match arity of predicate");
		}
		this.predicate = predicate;
		this.terms = terms;
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public Term[] getTerms() {
		return terms;
	}

	public int getArity() {
		return predicate.getArity();
	}

	@Override
	public String toString() {
		return predicate.toString() + Arrays.toString(terms);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Atom atom = (Atom) o;

		if (!predicate.equals(atom.predicate)) {
			return false;
		}
		return Arrays.equals(terms, atom.terms);
	}

	@Override
	public int hashCode() {
		int result = predicate.hashCode();
		result = 31 * result + Arrays.hashCode(terms);
		return result;
	}
}