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
package at.ac.tuwien.kr.alpha.core.solver.reboot.strategies.old;

import at.ac.tuwien.kr.alpha.core.common.NoGood;
import at.ac.tuwien.kr.alpha.core.solver.reboot.stats.ResettableStatTracker;
import at.ac.tuwien.kr.alpha.core.solver.reboot.stats.StatTracker;
import at.ac.tuwien.kr.alpha.core.solver.reboot.strategies.RebootStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicLearnedIntervalRebootStrategy implements RebootStrategy {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicLearnedIntervalRebootStrategy.class);

	private final ResettableStatTracker efficiencyMeasure;
	private final StatTracker intervalSizeTracker;
	private final int minInterval;
	private final double scalingFactor;
	private double intervalSize;
	private double nextIntervalSize;
	private int learnedCount;
	private double previousIntervalMeasure;
	private boolean isFirstInterval;
	private boolean wasHalfPointHandled;

	public DynamicLearnedIntervalRebootStrategy(ResettableStatTracker efficiencyMeasure,
												double scalingFactor,
												int minInterval, int startInterval) {
		this.efficiencyMeasure = efficiencyMeasure;
		this.intervalSizeTracker = new StatTracker() {
			@Override
			public String getStatName() {
				return "interval_size";
			}

			@Override
			public double getStatValue() {
				return intervalSize;
			}
		};
		this.scalingFactor = scalingFactor;
		this.minInterval = minInterval;
		this.intervalSize = startInterval;
		this.learnedCount = 0;
		this.previousIntervalMeasure = 0;
		this.isFirstInterval = true;
		this.wasHalfPointHandled = false;
	}

	public StatTracker getIntervalSizeTracker() {
		return intervalSizeTracker;
	}

	@Override
	public void newLearnedNoGood(NoGood noGood) {
		learnedCount++;
		handleHalfPointIfNeeded();
	}

	@Override
	public boolean isRebootScheduled() {
		return learnedCount >= (int) intervalSize;
	}

	@Override
	public void rebootPerformed() {
		previousIntervalMeasure = efficiencyMeasure.getStatValue();
		intervalSize = nextIntervalSize;

		efficiencyMeasure.reset();
		learnedCount = 0;
		wasHalfPointHandled = false;
	}

	private double getNewIntervalSize(double currentIntervalMeasure) {
		double oldIntervalSize = intervalSize;

		if (isFirstInterval) {
			isFirstInterval = false;
			return intervalSize;
		} else if (currentIntervalMeasure > previousIntervalMeasure) {
			double newIntervalSize = downscaleIntervalSize(intervalSize);
			LOGGER.info("Reboot interval size: {} -> {}", oldIntervalSize, newIntervalSize);
			LOGGER.info("Interval measures were: {} < {}", previousIntervalMeasure, currentIntervalMeasure);
			return Math.max(newIntervalSize, minInterval);
		} else {
			double newIntervalSize = upscaleIntervalSize(intervalSize);
			LOGGER.info("Reboot interval size: {} -> {}", oldIntervalSize, newIntervalSize);
			LOGGER.info("Interval measures were: {} >= {}", previousIntervalMeasure, currentIntervalMeasure);
			return Math.max(newIntervalSize, minInterval);
		}
	}

	private double downscaleIntervalSize(double intervalSize) {
		return intervalSize * scalingFactor;
	}

	private double upscaleIntervalSize(double intervalSize) {
		return intervalSize / (scalingFactor * scalingFactor);
	}

	private boolean isHalfPointReached() {
		return learnedCount >= (int) (intervalSize / 2);
	}

	private void handleHalfPoint() {
		double currentIntervalMeasure = efficiencyMeasure.getStatValue();
		nextIntervalSize = getNewIntervalSize(currentIntervalMeasure);

		efficiencyMeasure.reset();
		wasHalfPointHandled = true;
	}

	private void handleHalfPointIfNeeded() {
		if (!wasHalfPointHandled && isHalfPointReached()) {
			handleHalfPoint();
		}
	}
}
