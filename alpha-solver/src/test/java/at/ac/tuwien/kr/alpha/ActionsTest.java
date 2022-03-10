package at.ac.tuwien.kr.alpha;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;

public class ActionsTest {
	
	private static class MockActionBindings {

		/*
		Map<String, Action> actions = new HashMap<>();
		actions.put("printLine", Actions::printLine);
		actions.put("fileOutputStream", Actions::fileOpenOutputStream);
		actions.put("streamWrite", Actions::outputStreamWrite);
		actions.put("outputStreamClose", Actions::outputStreamClose);

		actions.put("fileInputStream", Actions::fileOpenInputStream);
		actions.put("streamReadLine", Actions::inputStreamReadLine);
		actions.put("inputStreamClose", Actions::inputStreamClose);
		return actions;
		*/

		public FunctionTerm fileOpenOutputStream(List<Term> input) {
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
