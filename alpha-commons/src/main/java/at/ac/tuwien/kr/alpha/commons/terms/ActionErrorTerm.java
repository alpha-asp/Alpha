package at.ac.tuwien.kr.alpha.commons.terms;

import at.ac.tuwien.kr.alpha.api.terms.ActionResultTerm;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.commons.util.Interner;

class ActionErrorTerm extends AbstractActionResultTerm<ConstantTerm<String>> {

	private static final Interner<ActionErrorTerm> INTERNER = new Interner<>();

	ActionErrorTerm(ConstantTerm<String> value) {
		super(ActionResultTerm.ERROR_SYMBOL, value);
	}

	public static ActionErrorTerm getInstance(ConstantTerm<String> term) {
		return INTERNER.intern(new ActionErrorTerm(term));
	}

	@Override
	public boolean isSuccess() {
		return false;
	}
	
}
