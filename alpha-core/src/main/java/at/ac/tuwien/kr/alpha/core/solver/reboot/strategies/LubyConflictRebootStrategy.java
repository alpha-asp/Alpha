/**
 * Copyright (c) 2022, the Alpha Team.
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
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
package at.ac.tuwien.kr.alpha.core.solver.reboot.strategies;

import at.ac.tuwien.kr.alpha.core.common.NoGood;

public class LubyConflictRebootStrategy implements RebootStrategy {
	private static final double SCALING_FACTOR = 10;

	private int rebootCount;
	private int conflictCount;

	@Override
	public void conflictEncountered() {
		conflictCount++;
	}

	@Override
	public boolean isRebootScheduled() {
		return conflictCount >= SCALING_FACTOR * luby(rebootCount + 1);
	}

	@Override
	public void rebootPerformed() {
		rebootCount++;
	}

	private double luby(double i) {
		for (int k = 1; k < 31; k++) {
			if (i == (Math.pow(2, k)) - 1) {
				return Math.pow(2, k - 1);
			}
		}
		for (int k = 1;; k++) {
			if (Math.pow(2, k - 1) <= i && i < Math.pow(2, k) - 1) {
				return luby(i - Math.pow(2, k - 1) + 1);
			}
		}
	}
}
