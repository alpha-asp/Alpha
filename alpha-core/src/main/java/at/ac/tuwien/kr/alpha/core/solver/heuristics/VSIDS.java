/**
 * Copyright (c) 2018-2019 Siemens AG
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
package at.ac.tuwien.kr.alpha.core.solver.heuristics;

import static at.ac.tuwien.kr.alpha.commons.util.Util.arrayGrowthSize;
import static at.ac.tuwien.kr.alpha.core.atoms.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.core.atoms.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.core.atoms.Literals.isPositive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.api.config.BinaryNoGoodPropagationEstimationStrategy;
import at.ac.tuwien.kr.alpha.core.common.Assignment;
import at.ac.tuwien.kr.alpha.core.common.NoGood;
import at.ac.tuwien.kr.alpha.core.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.core.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.core.solver.heuristics.activity.BodyActivityProvider;
import at.ac.tuwien.kr.alpha.core.solver.learning.GroundConflictNoGoodLearner.ConflictAnalysisResult;

/**
 * This implementation is inspired by the VSIDS implementation in <a href="https://github.com/potassco/clasp">clasp</a>.
 * Therefore, for example, decay is not realized by decreasing activity scores with age, but by
 * steadily increasing the increment added to the score of active atoms
 * (such that the activity score of older atoms does not have to be changed later).
 * <p/>
 * The implementation is simplified in some ways, e.g. it is not possible to specify a frequency in which the activity
 * increment is updated (which corresponds to decay), but it is updated after every conflict.
 * <p/>
 * Reference for VSIDS:
 * Moskewicz, Matthew W.; Madigan, Conor F.; Zhao, Ying; Zhang, Lintao; Malik, Sharad (2001):
 * Chaff: engineering an efficient SAT solver.
 * In: Proceedings of the 38th Design Automation Conference. IEEE, pp. 530–535.
 */
public class VSIDS extends AbstractVSIDS {
	protected static final Logger LOGGER = LoggerFactory.getLogger(VSIDS.class);

	protected int[] signBalances = new int[0];
	private int nChoicesTrue;
	private int nChoicesFalse;

	protected VSIDS(Assignment assignment, ChoiceManager choiceManager, HeapOfActiveAtoms heapOfActiveAtoms, BinaryNoGoodPropagationEstimationStrategy momsStrategy) {
		super(assignment, choiceManager, heapOfActiveAtoms, momsStrategy);
	}

	public VSIDS(Assignment assignment, ChoiceManager choiceManager, int decayPeriod, double decayFactor, BinaryNoGoodPropagationEstimationStrategy momsStrategy) {
		this(assignment, choiceManager, new HeapOfActiveAtoms(decayPeriod, decayFactor, choiceManager),  momsStrategy);
	}

	public VSIDS(Assignment assignment, ChoiceManager choiceManager, BinaryNoGoodPropagationEstimationStrategy momsStrategy) {
		this(assignment, choiceManager, DEFAULT_DECAY_PERIOD, DEFAULT_DECAY_FACTOR,  momsStrategy);
	}

	@Override
	protected void incrementActivityResolutionAtom(int resolutionAtom) {
		heapOfActiveAtoms.incrementActivity(resolutionAtom);
	}

	@Override
	protected void incrementActivityLearnedNoGood(int literal) {
		incrementSignCounter(literal);
		heapOfActiveAtoms.incrementActivity(atomOf(literal));
	}

	@Override
	protected int chooseAtom() {
		ingestBufferedNoGoods();
		Integer mostActiveAtom;
		while ((mostActiveAtom = heapOfActiveAtoms.getMostActiveAtom()) != null) {
			if (choiceManager.isActiveChoiceAtom(mostActiveAtom)) {
				return mostActiveAtom;
			}
		}
		return DEFAULT_CHOICE_ATOM;
	}

	/**
	 * Chooses a sign (truth value) to assign to the given atom.
	 * 
	 * To make this decision, sign counters are maintained that reflect how often an atom
	 * occurs positively or negatively in learnt nogoods.
	 * If the sign balance for the given atom is positive, {@code true} will be chosen.
	 * If it is negative, {@code false} will be chosen.
	 * If the sign balance is zero, the default sign is selected, which is {@code true}
	 * iff the atom represents a rule body (which is currently always the case for atoms chosen in Alpha).
	 * 
	 * @param atom
	 *          the chosen atom
	 * @return the truth value to assign to the given atom
	 */
	@Override
	protected boolean chooseSign(int atom) {
		atom = getAtomForChooseSign(atom);

		if (assignment.getTruth(atom) == ThriceTruth.MBT) {
			return true;
		}

		int signBalance = getSignBalance(atom);
		if (LOGGER.isDebugEnabled() && (nChoicesFalse + nChoicesTrue) % 100 == 0) {
			LOGGER.debug("chooseSign stats: f={}, t={}", nChoicesFalse, nChoicesTrue);
			LOGGER.debug("chooseSign stats: signBalance={}", signBalance);
		}

		if (signBalance >= 0) {
			nChoicesTrue++;
			return true;
		} else {
			nChoicesFalse++;
			return false;
		}
	}

	/**
	 * This method just returns {@code atom} by default but can be overridden in subclasses.
	 * @param atom the atom chosen by VSIDS
	 * @return the atom to base the choice of sign upon
	 */
	protected int getAtomForChooseSign(int atom) {
		return atom;
	}

	private void incrementSignCounter(int literal) {
		int atom = atomOf(literal);
		boolean sign = isPositive(literal);
		growForMaxAtomId(atom);
		signBalances[atom] += sign ? 1 : -1;
	}

	@Override
	public void growForMaxAtomId(int maxAtomId) {
		// Grow arrays only if needed.
		if (signBalances.length > maxAtomId) {
			return;
		}
		super.growForMaxAtomId(maxAtomId);
		// Grow to default size, except if bigger array is required due to maxAtomId.
		int newCapacity = arrayGrowthSize(signBalances.length);
		if (newCapacity < maxAtomId + 1) {
			newCapacity = maxAtomId + 1;
		}
		signBalances = Arrays.copyOf(signBalances, newCapacity);
	}

	@Override
	public double getActivity(int literal) {
		return heapOfActiveAtoms.getActivity(literal);
	}

	/**
	 * Returns the sign balance for the given atom in learnt nogoods.
	 * @param atom
	 * @return the number of times the given atom occurrs positively more often than negatively (which may be negative)
	 */
	int getSignBalance(int atom) {
		return signBalances.length > atom ? signBalances[atom] : 0;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
