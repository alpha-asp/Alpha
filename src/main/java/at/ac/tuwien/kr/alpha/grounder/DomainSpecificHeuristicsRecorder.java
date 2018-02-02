/**
 * Copyright (c) 2018 Siemens AG
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.heuristics.DomainSpecificHeuristicValues;
import at.ac.tuwien.kr.alpha.common.heuristics.NonGroundDomainSpecificHeuristicValues;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.common.heuristics.NonGroundDomainSpecificHeuristicValues.DEFAULT_LEVEL;
import static at.ac.tuwien.kr.alpha.common.heuristics.NonGroundDomainSpecificHeuristicValues.DEFAULT_WEIGHT;

/**
 * Records a mapping between rule atom IDs and their corresponding domain-specific heuristic values
 */
public class DomainSpecificHeuristicsRecorder {

	private Map<Integer, DomainSpecificHeuristicValues> newValues = new HashMap<>();
	private final AtomStore atomStore;
	private final WorkingMemory workingMemory;

	public DomainSpecificHeuristicsRecorder(AtomStore atomStore, WorkingMemory workingMemory) {
		this.atomStore = atomStore;
		this.workingMemory = workingMemory;
	}

	/**
	 * Adds the given mapping between rule body and domain-specific heuristic information
	 * 
	 * @param bodyId
	 *          the ID of a ground {@link RuleAtom}
	 * @param groundHeuristicAtom
	 *          the ground heuristic information taken from the corresponding ground rule
	 */
	public void record(int bodyId, Term weightTerm, Term levelTerm) {
		if (!weightTerm.isGround()) {
			oops("Weight is not ground: " + weightTerm);
		}
		if (!levelTerm.isGround()) {
			oops("Level is not ground: " + levelTerm);
		}
		newValues.put(bodyId, new DomainSpecificHeuristicValues(bodyId, toInt(weightTerm), toInt(levelTerm)));
	}

	/**
	 * Applies a substitution to non-ground heuristic information in a given rule, evaluates the result and records the resulting heuristic values.
	 * 
	 * @param bodyId
	 *          the ID of a ground {@link RuleAtom}
	 * @param nonGroundRule
	 *          the non-ground rule whose heuristic values are to be recorded
	 * @param substitution
	 *          the substitution to apply to non-ground heuristic values and the heuristic generator
	 * @param partialInterpretation
	 *          the latest partial interpretation communicated by the solver
	 */
	public void record(int bodyId, NonGroundRule nonGroundRule, Substitution substitution, Assignment partialInterpretation) {
		Term weightTerm = DEFAULT_WEIGHT;
		Term levelTerm = DEFAULT_LEVEL;
		NonGroundDomainSpecificHeuristicValues heuristicDefinition = nonGroundRule.getHeuristic();
		if (heuristicDefinition != null) {
			List<Literal> generator = substitution.applyTo(heuristicDefinition.getGenerator());
			if (isSatisfiedInPartialInterpretation(generator, partialInterpretation)) {
				weightTerm = substitution.applyTo(heuristicDefinition.getWeight());
				levelTerm = substitution.applyTo(heuristicDefinition.getLevel());
			}
		}
		record(bodyId, weightTerm, levelTerm);
	}

	private boolean isSatisfiedInPartialInterpretation(Collection<Literal> generator, Assignment partialInterpretation) {
		return generator.isEmpty() || generator.stream().noneMatch(l -> isFalseInPartialInterpretation(l, partialInterpretation));
	}

	private boolean isFalseInPartialInterpretation(Literal literal, Assignment partialInterpretation) {
		IndexedInstanceStorage instancesInWorkingMemory = workingMemory.get(literal);
		if (instancesInWorkingMemory != null) {
			return !instancesInWorkingMemory.containsInstance(new Instance(literal.getTerms()));
		}
		Integer integerLiteral = toIntegerLiteral(literal);
		return integerLiteral != null && partialInterpretation.isViolated(integerLiteral);
	}

	private Integer toIntegerLiteral(Literal literal) {
		Integer atomId = atomStore.getAtomId(literal);
		if (atomId != null && literal.isNegated()) {
			atomId *= -1;
		}
		return atomId;
	}

	/**
	 * @param heuristicValueTerm
	 * @return the integer value of heuristicValueTerm if it is an integer-typed ConstantTerm
	 */
	private int toInt(Term heuristicValueTerm) {
		if (!(heuristicValueTerm instanceof ConstantTerm<?>)) {
			oops("Not a constant integer: " + heuristicValueTerm);
		}
		return (Integer) ((ConstantTerm<?>) heuristicValueTerm).getObject();
	}

	/**
	 * Gets the set of mappings not yet retrieved and resets the cache
	 * 
	 * @return a set of new mappings between rule atom IDs and their corresponding domain-specific heuristic values
	 */
	public Map<Integer, DomainSpecificHeuristicValues> getAndReset() {
		Map<Integer, DomainSpecificHeuristicValues> currentValues = newValues;
		newValues = new HashMap<>();
		return currentValues;
	}
}
