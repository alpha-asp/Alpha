package at.ac.tuwien.kr.alpha.commons.atoms;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.terms.Term;

public final class Atoms {

	private Atoms() {
		throw new AssertionError("Cannot instantiate utility class");
	}

	/**
	 * Creates a positive BasicAtom over predicate and terms.
	 * 
	 * @param predicate
	 * @param terms
	 */
	public static BasicAtom newBasicAtom(Predicate predicate, List<Term> terms) {
		return new BasicAtomImpl(predicate, terms);
	}

	public static BasicAtom newBasicAtom(Predicate predicate, Term... terms) {
		return new BasicAtomImpl(predicate, Arrays.asList(terms));
	}

	public static BasicAtom newBasicAtom(Predicate predicate) {
		return new BasicAtomImpl(predicate, Collections.emptyList());
	}

}
