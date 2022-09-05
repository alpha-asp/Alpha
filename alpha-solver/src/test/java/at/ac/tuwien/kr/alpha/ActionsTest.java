package at.ac.tuwien.kr.alpha;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.test.util.MockedActionsAlphaFactory;

/**
 * End-to-end tests covering Evolog action support.
 * Note that all tests in this suite depend on stratified evaluation being enabled.
 */
public class ActionsTest {

	private static final String HELLO_WORLD = 
		"hello_result(RES) : @streamWrite[STDOUT, \" World!\"] = RES :- &stdout(STDOUT).";

	/**
	 * Simple smoke test which verifies correct behavior of an Evolog "Hello World" program.
	 */
	@Test
	public void helloWorld() {
		MockedActionsAlphaFactory alphaFactory = new MockedActionsAlphaFactory();
		Alpha alpha = alphaFactory.newAlpha(new SystemConfig());
		InputProgram program = alpha.readProgramString(HELLO_WORLD);
		alpha.solve(program);
		// TODO check mock for correct output content
	}

}
