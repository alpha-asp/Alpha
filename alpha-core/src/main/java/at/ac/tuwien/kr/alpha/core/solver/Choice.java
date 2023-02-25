/**
 * Copyright (c) 2017-2018, the Alpha Team.
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

import static at.ac.tuwien.kr.alpha.core.programs.atoms.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.core.programs.atoms.Literals.isPositive;

class Choice {
	private final int atom;
	private final boolean truthValue;
	private final boolean backtracked;

	Choice(int atom, boolean truthValue, boolean backtracked) {
		this.atom = atom;
		this.truthValue = truthValue;
		this.backtracked = backtracked;
	}

	Choice(int literal, boolean backtracked) {
		this(atomOf(literal), isPositive(literal), backtracked);
	}

	/**
	 * Returns the inverse choice to the given one.
	 * @param choice the choice to invert.
	 * @return a new Choice representing the inverse of the given one, or null if the given choice was already backtracked.
	 */
	public static Choice getInverted(Choice choice) {
		if (choice.isBacktracked()) {
			return null;
		}
		return new Choice(choice.getAtom(), !choice.getTruthValue(), true);
	}

	public int getAtom() {
		return atom;
	}

	public boolean getTruthValue() {
		return truthValue;
	}

	public boolean isBacktracked() {
		return backtracked;
	}

	@Override
	public String toString() {
		return atom + "=" + (truthValue ? "TRUE" : "FALSE");
	}
}
