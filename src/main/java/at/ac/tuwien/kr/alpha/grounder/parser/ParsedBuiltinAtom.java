package at.ac.tuwien.kr.alpha.grounder.parser;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedBuiltinAtom extends CommonParsedObject {
	public ParsedAtom left;
	public ParsedAtom right;
	public BINOP binop;
	public enum BINOP {EQUAL, UNEQUAL, LESS, GREATER, LESS_OR_EQ, GREATER_OR_EQ};

	public ParsedBuiltinAtom(ParsedAtom left, BINOP binop, ParsedAtom right) {
		this.left = left;
		this.right = right;
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
}
