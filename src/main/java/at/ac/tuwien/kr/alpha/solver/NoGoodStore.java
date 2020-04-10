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
package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;

/**
 * An interface defining the use of a NoGood store.
 *
 * Copyright (c) 2016-2019, the Alpha Team.
 */
public interface NoGoodStore {
	int LBD_NO_VALUE = -1;

	/**
	 * Adds a nogood with the given id.
	 * @param id the unique identifier of the nogood.
	 * @param noGood the nogood to add.
	 * @param lbd the literals block distance.
	 * @return {@code null} if the noGood was added without conflict, a {@link ConflictCause} describing
	 *         the conflict otherwise.
	 */
	ConflictCause add(int id, NoGood noGood, int lbd);

	/**
	 * Adds a NoGood with no LBD value set.
	 * @param id the unique identifier of the NoGood.
	 * @param noGood the NoGood to add.
	 * @return {@code null} if the noGood was added without conflict, a {@link ConflictCause} describing
* 	 *         the conflict otherwise.
	 */
	ConflictCause add(int id, NoGood noGood);

	/**
	 * Apply weak propagation and strong propagation. Propagation should stop as soon as some nogood is violated.
	 * @return some cause iff a conflict was reached or {@code null} otherwise
	 */
	ConflictCause propagate();

	/**
	 * After a call to {@link #propagate()} this method provides
	 * whether propagation was successful, i.e. at least one new
	 * assignment was inferred.
	 * @return {@code true} iff the last call to {@link #propagate()}
	 *         inferred at least one assignment, {@code false} otherwise.
	 */
	boolean didPropagate();

	void backtrack();

	void growForMaxAtomId(int maxAtomId);

	/**
	 * Tests whether a cleanup of the learned NoGoods database is appropriate and exectutes the cleaning if
	 * necessary.
	 */
	void cleanupLearnedNoGoods();

	NoGoodCounter<Integer> getNoGoodCounter();
}
