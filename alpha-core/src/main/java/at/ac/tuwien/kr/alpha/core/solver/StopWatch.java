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
package at.ac.tuwien.kr.alpha.core.solver;

public class StopWatch {
	private long startTime;
	private long nanoTime;
	private boolean running;

	/**
	 * Starts the {@link StopWatch}. If it is already running, this function does nothing.
	 */
	public void start() {
		if (!running) {
			startTime = System.nanoTime();
			running = true;
		}
	}

	/**
	 * Stops the {@link StopWatch}. If it is not running, this function does nothing.
	 */
	public void stop() {
		if (running) {
			long currentTime = System.nanoTime();
			nanoTime += currentTime - startTime;
			running = false;
		}
	}

	/**
	 * Returns the time in nanoseconds the {@link StopWatch} has been running in total.
	 * @return the total running time of the {@link StopWatch} in nanoseconds.
	 */
	public long getNanoTime() {
		long currentNanos = running ? System.nanoTime() - startTime : 0;
		return nanoTime + currentNanos;
	}
}
