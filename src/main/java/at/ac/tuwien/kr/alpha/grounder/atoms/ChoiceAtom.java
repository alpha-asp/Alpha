package at.ac.tuwien.kr.alpha.grounder.atoms;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.Variable;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Collections;
import java.util.List;

import static at.ac.tuwien.kr.alpha.Util.join;

public class ChoiceAtom implements Atom {
	public static final Predicate ON = Predicate.getInstance("ChoiceOn", 1, true);
	public static final Predicate OFF = Predicate.getInstance("ChoiceOff", 1, true);

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
	public List<Variable> getBindingVariables() {
		// NOTE: Term is a ConstantTerm, which has no variables by definition.
		return Collections.emptyList();
	}

	@Override
	public List<Variable> getNonBindingVariables() {
		return Collections.emptyList();
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return this;
	}

	@Override
	public String toString() {
		return join(predicate.getName() + "(", terms, ")");
	}
}