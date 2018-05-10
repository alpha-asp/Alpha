/**
 * Copyright (c) 2018 Siemens AG
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
package at.ac.tuwien.kr.alpha.grounder.heuristics.domspec;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.heuristics.DomainSpecificHeuristicValues;
import at.ac.tuwien.kr.alpha.common.heuristics.NonGroundDomainSpecificHeuristicValues;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.AtomStore;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.common.heuristics.NonGroundDomainSpecificHeuristicValues.*;

/**
 * Records a mapping between rule atom IDs and their corresponding domain-specific heuristic values
 */
public class DomainSpecificHeuristicsRecorder {
	private static final Logger LOGGER = LoggerFactory.getLogger(DomainSpecificHeuristicsRecorder.class);

	private Map<Integer, DomainSpecificHeuristicValues> newValues = new HashMap<>();
	private final AtomStore atomStore;

	public DomainSpecificHeuristicsRecorder(AtomStore atomStore) {
		this.atomStore = atomStore;
	}

	/**
	 * Adds the given mapping between rule body and domain-specific heuristic information.
	 * 
	 * When the heuristic information is evaluated later, the given {@code weightTerm} and {@code levelTerm} shall only be used if all literals in
	 * {@code preconditions} are true or MBT in the current partial assignment, otherwise the value 1 shall be used for both weight and level.
	 * 
	 * @param bodyId
	 *          the ID of a ground {@link RuleAtom}
	 * @param weightTerm
	 *          the ground term specifying the heuristic weight value
	 * @param levelTerm
	 *          the ground term specifying the heuristic level value
	 * @param preconditions
	 *          the set of literals that must be true for the given heuristics to apply
	 */
	private void record(int bodyId, Term weightTerm, Term levelTerm, Collection<Literal> preconditions) {
		if (!weightTerm.isGround()) {
			oops("Weight is not ground: " + weightTerm);
		}
		if (!levelTerm.isGround()) {
			oops("Level is not ground: " + levelTerm);
		}
		DomainSpecificHeuristicValues domainSpecificHeuristicValues;
		try {
			domainSpecificHeuristicValues = new DomainSpecificHeuristicValues(bodyId, toInt(weightTerm), toInt(levelTerm), toIntegerLiterals(preconditions));
		} catch (UnsatisfiableHeuristicConditionException e) {
			domainSpecificHeuristicValues = new DomainSpecificHeuristicValues(bodyId, DEFAULT_WEIGHT, DEFAULT_LEVEL);
		}
		newValues.put(bodyId, domainSpecificHeuristicValues);
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
	 */
	public void record(int bodyId, NonGroundRule nonGroundRule, Substitution substitution) {
		Term weightTerm;
		Term levelTerm;
		Collection<Literal> condition;

		NonGroundDomainSpecificHeuristicValues heuristicDefinition = nonGroundRule.getHeuristic();
		if (heuristicDefinition != null) {
			condition = substitution.applyTo(heuristicDefinition.getGenerator());
			weightTerm = substitution.applyTo(heuristicDefinition.getWeight());
			levelTerm = substitution.applyTo(heuristicDefinition.getLevel());
		} else {
			weightTerm = DEFAULT_WEIGHT_TERM;
			levelTerm = DEFAULT_LEVEL_TERM;
			condition = Collections.emptySet();
		}
		record(bodyId, weightTerm, levelTerm, condition);
		LOGGER.debug("Recorded heuristic values {}@{} for rule {} with substitution {}", weightTerm, levelTerm, nonGroundRule.getRuleId(), substitution);
	}

	private Integer toIntegerLiteral(Literal literal) {
		Integer atomId = atomStore.getAtomId(literal.getAtom());
		if (atomId != null && literal.isNegated()) {
			atomId *= -1;
		}
		return atomId;
	}

	private Collection<Integer> toIntegerLiterals(Collection<? extends Literal> literals) throws UnsatisfiableHeuristicConditionException {
		Collection<Integer> literalIds = new ArrayList<>(literals.size());
		for (Literal literal : literals) {
			Integer literalId = toIntegerLiteral(literal);
			if (literalId == null) {
				// assume the literal is a fact which is always true (TODO: is this assumption correct?)
				if (literal.isNegated()) {
					throw new UnsatisfiableHeuristicConditionException("Heuristic generator contains literal that is always false: " + literal);
				}
			} else {
				literalIds.add(literalId);
			}
		}
		return literalIds;
	}

	/**
	 * @param term
	 * @return the integer value of {@code term} if it is an integer-typed ConstantTerm
	 */
	private int toInt(Term term) {
		if (!(term instanceof ConstantTerm<?>)) {
			oops("Not a constant integer: " + term);
		}
		return (Integer) ((ConstantTerm<?>) term).getObject();
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
