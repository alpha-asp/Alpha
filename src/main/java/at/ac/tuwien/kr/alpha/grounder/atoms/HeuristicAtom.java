/*
 * Copyright (c) 2017-2020, the Alpha Team.
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
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveAtom;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveLiteral;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.common.heuristics.HeuristicSignSetUtil.toName;
import static at.ac.tuwien.kr.alpha.common.heuristics.HeuristicSignSetUtil.toSignSet;

/**
 * An internal atom that stores information on domain-specific heuristics.
 *
 * For example, the information from the heuristic directive:
 * 	<code>#heuristic b(1) : T a(0), MT a(1), M a(2), F a(3), not T a(4), not MT a(5), not M a(6), not F a(7). [3@2]</code>
 * is encoded in a heuristic atom in the following form:
 * 	<code>_h(3, 2, true, b(1), condpos(t(a(0)), tm(a(1)), m(a(2)), f(a(3))), condneg(t(a(4)), tm(a(5)), m(a(6)), f(a(7))))</code>
 *
 */
public class HeuristicAtom implements Atom {
	public static final Predicate PREDICATE = Predicate.getInstance("_h", 6, true);
	private static final String FUNCTION_POSITIVE_CONDITION = "condpos";
	private static final String FUNCTION_NEGATIVE_CONDITION = "condneg";
	
	private final WeightAtLevel weightAtLevel;
	private final ThriceTruth headSign;
	private final FunctionTerm headAtom;
	private final FunctionTerm positiveCondition;
	private final FunctionTerm negativeCondition;
	private final boolean ground;

	private HeuristicAtom(WeightAtLevel weightAtLevel, ThriceTruth headSign, FunctionTerm headAtom, FunctionTerm positiveCondition, FunctionTerm negativeCondition) {
		this.weightAtLevel = weightAtLevel;
		this.headSign = headSign;
		this.headAtom = headAtom;
		this.positiveCondition = positiveCondition;
		this.negativeCondition = negativeCondition;
		this.ground = getTerms().stream().allMatch(Term::isGround);
	}

	public WeightAtLevel getWeightAtLevel() {
		return weightAtLevel;
	}

	public ThriceTruth getHeadSign() {
		return headSign;
	}

	public FunctionTerm getHeadAtom() {
		return headAtom;
	}

	@Override
	public Predicate getPredicate() {
		return PREDICATE;
	}

	@Override
	public List<Term> getTerms() {
		return Arrays.asList(
				weightAtLevel.getWeight(),
				weightAtLevel.getLevel(),
				ConstantTerm.getInstance(headSign.toBoolean()),
				headAtom,
				positiveCondition,
				negativeCondition
		);
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
				weightAtLevel.substitute(substitution),
				headSign,
				headAtom.substitute(substitution),
				positiveCondition.substitute(substitution),
				negativeCondition.substitute(substitution)
		);
	}

	public List<HeuristicDirectiveLiteral> getOriginalCondition() {
		final List<HeuristicDirectiveAtom> originalPositiveCondition = getOriginalPositiveCondition();
		final List<HeuristicDirectiveAtom> originalNegativeCondition = getOriginalNegativeCondition();
		final List<HeuristicDirectiveLiteral> originalCondition = new ArrayList<>(originalPositiveCondition.size() + originalNegativeCondition.size());
		for (HeuristicDirectiveAtom posAtom : originalPositiveCondition) {
			originalCondition.add(new HeuristicDirectiveLiteral(posAtom, true));
		}
		for (HeuristicDirectiveAtom negAtom : originalNegativeCondition) {
			originalCondition.add(new HeuristicDirectiveLiteral(negAtom, false));
		}
		return originalCondition;
	}

	public List<HeuristicDirectiveAtom> getOriginalPositiveCondition() {
		return functionTermToCondition(positiveCondition);
	}

	public List<HeuristicDirectiveAtom> getOriginalNegativeCondition() {
		return functionTermToCondition(negativeCondition);
	}

	@Override
	public String toString() {
		return Util.join(PREDICATE.getName() + "(", this.getTerms(), ")");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		HeuristicAtom that = (HeuristicAtom) o;
		return weightAtLevel.equals(that.weightAtLevel) &&
				headSign == that.headSign &&
				headAtom.equals(that.headAtom) &&
				positiveCondition.equals(that.positiveCondition) &&
				negativeCondition.equals(that.negativeCondition);
	}

	@Override
	public int hashCode() {
		return Objects.hash(weightAtLevel, headSign, headAtom, positiveCondition, negativeCondition);
	}

	public static HeuristicAtom fromHeuristicDirective(HeuristicDirective heuristicDirective) {
		return new HeuristicAtom(
				heuristicDirective.getWeightAtLevel(),
				heuristicDirective.getHead().getSigns().iterator().next(),
				heuristicDirective.getHead().getAtom().toFunctionTerm(),
				conditionToFunctionTerm(heuristicDirective.getBody().getBodyAtomsPositive(), FUNCTION_POSITIVE_CONDITION),
				conditionToFunctionTerm(heuristicDirective.getBody().getBodyAtomsNegative(), FUNCTION_NEGATIVE_CONDITION)
		);
	}

	private static FunctionTerm conditionToFunctionTerm(List<HeuristicDirectiveAtom> heuristicDirectiveAtoms, String topLevelFunctionName) {
		final List<Term> terms = new ArrayList<>(heuristicDirectiveAtoms.size());
		for (HeuristicDirectiveAtom heuristicDirectiveAtom : heuristicDirectiveAtoms) {
			final Atom atom = heuristicDirectiveAtom.getAtom();
			String atomFunctionName = toName(heuristicDirectiveAtom.getSigns());
			terms.add(FunctionTerm.getInstance(atomFunctionName, atom.toFunctionTerm()));
		}
		return FunctionTerm.getInstance(topLevelFunctionName, terms);
	}

	private static List<HeuristicDirectiveAtom> functionTermToCondition(FunctionTerm functionTerm) {
		final List<Term> terms = functionTerm.getTerms();
		final List<HeuristicDirectiveAtom> condition = new ArrayList<>(terms.size());
		for (Term term : terms) {
			final FunctionTerm termHeuristicDirectiveAtom = (FunctionTerm) term;
			final Set<ThriceTruth> signSet = toSignSet(termHeuristicDirectiveAtom.getSymbol());
			condition.add(HeuristicDirectiveAtom.body(signSet, ((FunctionTerm)termHeuristicDirectiveAtom.getTerms().get(0)).toAtom()));
		}
		return condition;
	}
}
