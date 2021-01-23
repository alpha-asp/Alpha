package at.ac.tuwien.kr.alpha.core.rules.heads;

import static at.ac.tuwien.kr.alpha.core.util.Util.join;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.rules.ChoiceHead;
import at.ac.tuwien.kr.alpha.core.atoms.CoreAtom;
import at.ac.tuwien.kr.alpha.core.atoms.CoreLiteral;
import at.ac.tuwien.kr.alpha.core.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.core.common.terms.CoreTerm;

/**
 * Represents the head of a choice rule.
 * 
 * Copyright (c) 2017-2019, the Alpha Team.
 */
public class ChoiceHeadImpl implements ChoiceHead {
	private final List<ChoiceElement> choiceElements;

	private final CoreTerm lowerBound;
	private final ComparisonOperator lowerOp;

	private final CoreTerm upperBound;
	private final ComparisonOperator upperOp;

	public static class ChoiceElement {
		public final CoreAtom choiceAtom;
		public final List<CoreLiteral> conditionLiterals;

		public ChoiceElement(CoreAtom choiceAtom, List<CoreLiteral> conditionLiterals) {
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

	public CoreTerm getLowerBound() {
		return lowerBound;
	}

	public CoreTerm getUpperBound() {
		return upperBound;
	}

	public ChoiceHeadImpl(List<ChoiceElement> choiceElements, CoreTerm lowerBound, ComparisonOperator lowerOp, CoreTerm upperBound, ComparisonOperator upperOp) {
		this.choiceElements = choiceElements;
		this.lowerBound = lowerBound;
		this.lowerOp = lowerOp;
		this.upperBound = upperBound;
		this.upperOp = upperOp;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.choiceElements == null) ? 0 : this.choiceElements.hashCode());
		result = prime * result + ((this.lowerBound == null) ? 0 : this.lowerBound.hashCode());
		result = prime * result + ((this.lowerOp == null) ? 0 : this.lowerOp.hashCode());
		result = prime * result + ((this.upperBound == null) ? 0 : this.upperBound.hashCode());
		result = prime * result + ((this.upperOp == null) ? 0 : this.upperOp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ChoiceHeadImpl)) {
			return false;
		}
		ChoiceHeadImpl other = (ChoiceHeadImpl) obj;
		if (this.choiceElements == null) {
			if (other.choiceElements != null) {
				return false;
			}
		} else if (!this.choiceElements.equals(other.choiceElements)) {
			return false;
		}
		if (this.lowerBound == null) {
			if (other.lowerBound != null) {
				return false;
			}
		} else if (!this.lowerBound.equals(other.lowerBound)) {
			return false;
		}
		if (this.lowerOp != other.lowerOp) {
			return false;
		}
		if (this.upperBound == null) {
			if (other.upperBound != null) {
				return false;
			}
		} else if (!this.upperBound.equals(other.upperBound)) {
			return false;
		}
		if (this.upperOp != other.upperOp) {
			return false;
		}
		return true;
	}

}
