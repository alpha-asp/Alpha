package at.ac.tuwien.kr.alpha.grounder.atoms;

import at.ac.tuwien.kr.alpha.common.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
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
	private static final Predicate INTERVAL_PREDICATE = new BasicPredicate("_interval", 2);

	private final IntervalTerm intervalTerm;
	private final VariableTerm intervalRepresentingVariable;

	public IntervalAtom(IntervalTerm intervalTerm, VariableTerm intervalRepresentingVariable) {
		this.intervalTerm = intervalTerm;
		this.intervalRepresentingVariable = intervalRepresentingVariable;
	}

	public List<Substitution> getIntervalSubstitutions(Substitution partialSubstitution) {
		List<Substitution> substitutions = new ArrayList<>();
		for (int i = intervalTerm.getLowerBound(); i <= intervalTerm.getUpperBound(); i++) {
			Substitution ith = new Substitution(partialSubstitution);
			ith.put(intervalRepresentingVariable, ConstantTerm.getInstance(String.valueOf(i)));
			substitutions.add(ith);
		}
		return substitutions;
	}

	@Override
	public Predicate getPredicate() {
		return INTERVAL_PREDICATE;
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
	public boolean isInternal() {
		return true;
	}

	@Override
	public List<VariableTerm> getBindingVariables() {
		return Collections.singletonList(intervalRepresentingVariable);
	}

	@Override
	public List<VariableTerm> getNonBindingVariables() {
		return intervalTerm.getOccurringVariables();
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return new IntervalAtom((IntervalTerm) intervalTerm.substitute(substitution), intervalRepresentingVariable);
	}

	@Override
	public int compareTo(Atom o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(INTERVAL_PREDICATE.getPredicateName());
		sb.append("(");
		sb.append(intervalRepresentingVariable.toString());
		sb.append(", ");
		sb.append(intervalTerm.toString());
		sb.append(")");
		return sb.toString();
	}

	@Override
	public boolean isNegated() {
		// IntervalAtoms only occur positively.
		return false;
	}
}
