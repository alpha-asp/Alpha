package at.ac.tuwien.kr.alpha.commons.programs.literals;

import at.ac.tuwien.kr.alpha.api.programs.atoms.*;
import at.ac.tuwien.kr.alpha.api.programs.literals.*;

public final class Literals {

	private Literals() {
		throw new AssertionError("Cannot instantiate utility class");
	}

	public static BasicLiteral fromAtom(BasicAtom atom, boolean positive) {
		return new BasicLiteralImpl(atom, positive);
	}

	public static AggregateLiteral fromAtom(AggregateAtom atom, boolean positive) {
		return new AggregateLiteralImpl(atom, positive);
	}

	public static ComparisonLiteral fromAtom(ComparisonAtom atom, boolean positive) {
		return new ComparisonLiteralImpl(atom, positive);
	}

	public static ExternalLiteral fromAtom(ExternalAtom atom, boolean positive) {
		return new ExternalLiteralImpl(atom, positive);
	}

	public static ModuleLiteral fromAtom(ModuleAtom atom, boolean positive) {
		return new ModuleLiteralImpl(atom, positive);
	}

	public static ComparisonLiteral newComparisonLiteral(ComparisonAtom atom, boolean positive) {
		return new ComparisonLiteralImpl(atom, positive);
	}
	
}
