package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a non-ground rule or a constraint for the semi-naive grounder.
 * Copyright (c) 2016, the Alpha Team.
 */
public class Rule {
	private final List<Literal> bodyLiterals;
	private final Atom headAtom;

	public Rule(List<Literal> bodyLiterals, Atom headAtom) {
		this.bodyLiterals = bodyLiterals;
		this.headAtom = headAtom;

		if (!isSafe()) {
			// TODO: safety check needs to be adapted to solver what the solver actually understands. Will change in the future, adapt exception message accordingly.
			throw new RuntimeException("Encountered not safe rule: " + toString()
				+ "\nNotice: A rule is considered safe if all variables occurring in negative literals, builtin atoms, and the head of the rule also occurr in some positive litera.");
		}
	}

	/**
	 *
	 * @return a list of all ordinary predicates occurring in the rule (may contain duplicates, does not contain builtin atoms).
	 */
	public List<Predicate> getOccurringPredicates() {
		ArrayList<Predicate> predicateList = new ArrayList<>(bodyLiterals.size() + 1);
		for (Literal literal : bodyLiterals) {
			predicateList.add(literal.getPredicate());
		}
		if (!isConstraint()) {
			predicateList.add(headAtom.getPredicate());
		}
		return predicateList;
	}

	/**
	 * Checks whether a rule is safe.
	 * The actual safety condition may vary over the next improvements.
	 * Currently, a rule is safe iff all negated variables and all variables occurring in the
	 * head also occur in the positive body).
	 * @return true if this rule is safe.
	 */
	private boolean isSafe() {
		// TODO: do the real check.
		return true;
		/*Set<VariableTerm> positiveVariables = new HashSet<>();
		Set<VariableTerm> builtinVariables = new HashSet<>();

		// Check that all negative variables occur in the positive body.
		for (Literal literal : bodyLiterals) {
			// FIXME: The following five lines depend on concrete
			// implementations of the Atom interface. Not nice.
			if (literal instanceof BasicAtom) {
				positiveVariables.addAll(literal.getOccurringVariables());
			} else if (literal instanceof BuiltinAtom) {
				builtinVariables.addAll(literal.getOccurringVariables());
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
		*/
	}

	/**
	 * Returns the n-th atom in the body of this non-ground rule.
	 * @param atomPosition 0-based position of the body atom.
	 * @return
	 */
	public Literal getBodyLiteral(int atomPosition) {
		return bodyLiterals.get(atomPosition);
	}

	public int getNumBodyAtoms() {
		return bodyLiterals.size();
	}

	public Atom getHeadAtom() {
		return headAtom;
	}

	public boolean isGround() {
		if (!isConstraint() && !headAtom.isGround()) {
			return false;
		}
		for (Literal atom : bodyLiterals) {
			if (!atom.isGround()) {
				return false;
			}
		}
		return true;
	}

	public boolean isConstraint() {
		return headAtom == null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (!isConstraint()) {
			sb.append(headAtom);
			sb.append(" ");
		}
		sb.append(":- ");
		Util.appendDelimited(sb, bodyLiterals);
		sb.append(".\n");

		return sb.toString();
	}
}