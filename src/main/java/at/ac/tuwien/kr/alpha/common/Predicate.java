/*
 * Copyright (c) 2016-2018, 2021, the Alpha Team.
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

import java.util.Objects;

public class Predicate implements Comparable<Predicate> {
	private static final Interner<Predicate> INTERNER = new Interner<>();

	private final String name;
	private final int arity;
	private final boolean internal;
	private final boolean solverInternal;

	protected Predicate(String name, int arity, boolean internal, boolean solverInternal) {
		this.name = name;
		this.arity = arity;
		this.internal = internal;
		this.solverInternal = solverInternal;
	}

	public static Predicate getInstance(String symbol, int arity) {
		return getInstance(symbol, arity, false, false);
	}

	public static Predicate getInstance(String symbol, int arity, boolean internal) {
		return getInstance(symbol, arity, internal, false);
	}

	public static Predicate getInstance(String symbol, int arity, boolean internal, boolean solverInternal) {
		return INTERNER.intern(new Predicate(symbol, arity, internal, solverInternal));
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + arity;
		result = 31 * result + (internal ? 1 : 0);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof Predicate)) {
			return false;
		}

		Predicate predicate = (Predicate) o;

		if (arity != predicate.arity) {
			return false;
		}

		if (internal != predicate.internal) {
			return false;
		}

		if (solverInternal != predicate.solverInternal) {
			return false;
		}

		return Objects.equals(name, predicate.name);
	}

	/**
	 * Marks internal predicates that should not be shown/printed in answer sets.
	 * @return true iff this Predicate should be omitted from answer sets.
	 */
	public boolean isInternal() {
		return internal;
	}

	/**
	 * Marks predicates that are used purely for encoding rules by NoGoods in the solver component. Solver internal
	 * predicates are guaranteed to not occur in any rule bodies and hence are ignored by the grounder.
	 * @return true iff this Predicate is internal to the solver component.
	 */
	public boolean isSolverInternal() {
		return solverInternal;
	}

	@Override
	public int compareTo(Predicate other) {
		int result = getName().compareTo(other.getName());

		if (result != 0) {
			return result;
		}

		return Integer.compare(getArity(), other.getArity());
	}

	public String getName() {
		return name;
	}

	public int getArity() {
		return arity;
	}

	@Override
	public String toString() {
		return name + "/" + arity;
	}
}
