package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.Substitution;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.appendDelimited;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class FunctionTerm extends Term {
	private static final Interner<FunctionTerm> INTERNER = Interners.newStrongInterner();

	private final Symbol symbol;
	private final List<Term> terms;
	private final boolean ground;

	private FunctionTerm(Symbol symbol, List<Term> terms) {
		if (symbol == null) {
			throw new IllegalArgumentException();
		}

		this.symbol = symbol;
		this.terms = Collections.unmodifiableList(terms);

		boolean ground = true;
		for (Term term : terms) {
			if (!term.isGround()) {
				ground = false;
				break;
			}
		}
		this.ground = ground;
	}

	public static FunctionTerm getInstance(Symbol functionSymbol, List<Term> termList) {
		return INTERNER.intern(new FunctionTerm(functionSymbol, termList));
	}

	public static FunctionTerm getInstance(String functionSymbol, List<Term> termList) {
		return getInstance(Symbol.getInstance(functionSymbol, termList.size()), termList);
	}

	public static FunctionTerm getInstance(String functionSymbol, Term... terms) {
		return getInstance(Symbol.getInstance(functionSymbol, terms.length), Arrays.asList(terms));
	}

	public List<Term> getTerms() {
		return terms;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public boolean isSimilar(FunctionTerm other) {
		return symbol.equals(other.symbol);
	}

	@Override
	public boolean isGround() {
		return ground;
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		LinkedList<VariableTerm> vars = new LinkedList<>();
		for (Term term : terms) {
			vars.addAll(term.getOccurringVariables());
		}
		return vars;
	}

	@Override
	public Term substitute(Substitution substitution) {
		List<Term> groundTermList = new ArrayList<>(terms.size());
		for (Term term : terms) {
			groundTermList.add(term.substitute(substitution));
		}
		return FunctionTerm.getInstance(symbol, groundTermList);
	}

	@Override
	public String toString() {
		if (terms.isEmpty()) {
			return symbol.getSymbol();
		}

		final StringBuilder sb = new StringBuilder("(");
		appendDelimited(sb, terms);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		FunctionTerm that = (FunctionTerm) o;

		if (!symbol.equals(that.symbol)) {
			return false;
		}
		return terms.equals(that.terms);
	}

	@Override
	public int hashCode() {
		return 31 * symbol.hashCode() + terms.hashCode();
	}

	@Override
	public int compareTo(Term o) {
		if (!(o instanceof FunctionTerm)) {
			return -1;
		}
		FunctionTerm other = (FunctionTerm)o;

		int result = symbol.compareTo(other.symbol);

		if (result != 0) {
			return result;
		}

		if (terms.size() != other.terms.size()) {
			return terms.size() - other.terms.size();
		}

		if (terms.isEmpty() && other.terms.isEmpty()) {
			return 0;
		}

		for (int i = 0; i < terms.size(); i++) {
			result = terms.get(i).compareTo(other.terms.get(i));
			if (result != 0) {
				return result;
			}
		}

		return 0;
	}
}