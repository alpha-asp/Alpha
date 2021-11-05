package at.ac.tuwien.kr.alpha.api.programs.literals;

import java.util.List;
import java.util.Set;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;

// TODO This is a functional test and should not be run with standard unit tests
public interface Literal {
	Atom getAtom();

	boolean isNegated();

	Literal negate();

	Predicate getPredicate();

	List<Term> getTerms();

	Set<VariableTerm> getOccurringVariables();

	boolean isGround();

	Set<VariableTerm> getBindingVariables();

	Set<VariableTerm> getNonBindingVariables();

	Literal substitute(Substitution substitution);
}
