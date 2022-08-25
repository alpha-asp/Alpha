package at.ac.tuwien.kr.alpha.commons.terms;

import at.ac.tuwien.kr.alpha.api.terms.ActionResultTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.util.Interner;

class ActionSuccessTerm<T extends Term> extends AbstractActionResultTerm<T> {

	private static final Interner<ActionSuccessTerm<?>> INTERNER = new Interner<>();

	ActionSuccessTerm(T value) {
		super(ActionResultTerm.SUCCESS_SYMBOL, value);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Term> ActionSuccessTerm<T> getInstance(T term) {
		return (ActionSuccessTerm<T>) INTERNER.intern(new ActionSuccessTerm<>(term));
	}

	@Override
	public boolean isSuccess() {
		return true;
	}
	
}
