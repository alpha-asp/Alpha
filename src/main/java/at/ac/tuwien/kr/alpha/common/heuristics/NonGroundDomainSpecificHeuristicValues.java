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
import at.ac.tuwien.kr.alpha.common.WeightAtLevel;
import at.ac.tuwien.kr.alpha.common.atoms.HeuristicAtom;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;

/**
 * Holds values defined by a {@link HeuristicAtom} or {@link RuleAnnotation} to steer domain-specific heuristic choice for a single non-ground rule
 *
 */
public class NonGroundDomainSpecificHeuristicValues {

	private static final Term DEFAULT_WEIGHT = ConstantTerm.getInstance(1);
	private static final Term DEFAULT_LEVEL = ConstantTerm.getInstance(1);

	private Term weight;
	private Term level;

	/**
	 * @param weight
	 * @param level
	 */
	public NonGroundDomainSpecificHeuristicValues(Term weight, Term level) {
		this.weight = weight != null ? weight : DEFAULT_WEIGHT;
		this.level = level != null ? level : DEFAULT_LEVEL;
	}

	/**
	 * @return the weight
	 */
	public Term getWeight() {
		return weight;
	}

	/**
	 * @return the level
	 */
	public Term getLevel() {
		return level;
	}

	/**
	 * @param heuristicAtom
	 */
	public static NonGroundDomainSpecificHeuristicValues fromHeuristicAtom(HeuristicAtom heuristicAtom) {
		return new NonGroundDomainSpecificHeuristicValues(heuristicAtom.getWeight(), heuristicAtom.getLevel());
	}

	/**
	 * @param annotation
	 * @return
	 */
	public static NonGroundDomainSpecificHeuristicValues fromRuleAnnotation(RuleAnnotation annotation) {
		WeightAtLevel weightAtLevel = annotation.getWeightAtLevel();
		if (weightAtLevel != null) {
			return new NonGroundDomainSpecificHeuristicValues(weightAtLevel.getWeight(), weightAtLevel.getLevel());
		}
		return null;
	}

	public boolean isGround() {
		return weight.isGround() && level.isGround();
	}

	public Collection<VariableTerm> getOccurringVariables() {
		return CollectionUtils.union(weight.getOccurringVariables(), level.getOccurringVariables());
	}

}
