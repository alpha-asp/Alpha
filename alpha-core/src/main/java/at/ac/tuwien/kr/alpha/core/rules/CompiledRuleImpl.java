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
package at.ac.tuwien.kr.alpha.core.rules;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;

import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.AggregateLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.rules.Rules;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.commons.substitutions.Unifier;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.commons.util.IntIdGenerator;
import at.ac.tuwien.kr.alpha.core.grounder.RuleGroundingInfoImpl;

/**
 * Represents a normal rule or a constraint for the semi-naive grounder.
 * A normal rule has no head atom if it represents a constraint, otherwise it has one atom in its head.
 */
public class CompiledRuleImpl implements CompiledRule {

	private static final IntIdGenerator ID_GENERATOR = new IntIdGenerator();

	private final NormalRule wrappedRule;

	private final int ruleId;
	private final List<Predicate> occurringPredicates;
	private final RuleGroundingInfoImpl groundingOrders;
	
	CompiledRuleImpl(NormalHead head, Set<Literal> body) {
		if (body.isEmpty()) {
			throw new IllegalArgumentException(
					"Empty bodies are not supported for InternalRule! (Head = " + (head == null ? "NULL" : head.getAtom().toString()) + ")");
		}
		this.ruleId = CompiledRuleImpl.ID_GENERATOR.getNextId();
		this.wrappedRule = Rules.newNormalRule(head, body);

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

		this.groundingOrders = new RuleGroundingInfoImpl(this);
		this.groundingOrders.computeGroundingOrders();
	}

	@VisibleForTesting
	public static void resetIdGenerator() {
		CompiledRuleImpl.ID_GENERATOR.resetGenerator();
	}

	/**
	 * Returns a new Rule that is equal to this one except that all variables are renamed to have the newVariablePostfix
	 * appended.
	 * 
	 * @param newVariablePostfix
	 * @return
	 */
	@Override
	public CompiledRuleImpl renameVariables(String newVariablePostfix) {
		// TODO handle action heads!
		List<VariableTerm> occurringVariables = new ArrayList<>();
		BasicAtom headAtom = this.getHeadAtom();
		occurringVariables.addAll(headAtom.getOccurringVariables());
		for (Literal literal : this.getBody()) {
			occurringVariables.addAll(literal.getOccurringVariables());
		}
		Unifier variableReplacement = new Unifier();
		for (VariableTerm occurringVariable : occurringVariables) {
			final String newVariableName = occurringVariable.toString() + newVariablePostfix;
			variableReplacement.put(occurringVariable, Terms.newVariable(newVariableName));
		}
		BasicAtom renamedHeadAtom = headAtom.substitute(variableReplacement);
		Set<Literal> renamedBody = new LinkedHashSet<>(this.getBody().size());
		for (Literal literal : this.getBody()) {
			renamedBody.add(literal.substitute(variableReplacement));
		}
		// TODO action heads!
		// TODO we want to pull renameVariables down to atom, term, etc level
		return new CompiledRuleImpl(Heads.newNormalHead(renamedHeadAtom), renamedBody);
	}

	/**
	 * Returns the predicates occurring in this rule.
	 * @return a list of all predicates occurring in the rule (may contain duplicates and builtin atoms).
	 */
	@Override
	public List<Predicate> getOccurringPredicates() {
		return this.occurringPredicates;
	}

	@Override
	public RuleGroundingInfoImpl getGroundingInfo() {
		return this.groundingOrders;
	}

	@Override
	public int getRuleId() {
		return this.ruleId;
	}

	@Override
	public BasicAtom getHeadAtom() {
		return wrappedRule.getHeadAtom();
	}

	@Override
	public NormalHead getHead() {
		return wrappedRule.getHead();
	}

	@Override
	public Set<Literal> getBody() {
		return wrappedRule.getBody();
	}

	@Override
	public boolean isConstraint() {
		return wrappedRule.isConstraint();
	}

	@Override
	public Set<Literal> getPositiveBody() {
		return wrappedRule.getPositiveBody();
	}

	@Override
	public Set<Literal> getNegativeBody() {
		return wrappedRule.getNegativeBody();
	}

	@Override
	public boolean isGround() {
		return wrappedRule.isGround();
	}

	@Override
	public String toString() {
		return wrappedRule.toString();
	}

	@Override
	public boolean equals(Object o) {
		return wrappedRule.equals(o);
	}

	@Override
	public int hashCode() {
		return wrappedRule.hashCode();
	}

}
