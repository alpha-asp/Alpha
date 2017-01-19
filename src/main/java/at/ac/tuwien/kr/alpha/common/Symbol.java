package at.ac.tuwien.kr.alpha.common;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

/**
 * Provides a unique representation of each function and constant symbol.
 * Copyright (c) 2016, the Alpha Team.
 */
public class Symbol implements Comparable<Symbol> {
	private static final Interner<Symbol> INTERNER = Interners.newStrongInterner();

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

	public String getSymbol() {
		return symbol;
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
		return ComparisonChain.start().compare(symbol, o.symbol).compare(arity, o.arity).result();
	}
}