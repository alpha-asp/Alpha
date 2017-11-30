package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.Variable;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.*;

/**
 * Removes variable equalities from rules by replacing one variable with the other.
 * Copyright (c) 2017, the Alpha Team.
 */
public class VariableEqualityRemoval implements ProgramTransformation {
	@Override
	public void transform(Program inputProgram) {
		for (Rule rule : inputProgram.getRules()) {
			findAndReplaceVariableEquality(rule);
		}
	}

	private void findAndReplaceVariableEquality(Rule rule) {
		// Collect all equal variables.
		HashMap<Variable, HashSet<Variable>> variableToEqualVariables = new HashMap<>();
		//HashSet<Variable> equalVariables = new LinkedHashSet<>();
		HashSet<Literal> equalitiesToRemove = new HashSet<>();
		for (Literal literal : rule.getBody()) {
			if (!(literal instanceof ComparisonAtom)) {
				continue;
			}
			if (!((ComparisonAtom) literal).isNormalizedEquality()) {
				continue;
			}
			if (literal.getTerms().get(0) instanceof Variable && literal.getTerms().get(1) instanceof Variable) {
				Variable leftVariable = (Variable) literal.getTerms().get(0);
				Variable rightVariable = (Variable) literal.getTerms().get(1);
				HashSet<Variable> leftEqualVariables = variableToEqualVariables.get(leftVariable);
				HashSet<Variable> rightEqualVariables = variableToEqualVariables.get(rightVariable);
				if (leftEqualVariables == null && rightEqualVariables == null) {
					HashSet<Variable> equalVariables = new LinkedHashSet<>(Arrays.asList(leftVariable, rightVariable));
					variableToEqualVariables.put(leftVariable, equalVariables);
					variableToEqualVariables.put(rightVariable, equalVariables);
				}
				if (leftEqualVariables == null && rightEqualVariables != null) {
					rightEqualVariables.add(leftVariable);
					variableToEqualVariables.put(leftVariable, rightEqualVariables);
				}
				if (leftEqualVariables != null && rightEqualVariables == null) {
					leftEqualVariables.add(rightVariable);
					variableToEqualVariables.put(rightVariable, leftEqualVariables);
				}
				if (leftEqualVariables != null && rightEqualVariables != null) {
					leftEqualVariables.addAll(rightEqualVariables);
					for (Variable rightEqualVariable : rightEqualVariables) {
						variableToEqualVariables.put(rightEqualVariable, leftEqualVariables);
					}
				}
				equalitiesToRemove.add(literal);
			}
		}
		if (variableToEqualVariables.isEmpty()) {
			// Skip rule if there is no equality between variables.
			return;
		}

		// Use substitution for actual replacement.
		Substitution replacementSubstitution = new Substitution();
		// For each set of equal variables, take the first variable and replace all others by it.
		for (Map.Entry<Variable, HashSet<Variable>> variableEqualityEntry : variableToEqualVariables.entrySet()) {
			Variable variableToReplace = variableEqualityEntry.getKey();
			Variable replacementVariable = variableEqualityEntry.getValue().iterator().next();
			if (variableToReplace == replacementVariable) {
				continue;
			}
			replacementSubstitution.put(variableToReplace, replacementVariable);
		}
		// Replace/Substitute in each literal every term where one of the common variables occurs.
		Iterator<Literal> bodyIterator = rule.getBody().iterator();
		while (bodyIterator.hasNext()) {
			Literal literal = bodyIterator.next();
			if (equalitiesToRemove.contains(literal)) {
				bodyIterator.remove();
			}
			for (int i = 0; i < literal.getTerms().size(); i++) {
				Term replaced = literal.getTerms().get(i).substitute(replacementSubstitution);
				literal.getTerms().set(i, replaced);
			}
		}
		// Replace variables in head.
		if (rule.getHead() != null) {
			if (!rule.getHead().isNormal()) {
				throw new UnsupportedOperationException("Cannot treat non-normal rule heads yet.");
			}
			DisjunctiveHead head = (DisjunctiveHead) rule.getHead();
			Atom headAtom = head.disjunctiveAtoms.get(0);
			for (int i = 0; i < headAtom.getTerms().size(); i++) {
				Term replaced = headAtom.getTerms().get(i).substitute(replacementSubstitution);
				headAtom.getTerms().set(i, replaced);
			}
		}
	}
}
