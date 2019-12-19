package at.ac.tuwien.kr.alpha;

import static at.ac.tuwien.kr.alpha.Main.main;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Unparameterized version of {@link MainTest} for specific cases where parameterized test runner doesn't make sense
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class MainTestParameterless {

	/**
	 * Make sure that when no dep graph (or component graph) is written to file program analysis is only performed prior to stratified evaluation and not done
	 * twice. Verfiy this by checking for logger output in stdout (bit of a hack...)
	 */
	// FIXME this test unfortunately depends on the global loglevel - since the respective outputs from Main are on DEBUG level they are not written with the
	// global level of INFO. Re-enable test as soon as a separate logback xml for testing can be used (github-issue
	// https://github.com/alpha-asp/Alpha/issues/204)
	@Test
	@Ignore
	public void testDoNotWriteDepGraph() {
		String[] args = new String[] {"-str", "a. b :- a." };
		PrintStream sysOut = System.out;
		ByteArrayOutputStream newOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(newOut));
		main(args);
		System.setOut(sysOut);
		String solverOut = newOut.toString();
		Assert.assertTrue(solverOut.contains("{ a, b }"));
		Assert.assertTrue(solverOut.contains("Not writing dependency or component graphs, starting preprocessing"));
		Assert.assertTrue(solverOut.contains("Preprocessing InternalProgram!"));
	}

	/**
	 * Make sure that when a dependency graph (or component graph) is written to file program analysis is performed in main and not done a second time when
	 * performing stratified evaluation. Verfiy this by checking for logger output in stdout (bit of a hack...)
	 * 
	 * @throws IOException
	 */
	@Test
	public void testWriteDepGraph() throws IOException {
		File tmpFile = File.createTempFile("alphaTest", "depgraph.dot");
		String[] args = new String[] {"-str", "a. b :- a.", "-dg", tmpFile.getAbsolutePath() };
		PrintStream sysOut = System.out;
		ByteArrayOutputStream newOut = new ByteArrayOutputStream();
		System.setOut(new PrintStream(newOut));
		main(args);
		System.setOut(sysOut);
		long tmpFileLen = tmpFile.length();
		tmpFile.delete();
		String solverOut = newOut.toString();
		Assert.assertTrue(solverOut.contains("{ a, b }"));
		Assert.assertTrue(tmpFileLen > 0);
	}

}
