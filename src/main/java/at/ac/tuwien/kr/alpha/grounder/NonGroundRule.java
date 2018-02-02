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
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.heuristics.NonGroundDomainSpecificHeuristicValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static at.ac.tuwien.kr.alpha.Util.join;
import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Represents a non-ground rule or a constraint for the semi-naive grounder.
 * Copyright (c) 2017, the Alpha Team.
 */
public class NonGroundRule {
	private static final Logger LOGGER = LoggerFactory.getLogger(NonGroundRule.class);
	private static final IntIdGenerator ID_GENERATOR = new IntIdGenerator();

	private final int ruleId;
	private final Rule rule;

	private final List<Atom> bodyAtomsPositive;
	private final List<Atom> bodyAtomsNegative;
	private final Atom headAtom;
	private final NonGroundDomainSpecificHeuristicValues heuristic;

	final RuleGroundingOrder groundingOrder;

	private NonGroundRule(Rule rule, int ruleId, List<Atom> bodyAtomsPositive, List<Atom> bodyAtomsNegative, Atom headAtom, boolean containsIntervals,
			boolean containsExternals, NonGroundDomainSpecificHeuristicValues heuristic) {
		this.ruleId = ruleId;
		this.rule = rule;

		// Sort for better join order.
		this.bodyAtomsPositive = Collections.unmodifiableList(bodyAtomsPositive);

		// Since rule is safe, all variables in the negative body are already bound,
		// i.e., joining them cannot degenerate into cross-product.
		// Hence, there is no need to sort them.
		this.bodyAtomsNegative = Collections.unmodifiableList(bodyAtomsNegative);

		this.headAtom = headAtom;
		this.heuristic = heuristic;

		checkSafety();
		this.groundingOrder = new RuleGroundingOrder(this);
		groundingOrder.computeGroundingOrders();

		LOGGER.debug("Rule ID {} assigned to {}", ruleId, rule);
	}

	// FIXME: NonGroundRule should extend Rule and then its constructor directly be used.
	public static NonGroundRule constructNonGroundRule(Rule rule) {
		List<Literal> body = rule.getBody();
		final List<Atom> pos = new ArrayList<>(body.size() / 2);
		final List<Atom> neg = new ArrayList<>(body.size() / 2);
		boolean containsIntervals = false;
		boolean containsExternals = false;

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
		return new NonGroundRule(rule, ID_GENERATOR.getNextId(), pos, neg, headAtom, containsIntervals,
			containsExternals, rule.getHeuristic());
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
		// TODO: either do full check here or rely on RuleGroundingOrder to detect non-safety (on already-transformed rules, however).
		return;
	}

	public boolean isConstraint() {
		return headAtom == null;
	}

	@Override
	public String toString() {
		String ruleString = join(
			join(
						(isConstraint() ? "" : headAtom + " ") + ":- ",
				bodyAtomsPositive,
				bodyAtomsPositive.size() + bodyAtomsNegative.size() > 0 ? ", " : " "
			),
			bodyAtomsNegative,
			"."
		);
		if (heuristic != null) {
			ruleString += " [" + heuristic + "]";
		}
		return ruleString;
	}

	public NonGroundDomainSpecificHeuristicValues getHeuristic() {
		return heuristic;
	}

	public Rule getRule() {
		return rule;
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