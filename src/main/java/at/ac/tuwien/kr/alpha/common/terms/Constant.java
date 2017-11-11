package at.ac.tuwien.kr.alpha.common.terms;

import at.ac.tuwien.kr.alpha.common.symbols.FunctionSymbol;
import at.ac.tuwien.kr.alpha.common.Interner;
import at.ac.tuwien.kr.alpha.common.symbols.SymbolWithRank;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class Constant<T extends Comparable<T>> extends Term implements FunctionSymbol<T> {
	private static final Interner<Constant> INTERNER = new Interner<>();

	private final T symbol;
	private final boolean symbolic;

	private Constant(T symbol, boolean symbolic) {
		this.symbol = symbol;
		this.symbolic = symbolic;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> Constant<T> getInstance(T symbol) {
		return (Constant<T>) INTERNER.intern(new Constant<>(symbol, false));
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> Constant<T> getSymbolicInstance(String symbol) {
		return (Constant<T>) INTERNER.intern(new Constant<>(symbol, true));
	}

	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public List<Variable> getOccurringVariables() {
		return Collections.emptyList();
	}

	@Override
	public Term substitute(Substitution substitution) {
		return this;
	}

	@Override
	public String toString() {
		if (symbol instanceof String) {
			if (symbolic) {
				return (String) symbol;
			} else {
				return "\"" + symbol + "\"";
			}
		}
		return symbol.toString();

		// return symbol + "/" + rank;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Constant that = (Constant) o;

		return symbol.equals(that.symbol);
	}

	@Override
	public int hashCode() {
		int result = symbol.hashCode();
		result = 31 * result + (symbolic ? 1 : 0);
		return result;
	}

	/**
	 * Establishes "priority" for ordering of constant terms depending on the type
	 * of the corresponding object according to ASP-Core-2.03c.
	 */
	private static final int priority(final Class<?> clazz) {
		if (clazz.equals(Integer.class)) {
			return 1;
		} else if (clazz.equals(SymbolWithRank.class)) {
			return 2;
		} else if (clazz.equals(String.class)) {
			return 3;
		}
		return 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int compareTo(Term o) {
		if (this == o) {
			return 0;
		}

		if (!(o instanceof Constant)) {
			return super.compareTo(o);
		}

		Constant other = (Constant) o;

		// We will perform an unchecked cast.
		// Because of type erasure, we cannot know the exact type
		// of other.symbol.
		// However, may assume that other.symbol actually is of
		// type T.
		// We know that this.symbol if of type T and implements
		// Comparable<T>. We ensure that the class of other.symbol
		// equals the class of this.symbol, which in turn is T.
		// That assumption should be quite safe. It can only be
		// wrong if we have some bug that generates strange
		// ConstantTerms at runtime, bypassing the check for T
		// at compile-time.
		if (other.symbol.getClass() == this.symbol.getClass()) {
			return this.symbol.compareTo((T) other.symbol);
		}

		Class<?> thisType = this.symbol.getClass();
		Class<?> otherType = other.symbol.getClass();

		int thisPrio = priority(thisType);
		int otherPrio = priority(otherType);

		if (thisPrio == 0 || otherPrio == 0) {
			return thisType.getName().compareTo(otherType.getName());
		}

		return Integer.compare(thisPrio, otherPrio);
	}

	@Override
	public T getSymbol() {
		return symbol;
	}

	@Override
	public int getRank() {
		return 0;
	}
}
