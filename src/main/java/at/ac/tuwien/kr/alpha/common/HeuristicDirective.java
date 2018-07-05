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

import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;

import static at.ac.tuwien.kr.alpha.Util.join;

/**
 * Represents a heuristic directive, e.g. {@code #heuristic a : b. [2@1]}
 */
public class HeuristicDirective extends Directive {

	public static final boolean DEFAULT_SIGN = true;
	public static final Term DEFAULT_SIGN_TERM = ConstantTerm.getInstance(DEFAULT_SIGN);
	
	private final BasicAtom head;	// TODO: replace by classical literal and drop sign?
	private final List<Literal> body;
	private final WeightAtLevel weightAtLevel;
	private final Term sign;	//Term, not boolean, in case we want to support variable signs
	
	public HeuristicDirective(BasicAtom head, List<Literal> body, WeightAtLevel weightAtLevel, Term sign) {
		super();
		this.head = head;
		this.body = body;
		this.weightAtLevel = weightAtLevel;
		this.sign = sign != null ? sign : DEFAULT_SIGN_TERM;
	}
	
	public HeuristicDirective(BasicAtom head, List<Literal> body, WeightAtLevel weightAtLevel, boolean sign) {
		this(head, body, weightAtLevel, ConstantTerm.getInstance(sign));
	}

	public BasicAtom getHead() {
		return head;
	}

	public List<Literal> getBody() {
		return body;
	}

	public WeightAtLevel getWeightAtLevel() {
		return weightAtLevel;
	}
	
	public Term getSign() {
		return sign;
	}
	
	@Override
	public String toString() {
		return join("#heuristic " + head + " : ", body, ". [" + weightAtLevel + "]");
	}
	
}
