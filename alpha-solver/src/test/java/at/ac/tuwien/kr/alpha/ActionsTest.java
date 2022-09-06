package at.ac.tuwien.kr.alpha;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.test.util.MockedActionsAlphaFactory;

/**
 * End-to-end tests covering Evolog action support.
 * Note that all tests in this suite depend on stratified evaluation being enabled.
 */
public class ActionsTest {

	private static final String HELLO_WORLD = 
		"hello_result(RES) : @streamWrite[STDOUT, \"Hello World!\"] = RES :- &stdout(STDOUT).";

	private static final String WRITE_TO_FILE = 
		"outfile(\"dummy.file\")."
		 + "outfile_open_result(F, R) : @fileOutputStream[F] = R :- outfile(F)."
		 + "outfile_write_result(F, R) : @streamWrite[HD, \"Foo bar!\"] = R :- outfile_open_result(F, success(stream(HD)))."
		 + "outfile_close_result(F, R) : @outputStreamClose[HD] = R :- outfile_open_result(F, success(stream(HD))), outfile_write_result(F, success(ok)).";

		/**
	 * Simple smoke test which verifies correct behavior of an Evolog "Hello World" program.
	 */
	@Test
	public void helloWorld() {
		MockedActionsAlphaFactory alphaFactory = new MockedActionsAlphaFactory();
		Alpha alpha = alphaFactory.newAlpha(new SystemConfig());
		InputProgram program = alpha.readProgramString(HELLO_WORLD);
		alpha.solve(program);
		assertEquals("Hello World!", alphaFactory.getActionImplementationMock().getStdoutContent());
	}

	@Test
	@Disabled // TODO we need to add our destination file in the mock!
	public void writeToFile() {
		MockedActionsAlphaFactory alphaFactory = new MockedActionsAlphaFactory();
		Alpha alpha = alphaFactory.newAlpha(new SystemConfig());
		InputProgram program = alpha.readProgramString(WRITE_TO_FILE);
		List<AnswerSet> answerSets = alpha.solve(program).collect(Collectors.toList());
		// TODO check answer set
		// TODO check invocation counts
	}

}
