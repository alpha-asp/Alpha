package at.ac.tuwien.kr.alpha.common.rule.head.impl;

import static at.ac.tuwien.kr.alpha.Util.join;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.head.Head;
import at.ac.tuwien.kr.alpha.common.terms.Term;

/**
 * Represents the head of a choice rule.
 * 
 * Copyright (c) 2017-2019, the Alpha Team.
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
		if (!(obj instanceof ChoiceHead)) {
			return false;
		}
		ChoiceHead other = (ChoiceHead) obj;
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
