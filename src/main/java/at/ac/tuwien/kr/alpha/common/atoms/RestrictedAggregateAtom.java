package at.ac.tuwien.kr.alpha.common.atoms;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

public final class RestrictedAggregateAtom extends Atom {

	private final AggregateAtom atom;

	public RestrictedAggregateAtom(ComparisonOperator lowerBoundOperator, Term lowerBoundTerm, AggregateFunctionSymbol aggregateFunction,
			List<AggregateElement> aggregateElements) {
		this.atom = new AggregateAtom(lowerBoundOperator, lowerBoundTerm, null, null, aggregateFunction, aggregateElements);
	}

	public RestrictedAggregateAtom(AggregateAtom atom) {
		if (atom.getUpperBoundOperator() != null || atom.getUpperBoundTerm() != null) {
			throw new IllegalArgumentException("Cannot create RestrictedAggregateAtom from AggregateAtom with upper bound term and/or operator");
		}
		this.atom = atom;
	}

	@Override
	public Predicate getPredicate() {
		return this.atom.getPredicate();
	}

	@Override
	public List<Term> getTerms() {
		return this.atom.getTerms();
	}

	public ComparisonOperator getLowerBoundOperator() {
		return this.atom.getLowerBoundOperator();
	}

	public Term getLowerBoundTerm() {
		return this.atom.getLowerBoundTerm();
	}

	public AggregateFunctionSymbol getAggregatefunction() {
		return this.atom.getAggregatefunction();
	}

	public List<AggregateElement> getAggregateElements() {
		return this.atom.getAggregateElements();
	}

	/**
	 * Returns all variables occurring inside the aggregate, between { ... }.
	 * 
	 * @return each variable occurring in some aggregate element.
	 */
	public List<VariableTerm> getAggregateVariables() {
		return this.atom.getAggregateVariables();
	}

	@Override
	// Note: This method is deceptive: AggregateAtom#withTerms will throw an UnsupportedOperationException since not all
	// methods of Atom make sense for AggregateAtoms. The forwarding call to the wrapped atom here purely exists in the
	// interest of consistent code style and the theoretical scenario that some day, withTerms might get implemented in
	// AggregateAtom.
	public RestrictedAggregateAtom withTerms(List<Term> terms) {
		return new RestrictedAggregateAtom(this.atom.withTerms(terms));
	}

	@Override
	public boolean isGround() {
		return atom.isGround();
	}

	@Override
	// Note: This method is deceptive: AggregateAtom#substitute will throw an UnsupportedOperationException since not all
	// methods of Atom make sense for AggregateAtoms. The forwarding call to the wrapped atom here purely exists in the
	// interest of consistent code style and the theoretical scenario that some day, substitue might get implemented in
	// AggregateAtom.
	public RestrictedAggregateAtom substitute(Substitution substitution) {
		return new RestrictedAggregateAtom(this.atom.substitute(substitution));
	}

	@Override
	public RestrictedAggregateLiteral toLiteral(boolean positive) {
		return new RestrictedAggregateLiteral(this, positive);
	}

	@Override
	public boolean equals(Object o) {
		return this.atom.equals(o);
	}

	@Override
	public int hashCode() {
		return this.atom.hashCode();
	}

}
