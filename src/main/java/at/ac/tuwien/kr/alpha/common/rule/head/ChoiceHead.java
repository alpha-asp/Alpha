/*
 * Copyright (c) 2017-2020, 2022, the Alpha Team.
 * All rights reserved.
 *
 * Additional changes made by Siemens.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.common.rule.head;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Substitutable;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import static at.ac.tuwien.kr.alpha.Util.join;

/**
 * Represents the head of a choice rule.
 */
public class ChoiceHead extends Head {
	private final List<ChoiceElement> choiceElements;

	private final Term lowerBound;
	private final ComparisonOperator lowerOp;

	private final Term upperBound;
	private final ComparisonOperator upperOp;

	public static class ChoiceElement implements Substitutable<ChoiceElement> {
		public final Atom choiceAtom;
		public final List<Literal> conditionLiterals;

		public ChoiceElement(Atom choiceAtom, List<Literal> conditionLiterals) {
			this.choiceAtom = choiceAtom;
			this.conditionLiterals = conditionLiterals;
		}

		@Override
		public ChoiceElement substitute(Substitution substitution) {
			return new ChoiceElement(choiceAtom.substitute(substitution), substitution.substituteAll(conditionLiterals));
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
	public ChoiceHead substitute(Substitution substitution) {
		return new ChoiceHead(
				substitution.substituteAll(choiceElements),
				lowerBound.substitute(substitution),
				lowerOp,
				upperBound.substitute(substitution),
				upperOp);
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
		return this.upperOp == other.upperOp;
	}

}
