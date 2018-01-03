/**
 * Copyright (c) 2017 Siemens AG
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
package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.WritableAssignment;
import at.ac.tuwien.kr.alpha.solver.heuristics.activity.BodyActivityProviderFactory.BodyActivityType;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public final class BranchingHeuristicFactory {

	public enum Heuristic {
		NAIVE,
		DOMAIN_BERKMIN,
		BERKMIN,
		BERKMINLITERAL,
		DD,
		DD_SUM,
		DD_AVG,
		DD_MAX,
		DD_MIN,
		DD_PYRO,
		GDD,
		GDD_SUM,
		GDD_AVG,
		GDD_MAX,
		GDD_MIN,
		GDD_PYRO,
		ALPHA_ACTIVE_RULE,
		ALPHA_HEAD_MBT;

		/**
		 * @return a comma-separated list of names of known heuristics
		 */
		public static String listAllowedValues() {
			return Arrays.stream(values()).map(Heuristic::toString).collect(Collectors.joining(", "));
		}
	}

	public static BranchingHeuristic getInstance(Heuristic name, Grounder grounder, WritableAssignment assignment, ChoiceManager choiceManager, Random random) {
		switch (name) {
		case NAIVE:
			return new NaiveHeuristic(choiceManager);
		case BERKMIN:
			return new BerkMin(assignment, choiceManager, random, grounder);
		case DOMAIN_BERKMIN:
			return new DomainBerkMin(assignment, choiceManager, random, grounder);
		case BERKMINLITERAL:
			return new BerkMinLiteral(assignment, choiceManager, random, grounder);
		case DD:
			return new DependencyDrivenHeuristic(assignment, choiceManager, random, BodyActivityType.DEFAULT);
		case DD_SUM:
			return new DependencyDrivenHeuristic(assignment, choiceManager, random, BodyActivityType.SUM);
		case DD_AVG:
			return new DependencyDrivenHeuristic(assignment, choiceManager, random, BodyActivityType.AVG);
		case DD_MAX:
			return new DependencyDrivenHeuristic(assignment, choiceManager, random, BodyActivityType.MAX);
		case DD_MIN:
			return new DependencyDrivenHeuristic(assignment, choiceManager, random, BodyActivityType.MIN);
		case DD_PYRO:
			return new DependencyDrivenPyroHeuristic(assignment, choiceManager, random, BodyActivityType.DEFAULT);
		case GDD:
			return new GeneralizedDependencyDrivenHeuristic(assignment, choiceManager, random, BodyActivityType.DEFAULT);
		case GDD_SUM:
			return new GeneralizedDependencyDrivenHeuristic(assignment, choiceManager, random, BodyActivityType.SUM);
		case GDD_AVG:
			return new GeneralizedDependencyDrivenHeuristic(assignment, choiceManager, random, BodyActivityType.AVG);
		case GDD_MAX:
			return new GeneralizedDependencyDrivenHeuristic(assignment, choiceManager, random, BodyActivityType.MAX);
		case GDD_MIN:
			return new GeneralizedDependencyDrivenHeuristic(assignment, choiceManager, random, BodyActivityType.MIN);
		case GDD_PYRO:
			return new GeneralizedDependencyDrivenPyroHeuristic(assignment, choiceManager, random, BodyActivityType.DEFAULT);
		case ALPHA_ACTIVE_RULE:
			return new AlphaActiveRuleHeuristic(assignment, choiceManager, random);
		case ALPHA_HEAD_MBT:
			return new AlphaHeadMustBeTrueHeuristic(assignment, choiceManager, random);
		}
		throw new IllegalArgumentException("Unknown branching heuristic requested.");
	}
}
