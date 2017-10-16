package at.ac.tuwien.kr.alpha.grounder.atoms;

import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.List;

/**
 * An internal ordinary atom, i.e., a BasicAtom that is internal and not reported in answer sets.
 * Copyright (c) 2017, the Alpha Team.
 */
public class HiddenAtom extends BasicAtom {
	public HiddenAtom(Predicate predicate, List<Term> terms, boolean isNegated) {
		super(predicate, terms, isNegated);
	}

	public HiddenAtom(Predicate predicate, List<Term> terms) {
		super(predicate, terms);
	}

	public HiddenAtom(BasicAtom clone) {
		super(clone);
	}

	public HiddenAtom(BasicAtom clone, boolean isNegated) {
		super(clone, isNegated);
	}

	public HiddenAtom(Predicate predicate, Term... terms) {
		super(predicate, terms);
	}

	public HiddenAtom(Predicate predicate) {
		super(predicate);
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return new HiddenAtom((BasicAtom) super.substitute(substitution));
	}

	@Override
	public boolean isInternal() {
		return true;
	}
}
