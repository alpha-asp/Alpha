/**
 * Copyright (c) 2016-2020, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.common.rule;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.IntIdGenerator;
import at.ac.tuwien.kr.alpha.grounder.RuleGroundingOrders;
import at.ac.tuwien.kr.alpha.grounder.Unifier;
import at.ac.tuwien.kr.alpha.grounder.structure.DirectFunctionalDependency;
import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static at.ac.tuwien.kr.alpha.grounder.structure.DirectFunctionalDependency.computeDirectFunctionalDependencies;

/**
 * Represents a normal rule or a constraint for the semi-naive grounder.
 * A normal rule has no head atom if it represents a constraint, otherwise it has one atom in its head.
 */
public class InternalRule extends NormalRule {

	private static final IntIdGenerator ID_GENERATOR = new IntIdGenerator();

	private final int ruleId;

	private final List<Predicate> occurringPredicates;

	private final RuleGroundingOrders groundingOrders;
	private final boolean isNonProjective;
	private final DirectFunctionalDependency functionalDependency;

	public InternalRule(NormalHead head, List<Literal> body) {
		super(head, body);
		if (body.isEmpty()) {
			throw new IllegalArgumentException(
					"Empty bodies are not supported for InternalRule! (Head = " + (head == null ? "NULL" : head.getAtom().toString()) + ")");
		}
		this.ruleId = InternalRule.ID_GENERATOR.getNextId();

		this.occurringPredicates = new ArrayList<>();
		if (!isConstraint()) {
			this.occurringPredicates.add(this.getHeadAtom().getPredicate());
		}

		for (Literal literal : body) {
			if (literal instanceof AggregateLiteral) {
				throw new IllegalArgumentException("AggregateLiterals aren't supported in InternalRules! (lit: " + literal.toString() + ")");
			}
			this.occurringPredicates.add(literal.getPredicate());
		}

		this.isNonProjective = getHeadAtom() != null && checkIsNonProjective();
		this.functionalDependency = getHeadAtom() == null ? null : computeDirectFunctionalDependencies(this);

		// not needed, done in AbstractRule! Leaving it commented out for future reference since this might actually be the
		// proper place to put it
		// this.checkSafety();

		this.groundingOrders = new RuleGroundingOrders(this);
		this.groundingOrders.computeGroundingOrders();
	}

	private boolean checkIsNonProjective() {
		// Collect head and body variables.
		HashSet<VariableTerm> occurringVariablesHead = new HashSet<>(getHeadAtom().getOccurringVariables());
		HashSet<VariableTerm> occurringVariablesBody = new HashSet<>();
		for (Literal literal : getBody()) {
			occurringVariablesBody.addAll(literal.getBindingVariables());
		}
		// Check that all variables of the body also occur in the head (otherwise grounding is not unique).
		occurringVariablesBody.removeAll(occurringVariablesHead);
		// Check if ever body variables occurs in the head.
		return occurringVariablesBody.isEmpty();
	}

	@VisibleForTesting
	public static void resetIdGenerator() {
		InternalRule.ID_GENERATOR.resetGenerator();
	}

	public static InternalRule fromNormalRule(NormalRule rule) {
		return new InternalRule(rule.isConstraint() ? null : new NormalHead(rule.getHeadAtom()), new ArrayList<>(rule.getBody()));
	}

	/**
	 * Returns a new Rule that is equal to this one except that all variables are renamed to have the newVariablePostfix
	 * appended.
	 * 
	 * @param newVariablePostfix
	 * @return
	 */
	public InternalRule renameVariables(String newVariablePostfix) {
		List<VariableTerm> occurringVariables = new ArrayList<>();
		Atom headAtom = this.getHeadAtom();
		occurringVariables.addAll(headAtom.getOccurringVariables());
		for (Literal literal : this.getBody()) {
			occurringVariables.addAll(literal.getOccurringVariables());
		}
		Unifier variableReplacement = new Unifier();
		for (VariableTerm occurringVariable : occurringVariables) {
			final String newVariableName = occurringVariable.toString() + newVariablePostfix;
			variableReplacement.put(occurringVariable, VariableTerm.getInstance(newVariableName));
		}
		Atom renamedHeadAtom = headAtom.substitute(variableReplacement);
		ArrayList<Literal> renamedBody = new ArrayList<>(this.getBody().size());
		for (Literal literal : this.getBody()) {
			renamedBody.add(literal.substitute(variableReplacement));
		}
		return new InternalRule(new NormalHead(renamedHeadAtom), renamedBody);
	}

	/**
	 * Returns the predicates occurring in this rule.
	 * @return a list of all predicates occurring in the rule (may contain duplicates and builtin atoms).
	 */
	public List<Predicate> getOccurringPredicates() {
		return this.occurringPredicates;
	}

	public RuleGroundingOrders getGroundingOrders() {
		return this.groundingOrders;
	}

	public int getRuleId() {
		return this.ruleId;
	}

	public boolean isNonProjective() {
		return isNonProjective;
	}

	public boolean isFunctionallyDependent() {
		return functionalDependency != null;
	}

	public DirectFunctionalDependency getFunctionalDependency() {
		return functionalDependency;
	}

}
