package at.ac.tuwien.kr.alpha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static at.ac.tuwien.kr.alpha.test.AlphaAssertions.assertAnswerSetsEqual;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.test.util.MockedActionsAlphaFactory;

/**
 * End-to-end tests covering Evolog action support.
 */
public class ActionsTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActionsTest.class);

	private static final String HELLO_WORLD = "hello_result(RES) : @streamWrite[STDOUT, \"Hello World!\"] = RES :- &stdout(STDOUT).";

	private static final String WRITE_TO_FILE = "outfile(\"dummy.file\")."
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
	public void writeToFile() {
		Map<String, OutputStream> mockedFileOutputs = new HashMap<>();
		ByteArrayOutputStream dummyFileContent = new ByteArrayOutputStream();
		mockedFileOutputs.put("dummy.file", dummyFileContent);
		MockedActionsAlphaFactory alphaFactory = new MockedActionsAlphaFactory();
		alphaFactory.getActionImplementationMock().setMockedFileOutputs(mockedFileOutputs);
		Alpha alpha = alphaFactory.newAlpha(new SystemConfig());
		InputProgram program = alpha.readProgramString(WRITE_TO_FILE);
		Set<AnswerSet> answerSets = alpha.solve(program).collect(Collectors.toSet());
		LOGGER.debug("Got answer sets: {}", answerSets);
		assertAnswerSetsEqual(
				"outfile(\"dummy.file\"), outfile_open_result(\"dummy.file\", success(stream(outputStream_2))),"
				 + " outfile_write_result(\"dummy.file\", success(ok)), outfile_close_result(\"dummy.file\", success(ok))",
				answerSets);
		assertEquals("Foo bar!", dummyFileContent.toString());
	}

}
