package at.ac.tuwien.kr.alpha.common.terms;

import at.ac.tuwien.kr.alpha.grounder.Substitution;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ConstantTerm implements Term {
	private static final Interner<ConstantTerm> INTERNER = Interners.newStrongInterner();

	public Object getObject() {
		return object;
	}

	private final Object object;

	private ConstantTerm(Object object) {
		this.object = object;
	}

	public static ConstantTerm getInstance(Object object) {
		return INTERNER.intern(new ConstantTerm(object));
	}

	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		return Collections.emptyList();
	}

	@Override
	public Term substitute(Substitution substitution) {
		return this;
	}

	@Override
	public String toString() {
		return object.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ConstantTerm that = (ConstantTerm) o;

		return object.equals(that.object);
	}

	@Override
	public int hashCode() {
		return object.hashCode();
	}

	@Override
	public int compareTo(Term o) {
		if (o instanceof ConstantTerm) {
			if (this.object.equals(((ConstantTerm) o).object)) {
				return 0;
			}
			// FIXME
			//return object.compareTo(((ConstantTerm) o).object);
			return 1;
		}
		return 1;
	}
}
