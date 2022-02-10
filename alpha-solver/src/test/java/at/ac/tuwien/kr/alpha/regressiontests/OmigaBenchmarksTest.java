/**
 * Copyright (c) 2017-2019 Siemens AG
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
package at.ac.tuwien.kr.alpha.regressiontests;

import static at.ac.tuwien.kr.alpha.regressiontests.util.RegressionTestUtils.buildSolverForRegressionTest;
import static at.ac.tuwien.kr.alpha.regressiontests.util.RegressionTestUtils.ignoreTestForNaiveSolver;
import static at.ac.tuwien.kr.alpha.regressiontests.util.RegressionTestUtils.ignoreTestForNonDefaultDomainIndependentHeuristics;
import static at.ac.tuwien.kr.alpha.regressiontests.util.RegressionTestUtils.runWithTimeout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.core.parser.evolog.EvologProgramParser;
import at.ac.tuwien.kr.alpha.regressiontests.util.RegressionTest;

/**
 * Tests {@link AbstractSolver} using Omiga benchmark problems.
 *
 */
// TODO This is actually a performance benchmark and should not be run with standard unit tests
public class OmigaBenchmarksTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(OmigaBenchmarksTest.class);

	private static final int DEBUG_TIMEOUT_FACTOR = 20;

	@RegressionTest
	public void test3Col_10_18(SystemConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> test("3col", "3col-10-18.txt", cfg));
	}

	@RegressionTest
	public void test3Col_20_38(SystemConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> test("3col", "3col-20-38.txt", cfg));
	}

	@RegressionTest
	public void testCutedge_100_30(SystemConfig cfg) {
		long timeout = 15000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> test("cutedge", "cutedge-100-30.txt", cfg));
	}

	@RegressionTest
	public void testCutedge_100_50(SystemConfig cfg) {
		long timeout = 15000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> test("cutedge", "cutedge-100-50.txt", cfg));
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testLocstrat_200(SystemConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> test("locstrat", "locstrat-200.txt", cfg));
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testLocstrat_400(SystemConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> test("locstrat", "locstrat-400.txt", cfg));
	}

	@RegressionTest
	public void testReach_1(SystemConfig cfg) {
		long timeout = 20000L;
		ignoreTestForNaiveSolver(cfg);
		ignoreTestForNonDefaultDomainIndependentHeuristics(cfg);
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> test("reach", "reach-1.txt", cfg));
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testReach_4(SystemConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> test("reach", "reach-4.txt", cfg));
	}

	private void test(String folder, String aspFileName, SystemConfig cfg) throws IOException {
		@SuppressWarnings("unused")
		Optional<AnswerSet> answerSet = buildSolverForRegressionTest(
				new EvologProgramParser().parse(Files.newInputStream(Paths.get("benchmarks", "omiga", "omiga-testcases", folder, aspFileName))), cfg)
						.stream().findFirst();
		// System.out.println(answerSet);
		// TODO: check correctness of answer set
	}

}
