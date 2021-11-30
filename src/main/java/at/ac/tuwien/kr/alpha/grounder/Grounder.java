/*
 * Copyright (c) 2016-2020, the Alpha Team.
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

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.IntIterator;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveValues;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;

public interface Grounder {
	/**
	 * Translates an answer-set represented by true atom IDs into its logical representation.
	 *
	 * @param trueAtoms
	 * @return
	 */
	AnswerSet assignmentToAnswerSet(Iterable<Integer> trueAtoms);

	/**
	 * Applies lazy grounding and returns all newly derived (fully ground) NoGoods.
	 *
	 * @return a mapping of nogood IDs to NoGoods.
	 */
	Map<Integer, NoGood> getNoGoods(Assignment assignment);

	/**
	 * Returns new choice points and their enablers and disablers.
	 * Must be preceded by a call to {@link #getNoGoods(Assignment)}.
	 *
	 * @return a pair (choiceOn, choiceOff) of two maps from atomIds to atomIds,
	 *         choiceOn maps atoms (choice points) to their enabling atoms
	 *         and choiceOff maps atoms (choice points) to their disabling atoms.
	 */
	Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms();

	/**
	 * Updates the grounder with atoms assigned a positive truth value.
	 * @param it an iterator over all newly assigned positive atoms.
	 */
	void updateAssignment(IntIterator it);

	/**
	 * Returns new heuristic atoms and their enablers and disablers.
	 * Must be preceded by a call to {@link #getNoGoods(Assignment)}.
	 * @see #getChoiceAtoms()
	 */
	Pair<Map<Integer, Integer[]>, Map<Integer, Integer[]>> getHeuristicAtoms();

	/**
	 * Returns a set of new mappings from heuristic atoms to {@link HeuristicDirectiveValues}.
	 * Must be preceded by a call to {@link #getNoGoods(Assignment)}.
	 */
	Map<Integer, HeuristicDirectiveValues> getHeuristicValues();

	/**
	 * Returns a set of new mappings from head atoms to {@link RuleAtom}s deriving it.
	 */
	Map<Integer, Set<Integer>> getHeadsToBodies();


	void forgetAssignment(int[] atomIds);

	/**
	 * Registers the given NoGood and returns the identifier of it.
	 *
	 * @param noGood
	 * @return
	 */
	int register(NoGood noGood);

	AtomStore getAtomStore();
}
