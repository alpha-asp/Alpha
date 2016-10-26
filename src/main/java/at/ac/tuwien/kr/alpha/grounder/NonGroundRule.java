package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedRule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a non-ground rule or a constraint for the semi-naive grounder.
 * Copyright (c) 2016, the Alpha Team.
 */
public class NonGroundRule<P extends Predicate> {
	private final int ruleId;

	private final List<PredicateInstance<P>> bodyAtomsPositive;
	private final List<PredicateInstance<P>> bodyAtomsNegative;
	private final PredicateInstance<P> headAtom;

	public NonGroundRule(int ruleId, List<PredicateInstance<P>> bodyAtomsPositive, List<PredicateInstance<P>> bodyAtomsNegative, PredicateInstance<P> headAtom) {
		this.ruleId = ruleId;

		// Sort for better join order.
		this.bodyAtomsPositive = sortAtoms(bodyAtomsPositive);

		// Since rule is safe, all variables in the negative body are already bound,
		// i.e., joining them cannot degenerate into cross-product.
		// Hence, there is no need to sort them.
		this.bodyAtomsNegative = bodyAtomsNegative;

		this.headAtom = headAtom;

		if (!isSafe()) {
			throw new RuntimeException("Encountered not safe rule: " + toString());
		}
	}

	public static NonGroundRule<BasicPredicate> constructNonGroundRule(IntIdGenerator intIdGenerator, ParsedRule parsedRule) {
		final List<PredicateInstance<BasicPredicate>> pos = new ArrayList<>(parsedRule.body.size() / 2);
		final List<PredicateInstance<BasicPredicate>> neg = new ArrayList<>(parsedRule.body.size() / 2);

		for (ParsedAtom parsedAtom : parsedRule.body) {
			final PredicateInstance<BasicPredicate> predicateInstance = new BasicPredicateInstance(parsedAtom);
			if (parsedAtom.isNegated) {
				neg.add(predicateInstance);
			} else {
				pos.add(predicateInstance);
			}
		}

		// Construct head if the given parsedRule is no constraint
		final BasicPredicateInstance head = parsedRule.head != null ? new BasicPredicateInstance(parsedRule.head) : null;

		return new NonGroundRule<>(
			intIdGenerator.getNextId(),
			pos,
			neg,
			head
		);
	}

	public int getRuleId() {
		return ruleId;
	}

	/**
	 *
	 * @return a list of all predicates occurring in the rule (may contain duplicates).
	 */
	public List<P> getOccurringPredicates() {
		ArrayList<P> predicateList = new ArrayList<>(bodyAtomsPositive.size() + bodyAtomsNegative.size() + 1);
		for (PredicateInstance<P> predicateInstance : bodyAtomsPositive) {
			predicateList.add(predicateInstance.predicate);
		}
		for (PredicateInstance<P> predicateInstance : bodyAtomsNegative) {
			predicateList.add(predicateInstance.predicate);
		}
		if (!isConstraint()) {
			predicateList.add(headAtom.predicate);
		}
		return predicateList;
	}

	/**
	 * Checks whether a rule is safe. A rule is safe iff all negated variables and all variables occurring in the
	 * head also occur in the positive body).
	 * @return true if this rule is safe.
	 */
	private boolean isSafe() {
		Set<VariableTerm> positiveVariables = new HashSet<>();

		// Check that all negative variables occur in the positive body.
		for (PredicateInstance<P> posAtom : bodyAtomsPositive) {
			positiveVariables.addAll(posAtom.getOccurringVariables());
		}

		for (PredicateInstance<P> negAtom : bodyAtomsNegative) {
			for (VariableTerm term : negAtom.getOccurringVariables()) {
				if (!positiveVariables.contains(term)) {
					return false;
				}
			}
		}

		// Constraint are safe at this point
		if (isConstraint()) {
			return true;
		}

		// Check that all variables of the head occur in the positive body.
		List<VariableTerm> headVariables = headAtom.getOccurringVariables();
		headVariables.removeAll(positiveVariables);
		return headVariables.isEmpty();
	}

	/**
	 * Sorts atoms such that the join-order of the atoms is improved (= cannot degenerate into cross-product).
	 * Note that the below sorting can be improved to yield smaller joins.
	 */
	private List<PredicateInstance<P>> sortAtoms(List<PredicateInstance<P>> atoms) {
		Set<SortingBodyComponent> components = new HashSet<>();
		for (PredicateInstance<P> atom : atoms) {
			final Set<SortingBodyComponent> hits = new HashSet<>();

			// For each variable
			for (VariableTerm variableTerm : atom.getOccurringVariables()) {
				// Find all components it also occurs and record in hitting components
				for (SortingBodyComponent component : components) {
					if (component.occurringVariables.contains(variableTerm)) {
						hits.add(component);
					}
				}
			}

			// If no components were hit, create new component, else merge components
			if (hits.isEmpty()) {
				components.add(new SortingBodyComponent(atom));
				continue;
			}

			// If only one component hit, add atom to it
			if (hits.size() == 1) {
				hits.iterator().next().add(atom);
				continue;
			}

			// Merge all components that are hit by the current atom
			SortingBodyComponent firstComponent = hits.iterator().next();
			for (SortingBodyComponent hitComponent : hits) {
				if (hitComponent != firstComponent) {
					firstComponent.merge(hitComponent);
					// Remove merged component from the set of available components
					components.remove(hitComponent);
				}
			}
		}

		// Components now contains all components that are internally connected but not connected to another component
		List<PredicateInstance<P>> sortedPositiveBodyAtoms = new ArrayList<>(components.size());
		for (SortingBodyComponent component : components) {
			sortedPositiveBodyAtoms.addAll(component.atomSequence);
		}
		return sortedPositiveBodyAtoms;
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
		if (!bodyAtomsPositive.isEmpty()) {
			return bodyAtomsPositive.get(0).predicate;
		} else if (!bodyAtomsNegative.isEmpty()) {
			return bodyAtomsNegative.get(0).predicate;
		}
		throw new RuntimeException("Encountered NonGroundRule with empty body, which should have been treated as a fact.");
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

	public boolean isConstraint() {
		return headAtom == null;
	}

	public boolean hasNegativeBodyAtoms() {
		return !bodyAtomsNegative.isEmpty();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (!isConstraint()) {
			sb.append(headAtom);
			sb.append(" ");
		}

		sb.append(":- ");
		Util.appendDelimited(sb, bodyAtomsPositive);
		sb.append(" ");
		Util.appendDelimited(sb, bodyAtomsNegative);
		sb.append(".\n");

		return sb.toString();
	}

	private class SortingBodyComponent {
		private final Set<VariableTerm> occurringVariables;
		private final Set<PredicateInstance<P>> atoms;
		private final List<PredicateInstance<P>> atomSequence;
		int numAtoms;

		SortingBodyComponent(PredicateInstance<P> atom) {
			this.occurringVariables = new HashSet<>(atom.getOccurringVariables());
			this.atoms = new HashSet<>();
			this.atoms.add(atom);
			this.atomSequence = new ArrayList<>();
			this.atomSequence.add(atom);
			this.numAtoms = 1;
		}

		void add(PredicateInstance<P> atom) {
			this.atoms.add(atom);
			this.atomSequence.add(atom);
			this.occurringVariables.addAll(atom.getOccurringVariables());
			this.numAtoms++;
		}

		void merge(SortingBodyComponent other) {
			occurringVariables.addAll(other.occurringVariables);
			atoms.addAll(other.atoms);
			numAtoms += other.numAtoms;
			atomSequence.addAll(other.atomSequence);
		}
	}

	public List<PredicateInstance<P>> getBodyAtomsPositive() {
		return bodyAtomsPositive;
	}

	public List<PredicateInstance<P>> getBodyAtomsNegative() {
		return bodyAtomsNegative;
	}

	public PredicateInstance<P> getHeadAtom() {
		return headAtom;
	}
}
