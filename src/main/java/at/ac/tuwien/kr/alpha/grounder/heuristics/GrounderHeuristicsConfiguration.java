/**
 * Copyright (c) 2019 Siemens AG
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
package at.ac.tuwien.kr.alpha.grounder.heuristics;

import at.ac.tuwien.kr.alpha.grounder.Grounder;

/**
 * Contains configuration parameters for heuristics used by {@link Grounder}s.
 * 
 * Both parameters {@link #toleranceConstraints} and {@link #toleranceRules} are interpreted as follows:
 * A rule (or constraint) is grounded if the following conditions are satisfied:
 * <ul>
 * 	<li>All variables in the rule are bound (either by positive body literals that are already
 *      satisfied, or because the whole rule is ground).</li>
 * 	<li>No atom occurring positively in the body is assigned F, and no atom occurring negatively
 *      in the body is assigned T or MBT (because this would make the rule irrelevant in the current
 *      part of the search space).</li>
 * 	<li>At most {@code N} atoms occurring positively in the body are still unassigned.</li>
 * </ul>
 * 
 * The parameter {@link #toleranceConstraints} specifies {@code N} for constraints, while {@link #toleranceRules}
 * specifies {@code N} for all other rules. Infinity is represented by the value {@code -1}.
 * The default value for both parameters is {@code 0}, which means that only those rules and constraints are
 * grounded whose positive body is already satisfied.
 *
 */
public class GrounderHeuristicsConfiguration {
	
	private int toleranceConstraints = 0;
	private int toleranceRules = 0;
	
	public GrounderHeuristicsConfiguration() {
		super();
	}
	
	/**
	 * @param toleranceConstraints
	 * @param toleranceRules
	 */
	public GrounderHeuristicsConfiguration(int toleranceConstraints, int toleranceRules) {
		super();
		this.toleranceConstraints = toleranceConstraints;
		this.toleranceRules = toleranceRules;
	}

	/**
	 * @return the tolerance for constraints
	 */
	public int getToleranceConstraints() {
		return toleranceConstraints;
	}

	/**
	 * @return the tolerance for rules that are not constraints
	 */
	public int getToleranceRules() {
		return toleranceRules;
	}

}
