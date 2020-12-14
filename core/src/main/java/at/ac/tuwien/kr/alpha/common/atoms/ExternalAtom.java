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
package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.CorePredicate;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.TermImpl;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.Util.join;

public class ExternalAtom extends CoreAtom implements VariableNormalizableAtom {

	private final List<? extends TermImpl> input;
	private final List<? extends TermImpl> output;

	protected final CorePredicate predicate;
	protected final PredicateInterpretation interpretation;

	public ExternalAtom(CorePredicate predicate, PredicateInterpretation interpretation, List<? extends TermImpl> input, List<? extends TermImpl> output) {
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
	public CorePredicate getPredicate() {
		return predicate;
	}

	public PredicateInterpretation getInterpretation() {
		return interpretation;
	}

	public List<? extends TermImpl> getInput() {
		return Collections.unmodifiableList(input);
	}

	public List<? extends TermImpl> getOutput() {
		return Collections.unmodifiableList(output);
	}

	@Override
	public List<? extends TermImpl> getTerms() {
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
		List<? extends TermImpl> substitutedInput = this.input.stream().map(t -> t.substitute(substitution)).collect(Collectors.toList());
		List<? extends TermImpl> substitutedOutput = this.output.stream().map(t -> t.substitute(substitution)).collect(Collectors.toList());
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
			result += join("[", input, "]");
		}
		if (!output.isEmpty()) {
			result += join("(", output, ")");
		}
		return result;
	}

	@Override
	public CoreAtom withTerms(List<TermImpl> terms) {
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
		List<? extends TermImpl> renamedInput = TermImpl.renameTerms(this.input, prefix + "_IN_", counterStartingValue);
		List<? extends TermImpl> renamedOutput = TermImpl.renameTerms(this.output, prefix + "_OUT_", counterStartingValue);
		return new ExternalAtom(this.predicate, this.interpretation, renamedInput, renamedOutput);
	}

}
