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
package at.ac.tuwien.kr.alpha.solver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains counters measuring, e.g., the size of the search space being explored.
 * 
 * Copyright (c) 2017 Siemens AG
 *
 */
public class SolverCounters {
	private static final Logger LOGGER = LoggerFactory.getLogger(SolverCounters.class);
	private int decisionCounter;
	private int backtrackCounter;
	private int backjumpCounter;
	private int remainingMBTAtFixpointCounter;

	public int decision() {
		return ++decisionCounter;
	}

	public int backtrack() {
		return ++backtrackCounter;
	}

	public int backjump() {
		return ++backjumpCounter;
	}

	public int remainingMBTAtFixpoint() {
		return ++remainingMBTAtFixpointCounter;
	}

	public void log() {
		LOGGER.info("Choices\t: {}", decisionCounter);
		LOGGER.info("Conflicts\t: {}", backtrackCounter + backjumpCounter);
		LOGGER.info("  Backtracks\t: {}", backtrackCounter);
		LOGGER.info("    Caused by MBT at end\t: {}", remainingMBTAtFixpointCounter);
		LOGGER.info("  Backjumps\t: {}", backjumpCounter);
	}

	public int getDecisionCounter() {
		return decisionCounter;
	}

	public int getBacktrackCounter() {
		return backtrackCounter;
	}

	public int getBackjumpCounter() {
		return backjumpCounter;
	}

	public int getRemainingMBTAtFixpointCounter() {
		return remainingMBTAtFixpointCounter;
	}

	public int getConflictCounter() {
		return backjumpCounter + backtrackCounter;
	}
}