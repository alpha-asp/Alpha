/**
 * Copyright (c) 2017-2018, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.grounder.atoms;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.HeuristicDirective;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An internal atom that stores information on domain-specific heuristics.
 *
 */
public class HeuristicAtom implements Atom {
	public static final Predicate PREDICATE = Predicate.getInstance("_h", 4, true);
	
	private final Term weight;
	private final Term level;
	private final Term sign;
	private final FunctionTerm head;
	private final boolean ground;
	
	/**
	 * Constructs a heuristic atom using information from a {@link HeuristicDirective}.
	 */
	public HeuristicAtom(Atom head, Term weight, Term level, Term sign) {
		this(head.toFunctionTerm(), weight, level, sign);
	}
	
	private HeuristicAtom(FunctionTerm head, Term weight, Term level, Term sign) {
		this.head = head;
		this.weight = weight;
		this.level = level;
		this.sign = sign;
		this.ground = getTerms().stream().allMatch(Term::isGround);
	}

	public Term getWeight() {
		return weight;
	}

	public Term getLevel() {
		return level;
	}
	
	public Term getSign() {
		return sign;
	}
	
	public FunctionTerm getHead() {
		return head;
	}

	@Override
	public Predicate getPredicate() {
		return PREDICATE;
	}

	@Override
	public List<Term> getTerms() {
		return Arrays.asList(weight, level, sign, head);
	}

	@Override
	public boolean isGround() {
		return this.ground;
	}
	
	@Override
	public Literal toLiteral(boolean negated) {
		return new HeuristicLiteral(this, negated);
	}

	@Override
	public Set<VariableTerm> getBindingVariables() {
		Set<VariableTerm> bindingVariables = new HashSet<>();
		for (Term term : getTerms()) {
			bindingVariables.addAll(term.getOccurringVariables());
		}
		return bindingVariables;
	}

	@Override
	public Set<VariableTerm> getNonBindingVariables() {
		Set<VariableTerm> nonbindingVariables = new HashSet<>();
		for (Term term : getTerms()) {
			nonbindingVariables.addAll(term.getOccurringVariables());
		}
		return nonbindingVariables;
	}

	@Override
	public HeuristicAtom substitute(Substitution substitution) {
		return new HeuristicAtom(
			head.substitute(substitution),
			weight.substitute(substitution),
			level.substitute(substitution),
			sign.substitute(substitution)
		);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(PREDICATE.getName());
		if (!getTerms().isEmpty()) {
			sb.append("(");
			sb.append(Util.join(this.getTerms()));
			sb.append(")");
		}
		return sb.toString();
	}

	public static HeuristicAtom fromHeuristicDirective(HeuristicDirective heuristicDirective) {
		return new HeuristicAtom(heuristicDirective.getHead(), heuristicDirective.getWeight(), heuristicDirective.getLevel(), heuristicDirective.getSign());
	}
}
