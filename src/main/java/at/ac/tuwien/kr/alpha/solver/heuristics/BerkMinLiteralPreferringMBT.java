/**
 * Copyright (c) 2017 Siemens AG
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
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

import java.util.Random;
import java.util.stream.Stream;

/**
 * A variant of {@link BerkMinLiteral} preferring to assign true to choice points already assigned {@link at.ac.tuwien.kr.alpha.solver.ThriceTruth#MBT}.
 */
public class BerkMinLiteralPreferringMBT extends BerkMinLiteral {

	BerkMinLiteralPreferringMBT(Assignment assignment, ChoiceManager choiceManager, int decayAge, double decayFactor, Random random, int queueSize) {
		super(assignment, choiceManager, decayAge, decayFactor, random, queueSize);
	}

	BerkMinLiteralPreferringMBT(Assignment assignment, ChoiceManager choiceManager, Random random) {
		super(assignment, choiceManager, random);
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
		Stream<Integer> literalsToConsider;
		if (streamMbtAtoms().findAny().isPresent()) {
			literalsToConsider = streamMbtAtoms();
		} else {
			literalsToConsider = activeLiterals.stream();
		}

		return getMostActiveChoosableAtom(literalsToConsider);
	}

	private Stream<Integer> streamMbtAtoms() {
		return activeLiterals.stream().map(Literals::atomOf).filter(a -> assignment.getTruth(a) == ThriceTruth.MBT);
	}

	@Override
	public boolean chooseSign(int atom) {
		if (assignment.getTruth(atom) == ThriceTruth.MBT) {
			return true;
		}
		return super.chooseSign(atom);
	}
}
