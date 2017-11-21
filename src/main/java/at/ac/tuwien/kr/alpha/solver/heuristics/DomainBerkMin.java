/**
 * Copyright (c) 2016-2017 Siemens AG
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
package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.Literals;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.learning.GroundConflictNoGoodLearner.ConflictAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.solver.Atoms.isAtom;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

/**
 * The BerkMin heuristic, as described in (but adapted for lazy grounding):
 * Goldberg, E.; Novikov, Y. (2002): BerkMin: A fast and robust SAT-solver.
 * In : Design, Automation and Test in Europe Conference and Exhibition, 2002. Proceedings. IEEE, pp. 142-149.
 * 
 * Copyright (c) 2016 Siemens AG
 */
public class DomainBerkMin extends BerkMin{
	private static final Logger LOGGER = LoggerFactory.getLogger(DomainBerkMin.class);

	DomainBerkMin(Assignment assignment, ChoiceManager choiceManager, int decayAge, double decayFactor, Random random, Grounder grounder) {
		super(assignment, choiceManager, decayAge,decayFactor, random, grounder);
	}

	DomainBerkMin(Assignment assignment, ChoiceManager choiceManager, Random random, Grounder grounder) {
		this(assignment, choiceManager, DEFAULT_DECAY_AGE, DEFAULT_DECAY_FACTOR, random, grounder);
	}


	/**
	 * {@inheritDoc}
	 * In BerkMin, the atom to choose on is the most active atom in the current top clause.
	 * Here, we can only consider atoms which are currently active choice points. If we do
	 * not find such an atom in the current top clause, we consider the next undefined
	 * nogood in the stack, then the one after that and so on.
	 */
	@Override
	public int chooseAtom() {
		for (NoGood noGood : getStackOfNoGoods()) {
			if (assignment.isUndefined(noGood)) {
				int mostActiveAtom = getMostActiveChoosableAtom(noGood);
				if (mostActiveAtom != DEFAULT_CHOICE_ATOM) {
					return mostActiveAtom;
				}
			}
		}
		return DEFAULT_CHOICE_ATOM;
	}

	@Override
	protected int getMostActiveChoosableAtom(Stream<Integer> streamOfLiterals) {
		Set<Integer> activeChoices = streamOfLiterals
			.map(Literals::atomOf)
			.filter(this::isUnassigned)
			.filter(choiceManager::isActiveChoiceAtom).collect(Collectors.toSet());
		int maxWeight = activeChoices.stream().map(p -> (Integer)(getTermValue(p,3)))
			.max(Comparator.naturalOrder()).orElse(1);
		Integer atom = activeChoices.stream().max(Comparator.comparingDouble(p -> getActivity(p) +
			getTermIntValue(p, 2) + maxWeight * getTermIntValue(p, 3)))
			.orElse(DEFAULT_CHOICE_ATOM);
		return atom;


	}

	private Object getTermValue(int literal, int termIndex){
		return ((ConstantTerm)getGrounder().getAtomStore().get(atomOf(literal)).getTerms()
			.get(termIndex)).getObject();
	}

	private int getTermIntValue(int literal, int termIndex){
		return (Integer)getTermValue(literal, termIndex);
	}
}
