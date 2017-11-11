package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.terms.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Substitution {
	private TreeMap<Variable, Term> substitution;

	private Substitution(TreeMap<Variable, Term> substitution) {
		this.substitution = substitution;
	}

	public Substitution() {
		this(new TreeMap<>());
	}

	public Substitution(Substitution clone) {
		this(new TreeMap<>(clone.substitution));
	}

	/**
	 * Checks if the left possible non-ground term unifies with the ground term.
	 * @param termNonGround
	 * @param termGround
	 * @return
	 */
	public boolean unifyTerms(Term termNonGround, Term termGround) {
		if (termNonGround == termGround) {
			// Both terms are either the same constant or the same variable term
			return true;
		} else if (termNonGround instanceof Constant) {
			// Since right term is ground, both terms differ
			return false;
		} else if (termNonGround instanceof Variable) {
			Variable variableTerm = (Variable)termNonGround;
			// Left term is variable, bind it to the right term.
			Term bound = eval(variableTerm);

			if (bound != null) {
				// Variable is already bound, return true if binding is the same as the current ground term.
				return termGround == bound;
			}

			substitution.put(variableTerm, termGround);
			return true;
		} else if (termNonGround instanceof FunctionTerm && termGround instanceof FunctionTerm) {
			// Both terms are function terms
			FunctionTerm ftNonGround = (FunctionTerm) termNonGround;
			FunctionTerm ftGround = (FunctionTerm) termGround;

			if (!(ftNonGround.getSymbol().equals(ftGround.getSymbol()))) {
				return false;
			}

			// Iterate over all subterms of both function terms
			for (int i = 0; i < ftNonGround.getTerms().size(); i++) {
				if (!unifyTerms(ftNonGround.getTerms().get(i), ftGround.getTerms().get(i))) {
					return false;
				}
			}

			return true;
		}
		return false;
	}

	/**
	 * This method should be used to obtain the {@link Constant} to be used in place of
	 * a given {@link Variable} under this substitution.
	 *
	 * @param variableTerm the variable term to substitute, if possible
	 * @return a constant term if the substitution contains the given variable, {@code null} otherwise.
	 */
	public Term eval(Variable variableTerm) {
		return this.substitution.get(variableTerm);
	}

	public <T extends Comparable<T>> Term put(Variable variableTerm, Constant<T> groundTerm) {
		// Note: We're destroying type information here.
		return substitution.put(variableTerm, groundTerm);
	}

	public boolean isEmpty() {
		return substitution.isEmpty();
	}

	public Term apply(Term term) {
		if (term.isGround()) {
			return term;
		}

		if (term instanceof FunctionTerm) {
			return apply((FunctionTerm) term);
		} else if (term instanceof Variable) {
			return apply((Variable) term);
		} else if (term instanceof IntervalTerm) {
			return apply((IntervalTerm) term);
		} else {
			throw new RuntimeException("Unknown term type discovered.");
		}
	}

	public FunctionTerm apply(FunctionTerm ft) {
		if (ft.isGround()) {
			return ft;
		}

		List<Term> groundTermList = new ArrayList<>(ft.getTerms().size());
		for (Term term : ft.getTerms()) {
			groundTermList.add(apply(term));
		}
		return FunctionTerm.getInstance(ft.getSymbol(), groundTermList);
	}

	public IntervalTerm apply(IntervalTerm it) {
		if (it.isGround()) {
			return it;
		}

		return IntervalTerm.getInstance(apply(it.getLowerBound()), apply(it.getUpperBound()));
	}

	public Term apply(Variable variable) {
		return eval(variable);
	}

	/**
	 * Prints the variable substitution in a uniform way (sorted by variable names).
	 *
	 * @return
	 */
	@Override
	public String toString() {
		final StringBuilder ret = new StringBuilder();
		for (Map.Entry<Variable, Term> e : substitution.entrySet()) {
			ret.append("_").append(e.getKey()).append(":").append(e.getValue());
		}
		return ret.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Substitution that = (Substitution) o;

		return substitution != null ? substitution.equals(that.substitution) : that.substitution == null;
	}

	@Override
	public int hashCode() {
		return substitution != null ? substitution.hashCode() : 0;
	}
}
