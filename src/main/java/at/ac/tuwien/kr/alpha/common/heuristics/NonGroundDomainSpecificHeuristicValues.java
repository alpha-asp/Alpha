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
package at.ac.tuwien.kr.alpha.common.heuristics;

import at.ac.tuwien.kr.alpha.common.Literals;
import at.ac.tuwien.kr.alpha.common.RuleAnnotation;
import at.ac.tuwien.kr.alpha.common.WeightAtLevel;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.atoms.HeuristicAtom;

import java.util.Collections;
import java.util.List;

/**
 * Holds values defined by a {@link HeuristicAtom} or {@link RuleAnnotation} to steer domain-specific heuristic choice for a single non-ground rule
 *
 */
public class NonGroundDomainSpecificHeuristicValues {

	public static final Term DEFAULT_WEIGHT_TERM = ConstantTerm.getInstance(1);
	public static final Term DEFAULT_LEVEL_TERM = ConstantTerm.getInstance(1);
	public static final int DEFAULT_WEIGHT = 1;
	public static final int DEFAULT_LEVEL = 1;

	private final Term weight;
	private final Term level;
	private final List<Literal> generator;

	public NonGroundDomainSpecificHeuristicValues(Term weight, Term level) {
		this(weight, level, Collections.emptyList());
	}

	/**
	 * @param weight
	 * @param level
	 * @param generator
	 */
	public NonGroundDomainSpecificHeuristicValues(Term weight, Term level, List<Literal> generator) {
		this.weight = weight != null ? weight : DEFAULT_WEIGHT_TERM;
		this.level = level != null ? level : DEFAULT_LEVEL_TERM;
		this.generator = generator;
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
	 * @return the generator
	 */
	public List<Literal> getGenerator() {
		return generator;
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
			return new NonGroundDomainSpecificHeuristicValues(weightAtLevel.getWeight(), weightAtLevel.getLevel(), annotation.getGenerator());
		}
		return null;
	}

	public RuleAnnotation toRuleAnnotation() {
		return new RuleAnnotation(new WeightAtLevel(weight, level), generator);
	}

	public boolean isGround() {
		return weight.isGround() && level.isGround() && generator.stream().allMatch(Literal::isGround);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(weight);
		sb.append("@");
		sb.append(level);
		if (!generator.isEmpty()) {
			sb.append(" : ");
			sb.append(Literals.toString(generator));
		}
		return sb.toString();
	}

}
