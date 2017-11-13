/**
 * Copyright (c) 2016-2017, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
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
package at.ac.tuwien.kr.alpha;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static at.ac.tuwien.kr.alpha.Main.main;
import static org.junit.Assert.assertTrue;

public class MainTest {
	public static InputStream stream(String file) {
		return new ByteArrayInputStream(file.getBytes());
	}

	/**
	 * Temporarily redirects System.err and System.out while running the solver from the main entry point with the given parameters.
	 * Returns true if the output contains the given matches.
	 * Warning: this test is fragile and may require adaptions if printing is changed anywhere in Alpha.
	 * @param argv the arguments the solver is started with.
	 * @param matchOutput output that must occur on System.out - may be null if irrelevant.
	 * @param matchError the output that must occur on System.err - may be null if irrelevant.
	 * @return true if given output and errors appear on System.out and System.err while running main(argv).
	 */
	private boolean testMainForOutput(String[] argv, String matchOutput, String matchError) {
		PrintStream oldErr = System.err;
		PrintStream oldOut = System.out;
		ByteArrayOutputStream newOut = new ByteArrayOutputStream();
		ByteArrayOutputStream newErr = new ByteArrayOutputStream();
		System.setOut(new PrintStream(newOut));
		System.setErr(new PrintStream(newErr));
		main(argv);
		System.setOut(oldOut);
		System.setErr(oldErr);

		return !(matchOutput != null && !newOut.toString().contains(matchOutput))
			&& !(matchError != null && !newErr.toString().contains(matchError));
	}

	@Test
	public void testCommandLineOptions() {
		// Exercise the main entry point of the solver.
		String ls = System.lineSeparator();
		assertTrue(testMainForOutput(new String[] {"-DebugEnableInternalChecks", "-g", "naive", "-s", "default", "-e", "1119654162577372", "-n", "20", "-str", "p(a). " + ls + " b :- p(X)." + ls}, "{ b, p(a) }", null));
		assertTrue(testMainForOutput(new String[] {"-DebugEnableInternalChecks", "-g", "naive", "-s", "default", "-n", "0", "-str", "p(a). " + ls + " b :- p(X)." + ls}, "{ b, p(a) }", null));
		assertTrue(testMainForOutput(new String[] {"-DebugEnableInternalChecks", "-g", "naive", "-s", "default", "-n", "1", "-str", "p(a). " + ls + " b :- p(X)." + ls}, "{ b, p(a) }", null));
		assertTrue(testMainForOutput(new String[] {"-g", "naive", "-s", "default", "-r", "naive", "-e", "1119654162577372", "-n", "1", "-str", "p(a). " + ls + " b :- p(X)." + ls}, "{ b, p(a) }", null));
	}

	@Test
	public void previouslyProblematicRuns() {
		// Run tests that formerly caused some sort of exception.
		main(new String[]{"-DebugEnableInternalChecks", "-q", "-g", "naive", "-s", "default", "-e", "1119654162577372", "-n", "200", "-i", "./src/test/resources/PreviouslyProblematic/3col-20-38.txt"});
		main(new String[]{"-DebugEnableInternalChecks", "-q", "-g", "naive", "-s", "default", "-e", "1119718541727902", "-n", "200", "-i", ".//src/test/resources/PreviouslyProblematic/3col-20-38.txt"});
		main(new String[]{"-DebugEnableInternalChecks", "-q", "-g", "naive", "-s", "default", "-e", "97598271567626", "-n", "2", "-i", "./src/test/resources/PreviouslyProblematic/vehicle_normal_small.asp"});
		main(new String[]{"-DebugEnableInternalChecks", "-q", "-sort", "-g", "naive", "-s", "default", "-n", "400", "-i", "./src/test/resources/PreviouslyProblematic/3col-20-38.txt"});
	}
}