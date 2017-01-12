package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.grounder.VariableSubstitution;

import java.util.Collections;
import java.util.List;

import static at.ac.tuwien.kr.alpha.common.ConstantTerm.getInstance;

/**
 * Atoms corresponding to rule bodies use this predicate, first term is rule number,
 * second is a term containing variable substitutions.
 */
public class RuleAtom implements Atom {
	public static final Predicate PREDICATE = new BasicPredicate("_R_", 2);

	private final Term[] terms;

	public RuleAtom(NonGroundRule nonGroundRule, VariableSubstitution substitution) {
		this.terms = new Term[]{
			getInstance(Integer.toString(nonGroundRule.getRuleId())),
			getInstance(substitution.toUniformString())
		};
	}

	public RuleAtom(Term... terms) {
		if (terms.length != 2) {
			throw new IllegalArgumentException();
		}
		this.terms = terms;
	}

	@Override
	public Predicate getPredicate() {
		return PREDICATE;
	}

	@Override
	public Term[] getTerms() {
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
	public int compareTo(Atom o) {
		if (!(o instanceof  RuleAtom)) {
			return 1;
		}
		RuleAtom other = (RuleAtom)o;
		int result = terms[0].compareTo(other.terms[0]);
		if (result != 0) {
			return result;
		}
		return terms[1].compareTo(other.terms[1]);
	}
}
