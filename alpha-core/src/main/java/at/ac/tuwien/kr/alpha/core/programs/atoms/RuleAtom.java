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
package at.ac.tuwien.kr.alpha.core.programs.atoms;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.api.programs.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import at.ac.tuwien.kr.alpha.core.programs.rules.CompiledRule;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.commons.util.Util.oops;

/**
 * Atoms corresponding to rule bodies use this predicate, its only term is a Java object linking to the non-ground rule and grounding substitution.
 */
public class RuleAtom implements Atom {
	public static final Predicate PREDICATE = Predicates.getPredicate("_R_", 1, true, true);

	private final List<ConstantTerm<RuleAtomData>> terms;

	public static class RuleAtomData implements Comparable<RuleAtomData> {
		private final CompiledRule nonGroundRule;
		private final Substitution substitution;
		private final int substitutionHash;

		RuleAtomData(CompiledRule nonGroundRule, Substitution substitution) {
			this.nonGroundRule = nonGroundRule;
			this.substitution = substitution;
			this.substitutionHash = substitution.hashCode();
		}

		@Override
		public int compareTo(RuleAtomData other) {
			if (nonGroundRule.getRuleId() != other.nonGroundRule.getRuleId()) {
				return Integer.compare(nonGroundRule.getRuleId(), other.nonGroundRule.getRuleId());
			}
			if (substitution.getSubstitution().size() != other.getSubstitution().getSubstitution().size()) {
				throw oops("RuleAtoms over the same rule have different-sized substitutions.");
			}
			// Note: Since Substitutions are backed by TreeMaps, their variables can be iterated in the same order.
			Iterator<Map.Entry<VariableTerm, Term>> iteratorThis = substitution.getSubstitution().entrySet().iterator();
			Iterator<Map.Entry<VariableTerm, Term>> iteratorOther = other.getSubstitution().getSubstitution().entrySet().iterator();
			while (iteratorThis.hasNext()) {
				Map.Entry<VariableTerm, Term> thisNextEntry = iteratorThis.next();
				Map.Entry<VariableTerm, Term> otherNextEntry = iteratorOther.next();
				VariableTerm thisVariable = thisNextEntry.getKey();
				VariableTerm otherVariable = otherNextEntry.getKey();
				if (thisVariable != otherVariable) {
					throw oops("Comparing substitutions for the same non-ground rule whose variables differ: " + thisVariable + " != " + otherVariable);
				}
				int compare = thisNextEntry.getValue().compareTo(otherNextEntry.getValue());
				if (compare != 0) {
					return compare;
				}
			}
			return 0;
		}

		public CompiledRule getNonGroundRule() {
			return nonGroundRule;
		}

		public Substitution getSubstitution() {
			return substitution;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			RuleAtomData that = (RuleAtomData) o;
			if (!nonGroundRule.equals(that.nonGroundRule) || substitutionHash != that.substitutionHash) {
				return false;
			}
			return getSubstitution().equals(that.getSubstitution());
		}

		@Override
		public int hashCode() {
			return 31 * getNonGroundRule().hashCode() + substitutionHash;
		}

		@Override
		public String toString() {
			return "ruleId=" + nonGroundRule.getRuleId() + ":substitution=" + substitution.toString();
		}
	}

	public RuleAtom(CompiledRule nonGroundRule, Substitution substitution) {
		this.terms = Collections.singletonList(Terms.newConstant(new RuleAtomData(nonGroundRule, substitution)));
	}

	@Override
	public Predicate getPredicate() {
		return PREDICATE;
	}

	@Override
	public List<Term> getTerms() {
		return Collections.singletonList(terms.get(0));
	}

	@Override
	public boolean isGround() {
		// NOTE: single term is a ConstantTerm, which is ground by definition.
		return true;
	}

	@Override
	public Literal toLiteral(boolean positive) {
		throw new UnsupportedOperationException("RuleAtom cannot be literalized");
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return this;
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
		return PREDICATE.getName() + "(" + terms.get(0) + ')';
	}

	@Override
	public Atom withTerms(List<Term> terms) {
		throw new UnsupportedOperationException("RuleAtoms do not support setting of terms!");
	}

	@Override
	public Set<VariableTerm> getOccurringVariables() {
		// RuleAtom has 2 terms which are both constants
		return Collections.emptySet();
	}

	@Override
	public Atom renameVariables(String newVariablePrefix) {
		throw new UnsupportedOperationException("RuleAtom does not have any variables to rename!");
	}
}
