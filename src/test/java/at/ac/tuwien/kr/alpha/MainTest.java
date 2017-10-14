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
		assertTrue(testMainForOutput(new String[]{"-DebugEnableInternalChecks", "-g", "naive", "-s", "default", "-e", "1119654162577372", "-n", "20", "-str", "p(a). \n b :- p(X).\n"}, "{ b, p(a) }", null));
		assertTrue(testMainForOutput(new String[]{"-DebugEnableInternalChecks", "-g", "naive", "-s", "default", "-n", "0", "-str", "p(a). \n b :- p(X).\n"}, "{ b, p(a) }", null));
		assertTrue(testMainForOutput(new String[]{"-DebugEnableInternalChecks", "-g", "naive", "-s", "default", "-n", "1", "-str", "p(a). \n b :- p(X).\n"}, "{ b, p(a) }", null));
		assertTrue(testMainForOutput(new String[]{"-g", "naive", "-s", "default", "-r", "naive", "-e", "1119654162577372", "-n", "1", "-str", "p(a). \n b :- p(X).\n"}, "{ b, p(a) }", null));
	}

	@Test
	public void previouslyProblematicRuns() {
		// Run tests that formerly caused some sort of exception.
		main(new String[]{"-DebugEnableInternalChecks", "-g", "naive", "-s", "default", "-e", "1119654162577372", "-n", "200", "-i", "./src/test/resources/PreviouslyProblematic/3col-20-38.txt"});
		main(new String[]{"-DebugEnableInternalChecks", "-g", "naive", "-s", "default", "-e", "1119718541727902", "-n", "200", "-i", ".//src/test/resources/PreviouslyProblematic/3col-20-38.txt"});
		main(new String[]{"-DebugEnableInternalChecks", "-g", "naive", "-s", "default", "-e", "97598271567626", "-n", "2", "-i", "./src/test/resources/PreviouslyProblematic/vehicle_normal_small.asp"});
		main(new String[]{"-DebugEnableInternalChecks", "-sort", "-g", "naive", "-s", "default", "-n", "400", "-i", "./src/test/resources/PreviouslyProblematic/3col-20-38.txt"});
	}
}