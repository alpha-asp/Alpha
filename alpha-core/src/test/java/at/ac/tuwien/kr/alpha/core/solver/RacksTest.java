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
package at.ac.tuwien.kr.alpha.core.solver;

import static at.ac.tuwien.kr.alpha.core.test.util.TestUtils.buildSolverForRegressionTest;
import static at.ac.tuwien.kr.alpha.core.test.util.TestUtils.runWithTimeout;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Disabled;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.Solver;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;

/**
 * Tests {@link AbstractSolver} using a racks configuration problem.
 *
 */
// TODO this is a functional test
@Disabled("disabled to save resources during CI")
public class RacksTest {

	private static final long DEBUG_TIMEOUT_FACTOR = 5;

	@RegressionTest
	public void testRacks(RegressionTestConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> test(cfg));
	}

	private void test(RegressionTestConfig cfg) throws IOException {
		CharStream programInputStream = CharStreams.fromPath(
				Paths.get("benchmarks", "siemens", "racks", "racks.lp"));
		Solver solver = buildSolverForRegressionTest(new ProgramParserImpl().parse(programInputStream), cfg);
		@SuppressWarnings("unused")
		Optional<AnswerSet> answerSet = solver.stream().findFirst();
		// System.out.println(answerSet);
		// TODO: check correctness of answer set
	}
}
