/**
 * Copyright (c) 2020 Siemens AG
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
package at.ac.tuwien.kr.alpha.grounder;

import java.util.Arrays;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.grounder.CompletionConfiguration.Strategy.Both;
import static at.ac.tuwien.kr.alpha.grounder.CompletionConfiguration.Strategy.OnlyCompletion;
import static at.ac.tuwien.kr.alpha.grounder.CompletionConfiguration.Strategy.OnlyJustification;

/**
 * Contains configuration parameters for the generation of completion nogoods and justifications.
 * 
 * The parameters {@link #enableCompletionForSingleNonProjectiveRule}, {@link #enableCompletionForMultipleRules},
 * {@link #enableCompletionForDirectFunctionalDependencies}, and {@link #enableCompletionForSolvedPredicates} switch
 * the generation of completion formulas of certain types on or off.
 *
 * The parameter {@link #strategy} determines if completion nogoods and/or justifications
 * shall be generated at all, for documentation see {@link Strategy}.
 */
public class CompletionConfiguration {

	public static final Strategy DEFAULT_STRATEGY = Both;

	private boolean enableCompletionForSingleNonProjectiveRule = true;
	private boolean enableCompletionForMultipleRules = true;
	private boolean enableCompletionForDirectFunctionalDependencies = true;
	private boolean enableCompletionForSolvedPredicates = true;
	private boolean enableBackwardsCompletion = true;
	private boolean enableAtConflictAfterClosing = true;
	private Strategy strategy = DEFAULT_STRATEGY;

	/**
	 * Determines if completion nogoods and/or justifications shall be generated at all.
	 */
	public enum Strategy {
		/**
		 * Neither completion nogoods nor justifications shall be generated.
		 */
		None,

		/**
		 * Completion nogoods but no justifications shall be generated.
		 */
		OnlyCompletion,

		/**
		 * Justifications but no completion nogoods shall be generated.
		 */
		OnlyJustification,

		/**
		 * The solver shall first try to generate completion nogoods. If that fails, justifications shall be generated.
		 */
		Both;

		/**
		 * @return a comma-separated list of names of known strategies
		 */
		public static String listAllowedValues() {
			return Arrays.stream(values()).map(Strategy::toString).collect(Collectors.joining(", "));
		}
	}

	public boolean isJustificationEnabled() {
		return strategy == Both || strategy == OnlyJustification;
	}

	public boolean isCompletionEnabled() {
		return strategy == Both || strategy == OnlyCompletion;
	}

	public boolean isEnableCompletionForSingleNonProjectiveRule() {
		return enableCompletionForSingleNonProjectiveRule;
	}

	public void setEnableCompletionForSingleNonProjectiveRule(boolean enableCompletionForSingleNonProjectiveRule) {
		ensureCompletionEnabled();
		this.enableCompletionForSingleNonProjectiveRule = enableCompletionForSingleNonProjectiveRule;
	}

	public boolean isEnableCompletionForMultipleRules() {
		return enableCompletionForMultipleRules;
	}

	public void setEnableCompletionForMultipleRules(boolean enableCompletionForMultipleRules) {
		ensureCompletionEnabled();
		this.enableCompletionForMultipleRules = enableCompletionForMultipleRules;
	}

	public boolean isEnableCompletionForDirectFunctionalDependencies() {
		return enableCompletionForDirectFunctionalDependencies;
	}

	public void setEnableCompletionForDirectFunctionalDependencies(boolean enableCompletionForDirectFunctionalDependencies) {
		ensureCompletionEnabled();
		this.enableCompletionForDirectFunctionalDependencies = enableCompletionForDirectFunctionalDependencies;
	}

	public boolean isEnableCompletionForSolvedPredicates() {
		return enableCompletionForSolvedPredicates;
	}

	public void setEnableCompletionForSolvedPredicates(boolean enableCompletionForSolvedPredicates) {
		ensureCompletionEnabled();
		this.enableCompletionForSolvedPredicates = enableCompletionForSolvedPredicates;
	}

	public void setEnableBackwardsCompletion(boolean enableBackwardsCompletion) {
		ensureCompletionEnabled();
		this.enableBackwardsCompletion = enableBackwardsCompletion;
	}

	private void ensureCompletionEnabled() {
		if (!isCompletionEnabled()) {
			if (isJustificationEnabled()) {
				strategy = Both;
			} else {
				strategy = OnlyCompletion;
			}
		}
	}

	public boolean isEnableBackwardsCompletion() {
		return isCompletionEnabled() && enableBackwardsCompletion;
	}

	public boolean isEnableAtConflictAfterClosing() {
		return enableAtConflictAfterClosing && (isJustificationEnabled() || isCompletionEnabled());
	}

	public void setEnableAtConflictAfterClosing(boolean enableAtConflictAfterClosing) {
		this.enableAtConflictAfterClosing = enableAtConflictAfterClosing;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public void setStrategyName(String strategyName) {
		this.strategy = Strategy.valueOf(strategyName);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() +
				"(enableCompletionForSingleNonProjectiveRule=" + enableCompletionForSingleNonProjectiveRule +
				",enableCompletionForMultipleRules=" + enableCompletionForMultipleRules +
				",enableCompletionForDirectFunctionalDependencies=" + enableCompletionForDirectFunctionalDependencies +
				",enableCompletionForSolvedPredicates=" + enableCompletionForSolvedPredicates +
				",enableBackwardsCompletion=" + enableBackwardsCompletion +
				",strategy=" + strategy +
				")";
	}

}
