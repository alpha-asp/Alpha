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
package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.learning.GroundConflictNoGoodLearner.ConflictAnalysisResult;

import java.util.LinkedList;
import java.util.List;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * A chain of branching heuristics in which the entry at position n+1 is used as a fallback if the entry at position n cannot make a decision. 
 */
public class ChainOfBranchingHeuristics implements BranchingHeuristic {
	
	private List<BranchingHeuristic> chain = new LinkedList<>();
	
	@Override
	public void violatedNoGood(NoGood violatedNoGood) {
		for (BranchingHeuristic element : chain) {
			element.violatedNoGood(violatedNoGood);
		}
	}

	@Override
	public void analyzedConflict(ConflictAnalysisResult analysisResult) {
		for (BranchingHeuristic element : chain) {
			element.analyzedConflict(analysisResult);
		}
	}

	@Override
	public void newNoGood(NoGood newNoGood) {
		for (BranchingHeuristic element : chain) {
			element.newNoGood(newNoGood);
		}
	}
	
	@Override
	public int chooseLiteral() {
		for (BranchingHeuristic element : chain) {
			int chosenLiteral = element.chooseLiteral();
			if (chosenLiteral != DEFAULT_CHOICE_LITERAL) {
				return chosenLiteral;
			}
		}
		return DEFAULT_CHOICE_LITERAL;
	}
	
	public void add(BranchingHeuristic element) {
		if (chain.contains(element)) {
			throw oops("Cycle detected in chain of branching heuristics");
		}
		chain.add(element);
	}
	
	public static ChainOfBranchingHeuristics chainOf(BranchingHeuristic... branchingHeuristics) {
		ChainOfBranchingHeuristics chain = new ChainOfBranchingHeuristics();
		for (BranchingHeuristic element : branchingHeuristics) {
			chain.add(element);
		}
		return chain;
	}

}
