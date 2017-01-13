package at.ac.tuwien.kr.alpha.grounder.parser;

import java.util.Arrays;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedBuiltinAtom extends ParsedAtom {
	public BINOP binop;

	public enum BINOP {EQUAL, UNEQUAL, LESS, GREATER, LESS_OR_EQ, GREATER_OR_EQ;

		@Override
		public String toString() {
			switch (this) {
				case EQUAL: return "=";
				case UNEQUAL: return "!=";
				case LESS: return "<";
				case GREATER: return ">";
				case LESS_OR_EQ: return "<=";
				case GREATER_OR_EQ: return ">=";
			}
			return null;
		}
	};

	public ParsedBuiltinAtom(ParsedTerm left, BINOP binop, ParsedTerm right) {
		super(binop.toString(), Arrays.asList(left, right));
		this.binop = binop;
	}

	public void negateBinop() {
		switch (binop) {
			case EQUAL:
				binop = BINOP.UNEQUAL;
				break;
			case UNEQUAL:
				binop = BINOP.EQUAL;
				break;
			case LESS:
				binop = BINOP.GREATER_OR_EQ;
				break;
			case GREATER:
				binop = BINOP.LESS_OR_EQ;
				break;
			case LESS_OR_EQ:
				binop = BINOP.GREATER;
				break;
			case GREATER_OR_EQ:
				binop = BINOP.LESS;
				break;
			default:
				throw new RuntimeException("Unknown BINOP encountered, cannot negate it.");
		}
	}

	@Override
	public String toString() {
		return terms.get(0) + " " + binop + " " + terms.get(1);
	}
}
