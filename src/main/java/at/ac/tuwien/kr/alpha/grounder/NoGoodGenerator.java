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
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.*;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Class to generate ground NoGoods out of non-ground rules and grounding substitutions.
 * Copyright (c) 2017-2018, the Alpha Team.
 */
public class NoGoodGenerator {
	private final AtomStore store;
	private final ChoiceRecorder choiceRecorder;
	private final Map<Predicate, LinkedHashSet<Instance>> factsFromProgram;
	private final Map<Predicate, HashSet<NonGroundRule>> ruleHeadsToDefiningRules;
	private final Set<NonGroundRule> uniqueGroundRulePerGroundHead;

	NoGoodGenerator(AtomStore store, ChoiceRecorder choiceRecorder, Map<Predicate, LinkedHashSet<Instance>> factsFromProgram,
			Map<Predicate, HashSet<NonGroundRule>> ruleHeadsToDefiningRules, Set<NonGroundRule> uniqueGroundRulePerGroundHead) {
		this.store = store;
		this.choiceRecorder = choiceRecorder;
		this.factsFromProgram = factsFromProgram;
		this.ruleHeadsToDefiningRules = ruleHeadsToDefiningRules;
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
	 * @return the ID of the body atom generated (if one has been generated), and a list of the NoGoods corresponding to the ground rule.
	 */
	Pair<Integer, List<NoGood>> generateNoGoodsFromGroundSubstitution(final NonGroundRule nonGroundRule, final Substitution substitution) {
		final List<Integer> pos = collectPos(nonGroundRule, substitution);
		final List<Integer> neg = collectNeg(nonGroundRule, substitution);
		groundCondition(nonGroundRule, substitution);

		if (pos == null || neg == null) {
			return new ImmutablePair<>(null, emptyList());
		}

		// A constraint is represented by exactly one nogood.
		if (nonGroundRule.isConstraint()) {
			return new ImmutablePair<>(null, singletonList(NoGood.fromConstraint(pos, neg)));
		}

		final List<NoGood> result = new ArrayList<>();

		Integer bodyId = null;
		Atom groundHeadAtom = nonGroundRule.getHeadAtom().substitute(substitution);
		if (groundHeadAtom instanceof HeuristicAtom) {
			BasicAtom groundHeuristicHead = ((HeuristicAtom)groundHeadAtom).getHead().toAtom();
			final int heuristicHeadId = store.add(groundHeuristicHead);
			result.addAll(choiceRecorder.generateHeuristicNoGoods(pos, neg, (HeuristicAtom)groundHeadAtom, heuristicHeadId));
		} else {
			// Prepare atom representing the rule body.
			final RuleAtom bodyAtom = new RuleAtom(nonGroundRule, substitution);
	
			// Check uniqueness of ground rule by testing whether the
			// body representing atom already has an id.
			if (store.contains(bodyAtom)) {
				// The current ground instance already exists,
				// therefore all nogoods have already been created.
				return new ImmutablePair<>(null, emptyList());
			}
	
			bodyId = store.add(bodyAtom);
			final int headId = store.add(groundHeadAtom);
	
			// Create a nogood for the head.
			result.add(NoGood.headFirst(-headId, bodyId));
	
			final NoGood ruleBody = NoGood.fromBody(pos, neg, bodyId);
			result.add(ruleBody);
	
			// Nogoods such that the atom representing the body is true iff the body is true.
			for (int j = 1; j < ruleBody.size(); j++) {
				result.add(new NoGood(bodyId, -ruleBody.getLiteral(j)));
			}
	
			// If the rule head is unique, add support.
			if (uniqueGroundRulePerGroundHead.contains(nonGroundRule)) {
				result.add(NoGood.support(headId, bodyId));
			}
	
			// If the body of the rule contains negation, add choices.
			if (!neg.isEmpty()) {
				result.addAll(choiceRecorder.generateChoiceNoGoods(pos, neg, bodyId));
			}
		}

		return new ImmutablePair<>(bodyId, result);
		// TODO: when heuristic annotations are discarded, we do not need to return bodyId any longer
	}

	List<Integer> collectNeg(final NonGroundRule nonGroundRule, final Substitution substitution) {
		final List<Integer> bodyAtomsNegative = new ArrayList<>();
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

			bodyAtomsNegative.add(store.add(groundAtom));
		}
		return bodyAtomsNegative;
	}

	private List<Integer> collectPos(final NonGroundRule nonGroundRule, final Substitution substitution) {
		final List<Integer> bodyAtomsPositive = new ArrayList<>();
		for (Atom atom : nonGroundRule.getBodyAtomsPositive()) {
			if (atom.toLiteral() instanceof FixedInterpretationLiteral) {
				// TODO: conversion of atom to literal is ugly. NonGroundRule could manage atoms instead of literals, cf. FIXME there
				// Atom has fixed interpretation, hence was checked earlier that it
				// evaluates to true under the given substitution.
				// FixedInterpretationAtoms need not be shown to the solver, skip it.
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

			bodyAtomsPositive.add(store.add(groundAtom));
		}
		return bodyAtomsPositive;
	}

	/**
	 * Adds ground atoms appearing in a heuristic condition to the {@link AtomStore}.
	 * 
	 * Not needed for nogood generation, but without this method atoms might be missing
	 * in the store if they only occur in a heuristic condition (or if they are first encountered in a heuristic condition).
	 */
	private List<Integer> groundCondition(final NonGroundRule nonGroundRule, final Substitution substitution) {
		final List<Integer> conditionAtoms = new ArrayList<>();
		if (nonGroundRule.getHeuristic() == null) {
			return conditionAtoms;
		}
		
		for (Literal literal : nonGroundRule.getHeuristic().getGenerator()) {
			Atom atom = literal.getAtom();
			if (atom.toLiteral() instanceof FixedInterpretationLiteral) { 
				// TODO: conversion of atom to literal is ugly. NonGroundRule could manage atoms instead of literals, cf. FIXME there 
				// Atom has fixed interpretation, hence was checked earlier that it
				// evaluates to true under the given substitution.
				// FixedInterpretationAtoms need not be shown to the solver, skip it.
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

			conditionAtoms.add(store.add(groundAtom));
		}
		return conditionAtoms;
	}

	private boolean existsRuleWithPredicateInHead(final Predicate predicate) {
		final HashSet<NonGroundRule> definingRules = ruleHeadsToDefiningRules.get(predicate);
		return definingRules != null && !definingRules.isEmpty();
	}
}
