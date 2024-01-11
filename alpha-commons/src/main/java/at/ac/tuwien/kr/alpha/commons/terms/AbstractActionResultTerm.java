package at.ac.tuwien.kr.alpha.commons.terms;

import java.util.Collections;

import at.ac.tuwien.kr.alpha.api.terms.ActionResultTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;

abstract class AbstractActionResultTerm<T extends Term> extends FunctionTermImpl implements ActionResultTerm<T> {

	AbstractActionResultTerm(String symbol, T value) {
		super(symbol, Collections.singletonList(value));
	}

	public abstract boolean isSuccess();

	public boolean isError() {
		return !isSuccess();
	}

	// Note: Unchecked cast is ok, we permit only instances of T as constructor arguments.
	@SuppressWarnings("unchecked")
	public T getValue() {
		return (T) getTerms().get(0);
	}

}
