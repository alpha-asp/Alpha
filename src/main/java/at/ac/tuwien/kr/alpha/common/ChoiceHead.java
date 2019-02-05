package at.ac.tuwien.kr.alpha.common;

import static at.ac.tuwien.kr.alpha.Util.join;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.Term;

/**
 * Represents the head of a choice rule.
 */
public class ChoiceHead extends Head {
	private final List<ChoiceElement> choiceElements;

	private final Term lowerBound;
	private final ComparisonOperator lowerOp;

	private final Term upperBound;
	private final ComparisonOperator upperOp;

	public static class ChoiceElement {
		public final Atom choiceAtom;
		public final List<Literal> conditionLiterals;

		public ChoiceElement(Atom choiceAtom, List<Literal> conditionLiterals) {
			this.choiceAtom = choiceAtom;
			this.conditionLiterals = conditionLiterals;
		}

		@Override
		public String toString() {
			String result = choiceAtom.toString();

			if (conditionLiterals == null || conditionLiterals.size() == 0) {
				return result;
			}

			return join(result + " : ", conditionLiterals, "");
		}
	}

	public ComparisonOperator getLowerOperator() {
		return lowerOp;
	}

	public ComparisonOperator getUpperOperator() {
		return upperOp;
	}

	public List<ChoiceElement> getChoiceElements() {
		return choiceElements;
	}

	public Term getLowerBound() {
		return lowerBound;
	}

	public Term getUpperBound() {
		return upperBound;
	}

	public ChoiceHead(List<ChoiceElement> choiceElements, Term lowerBound, ComparisonOperator lowerOp, Term upperBound,
			ComparisonOperator upperOp) {
		this.choiceElements = choiceElements;
		this.lowerBound = lowerBound;
		this.lowerOp = lowerOp;
		this.upperBound = upperBound;
		this.upperOp = upperOp;
	}

	/**
	 * TODO added this quick and dirty for dependency graph creation. However, could
	 * we use the atoms as keys for a map with condition literals (or also simply
	 * choice elements) as values? If that were the case, instead of creating new
	 * lists, we could return the key set of that map here.
	 */
	@Override
	public List<Atom> getAtoms() {
		List<Atom> retVal = new ArrayList<>();
		this.choiceElements.forEach((ce) -> retVal.add(ce.choiceAtom));
		return retVal;
	}

	@Override
	public boolean isNormal() {
		return false;
	}

	@Override
	public String toString() {
		String result = "";

		if (lowerBound != null) {
			result += lowerBound.toString() + lowerOp;
		}

		result += join("{ ", choiceElements, "; ", " }");

		if (upperBound != null) {
			result += upperOp.toString() + upperBound;
		}

		return result;
	}
}
