package at.ac.tuwien.kr.alpha.common;

import java.util.HashMap;

/**
 * Provides a unique representation of each function and constant symbol.
 * Copyright (c) 2016, the Alpha Team.
 */
public class TermSymbol {
	private static final HashMap<String, TermSymbol> KNOWN_SYMBOLS = new HashMap<>();

	private final String symbol;

	private TermSymbol(String symbol) {
		this.symbol = symbol;
	}

	public static TermSymbol getTermSymbol(String symbol) {
		KNOWN_SYMBOLS.putIfAbsent(symbol, new TermSymbol(symbol));
		return KNOWN_SYMBOLS.get(symbol);
	}

	public String getSymbol() {
		return symbol;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TermSymbol that = (TermSymbol) o;

		return symbol.equals(that.symbol);

	}

	@Override
	public int hashCode() {
		return symbol.hashCode();
	}
}
