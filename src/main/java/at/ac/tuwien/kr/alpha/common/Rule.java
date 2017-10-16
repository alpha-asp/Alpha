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
	private final Atom head;
	private final List<Literal> body;

	public Rule(Atom head, List<Literal> body) {
		this.head = head;
		this.body = body;

		if (!isSafe()) {
			// TODO: safety check needs to be adapted to solver what the solver actually understands. Will change in the future, adapt exception message accordingly.
			throw new RuntimeException("Encountered not safe rule: " + toString()
				+ "\nNotice: A rule is considered safe if all variables occurring in negative literals, builtin atoms, and the head of the rule also occurr in some positive litera.");
		}
	}

	public Atom getHead() {
		return head;
	}

	public List<Literal> getBody() {
		return body;
	}

	/**
	 *
	 * @return a list of all ordinary predicates occurring in the rule (may contain duplicates, does not contain builtin atoms).
	 */
	public List<Predicate> getOccurringPredicates() {
		ArrayList<Predicate> predicateList = new ArrayList<>(body.size() + 1);
		for (Literal literal : body) {
			predicateList.add(literal.getPredicate());
		}
		if (!isConstraint()) {
			predicateList.add(head.getPredicate());
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
		for (Literal literal : body) {
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
		List<VariableTerm> headVariables = head.getOccurringVariables();
		headVariables.removeAll(positiveVariables);
		return headVariables.isEmpty();
		*/
	}

	public boolean isGround() {
		if (!isConstraint() && !head.isGround()) {
			return false;
		}
		for (Literal atom : body) {
			if (!atom.isGround()) {
				return false;
			}
		}
		return true;
	}

	public boolean isConstraint() {
		return head == null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (!isConstraint()) {
			sb.append(head);
			sb.append(" ");
		}
		sb.append(":- ");
		Util.appendDelimited(sb, body);
		sb.append(".\n");

		return sb.toString();
	}
}