/*
 * Copyright (c) 2017-2018, 2020, the Alpha Team.
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
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.NonGroundNoGood;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import at.ac.tuwien.kr.alpha.grounder.structure.ProgramAnalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.common.Literals.negateLiteral;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Class to generate ground NoGoods out of non-ground rules and grounding substitutions.
 * Copyright (c) 2017-2018, 2020, the Alpha Team.
 */
public class NoGoodGenerator {
	private final AtomStore atomStore;
	private final ChoiceRecorder choiceRecorder;
	private final Map<Predicate, LinkedHashSet<Instance>> factsFromProgram;
	private final ProgramAnalysis programAnalysis;
	private final Set<NonGroundRule> uniqueGroundRulePerGroundHead;

	private final boolean conflictGeneralisationEnabled = true; // TODO: make parameterisable

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
	List<NoGood> generateNoGoodsFromGroundSubstitution(final NonGroundRule nonGroundRule, final Substitution substitution) {
		final CollectedLiterals posLiterals = collectPosLiterals(nonGroundRule, substitution);
		final CollectedLiterals negLiterals = collectNegLiterals(nonGroundRule, substitution);

		if (posLiterals == null || negLiterals == null) {
			return emptyList();
		}

		final Map<Integer, Atom> mapGroundToNonGroundAtoms;
		if (conflictGeneralisationEnabled) {
			mapGroundToNonGroundAtoms = new HashMap<>();
			mapGroundToNonGroundAtoms.putAll(posLiterals.getAtomMapping());
			mapGroundToNonGroundAtoms.putAll(negLiterals.getAtomMapping());
		}

		// A constraint is represented by exactly one nogood.
		if (nonGroundRule.isConstraint()) {
			final NoGood ngConstraint = NoGood.fromConstraint(posLiterals.collectedGroundLiterals, negLiterals.collectedGroundLiterals);
			if (conflictGeneralisationEnabled) {
				ngConstraint.setNonGroundNoGood(NonGroundNoGood.fromBody(ngConstraint, posLiterals, negLiterals, mapGroundToNonGroundAtoms));
			}
			return singletonList(ngConstraint);
		}

		final List<NoGood> result = new ArrayList<>();

		final Atom groundHeadAtom = nonGroundRule.getHeadAtom().substitute(substitution);
		final int headId = atomStore.putIfAbsent(groundHeadAtom);
		
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
		final int bodyRepresentingLiteral = atomToLiteral(bodyRepresentingAtom);
		final int headLiteral = atomToLiteral(atomStore.putIfAbsent(nonGroundRule.getHeadAtom().substitute(substitution)));

		if (conflictGeneralisationEnabled) {
			mapGroundToNonGroundAtoms.put(headId, nonGroundRule.getHeadAtom());
			mapGroundToNonGroundAtoms.put(bodyRepresentingAtom, nonGroundRule.getNonGroundRuleAtom());
		}

		choiceRecorder.addHeadToBody(headId, atomOf(bodyRepresentingLiteral));
		
		// Create a nogood for the head.
		final NoGood ngHead = NoGood.headFirst(negateLiteral(headLiteral), bodyRepresentingLiteral);
		result.add(ngHead);
		if (conflictGeneralisationEnabled) {
			ngHead.setNonGroundNoGood(NonGroundNoGood.forGroundNoGood(ngHead, mapGroundToNonGroundAtoms));
		}

		final NoGood ngWholeBody = NoGood.fromBody(posLiterals.collectedGroundLiterals, negLiterals.collectedGroundLiterals, bodyRepresentingLiteral);
		if (conflictGeneralisationEnabled) {
			ngWholeBody.setNonGroundNoGood(NonGroundNoGood.fromBody(ngWholeBody, posLiterals, negLiterals, mapGroundToNonGroundAtoms));
		}
		result.add(ngWholeBody);

		// Nogoods such that the atom representing the body is true iff the body is true.
		for (int j = 1; j < ngWholeBody.size(); j++) {
			final NoGood ngOneBodyLiteral = new NoGood(bodyRepresentingLiteral, negateLiteral(ngWholeBody.getLiteral(j)));
			result.add(ngOneBodyLiteral);
			if (conflictGeneralisationEnabled) {
				ngOneBodyLiteral.setNonGroundNoGood(NonGroundNoGood.forGroundNoGood(ngOneBodyLiteral, mapGroundToNonGroundAtoms));
			}
		}

		// If the rule head is unique, add support.
		if (uniqueGroundRulePerGroundHead.contains(nonGroundRule)) {
			final NoGood ngSupport = NoGood.support(headLiteral, bodyRepresentingLiteral);
			if (conflictGeneralisationEnabled) {
				ngSupport.setNonGroundNoGood(NonGroundNoGood.forGroundNoGood(ngSupport, mapGroundToNonGroundAtoms));
			}
			result.add(ngSupport);
		}

		// If the body of the rule contains negation, add choices.
		if (!negLiterals.collectedGroundLiterals.isEmpty()) {
			result.addAll(choiceRecorder.generateChoiceNoGoods(posLiterals.collectedGroundLiterals, negLiterals.collectedGroundLiterals, bodyRepresentingLiteral));
		}

		return result;
	}

	CollectedLiterals collectNegLiterals(final NonGroundRule nonGroundRule, final Substitution substitution) {
		final CollectedLiterals collectedLiterals = new CollectedLiterals();
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

			collectedLiterals.collectedGroundLiterals.add(atomToLiteral(atomStore.putIfAbsent(groundAtom)));
			collectedLiterals.correspondingNonGroundLiterals.add(atom.toLiteral());
		}
		return collectedLiterals;
	}

	private CollectedLiterals collectPosLiterals(final NonGroundRule nonGroundRule, final Substitution substitution) {
		final CollectedLiterals collectedLiterals = new CollectedLiterals();
		for (Atom atom : nonGroundRule.getBodyAtomsPositive()) {
			final Literal literal = atom.toLiteral();
			if (literal instanceof FixedInterpretationLiteral) {
				// TODO: conversion of atom to literal is ugly. NonGroundRule could manage atoms instead of literals, cf. FIXME there
				// Atom has fixed interpretation, hence was checked earlier that it
				// evaluates to true under the given substitution.
				// FixedInterpretationAtoms need not be shown to the solver, skip it.
				collectedLiterals.skippedFixedInterpretationLiterals.add(literal);
				continue;
			}
			// Skip the special enumeration atom.
			if (atom instanceof EnumerationAtom) {
				// TODO: ???
				continue;
			}

			final Atom groundAtom = atom.substitute(substitution);

			// Consider facts to eliminate ground atoms from the generated nogoods that are always true
			// and eliminate nogoods that are always satisfied due to facts.
			Set<Instance> factInstances = factsFromProgram.get(groundAtom.getPredicate());
			if (factInstances != null && factInstances.contains(new Instance(groundAtom.getTerms()))) {
				// Skip positive atoms that are always true.
				collectedLiterals.skippedFacts.add(literal);
				continue;
			}

			if (!existsRuleWithPredicateInHead(groundAtom.getPredicate())) {
				// Atom is no fact and no rule defines it, it cannot be derived (i.e., is always false), skip whole rule as it will never fire.
				return null;
			}

			collectedLiterals.collectedGroundLiterals.add(atomToLiteral(atomStore.putIfAbsent(groundAtom)));
			collectedLiterals.correspondingNonGroundLiterals.add(literal);
		}
		return collectedLiterals;
	}

	private boolean existsRuleWithPredicateInHead(final Predicate predicate) {
		final HashSet<NonGroundRule> definingRules = programAnalysis.getPredicateDefiningRules().get(predicate);
		return definingRules != null && !definingRules.isEmpty();
	}

	public static class CollectedLiterals {
		private final List<Integer> collectedGroundLiterals = new ArrayList<>();
		private final List<Literal> correspondingNonGroundLiterals = new ArrayList<>();
		private final List<Literal> skippedFixedInterpretationLiterals = new ArrayList<>();
		private final List<Literal> skippedFacts = new ArrayList<>();

		public List<Integer> getCollectedGroundLiterals() {
			return Collections.unmodifiableList(collectedGroundLiterals);
		}

		public List<Literal> getCorrespondingNonGroundLiterals() {
			return Collections.unmodifiableList(correspondingNonGroundLiterals);
		}

		public List<Literal> getSkippedFixedInterpretationLiterals() {
			return Collections.unmodifiableList(skippedFixedInterpretationLiterals);
		}

		public List<Literal> getSkippedFacts() {
			return Collections.unmodifiableList(skippedFacts);
		}

		public Map<Integer, Atom> getAtomMapping() {
			final Map<Integer, Atom> atomMapping = new HashMap<>();
			for (int i = 0; i < collectedGroundLiterals.size(); i++) {
				final int groundAtom = atomOf(collectedGroundLiterals.get(i));
				final Atom nonGroundAtom = correspondingNonGroundLiterals.get(i).getAtom();
				atomMapping.put(groundAtom, nonGroundAtom);
			}
			return atomMapping;
		}
	}
}
