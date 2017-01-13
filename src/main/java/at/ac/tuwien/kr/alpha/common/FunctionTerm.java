package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.appendDelimited;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class FunctionTerm extends Term {
	private static HashMap<FunctionTerm, FunctionTerm> knownFunctionTerms = new HashMap<>();

	public final TermSymbol functionSymbol;
	public final List<Term> termList;
	private final boolean ground;

	private FunctionTerm(TermSymbol functionSymbol, List<Term> termList) {
		this.functionSymbol = functionSymbol;
		this.termList = termList;

		boolean ground = true;
		for (Term term : termList) {
			if (!term.isGround()) {
				ground = false;
				break;
			}
		}
		this.ground = ground;
	}

	public static FunctionTerm getInstance(TermSymbol functionSymbol, List<Term> termList) {
		FunctionTerm functionTerm = new FunctionTerm(functionSymbol, termList);
		knownFunctionTerms.putIfAbsent(functionTerm, functionTerm);
		return knownFunctionTerms.get(functionTerm);
	}

	public static FunctionTerm getInstance(String functionSymbol, List<Term> termList) {
		return getInstance(TermSymbol.getInstance(functionSymbol), termList);
	}

	public static FunctionTerm getInstance(String functionSymbol, Term... terms) {
		return getInstance(TermSymbol.getInstance(functionSymbol), Arrays.asList(terms));
	}

	@Override
	public boolean 	isGround() {
		return ground;
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		LinkedList<VariableTerm> vars = new LinkedList<>();
		for (Term term : termList) {
			vars.addAll(term.getOccurringVariables());
		}
		return vars;
	}

	@Override
	public Term substitute(Substitution substitution) {
		List<Term> groundTermList = new ArrayList<>(termList.size());
		for (Term term : termList) {
			groundTermList.add(term.substitute(substitution));
		}
		return FunctionTerm.getInstance(functionSymbol, groundTermList);
	}

	@Override
	public String toString() {
		if (termList.isEmpty()) {
			return functionSymbol.getSymbol();
		}

		final StringBuilder sb = new StringBuilder("(");
		appendDelimited(sb, termList);
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

		if (!functionSymbol.equals(that.functionSymbol)) {
			return false;
		}
		return termList.equals(that.termList);
	}

	@Override
	public int hashCode() {
		return 31 * functionSymbol.hashCode() + termList.hashCode();
	}

	@Override
	public int compareTo(Term o) {
		if (!(o instanceof FunctionTerm)) {
			return -1;
		}
		FunctionTerm other = (FunctionTerm)o;

		int result = functionSymbol.compareTo(other.functionSymbol);

		if (result != 0) {
			return result;
		}

		if (termList.size() != other.termList.size()) {
			return termList.size() - other.termList.size();
		}

		if (termList.isEmpty() && other.termList.isEmpty()) {
			return 0;
		}

		for (int i = 0; i < termList.size(); i++) {
			result = termList.get(i).compareTo(other.termList.get(i));
			if (result != 0) {
				return result;
			}
		}

		return 0;
	}
}
