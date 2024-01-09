package at.ac.tuwien.kr.alpha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.util.AnswerSetQueryImpl;
import at.ac.tuwien.kr.alpha.core.actions.ActionImplementationProvider;
import at.ac.tuwien.kr.alpha.core.actions.OutputStreamHandle;
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
	//@Disabled
	@SuppressWarnings("unchecked")
	public void writeToFile() {
		Map<String, OutputStream> mockedFileOutputs = new HashMap<>();
		ByteArrayOutputStream dummyFileContent = new ByteArrayOutputStream();
		mockedFileOutputs.put("dummy.file", dummyFileContent);
		MockedActionsAlphaFactory alphaFactory = new MockedActionsAlphaFactory();
		alphaFactory.getActionImplementationMock().setMockedFileOutputs(mockedFileOutputs);
		ActionImplementationProvider actionProvider = alphaFactory.getActionImplementationMock();
		Alpha alpha = alphaFactory.newAlpha(new SystemConfig());
		InputProgram program = alpha.readProgramString(WRITE_TO_FILE);
		Set<AnswerSet> answerSets = alpha.solve(program).collect(Collectors.toSet());
		LOGGER.debug("Got answer sets: {}", answerSets);
		assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.stream().findFirst().get();
		/*
		 * Note: We have to check answer set content here because we have no way of constructing an equal instance for
		 * the outputStreamHandle that is constructed when execution the "fileOutputStream" action.		 *
		 */
		assertEquals(1, answerSet.query(AnswerSetQueryImpl.forPredicate(Predicates.getPredicate("outfile_open_result", 2))
				.withFilter(0, term -> term instanceof ConstantTerm<?> && ((ConstantTerm<String>) term).getObject().endsWith("dummy.file"))
				.withFunctionTerm(1, "success", 1)
				.withFilter(1, (term) -> {
					FunctionTerm funcTerm = (FunctionTerm) term;
					assertEquals(1, funcTerm.getTerms().size());
					assertTrue(funcTerm.getTerms().get(0) instanceof FunctionTerm && ((FunctionTerm) funcTerm.getTerms().get(0)).getSymbol().equals("stream"));
					ConstantTerm<?> streamTerm = (ConstantTerm<?>) ((FunctionTerm) funcTerm.getTerms().get(0)).getTerms().get(0);
					return streamTerm.getObject() instanceof OutputStreamHandle;
				})
		).size());
		assertEquals(1, answerSet.query(AnswerSetQueryImpl.forPredicate(Predicates.getPredicate("outfile_write_result", 2))
				.withFilter(0, term -> term instanceof ConstantTerm<?> && ((ConstantTerm<String>) term).getObject().endsWith("dummy.file"))
				.withFunctionTerm(1, "success", 1)
				.withFilter(1, (term) -> {
					FunctionTerm funcTerm = (FunctionTerm) term;
					assertEquals(1, funcTerm.getTerms().size());
					return funcTerm.getTerms().get(0) instanceof ConstantTerm<?> && ((ConstantTerm<String>) funcTerm.getTerms().get(0)).getObject().equals("ok");
				})
		).size());
		assertEquals(1, answerSet.query(AnswerSetQueryImpl.forPredicate(Predicates.getPredicate("outfile_close_result", 2))
				.withFilter(0, term -> term instanceof ConstantTerm<?> && ((ConstantTerm<String>) term).getObject().endsWith("dummy.file"))
				.withFunctionTerm(1, "success", 1)
				.withFilter(1, (term) -> {
					FunctionTerm funcTerm = (FunctionTerm) term;
					assertEquals(1, funcTerm.getTerms().size());
					return funcTerm.getTerms().get(0) instanceof ConstantTerm<?> && ((ConstantTerm<String>) funcTerm.getTerms().get(0)).getObject().equals("ok");
				})
		).size());
		assertEquals("Foo bar!", dummyFileContent.toString());
	}

}
