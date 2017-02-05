package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Collections;
import java.util.List;

public class ChoiceAtom implements Atom {
	public static final Predicate ON = new BasicPredicate("ChoiceOn", 1);
	public static final Predicate OFF = new BasicPredicate("ChoiceOff", 1);

	private final Predicate predicate;
	private final List<Term> terms;

	private ChoiceAtom(Predicate predicate, Term term) {
		this.predicate = predicate;
		this.terms = Collections.singletonList(term);
	}

	private ChoiceAtom(Predicate predicate, int id) {
		this(predicate, ConstantTerm.getInstance(Integer.toString(id)));
	}

	public static ChoiceAtom on(int id) {
		return new ChoiceAtom(ON, id);
	}

	public static ChoiceAtom off(int id) {
		return new ChoiceAtom(OFF, id);
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	@Override
	public List<Term> getTerms() {
		return terms;
	}

	@Override
	public boolean isGround() {
		// NOTE: Term is a ConstantTerm, which is ground by definition.
		return true;
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		// NOTE: Term is a ConstantTerm, which has no variables by definition.
		return Collections.emptyList();
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return this;
	}

	@Override
	public int compareTo(Atom o) {
		if (!(o instanceof  ChoiceAtom)) {
			return 1;
		}
		ChoiceAtom other = (ChoiceAtom)o;
		int result = predicate.compareTo(other.predicate);
		if (result != 0) {
			return result;
		}
		return terms.get(0).compareTo(other.terms.get(0));
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(predicate.getPredicateName());
		sb.append("(");
		Util.appendDelimited(sb, terms);
		sb.append(")");
		return sb.toString();
	}
}