/*
 * Copyright (c) 2016-2018, 2020, the Alpha Team.
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

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.common.terms.ConstantTerm.getInstance;

/**
 * Atoms corresponding to rule bodies use this predicate, first term is rule number,
 * second is a term containing variable substitutions.
 */
public class RuleAtom implements Atom {
	public static final Predicate PREDICATE = Predicate.getInstance("_R_", 2, true, true);

	private final List<Term> terms;
	private final boolean ground;

	private RuleAtom(List<Term> terms, boolean ground) {
		if (terms.size() != 2) {
			throw new IllegalArgumentException();
		}

		this.terms = terms;
		this.ground = ground;
	}

	/**
	 * Constructs a {@link RuleAtom} representing a ground rule.
	 * @param nonGroundRule a rule
	 * @param substitution a substitution that makes the rule ground
	 * @return a rule atom representing the ground rule
	 */
	public static RuleAtom ground(NonGroundRule nonGroundRule, Substitution substitution) {
		return new RuleAtom(Arrays.asList(
			getInstance(Integer.toString(nonGroundRule.getRuleId())),
			getInstance(substitution.toString())
		), true);
	}

	/**
	 * Constructs a {@link RuleAtom} representing a non-ground rule (to be used in {@link at.ac.tuwien.kr.alpha.common.NonGroundNoGood}s, for example).
	 * @param nonGroundRule a rule
	 * @return a rule atom representing the non-ground rule which contains all the variables occurring in the rule
	 */
	public static RuleAtom nonGround(NonGroundRule nonGroundRule) {
		final Set<VariableTerm> occurringVariables = nonGroundRule.getOccurringVariables();
		final List<Term> sortedVariables = new ArrayList<>(occurringVariables);
		Collections.sort(sortedVariables);
		return new RuleAtom(Arrays.asList(
				getInstance(Integer.toString(nonGroundRule.getRuleId())),
				FunctionTerm.getInstance("", sortedVariables)
		), false);
	}

	@Override
	public Predicate getPredicate() {
		return PREDICATE;
	}

	@Override
	public List<Term> getTerms() {
		return Arrays.asList(
			terms.get(0),
			terms.get(1)
		);
	}

	@Override
	public boolean isGround() {
		return ground;
	}
	
	@Override
	public BodyRepresentingLiteral toLiteral(boolean positive) {
		return new BodyRepresentingLiteral(this, positive);
	}

	@Override
	public Atom substitute(Substitution substitution) {
		if (ground) {
			return this;
		} else {
			return new RuleAtom(terms.stream()
					.map(t -> t.substitute(substitution))
					.collect(Collectors.toList()), false);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		RuleAtom that = (RuleAtom) o;

		return terms.equals(that.terms);
	}

	@Override
	public int hashCode() {
		return 31 * PREDICATE.hashCode() + terms.hashCode();
	}

	@Override
	public String toString() {
		return PREDICATE.getName() + "(" + terms.get(0) + "," + terms.get(1) + ')';
	}
}