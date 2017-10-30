package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.predicates.FixedInterpretationPredicate;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class ExternalAtom implements Literal {
	private final List<Term> input;
	private final List<VariableTerm> output;

	protected final FixedInterpretationPredicate predicate;
	protected final boolean negated;

	public ExternalAtom(FixedInterpretationPredicate predicate, List<Term> input, List<VariableTerm> output, boolean negated) {
		this.predicate = predicate;
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

		Set<List<ConstantTerm>> results = predicate.evaluate(substitutes);

		if (results == null) {
			throw new NullPointerException("Predicate " + getPredicate().getPredicateName() + " returned null. It must return a Set.");
		}

		if (results.isEmpty()) {
			return emptyList();
		}

		for (List<ConstantTerm> bindings : results) {
			if (bindings.size() < output.size()) {
				throw new RuntimeException("Predicate " + getPredicate().getPredicateName() + " returned " + bindings.size() + " terms when at least " + output.size() + " were expected.");
			}
			Substitution ith = new Substitution(partialSubstitution);
			for (int i = 0; i < output.size(); i++) {
				ith.put(output.get(i), bindings.get(i));
			}
			substitutions.add(ith);
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
		if (negated) {
			// Negative literal has no binding variables.
			return Collections.emptyList();
		}
		LinkedList<VariableTerm> bindingVariables = new LinkedList<>();
		for (Term term : input) {
			bindingVariables.addAll(term.getOccurringVariables());
		}
		bindingVariables.addAll(output);
		return bindingVariables;
	}

	@Override
	public List<VariableTerm> getNonBindingVariables() {
		if (!negated) {
			// Positive literal has only binding variables.
			return Collections.emptyList();
		}
		LinkedList<VariableTerm> nonbindingVariables = new LinkedList<>();
		for (Term term : input) {
			nonbindingVariables.addAll(term.getOccurringVariables());
		}
		nonbindingVariables.addAll(output);
		return nonbindingVariables;
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return new ExternalAtom(predicate, input.stream().map(t -> {
			return t.substitute(substitution);
		}).collect(Collectors.toList()), output, negated);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("&");
		sb.append(predicate.getPredicateName());
		if (!output.isEmpty()) {
			sb.append("[");
			Util.appendDelimited(sb, output);
			sb.append("]");
		}
		if (!input.isEmpty()) {
			sb.append("(");
			Util.appendDelimited(sb, input);
			sb.append(")");
		}
		return sb.toString();
	}
}
