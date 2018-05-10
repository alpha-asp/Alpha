/**
 * Copyright (c) 2016-2018, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

import static java.lang.Math.abs;

public final class Literals {
	/**
	 * Given a literal, returns the corresponding atom.
	 * @param literal the literal to translate.
	 * @return the corresponding atom.
	 */
	public static int atomOf(int literal) {
		if (literal == 0) {
			throw new IllegalArgumentException("Zero is not a literal (because it cannot be negated).");
		}
		return abs(literal);
	}
	
	public static int fromAtomAndSign(int atom, boolean sign) {
		return sign ? atom : -atom;
	}

	/**
	 * A utility to check if a given literal is negated.
	 * @param literal the literal to check.
	 * @return {@code true} iff the literal is negated, {@code false} otherwise.
	 */
	public static boolean isNegated(int literal) {
		return literal < 0;
	}

	public static boolean isPositive(int literal) {
		return literal > 0;
	}

	/**
	 * Joins the string representations of the given literals, using the separator ", "
	 * 
	 * @param literals
	 * @return a string join of the given literals
	 */
	public static String toString(Iterable<Literal> literals) {
		return StringUtils.join(literals, ", ");
	}

	/**
	 * Removes both positive and negative occurences of {@code atoms} from a collection of {@code literals}
	 * @param atoms
	 * @param literals
	 */
	public static void removeAtomsFromLiterals(Collection<Atom> atoms, Collection<Literal> literals) {
		for (Atom atom : atoms) {
			literals.remove(atom.toLiteral(true));
			literals.remove(atom.toLiteral(false));
		}
	}
}
