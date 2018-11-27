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
package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.solver.BinaryNoGoodPropagationEstimation;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

/**
 * The well-known MOMs (Maximum Occurrences in clauses of Minimum size) heuristic
 * used to initialize atom scores.
 * This implementation is inspired by the MOMs implementation in <a href="https://github.com/potassco/clasp">clasp</a>.
 *
 */
public class MOMs {
	
	private BinaryNoGoodPropagationEstimation bnpEstimation;
	
	/**
	 * Configuration parameter to switch estimation of binary nogood propagation
	 * on or off. If {@code true}, BNP estimation will be preferred, otherwise
	 * only binary watches will be counted.
	 */
	private boolean preferBNPEstimation = false;

	public MOMs(BinaryNoGoodPropagationEstimation bnpEstimation) {
		super();
		this.bnpEstimation = bnpEstimation;
	}

	/**
	 * @param atom
	 * @return
	 */
	public double getScore(Integer atom) {
		int s1;
		int s2;
		if (preferBNPEstimation && bnpEstimation.hasBinaryNoGoods()) {
			s1 = bnpEstimation.estimate(atom, ThriceTruth.TRUE) - 1;
			s2 = bnpEstimation.estimate(atom, ThriceTruth.FALSE) - 1;
		} else {
			// fall back to counting watches:
			s1 = bnpEstimation.getNumberOfBinaryWatches(atom, true);
			s2 = bnpEstimation.getNumberOfBinaryWatches(atom, false);
		}
		return ((s1 * s2) << 10) + s1 + s2;
	}

}
