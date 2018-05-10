/**
 * Copyright (c) 2018 Siemens AG
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
package at.ac.tuwien.kr.alpha.common.heuristics;

import at.ac.tuwien.kr.alpha.common.RuleAnnotation;
import at.ac.tuwien.kr.alpha.grounder.atoms.HeuristicAtom;

import java.util.Collection;
import java.util.Collections;

/**
 * Holds values defined by a {@link HeuristicAtom} or a {@link RuleAnnotation} to steer domain-specific heuristic choice for a single ground rule
 *
 * @deprecated Use {@link HeuristicDirectiveValues} instead.
 */
@Deprecated
public class DomainSpecificHeuristicValues {

	private int ruleAtomId;
	private int weight;
	private int level;
	private Collection<Integer> conditionLiterals;

	public DomainSpecificHeuristicValues(int bodyId, int weight, int level, Collection<Integer> conditionLiterals) {
		this.ruleAtomId = bodyId;
		this.weight = weight;
		this.level = level;
		this.conditionLiterals = Collections.unmodifiableCollection(conditionLiterals);
	}

	public DomainSpecificHeuristicValues(int atom, int weight, int level) {
		this(atom, weight, level, Collections.emptySet());
	}

	public int getRuleAtomId() {
		return ruleAtomId;
	}

	public int getWeight() {
		return weight;
	}

	public int getLevel() {
		return level;
	}

	public Collection<Integer> getConditionLiterals() {
		return conditionLiterals;
	}

}
