package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.atoms.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Provides support for direct functional dependencies to be used in completions. Also includes an algorithm to identify
 * and create DirectFunctionalDependencies for a {@link NonGroundRule}.
 *
 * Copyright (c) 2020, the Alpha Team.
 */
public class DirectFunctionalDependency {
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectFunctionalDependency.class);

	private final List<Literal> evaluationOrder = new ArrayList<>();

	/**
	 * Evaluates the functional dependency, i.e., it enlarges the given substitution with those values derivable by
	 * this {@link DirectFunctionalDependency}.
	 * @param substitution the {@link Substitution} to enlarge.
	 * @return the enlarged {@link Substitution}.
	 */
	public Substitution evaluate(Substitution substitution) {
		LOGGER.debug("Evaluating FD.");
		Substitution extendedSubstitution = substitution;
		for (Literal literal : evaluationOrder) {
			if (literal instanceof ComparisonLiteral) {
				extendedSubstitution = ((ComparisonLiteral) literal).getSubstitutions(extendedSubstitution).get(0);
			} else if (literal instanceof EnumerationLiteral) {
				EnumerationAtom enumerationAtom = (EnumerationAtom) literal.getAtom();
				Term identifier = enumerationAtom.getTerms().get(0).substitute(extendedSubstitution);
				Term term = enumerationAtom.getTerms().get(1).substitute(extendedSubstitution);
				Term index = enumerationAtom.getTerms().get(3).substitute(extendedSubstitution);

				// Distinguish between the two possible FDs for EnumerationLiterals.
				if (identifier.isGround() && term.isGround()) {
					// FD is (A,X) -> I.
					enumerationAtom.addEnumerationToSubstitution(extendedSubstitution);
				} else if (identifier.isGround() && index.isGround() && !term.isGround()) {
					// FD is (A,I) -> X
					Term groundedTerm = enumerationAtom.getTermWithIndex(identifier, (Integer) ((ConstantTerm) index).getObject());
					// Unify the obtained ground term with the non-ground one to extend the substitution.
					Substitution unifyTestSubstitution = new Substitution(extendedSubstitution);
					if (unifyTestSubstitution.unifyTerms(term, groundedTerm)) {
						extendedSubstitution = unifyTestSubstitution;
					} else {
						throw oops("Substitution from EnumerationLiteral does not unify with given functional dependency: " + literal);
					}
				} else {
					throw oops("Recorded functional dependency for EnumerationLiteral has unexpected properties: " + literal);
				}
			} else {
				throw oops("Unknown DirectFunctionalDependency encountered, literal is: " + literal);
			}
		}
		LOGGER.debug("Extended substitution {} into {}.",  substitution, extendedSubstitution);
		return extendedSubstitution;
	}

	public static DirectFunctionalDependency computeDirectFunctionalDependencies(NonGroundRule nonGroundRule) {
		Set<VariableTerm> knownVariables = new LinkedHashSet<>(nonGroundRule.getHeadAtom().getOccurringVariables());
		List<Literal> remainingBodyLiterals = new LinkedList<>(nonGroundRule.getRule().getBody());
		DirectFunctionalDependency directFunctionalDependency = new DirectFunctionalDependency();
		boolean didChange;
		do {
			didChange = false;
			for (Iterator<Literal> iterator = remainingBodyLiterals.iterator(); iterator.hasNext();) {
				Literal bodyLiteral = iterator.next();
				if (bodyLiteral instanceof ComparisonLiteral) {
					ComparisonLiteral comparisonLiteral = (ComparisonLiteral) bodyLiteral;
					// Remove literal if it is not assigning some variable.
					if (!comparisonLiteral.isNormalizedEquality()) {
						iterator.remove();
						continue;
					}
					// Try to transform the equation such that it assigns one variable.
					try {
						ComparisonLiteral transformComparison = EquationRefactoring.transformToUnassignedEqualsRest(knownVariables, comparisonLiteral);
						VariableTerm assignedVariable = (VariableTerm)transformComparison.getAtom().getTerms().get(0);
						knownVariables.add(assignedVariable);
						iterator.remove();
						didChange = true;
						directFunctionalDependency.evaluationOrder.add(transformComparison);
					} catch (EquationRefactoring.CannotRewriteException ignored) {
						// Cannot transform the equation, skip it for now.
						continue;
					}
				} else if (bodyLiteral instanceof EnumerationLiteral) {
					EnumerationAtom enumerationAtom = (EnumerationAtom) bodyLiteral.getAtom();
					List<VariableTerm> variablesInA = enumerationAtom.getTerms().get(0).getOccurringVariables();
					List<VariableTerm> variablesInX = enumerationAtom.getTerms().get(1).getOccurringVariables();
					VariableTerm enumerationIndexVariable = (VariableTerm) enumerationAtom.getTerms().get(2);

					// Enumeration(A,X,I) has two FDs (A,I) -> X and (A,X) -> I
					if (knownVariables.containsAll(variablesInA) && knownVariables.contains(enumerationIndexVariable)) {
						// (A,I) is known, add variables in X.
						knownVariables.addAll(variablesInX);
						iterator.remove();
						didChange = true;
						directFunctionalDependency.evaluationOrder.add(bodyLiteral);
					} else if (knownVariables.containsAll(variablesInA) && knownVariables.containsAll(variablesInX)) {
						// (A,X) is known, add I then.
						knownVariables.add(enumerationIndexVariable);
						iterator.remove();
						didChange = true;
						directFunctionalDependency.evaluationOrder.add(bodyLiteral);
					}
				} else {
					// For all other literals, no functional dependency is known, stop considering them.
					iterator.remove();
				}
			}

		} while (didChange);
		// Collect all variables occurring in the rule.
		Set<VariableTerm> variablesOccurringInRule = new HashSet<>();
		for (Literal literal : nonGroundRule.getRule().getBody()) {
			variablesOccurringInRule.addAll(literal.getOccurringVariables());
		}
		variablesOccurringInRule.removeAll(knownVariables);
		// Return true iff all variables occurring in the rule can be obtained from the head by functional dependencies.
		if (variablesOccurringInRule.isEmpty()) {
			return directFunctionalDependency;
		} else {
			return null;
		}
	}
}
