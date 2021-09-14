package at.ac.tuwien.kr.alpha.commons.atoms;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ActionAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.terms.Term;

class ActionAtomImpl extends AbstractAtom implements ActionAtom {

	@Override
	public Predicate getPredicate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Term> getTerms() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isGround() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Literal toLiteral(boolean positive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Atom substitute(Substitution substitution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Atom withTerms(List<Term> terms) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

}
