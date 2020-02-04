/*
 *  Copyright (c) 2020 Siemens AG
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1) Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *
 *  2) Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package at.ac.tuwien.kr.alpha.common.heuristics;

import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Util.oops;

public class HeuristicDirectiveAtom {

	private final Set<ThriceTruth> signs;
	private final BasicAtom atom;

	private HeuristicDirectiveAtom(Set<ThriceTruth> signs, BasicAtom atom) {
		this.signs = Collections.unmodifiableSet(signs);
		this.atom = atom;
	}

	public static HeuristicDirectiveAtom head(ThriceTruth sign, BasicAtom atom) {
		if (sign == ThriceTruth.MBT) {
			throw oops("M sign in heuristic head");
		}
		return new HeuristicDirectiveAtom(Collections.singleton(sign), atom);
	}

	public static HeuristicDirectiveAtom body(Set<ThriceTruth> signs, BasicAtom atom) {
		return new HeuristicDirectiveAtom(signs, atom);
	}

	public Set<ThriceTruth> getSigns() {
		return signs;
	}

	public BasicAtom getAtom() {
		return atom;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		HeuristicDirectiveAtom that = (HeuristicDirectiveAtom) o;
		return signs.equals(that.signs) &&
				atom.equals(that.atom);
	}

	@Override
	public int hashCode() {
		return Objects.hash(signs, atom);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (ThriceTruth truth : ThriceTruth.values()) {
			if (signs.contains(truth)) {
				sb.append(truth);
			}
		}
		if (!signs.isEmpty()) {
			sb.append(" ");
		}
		sb.append(atom);
		return sb.toString();
	}
}
