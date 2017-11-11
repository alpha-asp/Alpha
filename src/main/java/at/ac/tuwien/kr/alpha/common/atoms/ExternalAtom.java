package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.symbols.Predicate;
import at.ac.tuwien.kr.alpha.common.interpretations.FixedInterpretation;
import at.ac.tuwien.kr.alpha.common.terms.Constant;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.Variable;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.Util.join;
import static java.util.Collections.emptyList;

public class ExternalAtom implements Literal {
	private final List<Term> input;
	private final List<Term> output;

	protected Predicate predicate;
	protected final FixedInterpretation interpretation;
	protected final boolean negated;

	public ExternalAtom(Predicate predicate, FixedInterpretation interpretation, List<Term> input, List<Term> output, boolean negated) {
		this.predicate = predicate;
		this.interpretation = interpretation;
		this.input = input;
		this.output = output;
		this.negated = negated;
	}

	@SuppressWarnings("unchecked")
	public List<Substitution> getSubstitutions(Substitution partialSubstitution) {
		List<Substitution> substitutions = new ArrayList<>();
		List<Term> substitutes = new ArrayList<>(input.size());

		for (Term t : input) {
			substitutes.add(t.substitute(partialSubstitution));
		}

		Set<List<Constant>> results = interpretation.evaluate(substitutes);

		if (results == null) {
			throw new NullPointerException("Predicate " + getPredicate().getSymbol() + " returned null. It must return a Set.");
		}

		if (results.isEmpty()) {
			return emptyList();
		}

		for (List<Constant> bindings : results) {
			if (bindings.size() < output.size()) {
				throw new RuntimeException("Predicate " + getPredicate().getSymbol() + " returned " + bindings.size() + " terms when at least " + output.size() + " were expected.");
			}

			Substitution ith = new Substitution(partialSubstitution);
			boolean skip = false;
			for (int i = 0; i < output.size(); i++) {
				Term out = output.get(i);

				if (out instanceof Variable) {
					ith.put((Variable) out, bindings.get(i));
				} else {
					if (!bindings.get(i).equals(out)) {
						skip = true;
						break;
					}
				}
			}

			if (!skip) {
				substitutions.add(ith);
			}
		}

		return substitutions;
	}

	public boolean hasOutput() {
		return !output.isEmpty();
	}

	@Override
	public boolean isNegated() {
		return negated;
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	public FixedInterpretation getInterpretation() {
		return interpretation;
	}

	@Override
	public List<Term> getTerms() {
		return input;
	}

	@Override
	public boolean isGround() {
		return false;
	}

	@Override
	public List<Variable> getBindingVariables() {
		// If the external atom is negative, then all variables of input and output are non-binding
		// and there are no binding variables (like for ordinary atoms).
		// If the external atom is positive, then variables of output are binding.

		if (isNegated()) {
			return emptyList();
		}

		List<Variable> binding = new ArrayList<>(output.size());

		for (Term out : output) {
			if (out instanceof Variable) {
				binding.add((Variable) out);
			}
		}

		return binding;
	}

	@Override
	public List<Variable> getNonBindingVariables() {
		// External atoms have their input always non-binding, since they cannot
		// be queried without some concrete input.
		LinkedList<Variable> nonbindingVariables = new LinkedList<>();
		for (Term term : input) {
			nonbindingVariables.addAll(term.getOccurringVariables());
		}

		// If the external atom is negative, then all variables of input and output are non-binding.
		if (negated) {
			for (Term out : output) {
				if (out instanceof Variable) {
					nonbindingVariables.add((Variable) out);
				}
			}
		}

		return nonbindingVariables;
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return new ExternalAtom(
			predicate,
			interpretation,
			input
				.stream()
				.map(t -> t.substitute(substitution))
				.collect(Collectors.toList()),
			output,
			negated
		);
	}

	@Override
	public String toString() {
		String result = "&" + predicate.getSymbol();
		if (!output.isEmpty()) {
			result += join("[", output, "]");
		}
		if (!input.isEmpty()) {
			result += join("(", input, ")");
		}
		return result;
	}
}
