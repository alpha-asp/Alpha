/**
 * Copyright (c) 2016-2018, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.common.rule.impl;

import static at.ac.tuwien.kr.alpha.Util.join;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.AbstractRule;
import at.ac.tuwien.kr.alpha.common.rule.head.impl.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.rule.head.impl.NormalHead;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.IntIdGenerator;
import at.ac.tuwien.kr.alpha.grounder.RuleGroundingOrder;
import at.ac.tuwien.kr.alpha.grounder.Unifier;

/**
 * Represents a normal rule or a constraint for the semi-naive grounder. A normal rule has one (or no if it's a constraint) atom in it's head. Copyright (c)
 * 2017-2019, the Alpha Team.
 */
public class NormalRule extends AbstractRule<NormalHead> {

	private static final IntIdGenerator ID_GENERATOR = new IntIdGenerator();

	private final int ruleId;

	private final List<Atom> bodyAtomsPositive;
	private final List<Atom> bodyAtomsNegative;

	private final RuleGroundingOrder groundingOrder;

	public NormalRule(NormalHead head, List<Literal> body) {
		super(head, body);
		this.ruleId = NormalRule.ID_GENERATOR.getNextId();

		final List<Atom> pos = new ArrayList<>(body.size() / 2);
		final List<Atom> neg = new ArrayList<>(body.size() / 2);

		for (Literal literal : body) {
			(literal.isNegated() ? neg : pos).add(literal.getAtom());
		}

		// Sort for better join order.
		this.bodyAtomsPositive = Collections.unmodifiableList(pos);

		// Since rule is safe, all variables in the negative body are already bound,
		// i.e., joining them cannot degenerate into cross-product.
		// Hence, there is no need to sort them.
		this.bodyAtomsNegative = Collections.unmodifiableList(neg);

		// not needed, done in AbstractRule! Leaving it commented out for future reference since this might actually be the proper place to put it
		// this.checkSafety();

		this.groundingOrder = new RuleGroundingOrder(this);
		this.groundingOrder.computeGroundingOrders();
	}

	public static NormalRule fromBasicRule(BasicRule rule) {
		Atom headAtom = null;
		if (!rule.isConstraint()) {
			if (!rule.getHead().isNormal()) {
				throw Util.oops("Trying to construct a NormalRule from rule with non-normal head!");
			}
			DisjunctiveHead dHead = (DisjunctiveHead) rule.getHead();
			headAtom = dHead.disjunctiveAtoms.get(0);
		}
		// FIXME This is totally not nice, add a getAtoms() to Head!
		return new NormalRule(headAtom != null ? new NormalHead(headAtom) : null, rule.getBody());
	}

	public boolean isGround() {
		if (!isConstraint() && !this.getHead().isGround()) {
			return false;
		}
		for (Literal bodyElement : this.getBody()) {
			if (!bodyElement.isGround()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns a new Rule that is equal to this one except that all variables are renamed to have the newVariablePostfix appended.
	 * 
	 * @param newVariablePostfix
	 * @return
	 */
	public NormalRule renameVariables(String newVariablePostfix) {
		if (!this.getHead().isNormal()) {
			throw Util.oops("Trying to rename variables in not-normal rule.");
		}
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
			renamedBody.add((Literal) literal.substitute(variableReplacement));
		}
		return new NormalRule(new NormalHead(renamedHeadAtom), renamedBody);
	}

	/**
	 *
	 * @return a list of all ordinary predicates occurring in the rule (may contain duplicates, does not contain builtin atoms).
	 */
	// FIXME NormalRule should be immutable, so we shouldn't have to calculate these every time!
	public List<Predicate> getOccurringPredicates() {
		ArrayList<Predicate> predicateList = new ArrayList<>(this.bodyAtomsPositive.size() + this.bodyAtomsNegative.size() + 1);
		for (Atom posAtom : this.bodyAtomsPositive) {
			predicateList.add(posAtom.getPredicate());
		}
		for (Atom negAtom : this.bodyAtomsNegative) {
			predicateList.add(negAtom.getPredicate());
		}
		if (!isConstraint()) {
			predicateList.add(this.getHeadAtom().getPredicate());
		}
		return predicateList;
	}

	@Override
	public String toString() {
		return join(join((isConstraint() ? "" : this.getHeadAtom() + " ") + ":- ", this.bodyAtomsPositive, this.bodyAtomsNegative.size() > 0 ? ", not " : ""),
				this.bodyAtomsNegative, ", not ", ".");
	}

	public List<Atom> getBodyAtomsPositive() {
		return this.bodyAtomsPositive;
	}

	public List<Atom> getBodyAtomsNegative() {
		return this.bodyAtomsNegative;
	}

	public Atom getHeadAtom() {
		return this.isConstraint() ? null : this.getHead().getAtom();
	}

	public RuleGroundingOrder getGroundingOrder() {
		return this.groundingOrder;
	}

	public int getRuleId() {
		return this.ruleId;
	}
}