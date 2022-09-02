/**
 * Copyright (c) 2022, the Alpha Team.
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
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
package at.ac.tuwien.kr.alpha.core.solver.reset;

import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.NoGood;

import java.util.LinkedList;
import java.util.List;

/**
 * A collection for nogoods that allows extraction of nogoods also after changes in atom ids. This is done by
 * converting nogoods to an atomized state based on a specific {@link AtomStore}.
 */
public class AtomizedNoGoodCollection {
	private final AtomStore atomStore;
	private final List<NoGoodAtomizer> atomizedNoGoods;

	/**
	 * Initializes the {@link AtomizedNoGoodCollection} with a specific {@link AtomStore} to use for conversions.
	 *
	 * @param atomStore the {@link AtomStore} to use.
	 */
	public AtomizedNoGoodCollection(AtomStore atomStore) {
		this.atomStore = atomStore;
		this.atomizedNoGoods = new LinkedList<>();
	}

	/**
	 * Adds a nogood to the collection. The nogood is stored in an atomized way, meaning that it does not depend on
	 * the atom ids of an {@link AtomStore}.
	 *
	 * @param noGood the nogood to add.
	 */
	public void add(NoGood noGood) {
		atomizedNoGoods.add(new NoGoodAtomizer(noGood, atomStore));
	}

	/**
	 * Gets all nogoods in the collection with atom ids based on the current {@link AtomStore}.
	 *
	 * @return all nogoods in the collection converted to contain atom ids based on the current {@link AtomStore}.
	 * The list is sorted in the order the nogoods were added to the collection.
	 */
	public List<NoGood> getNoGoods() {
		List<NoGood> noGoods = new LinkedList<>();
		for (NoGoodAtomizer atomizedNoGood : atomizedNoGoods) {
			noGoods.add(atomizedNoGood.deatomize(atomStore));
		}
		return noGoods;
	}
}
