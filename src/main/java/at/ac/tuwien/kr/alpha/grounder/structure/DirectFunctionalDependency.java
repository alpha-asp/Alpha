package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.atoms.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
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

/**
 * Copyright (c) 2020, the Alpha Team.
 */
public class DirectFunctionalDependency {
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectFunctionalDependency.class);

	private final List<Literal> evaluationOrder = new ArrayList<>();


	public Substitution evaluate(Substitution substitution) {
		LOGGER.debug("Evaluating FD.");
		Substitution extendedSubstitution = substitution;
		for (Literal literal : evaluationOrder) {
			extendedSubstitution = ((ComparisonLiteral) literal).getSubstitutions(extendedSubstitution).get(0);
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
					// We need to construct a dummy substitution and ground the arithmetic term to get correct result from isLefOrRightAssigning.
					// TODO: this may break if dummy substitution value of 1 leads to undefined arithmetics.
					// TODO: for p(X1) :- r(X), X1 = X+1 we need some way to get a functional dependency of X=X1-1 !
					Substitution dummyGroundingSubst = new Substitution();
					for (VariableTerm knownVariable : knownVariables) {
						dummyGroundingSubst.put(knownVariable, ConstantTerm.getInstance(1));
					}
					ComparisonLiteral dummyGrounded = comparisonLiteral.substitute(dummyGroundingSubst);
					if (dummyGrounded.isLeftOrRightAssigning()) {
						knownVariables.addAll(dummyGrounded.getBindingVariables());
						iterator.remove();
						didChange = true;
						directFunctionalDependency.evaluationOrder.add(comparisonLiteral);
					}
				} else if (bodyLiteral instanceof EnumerationLiteral) {
					// TODO: we know functional dependency, use it.
					iterator.remove();
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
