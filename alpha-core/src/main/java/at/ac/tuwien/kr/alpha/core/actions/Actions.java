package at.ac.tuwien.kr.alpha.core.actions;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;

public class Actions {

	public static FunctionTerm printLine(List<Term> input) {
		if (input.size() != 1) {
			// TODO do properly
			throw new RuntimeException("Incorrect arity!");
		}
		// TODO this should only work on ConstantTerm<String>
		System.out.println(input.get(0).toString());
		return Terms.newFunctionTerm("ok");
	}

}
