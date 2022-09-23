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
package at.ac.tuwien.kr.alpha.core.solver.reboot;


import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.core.atoms.Literals;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.NoGood;
import at.ac.tuwien.kr.alpha.core.common.NoGoodInterface.Type;

/**
 * A nogood independent of atom ids provided by an {@link AtomStore}.
 */
public class NoGoodAtomizer {
	private final AtomValuePair[] literals;
	private final Type type;

	/**
	 * Initializes a {@link NoGoodAtomizer} with the same literals as the given {@link NoGood}.
	 * Atom ids are provided by the given {@link AtomStore}.
	 *
	 * @param noGood    the {@link NoGood} to get the list of literals from.
	 * @param atomStore the {@link AtomStore} to get atom ids from.
	 */
	public NoGoodAtomizer(NoGood noGood, AtomStore atomStore) {
		this.literals = new AtomValuePair[noGood.size()];
		this.type = noGood.getType();
		for (int i = 0; i < noGood.size(); i++) {
			int literalId = noGood.getLiteral(i);
			int atomId = Literals.atomOf(literalId);
			Atom atom = atomStore.get(atomId);
			boolean truthValue = Literals.isPositive(literalId);

			this.literals[i] = new AtomValuePair(atom, truthValue);
		}
	}

	/**
	 * Creates a new {@link NoGood} with the same literals as this {@link NoGoodAtomizer}.
	 * Atom ids are provided by the given {@link AtomStore}.
	 *
	 * @param atomStore the {@link AtomStore} to get atom ids from.
	 * @return the newly created {@link NoGood} with the same literals as this {@link NoGoodAtomizer}.
	 */
	public NoGood deatomize(AtomStore atomStore) {
		int[] literals = new int[this.literals.length];
		for (int i = 0; i < this.literals.length; i++) {
			Atom atom = this.literals[i].getAtom();
			atomStore.putIfAbsent(atom);
			int atomId = atomStore.get(atom);
			boolean truthValue = this.literals[i].isPositive();

			literals[i] = Literals.atomToLiteral(atomId, truthValue);
		}

		return new NoGood(type, literals);
	}
}
