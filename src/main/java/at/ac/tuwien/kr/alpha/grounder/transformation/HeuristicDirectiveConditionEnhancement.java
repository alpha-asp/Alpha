/*
 * Copyright (c) 2021-2022 Siemens AG
 * All rights reserved.
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
package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.Directive;
import at.ac.tuwien.kr.alpha.common.HeuristicDirective;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveAtom;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveBody;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.head.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.rule.head.Head;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.grounder.Unification;
import at.ac.tuwien.kr.alpha.grounder.Unifier;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.heuristics.HeuristicsConfiguration;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Enhances conditions of heuristic conditions by:
 * <ul>
 *     <li>creating one copy of each heuristic directive for every rule that can derive its head</li>
 *     <li>copying the body of the head-deriving rule to the condition of the heuristic directive (unifying variables where necessary).</li>
 * </ul>
 * This ensures that every heuristic directive is only applicable if there is a rule that can derive its head.
 */
public class HeuristicDirectiveConditionEnhancement extends ProgramTransformation<InputProgram, InputProgram> {

	private final boolean respectDomspecHeuristics;

	public HeuristicDirectiveConditionEnhancement(HeuristicsConfiguration heuristicsConfiguration) {
		this.respectDomspecHeuristics = heuristicsConfiguration.isRespectDomspecHeuristics();
	}

	public HeuristicDirectiveConditionEnhancement(boolean respectDomspecHeuristics) {
		this.respectDomspecHeuristics = respectDomspecHeuristics;
	}

	@Override
	public InputProgram apply(InputProgram inputProgram) {
		Collection<Directive> heuristicDirectives = inputProgram.getInlineDirectives().getDirectives(InlineDirectives.DIRECTIVE.heuristic);
		if (heuristicDirectives == null) {
			return inputProgram;
		}

		final InputProgram.Builder prgBuilder = InputProgram.builder().addFacts(inputProgram.getFacts()).addRules(inputProgram.getRules());
		final InlineDirectives copiedDirectives = new InlineDirectives();
		copiedDirectives.accumulate(inputProgram.getInlineDirectives());
		final List<Directive> newHeuristicDirectives = new ArrayList<>();

		if (respectDomspecHeuristics) {
			for (Directive directive : copiedDirectives.getDirectives(InlineDirectives.DIRECTIVE.heuristic)) {
				transformAndAdd((HeuristicDirective) directive, newHeuristicDirectives, inputProgram);
			}
			copiedDirectives.replaceDirectives(InlineDirectives.DIRECTIVE.heuristic, newHeuristicDirectives);
		} else {
			copiedDirectives.getDirectives(InlineDirectives.DIRECTIVE.heuristic).clear();
		}
		prgBuilder.addInlineDirectives(copiedDirectives);

		return prgBuilder.build();
	}

	private void transformAndAdd(HeuristicDirective directive, Collection<Directive> newHeuristicDirectives, InputProgram inputProgram) {
		final Atom directiveHeadAtom = directive.getHead().getAtom();
		final Collection<BasicRule> headDerivingRules = new ArrayList<>();
		for (BasicRule rule : inputProgram.getRules()) {
			if (rule.getHead() == null) {
				continue;
			} else if (rule.getHead() instanceof NormalHead) {
				final NormalHead normalRuleHead = (NormalHead) rule.getHead();
				if (directiveHeadAtom.getPredicate().equals(normalRuleHead.getAtom().getPredicate())) {
					headDerivingRules.add(rule);
				}
			} else if (rule.getHead() instanceof DisjunctiveHead) {
				final DisjunctiveHead disjunctiveRuleHead = (DisjunctiveHead) rule.getHead();
				for (Atom ruleHeadAtom : disjunctiveRuleHead.disjunctiveAtoms) {
					if (directiveHeadAtom.getPredicate().equals(ruleHeadAtom.getPredicate())) {
						headDerivingRules.add(rule);
						break;
					}
				}
			} else {
				throw oops(this.getClass().getSimpleName() + " only supports rules with normal or disjunctive heads, all other heads should have been normalized already");
			}
		}
		for (BasicRule headDerivingRule : headDerivingRules) {
			HeuristicDirectiveBody newDirectiveBody = new HeuristicDirectiveBody(
					joinAtoms(directive.getBody().getBodyAtomsPositive(), headDerivingRule.getPositiveBody(), HeuristicDirectiveAtom.SIGNS_T),
					joinAtoms(directive.getBody().getBodyAtomsNegative(), headDerivingRule.getNegativeBody(), HeuristicDirectiveAtom.DEFAULT_BODY_SIGNS));
			newHeuristicDirectives.add(unify(directive, newDirectiveBody, headDerivingRule.getHead()));
		}
	}

	private Collection<HeuristicDirectiveAtom> joinAtoms(Collection<HeuristicDirectiveAtom> existingHeuristicCondition, Collection<Literal> ruleBodyToAdd, Set<ThriceTruth> defaultSignSet) {
		final Set<HeuristicDirectiveAtom> newHeuristicCondition = new HashSet<>(existingHeuristicCondition);
		for (Literal literal : ruleBodyToAdd) {
			final Atom atom = literal.getAtom();
			if (!(atom instanceof BasicAtom || atom instanceof ComparisonAtom || atom instanceof AggregateAtom)) {
				throw new UnsupportedOperationException("Body atom " + atom + " not yet supported by " + this.getClass().getSimpleName());
			}
			final Set<ThriceTruth> signSet = atom instanceof BasicAtom ? defaultSignSet : null;
			newHeuristicCondition.add(HeuristicDirectiveAtom.body(signSet, atom));
		}
		return newHeuristicCondition;
	}

	private HeuristicDirective unify(HeuristicDirective originalDirective, HeuristicDirectiveBody newDirectiveBody, Head ruleHead) {
		final Atom directiveHeadAtom = originalDirective.getHead().getAtom();
		Atom ruleHeadAtom = null;
		if (ruleHead instanceof NormalHead) {
			final NormalHead normalRuleHead = (NormalHead) ruleHead;
			assert directiveHeadAtom.getPredicate().equals(normalRuleHead.getAtom().getPredicate());
			ruleHeadAtom = normalRuleHead.getAtom();
		} else if (ruleHead instanceof DisjunctiveHead) {
			final DisjunctiveHead disjunctiveRuleHead = (DisjunctiveHead) ruleHead;
			for (Atom disjunctiveAtom : disjunctiveRuleHead.disjunctiveAtoms) {
				if (directiveHeadAtom.getPredicate().equals(disjunctiveAtom.getPredicate())) {
					if (ruleHeadAtom != null) {
						throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support disjunctive heads with multiple atoms of the same predicate.");
					}
					ruleHeadAtom = disjunctiveAtom;
				}
			}
		} else {
			throw oops(this.getClass().getSimpleName() + " only supports rules with normal or disjunctive heads, all other heads should have been normalized already");
		}
		// TODO: to be correct, strictly speaking we would have to standardise apart before unification,
		//  because the current implementation cannot handle cases like the following:
		// assign(U+1,d,D) :- elem(d,D), comUnit(U), not gt(d,D,U+1).
		// #heuristic assign(U,T,X) : T comUnit(U), T elem(T,X).
		Unifier unifier = Unification.instantiate(ruleHeadAtom, directiveHeadAtom);
		if (unifier == null) {
			unifier = Unification.instantiate(directiveHeadAtom, ruleHeadAtom);
		}
		if (unifier != null) {
			return new HeuristicDirective(originalDirective.getHead().substitute(unifier), newDirectiveBody.substitute(unifier), originalDirective.getWeightAtLevel().substitute(unifier));
		}
		throw oops("Could not unify heuristic directive with head-deriving rule.");
	}
}
