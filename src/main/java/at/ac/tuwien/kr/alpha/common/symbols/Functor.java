package at.ac.tuwien.kr.alpha.common.symbols;

import at.ac.tuwien.kr.alpha.common.Interner;

public final class Functor implements FunctionSymbol<String> {
	private static final Interner<Functor> INTERNER = new Interner<>();

	private final String symbol;
	private final int rank;

	private Functor(String symbol, int rank) {
		this.symbol = symbol;
		this.rank = rank;
	}

	public static Functor getInstance(String symbol, int rank) {
		return INTERNER.intern(new Functor(symbol, rank));
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

	@Override
	public int getArity() {
		return rank;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof FunctionSymbol)) {
			return false;
		}

		FunctionSymbol other = (FunctionSymbol) o;

		if (rank != other.getArity()) {
			return false;
		}

		return symbol.equals(other.getSymbol());
	}

	@Override
	public int hashCode() {
		int result = symbol.hashCode();
		result = 31 * result + rank;
		return result;
	}

	@Override
	public String toString() {
		return symbol + "/" + rank;
	}
}
