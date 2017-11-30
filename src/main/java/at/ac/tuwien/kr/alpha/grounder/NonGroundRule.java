/**
 * Copyright (c) 2016-2017, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.Variable;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.join;
import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Represents a non-ground rule or a constraint for the semi-naive grounder.
 * Copyright (c) 2017, the Alpha Team.
 */
public class NonGroundRule {
	private static final IntIdGenerator ID_GENERATOR = new IntIdGenerator();

	private final int ruleId;
	private final Rule rule;

	private final List<Literal> bodyAtomsPositive;
	private final List<Literal> bodyAtomsNegative;
	private final Atom headAtom;

	final RuleGroundingOrder groundingOrder;

	private NonGroundRule(Rule rule, int ruleId, List<Literal> bodyAtomsPositive, List<Literal> bodyAtomsNegative, Atom headAtom) {
		this.ruleId = ruleId;
		this.rule = rule;

		// Sort for better join order.
		this.bodyAtomsPositive = Collections.unmodifiableList(bodyAtomsPositive);

		// Since rule is safe, all variables in the negative body are already bound,
		// i.e., joining them cannot degenerate into cross-product.
		// Hence, there is no need to sort them.
		this.bodyAtomsNegative = Collections.unmodifiableList(bodyAtomsNegative);

		this.headAtom = headAtom;

		checkSafety();
		this.groundingOrder = new RuleGroundingOrder(this);
		groundingOrder.computeGroundingOrders();
	}

	// FIXME: NonGroundRule should extend Rule and then its constructor directly be used.
	public static NonGroundRule constructNonGroundRule(Rule rule) {
		List<Literal> body = rule.getBody();
		final List<Literal> pos = new ArrayList<>(body.size() / 2);
		final List<Literal> neg = new ArrayList<>(body.size() / 2);

		for (Literal literal : body) {
			(literal.isNegated() ? neg : pos).add(literal);
		}
		Atom headAtom = null;
		if (rule.getHead() != null) {
			if (!rule.getHead().isNormal()) {
				throw oops("Trying to construct NonGroundRule from rule that is not normal");
			}
			headAtom = ((DisjunctiveHead)rule.getHead()).disjunctiveAtoms.get(0);
		}
		return new NonGroundRule(rule, ID_GENERATOR.getNextId(), pos, neg, headAtom);
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
	private void checkSafety() {
		Set<Variable> bindingVariables = new HashSet<>();
		Set<Variable> nonbindingVariables = new HashSet<>();

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

		if (nonbindingVariables.isEmpty()) {
			return;
		}

		throw new RuntimeException("Encountered unsafe variable " + nonbindingVariables.iterator().next().toString() + " in rule: " + toString()
			+ "\nNotice: A rule is considered safe if all variables occurring in negative literals, builtin atoms, and the head of the rule also occur in some positive litera.");
	}

	/**
	 * Returns the interpretation occurring first in the body of the rule.
	 * @return the first interpretation of the body or null if the first interpretation is a builtin interpretation.
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
	public Literal getBodyAtom(int atomPosition) {
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

	@Override
	public String toString() {
		return join(
			join(
				isConstraint() ? "" : ":- " + headAtom + " ",
				bodyAtomsPositive,
				bodyAtomsPositive.size() + bodyAtomsNegative.size() > 0 ? ", " : " "
			),
			bodyAtomsNegative,
			"."
		);
	}

	private class SortingBodyComponent {
		private final Set<Variable> occurringVariables;
		private final Set<Literal> atoms;
		private final List<Literal> atomSequence;
		int numAtoms;

		SortingBodyComponent(Literal atom) {
			this.occurringVariables = new LinkedHashSet<>(atom.getBindingVariables());
			this.atoms = new LinkedHashSet<>();
			this.atoms.add(atom);
			this.atomSequence = new ArrayList<>();
			this.atomSequence.add(atom);
			this.numAtoms = 1;
		}

		void add(Literal atom) {
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

	public Rule getRule() {
		return rule;
	}

	public List<Literal> getBodyAtomsPositive() {
		return bodyAtomsPositive;
	}

	public List<Literal> getBodyAtomsNegative() {
		return bodyAtomsNegative;
	}

	public Atom getHeadAtom() {
		return headAtom;
	}
}