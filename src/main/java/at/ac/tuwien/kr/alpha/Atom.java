package at.ac.tuwien.kr.alpha;

import java.util.Arrays;

class Atom {
	private final int predicate;
	private final int[] terms;

	Atom(int predicate, int[] terms) {
		if (terms == null) {
			throw new NullPointerException("terms must not be null");
		}
		this.predicate = predicate;
		this.terms = terms;
	}

	public int getPredicate() {
		return predicate;
	}

	public int[] getTerms() {
		return terms;
	}

	public boolean isConstant(int index) {
		return terms[index] < 0;
	}

	public boolean isVariable(int index) {
		return terms[index] > 0;
	}

	public boolean isValid(int index) {
		return terms[index] != 0;
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

		return predicate == atom.predicate && Arrays.equals(terms, atom.terms);
	}

	@Override
	public int hashCode() {
		int result = predicate;
		result = 31 * result + Arrays.hashCode(terms);
		return result;
	}
}