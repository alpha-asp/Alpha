/**
 * Copyright (c) 2017-2019, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.core.atoms;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import at.ac.tuwien.kr.alpha.api.Util;
import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.program.Atom;
import at.ac.tuwien.kr.alpha.api.program.Predicate;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Terms;

public class ExternalAtom extends CoreAtom implements VariableNormalizableAtom {

	private final List<Term> input;
	private final List<Term> output;

	protected final Predicate predicate;
	protected final PredicateInterpretation interpretation;

	public ExternalAtom(Predicate predicate, PredicateInterpretation interpretation, List<Term> input, List<Term> output) {
		if (predicate == null) {
			throw new IllegalArgumentException("predicate must not be null!");
		}
		if (interpretation == null) {
			throw new IllegalArgumentException("interpretation must not be null!");
		}
		if (input == null) {
			throw new IllegalArgumentException("input must not be null!");
		}
		if (output == null) {
			throw new IllegalArgumentException("output must not be null!");
		}
		this.predicate = predicate;
		this.interpretation = interpretation;
		this.input = input;
		this.output = output;
	}

	public boolean hasOutput() {
		return !output.isEmpty();
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	public PredicateInterpretation getInterpretation() {
		return interpretation;
	}

	public List<Term> getInput() {
		return Collections.unmodifiableList(input);
	}

	public List<Term> getOutput() {
		return Collections.unmodifiableList(output);
	}

	@Override
	public List<Term> getTerms() {
		return input;
	}

	@Override
	public boolean isGround() {
		for (Term t : input) {
			if (!t.isGround()) {
				return false;
			}
		}
		for (Term t : output) {
			if (!t.isGround()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ExternalAtom substitute(Substitution substitution) {
		List<Term> substitutedInput = this.input.stream().map(t -> t.substitute(substitution)).collect(Collectors.toList());
		List<Term> substitutedOutput = this.output.stream().map(t -> t.substitute(substitution)).collect(Collectors.toList());
		return new ExternalAtom(predicate, interpretation, substitutedInput, substitutedOutput);
	}

	@Override
	public ExternalLiteral toLiteral(boolean positive) {
		return new ExternalLiteral(this, positive);
	}

	@Override
	public String toString() {
		String result = "&" + predicate.getName();
		if (!input.isEmpty()) {
			result += Util.join("[", input, "]");
		}
		if (!output.isEmpty()) {
			result += Util.join("(", output, ")");
		}
		return result;
	}

	@Override
	public Atom withTerms(List<Term> terms) {
		throw new UnsupportedOperationException("Editing term list is not supported for external atoms!");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.input.hashCode();
		result = prime * result + this.output.hashCode();
		result = prime * result + this.predicate.hashCode();
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
		if (!(obj instanceof ExternalAtom)) {
			return false;
		}
		ExternalAtom other = (ExternalAtom) obj;
		if (!this.input.equals(other.input)) {
			return false;
		}
		if (!this.output.equals(other.output)) {
			return false;
		}
		if (!this.predicate.equals(other.predicate)) {
			return false;
		}
		return true;
	}

	@Override
	public ExternalAtom normalizeVariables(String prefix, int counterStartingValue) {
		List<Term> renamedInput = Terms.renameTerms(this.input, prefix + "_IN_", counterStartingValue);
		List<Term> renamedOutput = Terms.renameTerms(this.output, prefix + "_OUT_", counterStartingValue);
		return new ExternalAtom(this.predicate, this.interpretation, renamedInput, renamedOutput);
	}

}
