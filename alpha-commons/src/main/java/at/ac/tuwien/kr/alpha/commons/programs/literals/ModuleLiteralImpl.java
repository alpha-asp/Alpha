package at.ac.tuwien.kr.alpha.commons.programs.literals;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ModuleAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.ModuleLiteral;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.api.programs.terms.VariableTerm;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ModuleLiteralImpl extends AbstractLiteral implements ModuleLiteral {

	ModuleLiteralImpl(ModuleAtom atom, boolean positive) {
		super(atom, positive);
	}

	@Override
	public ModuleAtom getAtom() {
		return (ModuleAtom) atom;
	}

	@Override
	public ModuleLiteral negate() {
		return new ModuleLiteralImpl(getAtom(), isNegated());
	}

	@Override
	public ModuleLiteral substitute(Substitution substitution) {
		return new ModuleLiteralImpl(getAtom().substitute(substitution), positive);
	}

	// TODO introduce common abstract supertype for external an module literals to avoid code duplication (same goes for atoms!)
	@Override
	public Set<VariableTerm> getBindingVariables() {
		// If the external atom is negative, then all variables of input and output are non-binding
		// and there are no binding variables (like for ordinary atoms).
		// If the external atom is positive, then variables of output are binding.

		if (this.isNegated()) {
			return Collections.emptySet();
		}

		List<Term> output = getAtom().getOutput();

		Set<VariableTerm> binding = new HashSet<>(output.size());

		for (Term out : output) {
			if (out instanceof VariableTerm) {
				binding.add((VariableTerm) out);
			}
		}

		return binding;
	}

	@Override
	public Set<VariableTerm> getNonBindingVariables() {
		List<Term> input = getAtom().getInput();
		List<Term> output = getAtom().getOutput();

		// External atoms have their input always non-binding, since they cannot
		// be queried without some concrete input.
		Set<VariableTerm> nonbindingVariables = new HashSet<>();
		for (Term term : input) {
			nonbindingVariables.addAll(term.getOccurringVariables());
		}

		// If the external atom is negative, then all variables of input and output are
		// non-binding.
		if (this.isNegated()) {
			for (Term out : output) {
				if (out instanceof VariableTerm) {
					nonbindingVariables.add((VariableTerm) out);
				}
			}
		}

		return nonbindingVariables;
	}

}
