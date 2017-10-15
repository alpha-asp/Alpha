package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.AbstractMap;
import java.util.List;

/**
 * Represents the head of a choice rule.
 * Copyright (c) 2017, the Alpha Team.
 */
public class ChoiceHead implements Atom {
	private final List<AbstractMap.SimpleEntry<BasicAtom, List<Literal>>> choiceElements;
	private final Term lowerBound;

	public BuiltinAtom.BINOP getLowerOp() {
		return lowerOp;
	}

	public BuiltinAtom.BINOP getUpperOp() {
		return upperOp;
	}

	private final BuiltinAtom.BINOP lowerOp;
	private final Term upperBound;
	private final BuiltinAtom.BINOP upperOp;

	public List<AbstractMap.SimpleEntry<BasicAtom, List<Literal>>> getChoiceElements() {
		return choiceElements;
	}

	public Term getLowerBound() {
		return lowerBound;
	}

	public Term getUpperBound() {
		return upperBound;
	}

	public ChoiceHead(List<AbstractMap.SimpleEntry<BasicAtom, List<Literal>>> choiceElements, Term lowerBound, BuiltinAtom.BINOP lowerOp, Term upperBound, BuiltinAtom.BINOP upperOp) {
		this.choiceElements = choiceElements;
		this.lowerBound = lowerBound;
		this.lowerOp = lowerOp;
		this.upperBound = upperBound;
		this.upperOp = upperOp;
	}

	@Override
	public Predicate getPredicate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Term> getTerms() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isGround() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInternal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<VariableTerm> getBindingVariables() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<VariableTerm> getNonBindingVariables() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Atom substitute(Substitution substitution) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(Atom o) {
		throw new UnsupportedOperationException();
	}
}
