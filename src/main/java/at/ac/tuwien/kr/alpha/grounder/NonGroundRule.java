package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedRule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a non-ground rule or a constraint for the semi-naive grounder.
 * Copyright (c) 2016, the Alpha Team.
 */
public class NonGroundRule {
	List<PredicateInstance> bodyAtomsPositive = new ArrayList<>();
	List<PredicateInstance> bodyAtomsNegative = new ArrayList<>();
	PredicateInstance headAtom;

	public int getRuleId() {
		return ruleId;
	}

	private final int ruleId;

	public NonGroundRule(int ruleId) {
		this.ruleId = ruleId;
	}

	public static NonGroundRule constructNonGroundRule(IntIdGenerator intIdGenerator, ParsedRule parsedRule) {
		NonGroundRule nonGroundRule = new NonGroundRule(intIdGenerator.getNextId());

		for (ParsedAtom parsedAtom : parsedRule.body) {
			PredicateInstance predicateInstance = constructPredicateInstanceFromParsedAtom(parsedAtom);
			if (parsedAtom.isNegated) {
				nonGroundRule.bodyAtomsNegative.add(predicateInstance);
			} else {
				nonGroundRule.bodyAtomsPositive.add(predicateInstance);
			}
		}
		// Construct head if the given parsedRule is no constraint
		if (parsedRule.head != null) {
			nonGroundRule.headAtom = constructPredicateInstanceFromParsedAtom(parsedRule.head);
		}

		// Check safety of rule and sort its atoms for better join-order
		nonGroundRule.checkSafety();
		nonGroundRule.sortBodyAtoms();
		return nonGroundRule;
	}

	/**
	 *
	 * @return a list of all predicates occurring in the rule (may contain duplicates).
	 */
	public List<Predicate> getOccurringPredicates() {
		ArrayList<Predicate> predicateList = new ArrayList<>();
		for (PredicateInstance predicateInstance : bodyAtomsPositive) {
			predicateList.add(predicateInstance.predicate);
		}
		for (PredicateInstance predicateInstance : bodyAtomsNegative) {
			predicateList.add(predicateInstance.predicate);
		}
		if (!isConstraint()) {
			predicateList.add(headAtom.predicate);
		}
		return  predicateList;
	}

	private static PredicateInstance constructPredicateInstanceFromParsedAtom(ParsedAtom parsedAtom) {
		Predicate predicate = new BasicPredicate(parsedAtom.predicate, parsedAtom.arity);
		Term[] terms;
		if (parsedAtom.arity == 0) {
			terms = new Term[0];
		} else {
			terms = new Term[parsedAtom.terms.size()];
			for (int i = 0; i < parsedAtom.terms.size(); i++) {
				terms[i] = AtomStore.convertFromParsedTerm(parsedAtom.terms.get(i));
			}
		}
		return new PredicateInstance(predicate, terms);
	}

	/**
	 * Checks whether a rule is safe. A rule is safe iff all negated variables and all variables occurring in the
	 * head also occur in the positive body).
	 * @return true if this rule is safe.
	 */
	private boolean isSafe() {
		HashSet<VariableTerm> positiveVariables = new HashSet<>();
		HashSet<VariableTerm> negativeVariables = new HashSet<>();

		// Check that all negative variables occur in the positive body.
		for (PredicateInstance posAtom : bodyAtomsPositive) {
			positiveVariables.addAll(posAtom.getOccurringVariables());
		}
		for (PredicateInstance negAtom : bodyAtomsNegative) {
			negativeVariables.addAll(negAtom.getOccurringVariables());
		}
		negativeVariables.removeAll(positiveVariables);
		if (negativeVariables.size() != 0) {
			return false;
		}

		// Constraint are safe at this point
		if (isConstraint()) {
			return true;
		}

		// Check that all variables of the head occur in the positive body.
		List<VariableTerm> headVariables = headAtom.getOccurringVariables();
		headVariables.removeAll(positiveVariables);
		return headVariables.size() == 0;
	}

	/**
	 * Ensure that the rule is safe: throw exception if it is not safe.
	 */
	public void checkSafety() {
		if (!isSafe()) {
			throw new RuntimeException("Encountered not safe rule: " + toString());
		}
	}

	/**
	 * Sorts bodyAtoms such that the join-order of the atoms is improved (= cannot degenerate into cross-product).
	 * Note that the below sorting can be improved to yield smaller joins.
	 */
	private void sortBodyAtoms() {
		// Sort positive body atoms
		HashSet<SortingBodyComponent> componentsPositive = new HashSet<>();
		for (PredicateInstance posAtom : bodyAtomsPositive) {
			List<VariableTerm> variablesInAtom = posAtom.getOccurringVariables();
			HashSet<SortingBodyComponent> hittingComponents = new HashSet<>();
			// For each variable
			for (VariableTerm variableTerm : variablesInAtom) {
				// Find all components it also occurs and record in hitting components
				for (SortingBodyComponent sortingBodyComponent : componentsPositive) {
					if (sortingBodyComponent.occurringVariables.contains(variableTerm)) {
						hittingComponents.add(sortingBodyComponent);
					}
				}
			}
			// If no components were hit, create new component, else merge components
			if (hittingComponents.size() == 0) {
				SortingBodyComponent componentForAtom = new SortingBodyComponent();
				componentForAtom.occurringVariables.addAll(variablesInAtom);
				componentForAtom.atoms.add(posAtom);
				componentForAtom.atomSequence = new LinkedList<>();
				componentForAtom.atomSequence.add(posAtom);
				componentForAtom.numAtoms = 1;
				componentsPositive.add(componentForAtom);
			} else {
				// If only one component hit, add atom to it
				if (hittingComponents.size() == 1) {
					SortingBodyComponent bodyComponent = hittingComponents.iterator().next();
					bodyComponent.occurringVariables.addAll(variablesInAtom);
					bodyComponent.atoms.add(posAtom);
					bodyComponent.atomSequence.add(posAtom);
					bodyComponent.numAtoms += 1;
				} else {
					// Merge all components that are hit by the current atom
					SortingBodyComponent firstComponent = hittingComponents.iterator().next();
					for (SortingBodyComponent hitComponent : hittingComponents) {
						if (hitComponent != firstComponent) {
							firstComponent.mergeComponent(hitComponent);
							// Remove merged component from the set of available components
							componentsPositive.remove(hitComponent);
						}
					}
				}
			}

		}
		// Components now contains all components that are internally connected but not connected to another component
		LinkedList<PredicateInstance> sortedPositiveBodyAtoms = new LinkedList<>();
		for (SortingBodyComponent component : componentsPositive) {
			sortedPositiveBodyAtoms.addAll(component.atomSequence);
		}
		bodyAtomsPositive = sortedPositiveBodyAtoms;

		// Since rule is safe, all variables in the negative body are already bound,
		// i.e., joining them cannot degenerate into cross-product.
		// Hence, there is no need to sort them.
	}

	/**
	 * Returns all predicates occurring in the positive body of the rule.
	 * @return
	 */
	public List<Predicate> usedPositiveBodyPredicates() {
		ArrayList<Predicate> usedPredicates = new ArrayList<>();
		for (PredicateInstance predicateInstance : bodyAtomsPositive) {
			usedPredicates.add(predicateInstance.predicate);
		}
		return usedPredicates;
	}

	/**
	 * Returns the predicate occurring first in the body of the rule.
	 * @return
	 */
	public Predicate usedFirstBodyPredicate() {
		if (bodyAtomsPositive.size() > 0) {
			return bodyAtomsPositive.get(0).predicate;
		} else if (bodyAtomsNegative.size() > 0) {
			return bodyAtomsNegative.get(0).predicate;
		} else {
			throw new RuntimeException("Encountered NonGroundRule with empty body, which should have been treated as a fact.");
		}
	}

	public boolean isFirstBodyPredicatePositive() {
		return bodyAtomsPositive.size() > 0;
	}

	/**
	 * Returns the n-th atom in the body of this non-ground rule.
	 * @param atomPosition 0-based position of the body atom.
	 * @return
	 */
	public PredicateInstance getBodyAtom(int atomPosition) {
		if (atomPosition < bodyAtomsPositive.size()) {
			return bodyAtomsPositive.get(atomPosition);
		} else {
			return bodyAtomsNegative.get(atomPosition - bodyAtomsPositive.size());
		}
	}

	public boolean isBodyAtomPositive(int atomPosition) {
		return atomPosition < bodyAtomsPositive.size();
	}

	/**
	 * Returns all predicates occurring in the negative body of the rule.
	 * @return
	 */
	public List<Predicate> usedNegativeBodyPredicates() {
		ArrayList<Predicate> usedPredicates = new ArrayList<>();
		for (PredicateInstance predicateInstance : bodyAtomsNegative) {
			usedPredicates.add(predicateInstance.predicate);
		}
		return usedPredicates;
	}

	public int getNumBodyAtoms() {
		return bodyAtomsPositive.size() + bodyAtomsNegative.size();
	}

	public boolean isGround() {
		if (!isConstraint() && !headAtom.isGround()) {
			return false;
		}
		for (PredicateInstance predicateInstance : bodyAtomsPositive) {
			if (!predicateInstance.isGround()) {
				return false;
			}
		}
		for (PredicateInstance predicateInstance : bodyAtomsNegative) {
			if (!predicateInstance.isGround()) {
				return false;
			}
		}
		return true;
	}

	private class SortingBodyComponent {
		HashSet<VariableTerm> occurringVariables = new HashSet<>();
		HashSet<PredicateInstance> atoms = new HashSet<>();
		LinkedList<PredicateInstance> atomSequence = new LinkedList<>();
		int numAtoms;

		public void mergeComponent(SortingBodyComponent other) {
			occurringVariables.addAll(other.occurringVariables);
			atoms.addAll(other.atoms);
			numAtoms += other.numAtoms;
			atomSequence.addAll(other.atomSequence);
		}
	}

	public boolean isConstraint() {
		return headAtom == null;
	}

	public boolean hasNegativeBodyAtoms() {
		return bodyAtomsNegative.size() != 0;
	}

	@Override
	public String toString() {
		String ret = "";
		ret += !isConstraint() ? headAtom + " :- " : ":- ";
		for (int i = 0; i < bodyAtomsPositive.size(); i++) {
			ret += (i != 0 ? ", " : "") + bodyAtomsPositive.get(i);
		}
		for (int i = 0; i < bodyAtomsNegative.size(); i++) {
			ret += (i != 0 ? ", " : "") + bodyAtomsNegative.get(i);
		}
		ret += ".\n";
		return ret;
	}
}
