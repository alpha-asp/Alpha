package at.ac.tuwien.kr.alpha.api.programs.atoms;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.programs.VariableNormalizableAtom;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;

/**
 * An external atom, i.e. an {@link Atom} that is interpreted by calling a linked Java-Method.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface ExternalAtom extends Atom, VariableNormalizableAtom {

	boolean hasOutput();

	List<Term> getInput();

	List<Term> getOutput();

	PredicateInterpretation getInterpretation();

}
