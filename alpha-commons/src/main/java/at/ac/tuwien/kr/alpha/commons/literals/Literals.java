package at.ac.tuwien.kr.alpha.commons.literals;

import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.literals.AggregateLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;

public final class Literals {
	
	private Literals() {
		throw new AssertionError("Cannot instantiate utility class");
	}

	public static Literal fromAtom(Atom atom, boolean positive) {
		return null; // TODO
	}
	
	public static AggregateLiteral fromAtom(AggregateAtom atom, boolean positive) {
		return null; // TODO
	}
	
}
