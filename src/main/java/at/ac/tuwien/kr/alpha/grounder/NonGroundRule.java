package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedBuiltinAtom;
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

	private final List<Atom> bodyAtomsPositive;
	private final List<Atom> bodyAtomsNegative;
	private final BasicAtom headAtom;

	public NonGroundRule(int ruleId, List<Atom> bodyAtomsPositive, List<Atom> bodyAtomsNegative, BasicAtom headAtom) {
		this.ruleId = ruleId;

		// Sort for better join order.
		this.bodyAtomsPositive = sortAtoms(bodyAtomsPositive);

		// Since rule is safe, all variables in the negative body are already bound,
		// i.e., joining them cannot degenerate into cross-product.
		// Hence, there is no need to sort them.
		this.bodyAtomsNegative = bodyAtomsNegative;

		this.headAtom = headAtom;

		if (!isSafe()) {
			throw new RuntimeException("Encountered not safe rule: " + toString()
				+ "\nNotice: A rule is considered safe if all variables occurring in negative literals, builtin atoms, and the head of the rule also occurr in some positive litera.");
		}
	}

	public static NonGroundRule<BasicPredicate> constructNonGroundRule(IntIdGenerator intIdGenerator, ParsedRule parsedRule) {
		final List<Atom> pos = new ArrayList<>(parsedRule.body.size() / 2);
		final List<Atom> neg = new ArrayList<>(parsedRule.body.size() / 2);

		for (ParsedAtom parsedAtom : parsedRule.body) {
			if (parsedAtom instanceof ParsedBuiltinAtom) {
				pos.add(new BuiltinAtom((ParsedBuiltinAtom) parsedAtom));
			} else {
				final BasicAtom basicAtom = BasicAtom.fromParsedAtom(parsedAtom);
				if (parsedAtom.isNegated) {
					neg.add(basicAtom);
				} else {
					pos.add(basicAtom);
				}
			}
		}

		// Construct head if the given parsedRule is no constraint
		final BasicAtom head = parsedRule.head != null ? BasicAtom.fromParsedAtom(parsedRule.head) : null;

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
	 * @return a list of all ordinary predicates occurring in the rule (may contain duplicates, does not contain builtin atoms).
	 */
	public List<Predicate> getOccurringPredicates() {
		ArrayList<Predicate> predicateList = new ArrayList<>(bodyAtomsPositive.size() + bodyAtomsNegative.size() + 1);
		for (Atom posAtom : bodyAtomsPositive) {
			if (posAtom instanceof BasicAtom) {
				predicateList.add(((BasicAtom)posAtom).predicate);
			}
		}
		for (Atom negAtom : bodyAtomsNegative) {
			if (negAtom instanceof BasicAtom) {
				predicateList.add(((BasicAtom) negAtom).predicate);
			}
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
		Set<VariableTerm> builtinVariables = new HashSet<>();

		// Check that all negative variables occur in the positive body.
		for (Atom posAtom : bodyAtomsPositive) {
			if (posAtom instanceof BasicAtom) {
				positiveVariables.addAll(posAtom.getOccurringVariables());
			} else if (posAtom instanceof BuiltinAtom) {
				builtinVariables.addAll(posAtom.getOccurringVariables());
			}
		}

		for (Atom negAtom : bodyAtomsNegative) {
			for (VariableTerm term : negAtom.getOccurringVariables()) {
				if (!positiveVariables.contains(term)) {
					return false;
				}
			}
		}
		for (VariableTerm builtinVariable : builtinVariables) {
			if (!positiveVariables.contains(builtinVariable)) {
				return false;
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
	private List<Atom> sortAtoms(List<Atom> atoms) {
		Set<SortingBodyComponent> components = new HashSet<>();
		Set<BuiltinAtom> builtinAtoms = new HashSet<>();
		for (Atom atom : atoms) {
			if (atom instanceof BuiltinAtom) {
				// Sort out builtin atoms (we consider them as not creating new bindings)
				builtinAtoms.add((BuiltinAtom) atom);
				continue;
			}
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
				components.add(new SortingBodyComponent((BasicAtom) atom));
				continue;
			}

			// If only one component hit, add atom to it
			if (hits.size() == 1) {
				hits.iterator().next().add((BasicAtom) atom);
				continue;
			}

			// Merge all components that are hit by the current atom
			SortingBodyComponent firstComponent = hits.iterator().next();
			firstComponent.add((BasicAtom) atom);
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
		sortedPositiveBodyAtoms.addAll(builtinAtoms);	// Put builtin atoms after positive literals and before negative ones.
		return sortedPositiveBodyAtoms;
	}


	/**
	 * Returns the predicate occurring first in the body of the rule.
	 * @return the first predicate of the body or null if the first predicate is a builtin predicate.
	 */
	public Predicate usedFirstBodyPredicate() {
		if (!bodyAtomsPositive.isEmpty()) {
			if (bodyAtomsPositive.get(0) instanceof BasicAtom) {
				return ((BasicAtom)bodyAtomsPositive.get(0)).predicate;
			} else {
				return null;
			}
		} else if (!bodyAtomsNegative.isEmpty()) {
			if (bodyAtomsNegative.get(0) instanceof BasicAtom) {
				return ((BasicAtom)bodyAtomsNegative.get(0)).predicate;
			} else {
				return null;
			}
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
			Atom bodyAtom = getBodyAtom(i);
			if (bodyAtom instanceof BasicAtom) {
				if (((BasicAtom) bodyAtom).predicate.equals(predicate)) {
					return i;
				}
			}
		}
		throw new RuntimeException("Predicate " + predicate + " does not occur in rule " + this.toString());
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
		for (Atom basicAtom : bodyAtomsNegative) {
			if (basicAtom instanceof BasicAtom) {
				usedPredicates.add(((BasicAtom) basicAtom).predicate);
			}
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
		for (Atom basicAtom : bodyAtomsPositive) {
			if (!basicAtom.isGround()) {
				return false;
			}
		}
		for (Atom basicAtom : bodyAtomsNegative) {
			if (!basicAtom.isGround()) {
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
		if (bodyAtomsPositive.size() > 0 && bodyAtomsNegative.size() > 0) {
			sb.append(", ");
		} else {
			sb.append(" ");
		}
		Util.appendDelimitedPrefix(sb, "not ", bodyAtomsNegative);
		sb.append(".\n");

		return sb.toString();
	}

	private class SortingBodyComponent {
		private final Set<VariableTerm> occurringVariables;
		private final Set<BasicAtom> atoms;
		private final List<BasicAtom> atomSequence;
		int numAtoms;

		SortingBodyComponent(BasicAtom atom) {
			this.occurringVariables = new HashSet<>(atom.getOccurringVariables());
			this.atoms = new HashSet<>();
			this.atoms.add(atom);
			this.atomSequence = new ArrayList<>();
			this.atomSequence.add(atom);
			this.numAtoms = 1;
		}

		void add(BasicAtom atom) {
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

	public List<Atom> getBodyAtomsPositive() {
		return bodyAtomsPositive;
	}

	public List<Atom> getBodyAtomsNegative() {
		return bodyAtomsNegative;
	}

	public BasicAtom getHeadAtom() {
		return headAtom;
	}
}
