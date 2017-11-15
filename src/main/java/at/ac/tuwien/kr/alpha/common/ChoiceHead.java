package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;

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
		public final BasicAtom choiceAtom;
		public final List<Literal> conditionLiterals;

		public ChoiceElement(BasicAtom choiceAtom, List<Literal> conditionLiterals) {
			this.choiceAtom = choiceAtom;
			this.conditionLiterals = conditionLiterals;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append(choiceAtom);
			if (conditionLiterals == null || conditionLiterals.size() == 0) {
				return sb.toString();
			}
			sb.append(" : ");
			Util.appendDelimited(sb, ", ", conditionLiterals);

			return sb.toString();
		}
	}

	public ComparisonOperator getLowerOp() {
		return lowerOp;
	}

	public ComparisonOperator getUpperOp() {
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

	public ChoiceHead(List<ChoiceElement> choiceElements, Term lowerBound, ComparisonOperator lowerOp, Term upperBound, ComparisonOperator upperOp) {
		this.choiceElements = choiceElements;
		this.lowerBound = lowerBound;
		this.lowerOp = lowerOp;
		this.upperBound = upperBound;
		this.upperOp = upperOp;
	}

	@Override
	public boolean isNormal() {
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (lowerBound != null) {
			sb.append(lowerBound);
			sb.append(lowerOp);
		}
		sb.append("{ ");
		Util.appendDelimited(sb, "; ", choiceElements);
		sb.append(" }");

		if (upperBound != null) {
			sb.append(upperOp);
			sb.append(upperBound);
		}


		return sb.toString();
	}
}
