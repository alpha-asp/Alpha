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
import at.ac.tuwien.kr.alpha.common.WeightAtLevel;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Arrays;
import java.util.List;

/**
 * An internal atom that stores information on domain-specific heuristics.
 *
 */
public class HeuristicAtom implements Atom {
	public static final Predicate PREDICATE = Predicate.getInstance("_h", 4, true);
	
	private final WeightAtLevel weightAtLevel;
	private final Term sign;
	private final FunctionTerm head;
	private final boolean ground;
	
	/**
	 * Constructs a heuristic atom using information from a {@link HeuristicDirective}.
	 */
	public HeuristicAtom(Atom head, WeightAtLevel weightAtLevel, Term sign) {
		this(head.toFunctionTerm(), weightAtLevel, sign);
	}
	
	private HeuristicAtom(FunctionTerm head, WeightAtLevel weightAtLevel, Term sign) {
		this.head = head;
		this.weightAtLevel = weightAtLevel;
		this.sign = sign;
		this.ground = getTerms().stream().allMatch(Term::isGround);
	}

	public WeightAtLevel getWeightAtLevel() {
		return weightAtLevel;
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
		return Arrays.asList(weightAtLevel.getWeight(), weightAtLevel.getLevel(), sign, head);
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
	public HeuristicAtom substitute(Substitution substitution) {
		return new HeuristicAtom(
			head.substitute(substitution),
			weightAtLevel.substitute(substitution),
			sign.substitute(substitution)
		);
	}

	@Override
	public String toString() {
		return Util.join(PREDICATE.getName() + "(", this.getTerms(), ")");
	}

	public static HeuristicAtom fromHeuristicDirective(HeuristicDirective heuristicDirective) {
		return new HeuristicAtom(heuristicDirective.getHead(), heuristicDirective.getWeightAtLevel(), heuristicDirective.getSign());
	}
}
