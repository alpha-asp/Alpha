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
package at.ac.tuwien.kr.alpha.common.rule.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.AbstractRule;
import at.ac.tuwien.kr.alpha.common.rule.head.Head;
import at.ac.tuwien.kr.alpha.common.rule.head.impl.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Unifier;

/**
 * Represents a non-ground rule or a constraint. A @{link Rule} has a general {@link Head}, meaning both choice heads and disjunctive heads are permissible.
 * This implementation represents a rule after being parsed from a given ASP program, but before being transformed into a @{link NormalRule} Copyright (c)
 * 2016-2019, the Alpha Team.
 */
public class Rule extends AbstractRule<Head> {

	public Rule(Head head, List<Literal> body) {
		super(head, body);
	}

	/**
	 * Returns a new Rule that is equal to this one except that all variables are renamed to have the newVariablePostfix appended.
	 * 
	 * @param newVariablePostfix
	 * @return
	 */
	// TODO this goes into NormalRule!
	public Rule renameVariables(String newVariablePostfix) {
		if (!this.getHead().isNormal()) {
			throw Util.oops("Trying to rename variables in not-normal rule.");
		}
		List<VariableTerm> occurringVariables = new ArrayList<>();
		Atom headAtom = ((DisjunctiveHead) this.getHead()).disjunctiveAtoms.get(0);
		occurringVariables.addAll(headAtom.getOccurringVariables());
		for (Literal literal : this.getBody()) {
			occurringVariables.addAll(literal.getOccurringVariables());
		}
		Unifier variableReplacement = new Unifier();
		for (VariableTerm occurringVariable : occurringVariables) {
			final String newVariableName = occurringVariable.toString() + newVariablePostfix;
			variableReplacement.put(occurringVariable, VariableTerm.getInstance(newVariableName));
		}
		Atom renamedHeadAtom = headAtom.substitute(variableReplacement);
		ArrayList<Literal> renamedBody = new ArrayList<>(this.getBody().size());
		for (Literal literal : this.getBody()) {
			renamedBody.add((Literal) literal.substitute(variableReplacement));
		}
		return new Rule(new DisjunctiveHead(Collections.singletonList(renamedHeadAtom)), renamedBody);
	}

}