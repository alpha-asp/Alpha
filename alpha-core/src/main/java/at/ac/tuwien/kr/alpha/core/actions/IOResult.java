package at.ac.tuwien.kr.alpha.core.actions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;

public class IOResult<T extends Term> implements FunctionTerm {

	private final Optional<T> value;
	private final Optional<ConstantTerm<String>> error;

	public boolean isSuccess() {
		return value.isPresent();
	}

	public boolean isError() {
		return error.isPresent();
	}

	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public Set<VariableTerm> getOccurringVariables() {
		return Collections.emptySet();
	}

	@Override
	public Term substitute(Substitution substitution) {
		return this;
	}

	@Override
	public Term renameVariables(String renamePrefix) {
		return this;
	}

	@Override
	public Term normalizeVariables(String renamePrefix, RenameCounter counter) {
		return this;
	}

	@Override
	public int compareTo(Term arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Term> getTerms() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSymbol() {
		// TODO Auto-generated method stub
		return null;
	}

}
