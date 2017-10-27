package at.ac.tuwien.kr.alpha.grounder.atoms;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static at.ac.tuwien.kr.alpha.common.terms.ConstantTerm.getInstance;

/**
 * Atoms corresponding to rule bodies use this predicate, first term is rule number,
 * second is a term containing variable substitutions.
 */
public class RuleAtom implements Atom {
	public static final Predicate PREDICATE = new Predicate("_R_", 2);

	private final List<ConstantTerm<String>> terms;

	private RuleAtom(List<ConstantTerm<String>> terms) {
		if (terms.size() != 2) {
			throw new IllegalArgumentException();
		}

		this.terms = terms;
	}

	public RuleAtom(NonGroundRule nonGroundRule, Substitution substitution) {
		this(Arrays.<ConstantTerm<String>>asList(
			getInstance(Integer.toString(nonGroundRule.getRuleId())),
			getInstance(substitution.toString())
		));
	}

	@Override
	public Predicate getPredicate() {
		return PREDICATE;
	}

	@Override
	public List<Term> getTerms() {
		return Arrays.asList(
			terms.get(0),
			terms.get(1)
		);
	}

	@Override
	public boolean isGround() {
		// NOTE: Both terms are ConstantTerms, which are ground by definition.
		return true;
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	@Override
	public List<VariableTerm> getBindingVariables() {
		// NOTE: Both terms are ConstantTerms, which have no variables by definition.
		return Collections.emptyList();
	}

	@Override
	public List<VariableTerm> getNonBindingVariables() {
		return Collections.emptyList();
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		RuleAtom that = (RuleAtom) o;

		return terms.equals(that.terms);
	}

	@Override
	public int hashCode() {
		return 31 * PREDICATE.hashCode() + terms.hashCode();
	}

	@Override
	public String toString() {
		return PREDICATE.getPredicateName() + "(" + terms.get(0) + "," + terms.get(1) + ')';
	}
}