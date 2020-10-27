/**
 * Copyright (c) 2017-2018, the Alpha Team.
 * All rights reserved.
 * <p>
 * Additional changes made by Siemens.
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
package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.PredicateImpl;
import at.ac.tuwien.kr.alpha.common.terms.TermImpl;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import org.apache.commons.collections4.SetUtils;

import java.util.List;
import java.util.Set;

/**
 * A potentially negated {@link AtomImpl}
 *
 * Copyright (c) 2017-2018, the Alpha Team.
 */
public abstract class LiteralImpl implements Literal {

	protected final AtomImpl atom;
	protected final boolean positive;

	public LiteralImpl(AtomImpl atom, boolean positive) {
		this.atom = atom;
		this.positive = positive;
	}

	@Override
	public AtomImpl getAtom() {
		return atom;
	}

	@Override
	public boolean isNegated() {
		return !positive;
	}

	@Override
	public abstract LiteralImpl negate();

	public abstract LiteralImpl substitute(Substitution substitution);

	public abstract Set<VariableTerm> getBindingVariables();

	public abstract Set<VariableTerm> getNonBindingVariables();

	/**
	 * Union of {@link #getBindingVariables()} and {@link #getNonBindingVariables()}
	 */
	public Set<VariableTerm> getOccurringVariables() {
		return SetUtils.union(getBindingVariables(), getNonBindingVariables());
	}

	/**
	 * @see AtomImpl#getPredicate()
	 */
	@Override
	public PredicateImpl getPredicate() {
		return atom.getPredicate();
	}

	/**
	 * @see AtomImpl#getTerms()
	 */
	@Override
	public List<? extends TermImpl> getTerms() {
		return atom.getTerms();
	}

	/**
	 * @see AtomImpl#isGround()
	 */
	@Override
	public boolean isGround() {
		return atom.isGround();
	}

	@Override
	public String toString() {
		return (positive ? "" : "not ") + atom.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		LiteralImpl that = (LiteralImpl) o;

		return atom.equals(that.atom) && positive == that.positive;
	}

	@Override
	public int hashCode() {
		return 12 * atom.hashCode() + (positive ? 1 : 0);
	}

}
