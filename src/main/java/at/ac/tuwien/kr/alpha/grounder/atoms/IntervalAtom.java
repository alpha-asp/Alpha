package at.ac.tuwien.kr.alpha.grounder.atoms;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.symbols.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Constant;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.Variable;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Helper for treating IntervalTerms in rules. Each IntervalTerm is replaced by a variable and a special IntervalAtom
 * is added to the rule body for generating all bindings of the variable.
 * Copyright (c) 2017, the Alpha Team.
 */
public class IntervalAtom implements Literal {
	private static final Predicate PREDICATE = Predicate.getInstance("_interval", 2, true);

	private final IntervalTerm intervalTerm;
	private final Variable intervalRepresentingVariable;

	public IntervalAtom(IntervalTerm intervalTerm, Variable intervalRepresentingVariable) {
		this.intervalTerm = intervalTerm;
		this.intervalRepresentingVariable = intervalRepresentingVariable;
	}

	@SuppressWarnings("unchecked")
	public List<Substitution> getIntervalSubstitutions(Substitution partialSubstitution) {
		List<Substitution> substitutions = new ArrayList<>();

		int lower = ((Constant<Integer>)intervalTerm.getLowerBound()).getSymbol();
		int upper = ((Constant<Integer>)intervalTerm.getUpperBound()).getSymbol();

		for (int i = lower; i <= upper; i++) {
			Substitution ith = new Substitution(partialSubstitution);
			ith.put(intervalRepresentingVariable, Constant.getInstance(i));
			substitutions.add(ith);
		}
		return substitutions;
	}

	@Override
	public Predicate getPredicate() {
		return PREDICATE;
	}

	@Override
	public List<Term> getTerms() {
		return Arrays.asList(intervalRepresentingVariable, intervalTerm);
	}

	@Override
	public boolean isGround() {
		return false;
	}

	@Override
	public List<Variable> getBindingVariables() {
		return Collections.singletonList(intervalRepresentingVariable);
	}

	@Override
	public List<Variable> getNonBindingVariables() {
		return intervalTerm.getOccurringVariables();
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return new IntervalAtom(intervalTerm.substitute(substitution), intervalRepresentingVariable);
	}

	@Override
	public String toString() {
		return PREDICATE.getSymbol() + "(" + intervalRepresentingVariable + ", " + intervalTerm + ")";
	}

	@Override
	public boolean isNegated() {
		// IntervalAtoms only occur positively.
		return false;
	}
}
