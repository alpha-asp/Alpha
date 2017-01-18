package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedAtom;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BasicAtom implements Atom {
	public final Predicate predicate;
	public final Term[] termList;

	public BasicAtom(Predicate predicate, Term... termList) {
		this.predicate = predicate;
		this.termList = termList;
	}

	public BasicAtom(ParsedAtom parsedAtom) {
		this(new BasicPredicate(parsedAtom.predicate, parsedAtom.getArity()), terms(parsedAtom));
	}

	private static Term[] terms(ParsedAtom parsedAtom) {
		Term[] terms;
		if (parsedAtom.getArity() == 0) {
			terms = new Term[0];
		} else {
			terms = new Term[parsedAtom.terms.size()];
			for (int i = 0; i < parsedAtom.terms.size(); i++) {
				terms[i] = parsedAtom.terms.get(i).toTerm();
			}
		}
		return terms;
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	@Override
	public Term[] getTerms() {
		return termList;
	}

	public boolean isGround() {
		for (Term term : termList) {
			if (!term.isGround()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		BasicAtom that = (BasicAtom) o;

		if (!predicate.equals(that.predicate)) {
			return false;
		}

		return Arrays.equals(termList, that.termList);
	}

	@Override
	public int hashCode() {
		return 31 * predicate.hashCode() + Arrays.hashCode(termList);
	}

	public List<VariableTerm> getOccurringVariables() {
		LinkedList<VariableTerm> occurringVariables = new LinkedList<>();
		for (Term term : termList) {
			occurringVariables.addAll(term.getOccurringVariables());
		}
		return occurringVariables;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(predicate.getPredicateName());
		sb.append("(");
		Util.appendDelimited(sb, Arrays.asList(termList));
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int compareTo(Atom o) {
		if (this.termList.length != o.getTerms().length) {
			return this.termList.length - o.getTerms().length;
		}

		int result = this.predicate.compareTo(o.getPredicate());

		if (result != 0) {
			return result;
		}

		for (int i = 0; i < termList.length; i++) {
			result = termList[i].compareTo(o.getTerms()[i]);
			if (result != 0) {
				return result;
			}
		}

		return 0;
	}
}
