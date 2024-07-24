package at.ac.tuwien.kr.alpha.api.programs.atoms;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;

import java.util.List;
import java.util.Optional;

/**
 * An atom that is implemented using an additional ASP program (i.e. a module).
 * Note that a module atom itself can not be instantiated, but needs to be compiled
 * into some kind of instantiable atom by linking it to an ASP program that implements
 * the referenced module.
 */
public interface ModuleAtom extends Atom {

	String getModuleName();

	List<Term> getInput();

	List<Term> getOutput();

	ModuleInstantiationMode getInstantiationMode();

	@Override
	ModuleAtom substitute(Substitution substitution);

	interface ModuleInstantiationMode {
		Optional<Integer> requestedAnswerSets();

		ModuleInstantiationMode ALL = Optional::empty;

		static ModuleInstantiationMode forNumAnswerSets(int answerSets) {
			return () -> Optional.of(answerSets);
		}

	}

}
