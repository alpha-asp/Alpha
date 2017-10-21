package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.predicates.Evaluable;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.atoms.IntervalAtom;
import at.ac.tuwien.kr.alpha.grounder.transformation.IntervalTermToIntervalAtom;

import java.util.*;

/**
 * Represents a non-ground rule or a constraint for the semi-naive grounder.
 * Copyright (c) 2017, the Alpha Team.
 */
public class NonGroundRule {
	private final int ruleId;

	private final List<Atom> bodyAtomsPositive;
	private final List<Atom> bodyAtomsNegative;
	private final Atom headAtom;

	private final boolean containsIntervals;
	private final boolean containsExternals;
	private final boolean isOriginallyGround;

	public boolean containsIntervals() {
		return containsIntervals;
	}

	public boolean containsExternals() {
		return containsExternals;
	}

	public boolean isOriginallyGround() {
		return isOriginallyGround;
	}

	private NonGroundRule(int ruleId, List<Atom> bodyAtomsPositive, List<Atom> bodyAtomsNegative, Atom headAtom, boolean containsIntervals, boolean containsExternals) {
		this.ruleId = ruleId;

		this.isOriginallyGround = isOriginallyGround(bodyAtomsPositive, bodyAtomsNegative, headAtom);
		this.containsIntervals = containsIntervals;
		this.containsExternals = containsExternals;

		// Sort for better join order.
		this.bodyAtomsPositive = Collections.unmodifiableList(sortAtoms(bodyAtomsPositive));

		// Since rule is safe, all variables in the negative body are already bound,
		// i.e., joining them cannot degenerate into cross-product.
		// Hence, there is no need to sort them.
		this.bodyAtomsNegative = Collections.unmodifiableList(bodyAtomsNegative);

		this.headAtom = headAtom;

		if (!isSafe()) {
			throw new RuntimeException("Encountered not safe rule: " + toString()
				+ "\nNotice: A rule is considered safe if all variables occurring in negative literals, builtin atoms, and the head of the rule also occurr in some positive litera.");
		}
	}

	// FIXME: NonGroundRule should extend Rule and then its constructor directly be used.
	public static NonGroundRule constructNonGroundRule(IntIdGenerator intIdGenerator, Rule rule) {
		List<Literal> body = rule.getBody();
		final List<Atom> pos = new ArrayList<>(body.size() / 2);
		final List<Atom> neg = new ArrayList<>(body.size() / 2);
		boolean containsIntervals = false;
		boolean containsExternals = false;
		for (Literal literal : body) {
			if (literal instanceof IntervalAtom) {
				containsIntervals = true;
			}
			if ((literal instanceof ExternalAtom)) {
				if (((ExternalAtom) literal).hasOutput()) {
					containsExternals = true;
				}
			}

			(literal.isNegated() ? neg : pos).add(literal);
		}
		return new NonGroundRule(intIdGenerator.getNextId(), pos, neg, rule.getHead(), containsIntervals, containsExternals);
	}

	private static boolean isOriginallyGround(List<Atom> bodyAtomsPositive, List<Atom> bodyAtomsNegative, Atom headAtom) {
		List<VariableTerm> occurringVariables = new ArrayList<>();
		if (headAtom != null) {
			occurringVariables.addAll(headAtom.getBindingVariables());
			occurringVariables.addAll(headAtom.getNonBindingVariables());
		}
		for (Atom atom : bodyAtomsPositive) {
			occurringVariables.addAll(atom.getBindingVariables());
			occurringVariables.addAll(atom.getNonBindingVariables());
		}
		for (Atom atom : bodyAtomsNegative) {
			occurringVariables.addAll(atom.getBindingVariables());
			occurringVariables.addAll(atom.getNonBindingVariables());
		}
		for (VariableTerm variable : occurringVariables) {
			// Ignore variables introduced by interval rewriting.
			if (variable.toString().startsWith(IntervalTermToIntervalAtom.INTERVAL_VARIABLE_PREFIX)) {
				continue;
			}
			return false;
		}
		return true;
	}

	public int getRuleId() {
		return ruleId;
	}

	/**
	 *
	 * @return a list of all ordinary predicates occurring in the rule (may contain duplicates, does not contain builtin atoms).
	 */
	public List<Predicate> getOccurringPredicates() {
		ArrayList<Predicate> predicateList = new ArrayList<>(bodyAtomsPositive.size() + bodyAtomsNegative.size() + 1);
		for (Atom posAtom : bodyAtomsPositive) {
			predicateList.add(posAtom.getPredicate());
		}
		for (Atom negAtom : bodyAtomsNegative) {
			predicateList.add(negAtom.getPredicate());
		}
		if (!isConstraint()) {
			predicateList.add(headAtom.getPredicate());
		}
		return predicateList;
	}

	/**
	 * Checks whether a rule is safe. A rule is safe iff all negated variables and all variables occurring in the
	 * head also occur in the positive body).
	 * @return true if this rule is safe.
	 */
	private boolean isSafe() {
		Set<VariableTerm> bindingVariables = new HashSet<>();
		Set<VariableTerm> nonbindingVariables = new HashSet<>();

		// Check that all negative variables occur in the positive body.
		for (Atom posAtom : bodyAtomsPositive) {
			bindingVariables.addAll(posAtom.getBindingVariables());
			nonbindingVariables.addAll(posAtom.getNonBindingVariables());
		}

		for (Atom negAtom : bodyAtomsNegative) {
			// No variables in a negated atom are binding.
			nonbindingVariables.addAll(negAtom.getBindingVariables());
			nonbindingVariables.addAll(negAtom.getNonBindingVariables());
		}

		// Rule heads must be safe, i.e., all their variables must be bound by the body.
		if (!isConstraint()) {
			nonbindingVariables.addAll(headAtom.getBindingVariables());
			nonbindingVariables.addAll(headAtom.getNonBindingVariables());
		}

		// Check that all non-binding variables are bound in this rule.
		nonbindingVariables.removeAll(bindingVariables);
		return nonbindingVariables.isEmpty();
	}

	/**
	 * Sorts atoms such that the join-order of the atoms is improved (= cannot degenerate into cross-product).
	 * Note that the below sorting can be improved to yield smaller joins.
	 */
	private List<Atom> sortAtoms(List<Atom> atoms) {
		final Set<SortingBodyComponent> components = new LinkedHashSet<>();
		final Set<ExternalAtom> evaluableAtoms = new LinkedHashSet<>();
		final Set<IntervalAtom> intervalAtoms = new LinkedHashSet<>();

		for (Atom atom : atoms) {
			if (atom instanceof ExternalAtom) {
				// Sort out builtin atoms (we consider them as not creating new bindings)
				evaluableAtoms.add((ExternalAtom) atom);
				continue;
			}
			if (atom instanceof IntervalAtom) {
				intervalAtoms.add((IntervalAtom) atom);
				continue;
			}
			final Set<SortingBodyComponent> hits = new LinkedHashSet<>();

			// For each variable
			for (VariableTerm variableTerm : atom.getBindingVariables()) {
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
			firstComponent.add(atom);
			for (SortingBodyComponent hitComponent : hits) {
				if (hitComponent != firstComponent) {
					firstComponent.merge(hitComponent);
					// Remove merged component from the set of available components
					components.remove(hitComponent);
				}
			}
		}

		// Components now contains all components that are internally connected but not connected to another component
		List<Atom> sortedPositiveBodyAtoms = new ArrayList<>(components.size());
		for (SortingBodyComponent component : components) {
			sortedPositiveBodyAtoms.addAll(component.atomSequence);
		}

		sortedPositiveBodyAtoms.addAll(intervalAtoms); // Put interval atoms after positive literals generating their bindings and before builtin atom.
		sortedPositiveBodyAtoms.addAll(evaluableAtoms);	// Put builtin atoms after positive literals and before negative ones.
		return sortedPositiveBodyAtoms;
	}

	/**
	 * Returns the predicate occurring first in the body of the rule.
	 * @return the first predicate of the body or null if the first predicate is a builtin predicate.
	 */
	public Predicate usedFirstBodyPredicate() {
		if (!bodyAtomsPositive.isEmpty()) {
			return (bodyAtomsPositive.get(0)).getPredicate();
		} else if (!bodyAtomsNegative.isEmpty()) {
			return (bodyAtomsNegative.get(0)).getPredicate();
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
	public Atom getBodyAtom(int atomPosition) {
		if (atomPosition < bodyAtomsPositive.size()) {
			return bodyAtomsPositive.get(atomPosition);
		} else {
			return bodyAtomsNegative.get(atomPosition - bodyAtomsPositive.size());
		}
	}

	public int getFirstOccurrenceOfPredicate(Predicate predicate) {
		for (int i = 0; i < getNumBodyAtoms(); i++) {
			if (getBodyAtom(i).getPredicate().equals(predicate)) {
				return i;
			}
		}
		throw new RuntimeException("Predicate " + predicate + " does not occur in rule " + this);
	}

	public boolean isBodyAtomPositive(int atomPosition) {
		return atomPosition < bodyAtomsPositive.size();
	}

	/**
	 * Returns all predicates occurring in the negative body of the rule.
	 * @return
	 */
	public List<Predicate> usedNegativeBodyPredicates() {
		ArrayList<Predicate> usedPredicates = new ArrayList<>(bodyAtomsNegative.size());
		for (Atom basicAtom : bodyAtomsNegative) {
			usedPredicates.add(basicAtom.getPredicate());
		}
		return usedPredicates;
	}

	public int getNumBodyAtoms() {
		return bodyAtomsPositive.size() + bodyAtomsNegative.size();
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
		if (bodyAtomsPositive.size() > 0 && bodyAtomsNegative.size() > 0) {
			sb.append(", ");
		} else {
			sb.append(" ");
		}
		Util.appendDelimited(sb, bodyAtomsNegative);
		sb.append(".\n");

		return sb.toString();
	}

	private class SortingBodyComponent {
		private final Set<VariableTerm> occurringVariables;
		private final Set<Atom> atoms;
		private final List<Atom> atomSequence;
		int numAtoms;

		SortingBodyComponent(Atom atom) {
			this.occurringVariables = new LinkedHashSet<>(atom.getBindingVariables());
			this.atoms = new LinkedHashSet<>();
			this.atoms.add(atom);
			this.atomSequence = new ArrayList<>();
			this.atomSequence.add(atom);
			this.numAtoms = 1;
		}

		void add(Atom atom) {
			this.atoms.add(atom);
			this.atomSequence.add(atom);
			this.occurringVariables.addAll(atom.getBindingVariables());
			this.numAtoms++;
		}

		void merge(SortingBodyComponent other) {
			occurringVariables.addAll(other.occurringVariables);
			atoms.addAll(other.atoms);
			numAtoms += other.numAtoms;
			atomSequence.addAll(other.atomSequence);
		}
	}

	public List<Atom> getBodyAtomsPositive() {
		return bodyAtomsPositive;
	}

	public List<Atom> getBodyAtomsNegative() {
		return bodyAtomsNegative;
	}

	public Atom getHeadAtom() {
		return headAtom;
	}
}