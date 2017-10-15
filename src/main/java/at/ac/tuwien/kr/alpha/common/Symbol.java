package at.ac.tuwien.kr.alpha.common;

/**
 * Provides a unique representation of each function and constant symbol.
 * Copyright (c) 2016, the Alpha Team.
 */
public class Symbol implements Comparable<Symbol> {
	private static final Interner<Symbol> INTERNER = new Interner<>();

	private final String symbol;
	private final int arity;

	private Symbol(String symbol, int arity) {
		this.symbol = symbol;
		this.arity = arity;
	}

	public static Symbol getInstance(String symbol) {
		return getInstance(symbol, 0);
	}

	public static Symbol getInstance(String symbol, int arity) {
		return INTERNER.intern(new Symbol(symbol, arity));
	}

	public int getArity() {
		return arity;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Symbol that = (Symbol) o;

		return symbol.equals(that.symbol) && arity == that.arity;
	}

	@Override
	public int hashCode() {
		return symbol.hashCode() + 31 * arity;
	}

	@Override
	public int compareTo(Symbol o) {
		int result = symbol.compareTo(o.symbol);

		if (result != 0) {
			return result;
		}

		return arity - o.arity;
	}

	@Override
	public String toString() {
		return symbol;
	}
}
