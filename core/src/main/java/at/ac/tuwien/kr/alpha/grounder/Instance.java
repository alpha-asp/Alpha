package at.ac.tuwien.kr.alpha.grounder;

import static at.ac.tuwien.kr.alpha.Util.join;

import java.util.Arrays;
import java.util.List;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.atoms.CoreAtom;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.TermImpl;

/**
 * An instance is a positional association of terms, e.g., representing a variable substitution, or a ground instance of
 * a predicate.
 * Copyright (c) 2016, the Alpha Team.
 */
public class Instance {
	public final List<? extends TermImpl> terms;

	public Instance(TermImpl... terms) {
		this(Arrays.asList(terms));
	}

	public Instance(List<? extends TermImpl> terms) {
		this.terms = terms;
	}

	public static Instance fromAtom(CoreAtom atom) {
		if (!atom.isGround()) {
			throw Util.oops("Cannot create instance from non-ground atom " + atom.toString());
		}
		return new Instance(atom.getTerms());
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		return terms.equals(((Instance) o).terms);
	}

	@Override
	public int hashCode() {
		return terms.hashCode();
	}

	@Override
	public String toString() {
		return join("(", terms, ")");
	}
}
