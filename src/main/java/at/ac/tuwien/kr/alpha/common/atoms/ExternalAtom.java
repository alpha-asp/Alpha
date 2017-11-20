package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.Util.join;
import static java.util.Collections.emptyList;

public class ExternalAtom implements FixedInterpretationLiteral {
	private final List<Term> input;
	private final List<Term> output;

	protected Predicate predicate;
	protected final PredicateInterpretation interpretation;
	protected final boolean negated;

	public ExternalAtom(Predicate predicate, PredicateInterpretation interpretation, List<Term> input, List<Term> output, boolean negated) {
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

		Set<List<ConstantTerm>> results = interpretation.evaluate(substitutes);

		if (results == null) {
			throw new NullPointerException("Predicate " + getPredicate().getName() + " returned null. It must return a Set.");
		}

		if (results.isEmpty()) {
			return emptyList();
		}

		for (List<ConstantTerm> bindings : results) {
			if (bindings.size() < output.size()) {
				throw new RuntimeException("Predicate " + getPredicate().getName() + " returned " + bindings.size() + " terms when at least " + output.size() + " were expected.");
			}

			Substitution ith = new Substitution(partialSubstitution);
			boolean skip = false;
			for (int i = 0; i < output.size(); i++) {
				Term out = output.get(i);

				if (out instanceof VariableTerm) {
					ith.put((VariableTerm) out, bindings.get(i));
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
	public Type getType() {
		return Type.EXTERNAL_ATOM;
	}

	@Override
	public boolean isNegated() {
		return negated;
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	public PredicateInterpretation getInterpretation() {
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
	public List<VariableTerm> getBindingVariables() {
		// If the external atom is negative, then all variables of input and output are non-binding
		// and there are no binding variables (like for ordinary atoms).
		// If the external atom is positive, then variables of output are binding.

		if (isNegated()) {
			return emptyList();
		}

		List<VariableTerm> binding = new ArrayList<>(output.size());

		for (Term out : output) {
			if (out instanceof VariableTerm) {
				binding.add((VariableTerm) out);
			}
		}

		return binding;
	}

	@Override
	public List<VariableTerm> getNonBindingVariables() {
		// External atoms have their input always non-binding, since they cannot
		// be queried without some concrete input.
		LinkedList<VariableTerm> nonbindingVariables = new LinkedList<>();
		for (Term term : input) {
			nonbindingVariables.addAll(term.getOccurringVariables());
		}

		// If the external atom is negative, then all variables of input and output are non-binding.
		if (negated) {
			for (Term out : output) {
				if (out instanceof VariableTerm) {
					nonbindingVariables.add((VariableTerm) out);
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
		String result = "&" + predicate.getName();
		if (!output.isEmpty()) {
			result += join("[", output, "]");
		}
		if (!input.isEmpty()) {
			result += join("(", input, ")");
		}
		return result;
	}
}
