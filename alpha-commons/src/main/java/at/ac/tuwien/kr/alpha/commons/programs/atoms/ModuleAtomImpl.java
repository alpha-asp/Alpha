package at.ac.tuwien.kr.alpha.commons.programs.atoms;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ModuleAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.ModuleLiteral;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.programs.literals.Literals;
import at.ac.tuwien.kr.alpha.commons.util.Util;
import org.apache.commons.collections4.ListUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class ModuleAtomImpl extends AbstractAtom implements ModuleAtom {

	private final String moduleName;
	private final List<Term> input;
	private final List<Term> output;
	private final ModuleInstantiationMode instantiationMode;

	ModuleAtomImpl(String moduleName, List<Term> input, List<Term> output, ModuleInstantiationMode instantiationMode) {
		this.moduleName = Objects.requireNonNull(moduleName);
		this.input = Objects.requireNonNull(input);
		this.output = Objects.requireNonNull(output);
		this.instantiationMode = Objects.requireNonNull(instantiationMode);
	}

	@Override
	public String getModuleName() {
		return moduleName;
	}

	@Override
	public List<Term> getInput() {
		return input;
	}

	@Override
	public List<Term> getOutput() {
		return output;
	}

	@Override
	public ModuleInstantiationMode getInstantiationMode() {
		return instantiationMode;
	}

	@Override
	public Atom withTerms(List<Term> terms) {
		if (terms.size() != this.input.size() + this.output.size()) {
			throw new IllegalArgumentException(
					"Cannot apply term list " + terms + " to module atom " + this + ", terms has invalid size!");
		}
		List<Term> newInput = terms.subList(0, this.input.size());
		List<Term> newOutput = terms.subList(this.input.size(), terms.size());
		return new ModuleAtomImpl(this.moduleName, newInput, newOutput, this.instantiationMode);
	}

	@Override
	public ModuleAtom substitute(Substitution substitution) {
		List<Term> substitutedInput = this.input.stream().map(t -> t.substitute(substitution)).collect(Collectors.toList());
		List<Term> substitutedOutput = this.output.stream().map(t -> t.substitute(substitution)).collect(Collectors.toList());
		return new ModuleAtomImpl(this.moduleName, substitutedInput, substitutedOutput, this.instantiationMode);
	}

	@Override
	public List<Term> getTerms() {
		return ListUtils.union(input, output);
	}

	@Override
	public Predicate getPredicate() {
		return Predicates.getPredicate(moduleName, output.size());
	}

	@Override
	public boolean isGround() {
		for (Term t : input) {
			if (!t.isGround()) {
				return false;
			}
		}
		for (Term t : output) {
			if (!t.isGround()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ModuleLiteral toLiteral(boolean positive) {
		return Literals.fromAtom(this, positive);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ModuleAtomImpl that = (ModuleAtomImpl) o;
		return Objects.equals(moduleName, that.moduleName)
				&& Objects.equals(input, that.input)
				&& Objects.equals(output, that.output)
				&& Objects.equals(instantiationMode, that.instantiationMode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(moduleName, input, output, instantiationMode);
	}

	@Override
	public String toString() {
		String result = "#" + moduleName;
		if (!input.isEmpty()) {
			result += Util.join("[", input, "]");
		}
		if (!output.isEmpty()) {
			result += Util.join("(", output, ")");
		}
		return result;
	}

}
