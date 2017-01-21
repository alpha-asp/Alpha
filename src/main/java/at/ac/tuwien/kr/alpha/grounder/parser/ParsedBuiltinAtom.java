package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Atom;
import at.ac.tuwien.kr.alpha.common.BuiltinAtom;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedBuiltinAtom extends ParsedAtom {
	public BINOP binop;

	public enum BINOP {
		EQ("=",  new BasicPredicate("=",  2)),
		NE("!=", new BasicPredicate("!=", 2)),
		LT("<",  new BasicPredicate("<",  2)),
		GT(">",  new BasicPredicate(">",  2)),
		LE("<=", new BasicPredicate("<=", 2)),
		GE(">=", new BasicPredicate(">=", 2));

		private String asString;
		private Predicate asPredicate;

		BINOP(String asString, Predicate asPredicate) {
			this.asString = asString;
			this.asPredicate = asPredicate;
		}

		@Override
		public String toString() {
			return asString;
		}

		public Predicate toPredicate() {
			return asPredicate;
		}

		public BINOP getNegation() {
			switch (this) {
				case EQ: return NE;
				case NE: return EQ;
				case LT: return GE;
				case GT: return LE;
				case LE: return GT;
				case GE: return LT;
			}
			throw new RuntimeException("Unknown BINOP encountered, cannot negate it.");
		}
	};

	public ParsedBuiltinAtom(BINOP binop, List<ParsedTerm> terms) {
		super(binop.toString(), terms);
		this.binop = binop;
	}

	public ParsedBuiltinAtom(ParsedTerm left, BINOP binop, ParsedTerm right) {
		this(binop, Arrays.asList(left, right));
	}

	@Override
	public Atom toAtom() {
		return new BuiltinAtom(this);
	}

	@Override
	public String toString() {
		return terms.get(0) + " " + binop + " " + terms.get(1);
	}

	public ParsedBuiltinAtom getNegation() {
		return new ParsedBuiltinAtom(binop.getNegation(), terms);
	}
}