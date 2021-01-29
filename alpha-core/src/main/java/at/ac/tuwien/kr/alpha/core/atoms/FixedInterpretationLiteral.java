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
package at.ac.tuwien.kr.alpha.core.atoms;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.core.grounder.SubstitutionImpl;

/**
 * Represents a literal whose ground truth value(s) are independent of the
 * current assignment.
 * Examples of atoms underlying such literals are builtin atoms and external
 * atoms.
 * Copyright (c) 2017-2018, the Alpha Team.
 */
public abstract class FixedInterpretationLiteral extends CoreLiteral {

	public FixedInterpretationLiteral(CoreAtom atom, boolean positive) {
		super(atom, positive);
	}

	/**
	 * Creates a list of {@link SubstitutionImpl}s based on the given partial
	 * substitution, such that
	 * this {@link FixedInterpretationLiteral} is true in every returned
	 * substitution.
	 * In cases where this is not possible (because of conflicting variable
	 * assignments in the partial substitution), getSatisfyingSubstitutions is
	 * required to return an empty list
	 * 
	 * @param partialSubstitution a partial substitution that is required to bind
	 *                            all variables that are non-binding in this literal
	 * @return a list of substitutions, in each of which this literal is true, or an
	 *         empty list if no such substitution exists
	 */
	public abstract List<Substitution> getSatisfyingSubstitutions(Substitution partialSubstitution);
}
