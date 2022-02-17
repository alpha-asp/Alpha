package at.ac.tuwien.kr.alpha.commons.terms;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.util.IntIdGenerator;
import at.ac.tuwien.kr.alpha.commons.util.Interner;

/**
 * Copyright (c) 2016-2017, the Alpha Team.
 */
class VariableTermImpl extends AbstractTerm implements VariableTerm {
	
	private static final Interner<VariableTermImpl> INTERNER = new Interner<>();

	private static final String ANONYMOUS_VARIABLE_PREFIX = "_";
	private static final IntIdGenerator ANONYMOUS_VARIABLE_COUNTER = new IntIdGenerator();

	private final String variableName;

	private VariableTermImpl(String variableName) {
		this.variableName = variableName;
	}

	public static VariableTermImpl getInstance(String variableName) {
		return INTERNER.intern(new VariableTermImpl(variableName));
	}

	public static VariableTermImpl getAnonymousInstance() {
		return getInstance(ANONYMOUS_VARIABLE_PREFIX + ANONYMOUS_VARIABLE_COUNTER.getNextId());
	}

	@Override
	public boolean isGround() {
		return false;
	}

	@Override
	public Set<VariableTerm> getOccurringVariables() {
		return Collections.singleton(this);
	}

	@Override
	public Term substitute(Substitution substitution) {
		Term groundTerm = substitution.eval(this);
		if (groundTerm == null) {
			// If variable is not substituted, keep term as is.
			return this;
		}
		return groundTerm;
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

		VariableTermImpl that = (VariableTermImpl) o;

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

		if (!(o instanceof VariableTerm)) {
			return super.compareTo(o);
		}

		VariableTerm other = (VariableTerm) o;
		return variableName.compareTo(other.getName());
	}

	@Override
	public Term normalizeVariables(String renamePrefix, Term.RenameCounter counter) {
		VariableTerm renamedThis = counter.getRenamedVariables().get(this);
		if (renamedThis != null) {
			return renamedThis;
		} else {
			VariableTerm renamedVariable = VariableTermImpl.getInstance(renamePrefix + counter.getAndIncrement());
			counter.getRenamedVariables().put(this, renamedVariable);
			return renamedVariable;
		}
	}

	@Override
	public VariableTerm renameVariables(Function<String, String> mapping) {
		return new VariableTermImpl(mapping.apply(this.variableName));
	}

	@Override
	public String getName() {
		return variableName;
	}
}