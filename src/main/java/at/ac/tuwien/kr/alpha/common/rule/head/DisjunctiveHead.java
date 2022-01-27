/*
 * Copyright (c) 2017-2018, 2020, 2022, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.common.rule.head;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import static at.ac.tuwien.kr.alpha.Util.join;

public class DisjunctiveHead extends Head {
	public final List<Atom> disjunctiveAtoms;

	public DisjunctiveHead(List<Atom> disjunctiveAtoms) {
		this.disjunctiveAtoms = disjunctiveAtoms;
		if (disjunctiveAtoms != null && disjunctiveAtoms.size() > 1) {
			throw new UnsupportedOperationException("Disjunction in rule heads is not yet supported");
		}
	}

	@Override
	public String toString() {
		return join("", disjunctiveAtoms, " | ", "");
	}

	public boolean isGround() {
		for (Atom atom : disjunctiveAtoms) {
			if (!atom.isGround()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public DisjunctiveHead substitute(Substitution substitution) {
		return new DisjunctiveHead(substitution.substituteAll(disjunctiveAtoms));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.disjunctiveAtoms == null) ? 0 : this.disjunctiveAtoms.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DisjunctiveHead)) {
			return false;
		}
		DisjunctiveHead other = (DisjunctiveHead) obj;
		if (this.disjunctiveAtoms == null) {
			return other.disjunctiveAtoms == null;
		} else {
			return this.disjunctiveAtoms.equals(other.disjunctiveAtoms);
		}
	}
}
