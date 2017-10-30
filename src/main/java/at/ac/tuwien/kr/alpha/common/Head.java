package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;

/**
 * Represents the head of a rule, i.e., either a choice or a disjunction of atoms, but not both.
 * For normal rules the disjunction contains only one atom.
 *
 * Copyright (c) 2017, the Alpha Team.
 */
public class Head {
	public final ChoiceHead choiceHead;
	public final List<Atom> disjunctiveHead;

	private Head(ChoiceHead choiceHead, List<Atom> disjunctiveHead) {
		this.choiceHead = choiceHead;
		this.disjunctiveHead = disjunctiveHead;
		if (choiceHead != null && disjunctiveHead != null) {
			throw new RuntimeException("Created rule head with choice and disjunctive part. Should not happen.");
		}
		if (disjunctiveHead != null && disjunctiveHead.size() > 1) {
			throw new RuntimeException("Disjunction in rule heads is not yet supported.");
		}
	}

	public static Head constructChoiceHead(ChoiceHead choiceHead) {
		return new Head(choiceHead, null);
	}

	public static Head constructDisjunctiveHead(List<Atom> disjunctiveHead) {
		return new Head(null, disjunctiveHead);
	}

	public boolean isNormal() {
		return disjunctiveHead != null && disjunctiveHead.size() <= 1;
	}

	@Override
	public String toString() {
		if (isNormal()) {
			return disjunctiveHead.get(0).toString();
		} else if (choiceHead != null) {
			return choiceHead.toString();
		}
		return super.toString();	// TODO: print actual disjunction here.
	}

	/**
	 * Represents the head of a choice rule.
	 */
	public static class ChoiceHead {
		private final List<ChoiceElement> choiceElements;
		private final Term lowerBound;
		private final BinaryOperator lowerOp;
		private final Term upperBound;
		private final BinaryOperator upperOp;

		public static class ChoiceElement {
			public final BasicAtom choiceAtom;
			public final List<Literal> conditionLiterals;

			public ChoiceElement(BasicAtom choiceAtom, List<Literal> conditionLiterals) {
				this.choiceAtom = choiceAtom;
				this.conditionLiterals = conditionLiterals;
			}
		}

		public BinaryOperator getLowerOp() {
			return lowerOp;
		}

		public BinaryOperator getUpperOp() {
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

		public ChoiceHead(List<ChoiceElement> choiceElements, Term lowerBound, BinaryOperator lowerOp, Term upperBound, BinaryOperator upperOp) {
			this.choiceElements = choiceElements;
			this.lowerBound = lowerBound;
			this.lowerOp = lowerOp;
			this.upperBound = upperBound;
			this.upperOp = upperOp;
		}
	}
}
