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
package at.ac.tuwien.kr.alpha.core.solver.reboot.stats;

import java.util.ArrayList;
import java.util.List;

public class PropagationStatManager {
	private final SimpleCountingTracker propagationTracker;
	private final SimpleCountingTracker propagationConflictTracker;
	private final SimpleCountingTracker nonbinPropagationTracker;
	private final SimpleCountingTracker nonbinPropagationConflictTracker;

	public PropagationStatManager() {
		this.propagationTracker = new SimpleCountingTracker("prop_count");
		this.propagationConflictTracker = new SimpleCountingTracker("prop_conflicts");
		this.nonbinPropagationTracker = new SimpleCountingTracker("nonbin_prop_count");
		this.nonbinPropagationConflictTracker = new SimpleCountingTracker("nonbin_prop_conflicts");
	}

	public ResettableStatTracker getPropagationTracker() {
		return propagationTracker;
	}

	public ResettableStatTracker getPropagationConflictTracker() {
		return propagationConflictTracker;
	}

	public ResettableStatTracker getNonbinPropagationTracker() {
		return nonbinPropagationTracker;
	}

	public ResettableStatTracker getNonbinPropagationConflictTracker() {
		return nonbinPropagationConflictTracker;
	}

	public List<ResettableStatTracker> getStatTrackerList() {
		List<ResettableStatTracker> statTrackers = new ArrayList<>();
		statTrackers.add(propagationTracker);
		statTrackers.add(propagationConflictTracker);
		statTrackers.add(nonbinPropagationTracker);
		statTrackers.add(nonbinPropagationConflictTracker);
		return statTrackers;
	}

	public void handleBinaryConflict() {
		propagationTracker.increment();
		propagationConflictTracker.increment();
	}

	public void handleNonBinaryConflict() {
		propagationTracker.increment();
		nonbinPropagationTracker.increment();
		propagationConflictTracker.increment();
		nonbinPropagationConflictTracker.increment();
	}

	public void handleNoConflict() {
		propagationTracker.increment();
		nonbinPropagationTracker.increment();
	}

	public void reset() {
		propagationTracker.reset();
		propagationConflictTracker.reset();
		nonbinPropagationTracker.reset();
		nonbinPropagationConflictTracker.reset();
	}
}
