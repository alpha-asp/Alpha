package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.IntIdGenerator;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class VariableTerm extends Term {
	private static final Interner<VariableTerm> INTERNER = Interners.newStrongInterner();

	private static final String ANONYMOUS_VARIABLE_PREFIX = "_";
	private static final IntIdGenerator ANONYMOUS_VARIABLE_COUNTER = new IntIdGenerator();

	private final String variableName;

	private VariableTerm(String variableName) {
		this.variableName = variableName;
	}

	public static VariableTerm getInstance(String variableName) {
		return INTERNER.intern(new VariableTerm(variableName));
	}

	public static VariableTerm getAnonymousInstance() {
		return getInstance(ANONYMOUS_VARIABLE_PREFIX + ANONYMOUS_VARIABLE_COUNTER.getNextId());
	}

	@Override
	public boolean isGround() {
		return false;
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		return Collections.singletonList(this);
	}

	@Override
	public Term substitute(Substitution substitution) {
		Term groundTerm = substitution.eval(this);
		if (groundTerm == null) {
			// If variable is not substituted, keep term as is.
			return this;
		}
		return  groundTerm;
	}

	@Override
	public String toString() {
		return variableName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		VariableTerm that = (VariableTerm) o;

		return variableName.equals(that.variableName);
	}

	@Override
	public int hashCode() {
		return variableName.hashCode();
	}

	@Override
	public int compareTo(Term o) {
		if (this == o) {
			return 0;
		}
		if (o == null) {
			return 1;
		}

		if (!(o instanceof VariableTerm)) {
			return -1;
		}

		VariableTerm other = (VariableTerm)o;

		return variableName.compareTo(other.variableName);
	}
}