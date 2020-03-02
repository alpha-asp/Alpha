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
package at.ac.tuwien.kr.alpha.common.atoms;

import static at.ac.tuwien.kr.alpha.Util.join;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

public class ExternalAtom implements Atom, VariableNormalizableAtom {

	private final List<Term> input;
	private final List<Term> output;

	protected Predicate predicate;
	protected final PredicateInterpretation interpretation;

	public ExternalAtom(Predicate predicate, PredicateInterpretation interpretation, List<Term> input, List<Term> output) {
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
	
	List<Term> getInput() {
		return Collections.unmodifiableList(input);
	}
	
	List<Term> getOutput() {
		return Collections.unmodifiableList(output);
	}

	@Override
	public List<Term> getTerms() {
		return input;
	}

	@Override
	public boolean isGround() {
		return false;
	}

	@Override
	public ExternalAtom substitute(Substitution substitution) {
		return new ExternalAtom(
			predicate,
			interpretation,
			input
				.stream()
				.map(t -> t.substitute(substitution))
				.collect(Collectors.toList()),
			output
				.stream()
				.map(t -> t.substitute(substitution))
				.collect(Collectors.toList())
		);
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
	public ExternalAtom normalizeVariables(String prefix, int counterStartingValue) {
		List<Term> renamedInput = Term.renameTerms(this.input, prefix + "_IN_", counterStartingValue);
		List<Term> renamedOutput = Term.renameTerms(this.output, prefix + "_OUT_", counterStartingValue);
		return new ExternalAtom(this.predicate, this.interpretation, renamedInput, renamedOutput);
	}

}
