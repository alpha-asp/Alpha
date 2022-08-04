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
package at.ac.tuwien.kr.alpha.core.solver;


import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.core.atoms.Literals;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.NoGood;

/**
 * A nogood independent of atom ids provided by an {@link AtomStore}.
 */
public class AtomizedNoGood {
	private final SimpleLiteral[] literals;

	public AtomizedNoGood(NoGood noGood, AtomStore atomStore) {
		this.literals = new SimpleLiteral[noGood.size()];
		for (int i = 0; i < noGood.size(); i++) {
			int literalId = noGood.getLiteral(i);
			int atomId = Literals.atomOf(literalId);
			Atom atom = atomStore.get(atomId);
			boolean truthValue = Literals.isPositive(literalId);

			this.literals[i] = new SimpleLiteral(atom, truthValue);
		}
	}

	public NoGood deatomize(AtomStore atomStore) {
		int[] literals = new int[this.literals.length];
		for (int i = 0; i < this.literals.length; i++) {
			Atom atom = this.literals[i].getAtom();
			atomStore.putIfAbsent(atom);
			int atomId = atomStore.get(atom);
			boolean truthValue = this.literals[i].isPositive();

			literals[i] = Literals.atomToLiteral(atomId, truthValue);
		}

		return new NoGood(literals);
	}

	private static class SimpleLiteral {
		private final Atom atom;
		private final boolean truthValue;

		public SimpleLiteral(Atom atom, boolean truthValue) {
			this.atom = atom;
			this.truthValue = truthValue;
		}

		public Atom getAtom() {
			return atom;
		}

		public boolean isPositive() {
			return truthValue;
		}
	}
}
