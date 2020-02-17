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
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.Literals;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.NoGoodCreator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationLiteral;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.HeuristicAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import at.ac.tuwien.kr.alpha.grounder.structure.ProgramAnalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.common.Literals.negateLiteral;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Class to generate ground NoGoods out of non-ground rules and grounding substitutions.
 * Copyright (c) 2017-2018, the Alpha Team.
 */
public class NoGoodGenerator {
	private final AtomStore atomStore;
	private final ChoiceRecorder choiceRecorder;
	private final Map<Predicate, LinkedHashSet<Instance>> factsFromProgram;
	private final ProgramAnalysis programAnalysis;
	private final Set<NonGroundRule> uniqueGroundRulePerGroundHead;

	NoGoodGenerator(AtomStore atomStore, ChoiceRecorder recorder, Map<Predicate, LinkedHashSet<Instance>> factsFromProgram, ProgramAnalysis programAnalysis, Set<NonGroundRule> uniqueGroundRulePerGroundHead) {
		this.atomStore = atomStore;
		this.choiceRecorder = recorder;
		this.factsFromProgram = factsFromProgram;
		this.programAnalysis = programAnalysis;
		this.uniqueGroundRulePerGroundHead = uniqueGroundRulePerGroundHead;
	}

	/**
	 * Generates all NoGoods resulting from a non-ground rule and a variable substitution.
	 *
	 * @param nonGroundRule
	 *          the non-ground rule.
	 * @param substitution
	 *          the grounding substitution, i.e., applying substitution to nonGroundRule results in a ground rule.
	 *          Assumption: atoms with fixed interpretation evaluate to true under the substitution.
	 * @return a list of the NoGoods corresponding to the ground rule.
	 */
	Collection<NoGood> generateNoGoodsFromGroundSubstitution(final NonGroundRule nonGroundRule, final Substitution substitution) {
		final List<Integer> posLiterals = collectPosLiterals(nonGroundRule, substitution);
		final List<Integer> negLiterals = collectNegLiterals(nonGroundRule, substitution);

		if (posLiterals == null || negLiterals == null) {
			return emptyList();
		}

		// A constraint is represented by exactly one nogood.
		if (nonGroundRule.isConstraint()) {
			return singletonList(NoGoodCreator.fromConstraint(posLiterals, negLiterals));
		}

		final Atom groundHeadAtom = nonGroundRule.getHeadAtom().substitute(substitution);

		// Prepare atom representing the rule body.
		final RuleAtom bodyAtom = new RuleAtom(nonGroundRule, substitution);

		// Check uniqueness of ground rule by testing whether the
		// body representing atom already has an id.
		if (atomStore.contains(bodyAtom)) {
			// The current ground instance already exists,
			// therefore all nogoods have already been created.
			return emptyList();
		}

		final int bodyRepresentingAtom = atomStore.putIfAbsent(bodyAtom);

		if (groundHeadAtom instanceof HeuristicAtom) {
			return generateNoGoodsForHeuristicRule((HeuristicAtom) groundHeadAtom, bodyRepresentingAtom);
		} else {
			return generateNoGoodsForNonConstraintNonHeuristicRule(nonGroundRule, posLiterals, negLiterals, groundHeadAtom, bodyRepresentingAtom);
		}
	}

	private Collection<NoGood> generateNoGoodsForHeuristicRule(HeuristicAtom groundHeadAtom, int bodyRepresentingAtom) {
		BasicAtom groundHeuristicHead = groundHeadAtom.getHeadAtom().toAtom();
		final int heuristicHeadId = atomStore.putIfAbsent(groundHeuristicHead);

		final List<NoGood> result = new ArrayList<>(choiceRecorder.generateHeuristicNoGoods(groundHeadAtom, bodyRepresentingAtom, heuristicHeadId));

		// if the head of the heuristic directive is assigned, the body of the heuristic rule shall also be assigned s.t. it is not applicable anymore:
		boolean heuristicSign = groundHeadAtom.getHeadSign().toBoolean();
		result.add(NoGoodCreator.headFirstInternal(atomToLiteral(bodyRepresentingAtom, false), atomToLiteral(heuristicHeadId, heuristicSign)));
		result.add(NoGoodCreator.internal(atomToLiteral(bodyRepresentingAtom,  true), atomToLiteral(heuristicHeadId, !heuristicSign)));

		return result;
	}

	private Collection<NoGood> generateNoGoodsForNonConstraintNonHeuristicRule(NonGroundRule nonGroundRule, List<Integer> posLiterals, List<Integer> negLiterals, Atom groundHeadAtom, int bodyRepresentingAtom) {
		final List<NoGood> result = new ArrayList<>();
		final int headId = atomStore.putIfAbsent(groundHeadAtom);
		final int headLiteral = atomToLiteral(headId);
		final int bodyRepresentingLiteral = Literals.atomToLiteral(bodyRepresentingAtom);

		choiceRecorder.addHeadToBody(headId, bodyRepresentingAtom);

		// Create a nogood for the head.
		result.add(NoGoodCreator.headFirst(negateLiteral(headLiteral), bodyRepresentingLiteral));

		final NoGood ruleBody = NoGoodCreator.fromBody(posLiterals, negLiterals, bodyRepresentingLiteral);
		result.add(ruleBody);

		// Nogoods such that the atom representing the body is true iff the body is true.
		for (int j = 1; j < ruleBody.size(); j++) {
			result.add(new NoGood(bodyRepresentingLiteral, negateLiteral(ruleBody.getLiteral(j))));
		}

		// If the rule head is unique, add support.
		if (uniqueGroundRulePerGroundHead.contains(nonGroundRule)) {
			result.add(NoGoodCreator.support(headLiteral, bodyRepresentingLiteral));
		}

		// If the body of the rule contains negation, add choices.
		if (!negLiterals.isEmpty()) {
			result.addAll(choiceRecorder.generateChoiceNoGoods(posLiterals, negLiterals, bodyRepresentingAtom));
		}
		return result;
	}

	List<Integer> collectNegLiterals(final NonGroundRule nonGroundRule, final Substitution substitution) {
		final List<Integer> bodyLiteralsNegative = new ArrayList<>();
		for (Atom atom : nonGroundRule.getBodyAtomsNegative()) {
			Atom groundAtom = atom.substitute(substitution);
			
			final Set<Instance> factInstances = factsFromProgram.get(groundAtom.getPredicate());

			if (factInstances != null && factInstances.contains(new Instance(groundAtom.getTerms()))) {
				// Negative atom that is always true encountered, skip whole rule as it will never fire.
				return null;
			}

			if (!existsRuleWithPredicateInHead(groundAtom.getPredicate())) {
				// Negative atom is no fact and no rule defines it, it is always false, skip it.
				continue;
			}

			bodyLiteralsNegative.add(atomToLiteral(atomStore.putIfAbsent(groundAtom)));
		}
		return bodyLiteralsNegative;
	}

	private List<Integer> collectPosLiterals(final NonGroundRule nonGroundRule, final Substitution substitution) {
		final List<Integer> bodyLiteralsPositive = new ArrayList<>();
		for (Atom atom : nonGroundRule.getBodyAtomsPositive()) {
			if (atom.toLiteral() instanceof FixedInterpretationLiteral) {
				// TODO: conversion of atom to literal is ugly. NonGroundRule could manage atoms instead of literals, cf. FIXME there
				// Atom has fixed interpretation, hence was checked earlier that it
				// evaluates to true under the given substitution.
				// FixedInterpretationAtoms need not be shown to the solver, skip it.
				continue;
			}
			// Skip the special enumeration atom.
			if (atom instanceof EnumerationAtom) {
				continue;
			}

			final Atom groundAtom = atom.substitute(substitution);

			// Consider facts to eliminate ground atoms from the generated nogoods that are always true
			// and eliminate nogoods that are always satisfied due to facts.
			Set<Instance> factInstances = factsFromProgram.get(groundAtom.getPredicate());
			if (factInstances != null && factInstances.contains(new Instance(groundAtom.getTerms()))) {
				// Skip positive atoms that are always true.
				continue;
			}

			if (!existsRuleWithPredicateInHead(groundAtom.getPredicate())) {
				// Atom is no fact and no rule defines it, it cannot be derived (i.e., is always false), skip whole rule as it will never fire.
				return null;
			}

			bodyLiteralsPositive.add(atomToLiteral(atomStore.putIfAbsent(groundAtom)));
		}
		return bodyLiteralsPositive;
	}

	private boolean existsRuleWithPredicateInHead(final Predicate predicate) {
		final HashSet<NonGroundRule> definingRules = programAnalysis.getPredicateDefiningRules().get(predicate);
		return definingRules != null && !definingRules.isEmpty();
	}
}
