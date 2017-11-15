package at.ac.tuwien.kr.alpha.common.symbols;

/**
 * Provides a unique representation of each function and constant symbol.
 * Copyright (c) 2016, the Alpha Team.
 */
public interface SymbolWithRank<S extends Comparable<S>> {
	S getSymbol();

	int getRank();
}
