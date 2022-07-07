package at.ac.tuwien.kr.alpha;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;

public class ActionsTest {

	private static class MockActionBindings {

		static final Logger LOGGER = LoggerFactory.getLogger(MockActionBindings.class);

		Map<List<Term>, Integer> fileOpenOutputStreamInvocations = new HashMap<>();
		Map<List<Term>, Integer> outputStreamWriteInvocations = new HashMap<>();
		Map<List<Term>, Integer> outputStreamCloseInvocations = new HashMap<>();
		Map<List<Term>, Integer> fileOpenInputStreamInvocations = new HashMap<>();
		Map<List<Term>, Integer> inputStreamReadLineInvocations = new HashMap<>();
		Map<List<Term>, Integer> inputStreamCloseInvocations = new HashMap<>();

		public FunctionTerm fileOpenOutputStream(List<Term> input) {
			if (fileOpenOutputStreamInvocations.containsKey(input)) {
				fileOpenOutputStreamInvocations.put(input, fileOpenOutputStreamInvocations.get(input) + 1);
			} else {
				fileOpenOutputStreamInvocations.put(input, 1);
			}
			LOGGER.info("Action fileOpenOutputStream({})", StringUtils.join(input, ", "));
			return null;
		}

		public FunctionTerm outputStreamWrite(List<Term> input) {
			return null;
		}

		public FunctionTerm outputStreamClose(List<Term> input) {
			return null;
		}

		public FunctionTerm fileOpenInputStream(List<Term> input) {
			return null;
		}

		public FunctionTerm inputStreamReadLine(List<Term> input) {
			return null;
		}

		public FunctionTerm inputStreamClose(List<Term> inputStream) {
			return null;
		}

	}

}
