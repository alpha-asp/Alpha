/**
 * Copyright (c) 2018 Siemens AG
 * All rights reserved.
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

import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;

import static at.ac.tuwien.kr.alpha.Util.join;

/**
 * Represents a heuristic directive, e.g. {@code #heuristic a : b. [2@1]}
 */
public class HeuristicDirective extends Directive {

	public static final int DEFAULT_WEIGHT = 1;
	public static final int DEFAULT_PRIORITY = 1;
	public static final Term DEFAULT_WEIGHT_TERM = ConstantTerm.getInstance(DEFAULT_WEIGHT);
	public static final Term DEFAULT_PRIORITY_TERM = ConstantTerm.getInstance(DEFAULT_PRIORITY);
	
	private final Literal head;
	private final List<Literal> body;
	private final Term weight;
	private final Term priority;
	
	private HeuristicDirective(Literal head, List<Literal> body, Term weight, Term priority) {
		super();
		this.head = head;
		this.body = body;
		this.weight = weight != null ? weight : DEFAULT_WEIGHT_TERM;
		this.priority = priority != null ? priority : DEFAULT_PRIORITY_TERM;
	}

	public HeuristicDirective(Literal head, List<Literal> body, WeightAtLevel weightAtLevel) {
		this(head, body, weightAtLevel.getWeight(), weightAtLevel.getLevel());
	}

	public Literal getHead() {
		return head;
	}

	public List<Literal> getBody() {
		return body;
	}

	public Term getWeight() {
		return weight;
	}

	public Term getPriority() {
		return priority;
	}
	
	@Override
	public String toString() {
		return join("#heuristic " + head + " : ", body, ". [" + weight + "@" + priority + "]");
	}
	
}
