package at.ac.tuwien.kr.alpha.common.symbols;

import at.ac.tuwien.kr.alpha.common.Interner;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class Predicate implements Comparable<Predicate>, SymbolWithRank<String> {
	private static final Interner<Predicate> INTERNER = new Interner<>();

	private final String symbol;
	private final int rank;
	private final boolean internal;

	protected Predicate(String symbol, int rank, boolean internal) {
		this.symbol = symbol;
		this.rank = rank;
		this.internal = internal;
	}

	public static Predicate getInstance(String symbol) {
		return getInstance(symbol, 0);
	}

	public static Predicate getInstance(String symbol, int arity) {
		return getInstance(symbol, arity, false);
	}

	public static Predicate getInstance(String symbol, int arity, boolean internal) {
		return INTERNER.intern(new Predicate(symbol, arity, internal));
	}

	public Predicate(String name, int arity) {
		this(name, arity, false);
	}

	@Override
	public int hashCode() {
		int result = symbol != null ? symbol.hashCode() : 0;
		result = 31 * result + rank;
		result = 31 * result + (internal ? 1 : 0);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Predicate)) return false;

		Predicate predicate = (Predicate) o;

		if (rank != predicate.rank) return false;
		if (internal != predicate.internal) return false;
		return symbol != null ? symbol.equals(predicate.symbol) : predicate.symbol == null;
	}

	public boolean isInternal() {
		return internal;
	}

	@Override
	public int compareTo(Predicate other) {
		int result = getSymbol().compareTo(other.getSymbol());

		if (result != 0) {
			return result;
		}

		return Integer.compare(getRank(), other.getRank());
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

	@Override
	public int getRank() {
		return rank;
	}
}
