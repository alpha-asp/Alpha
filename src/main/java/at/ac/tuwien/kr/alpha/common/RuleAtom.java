package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.common.ConstantTerm.getInstance;

/**
 * Atoms corresponding to rule bodies use this predicate, first term is rule number,
 * second is a term containing variable substitutions.
 */
public class RuleAtom implements Atom {
	public static final Predicate PREDICATE = new BasicPredicate("_R_", 2);

	private final List<Term> terms;

	private RuleAtom(List<Term> terms) {
		if (terms.size() != 2) {
			throw new IllegalArgumentException();
		}

		this.terms = terms;
	}

	public RuleAtom(NonGroundRule nonGroundRule, Substitution substitution) {
		this(Arrays.asList(
			getInstance(Integer.toString(nonGroundRule.getRuleId())),
			getInstance(substitution.toUniformString())
		));
	}

	public RuleAtom(Term... terms) {
		this(Arrays.asList(terms));
	}

	@Override
	public Predicate getPredicate() {
		return PREDICATE;
	}

	@Override
	public List<Term> getTerms() {
		return terms;
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
	public List<VariableTerm> getOccurringVariables() {
		// NOTE: Both terms are ConstantTerms, which have no variables by definition.
		return Collections.emptyList();
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return new RuleAtom(terms.stream().map(t -> {
			return t.substitute(substitution);
		}).collect(Collectors.toList()));
	}

	@Override
	public int compareTo(Atom o) {
		if (!(o instanceof  RuleAtom)) {
			return 1;
		}
		RuleAtom other = (RuleAtom)o;
		int result = terms.get(0).compareTo(other.terms.get(0));
		if (result != 0) {
			return result;
		}
		return terms.get(1).compareTo(other.terms.get(1));
	}

	@Override
	public String toString() {
		return PREDICATE.getPredicateName() + "(" + terms.get(0) + "," + terms.get(1) + ')';
	}
}