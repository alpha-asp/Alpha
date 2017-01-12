package at.ac.tuwien.kr.alpha.common;

import java.util.List;

public class ChoiceAtom implements Atom {
	public static final Predicate ON = new BasicPredicate("ChoiceOn", 1);
	public static final Predicate OFF = new BasicPredicate("ChoiceOff", 1);

	private final Predicate predicate;
	private final Term[] terms;

	private ChoiceAtom(Predicate predicate, Term term) {
		this.predicate = predicate;
		this.terms = new Term[]{term};
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
	public Term[] getTerms() {
		return terms;
	}

	@Override
	public boolean isGround() {
		return terms[0].isGround();
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		return terms[0].getOccurringVariables();
	}

	@Override
	public int compareTo(Atom o) {
		return 0;
	}
}