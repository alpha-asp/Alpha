package at.ac.tuwien.kr.alpha.core.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.programs.actions.Action;
import at.ac.tuwien.kr.alpha.api.terms.ActionResultTerm;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.commons.util.IntIdGenerator;

public abstract class AbstractActionImplementationProvider implements ActionImplementationProvider {

	private final IntIdGenerator idGenerator = new IntIdGenerator();
	private final Map<String, Action> supportedActions = new HashMap<>();

	private ConstantTerm<OutputStreamHandle> stdoutHandle;
	private ConstantTerm<InputStreamHandle> stdinHandle;

	public AbstractActionImplementationProvider() {
		registerAction("fileOutputStream", this::openFileOutputStreamAction);
		registerAction("streamWrite", this::outputStreamWriteAction);
		registerAction("outputStreamClose", this::outputStreamCloseAction);
		registerAction("fileInputStream", this::openFileInputStreamAction);
		registerAction("streamReadLine", this::inputStreamReadLineAction);
		registerAction("inputStreamClose", this::inputStreamCloseAction);
	}

	/**
	 * Returns a map of all actions supported by this implementation provider.
	 */
	public final Map<String, Action> getSupportedActions() {
		return supportedActions;
	}

	/**
	 * Returns a predicate interpretation specifying an external that takes no arguments
	 * and returns a reference to the standard system output stream (stdout).
	 */
	public final PredicateInterpretation getStdoutTerm() {
		if (stdoutHandle == null) {
			stdoutHandle = Terms.newConstant(new OutputStreamHandle(idGenerator.getNextId(), getStdoutStream()));
		}
		return (trms) -> {
			if (!trms.isEmpty()) {
				throw new IllegalArgumentException("Invalid method call! Expected term list to be empty!");
			}
			return Collections.singleton(Collections.singletonList(stdoutHandle));
		};
	}

	/**
	 * Returns a predicate interpretation specifying an external that takes no arguments
	 * and returns a reference to the standard system input stream (stdin).
	 */
	public final PredicateInterpretation getStdinTerm() {
		if (stdinHandle == null) {
			stdinHandle = Terms.newConstant(new InputStreamHandle(idGenerator.getNextId(),
					new BufferedReader(new InputStreamReader(getStdinStream()))));
		}
		return (trms) -> {
			if (!trms.isEmpty()) {
				throw new IllegalArgumentException("Invalid method call! Expected term list to be empty!");
			}
			return Collections.singleton(Collections.singletonList(stdinHandle));
		};
	}

	protected final void registerAction(String name, Action action) {
		supportedActions.put(name, action);
	}

	private ActionResultTerm<?> openFileOutputStreamAction(List<Term> input) {
		if (input.size() != 1) {
			return Terms.actionError("Incorrect input size!");
		}
		Term inTerm = input.get(0);
		if (!(inTerm instanceof ConstantTerm)) {
			return Terms.actionError("Input must be a string constant!");
		}
		ConstantTerm<?> inConst = (ConstantTerm<?>) inTerm;
		if (!(inConst.getObject() instanceof String) || inConst.isSymbolic()) {
			return Terms.actionError("Input must be a string constant!");
		}
		String path = (String) inConst.getObject();
		try {
			OutputStreamHandle streamHandle = new OutputStreamHandle(idGenerator.getNextId(), getFileOutputStream(path));
			return Terms.actionSuccess(Terms.newFunctionTerm("stream", Terms.newConstant(streamHandle)));
		} catch (IOException e) {
			return Terms.actionError("File not  found: " + path);
		}
	}

	@SuppressWarnings("unchecked")
	private ActionResultTerm<ConstantTerm<String>> outputStreamWriteAction(List<Term> input) {
		if (input.size() != 2) {
			return Terms.actionError("Incorrect input size!");
		}
		if (!(input.get(0) instanceof ConstantTerm) || !(((ConstantTerm<?>) input.get(0)).getObject() instanceof OutputStreamHandle)) {
			return Terms.actionError("First input term must be an output stream handle!");
		}
		OutputStreamHandle dstHandle = ((ConstantTerm<OutputStreamHandle>) input.get(0)).getObject();
		if (!(input.get(1) instanceof ConstantTerm) || !(((ConstantTerm<?>) input.get(1)).getObject() instanceof String)
				|| ((ConstantTerm<?>) input.get(1)).isSymbolic()) {
			return Terms.actionError("Second input term must be a string constant!");
		}
		String str = ((ConstantTerm<String>) input.get(1)).getObject();
		// TODO this needs some built-in conversion function
		byte[] data = str.getBytes();
		OutputStream dst = dstHandle.getStream();
		try {
			dst.write(data);
			return Terms.actionSuccess(Terms.newSymbolicConstant("ok"));
		} catch (IOException ex) {
			return Terms.actionError("Error writing data: " + ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private ActionResultTerm<ConstantTerm<String>> outputStreamCloseAction(List<Term> input) {
		if (input.size() != 1) {
			return Terms.actionError("Incorrect input size!");
		}
		if (!(input.get(0) instanceof ConstantTerm) || !(((ConstantTerm<?>) input.get(0)).getObject() instanceof OutputStreamHandle)) {
			return Terms.actionError("First input term must be an output stream handle!");
		}
		OutputStreamHandle handle = ((ConstantTerm<OutputStreamHandle>) input.get(0)).getObject();
		try {
			handle.getStream().close();
			return Terms.actionSuccess(Terms.newSymbolicConstant("ok"));
		} catch (IOException ex) {
			return Terms.actionError("Error closing stream: " + ex.getMessage());
		}
	}

	private ActionResultTerm<?> openFileInputStreamAction(List<Term> input) {
		if (input.size() != 1) {
			return Terms.actionError("Incorrect input size!");
		}
		Term inTerm = input.get(0);
		if (!(inTerm instanceof ConstantTerm)) {
			return Terms.actionError("Input must be a string constant!");
		}
		ConstantTerm<?> inConst = (ConstantTerm<?>) inTerm;
		if (!(inConst.getObject() instanceof String) || inConst.isSymbolic()) {
			return Terms.actionError("Input must be a string constant!");
		}
		String path = (String) inConst.getObject();
		try {
			InputStreamHandle streamHandle = new InputStreamHandle(idGenerator.getNextId(), new BufferedReader(new InputStreamReader(getInputStream(path))));
			return Terms.actionSuccess(Terms.newFunctionTerm("stream", Terms.newConstant(streamHandle)));
		} catch (IOException ex) {
			return Terms.actionError("Error opening input stream: " + ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private ActionResultTerm<?> inputStreamReadLineAction(List<Term> input) {
		if (input.size() != 1) {
			return Terms.actionError("Incorrect input size!");
		}
		if (!(input.get(0) instanceof ConstantTerm) || !(((ConstantTerm<?>) input.get(0)).getObject() instanceof InputStreamHandle)) {
			return Terms.actionError("First input term must be an input stream handle!");
		}
		InputStreamHandle handle = ((ConstantTerm<InputStreamHandle>) input.get(0)).getObject();
		try {
			String line = handle.getStream().readLine();
			ConstantTerm<String> lineTerm;
			if (line == null) {
				// we reached EOF
				lineTerm = Terms.newSymbolicConstant("eof");
			} else {
				lineTerm = Terms.newConstant(line);
			}
			return Terms.actionSuccess(Terms.newFunctionTerm("line", lineTerm));
		} catch (IOException ex) {
			return Terms.actionError("Error reading data");
		}
	}

	@SuppressWarnings("unchecked")
	private ActionResultTerm<?> inputStreamCloseAction(List<Term> input) {
		if (input.size() != 1) {
			return Terms.actionError("Incorrect input size!");
		}
		if (!(input.get(0) instanceof ConstantTerm) || !(((ConstantTerm<?>) input.get(0)).getObject() instanceof InputStreamHandle)) {
			return Terms.actionError("First input term must be an input stream handle!");
		}
		InputStreamHandle handle = ((ConstantTerm<InputStreamHandle>) input.get(0)).getObject();
		try {
			handle.getStream().close();
			return Terms.actionSuccess(Terms.newFunctionTerm("closeResult", Terms.newSymbolicConstant("ok")));
		} catch (IOException ex) {
			return Terms.actionError("Error writing data: " + ex.getMessage());
		}
	}

	protected abstract OutputStream getStdoutStream();

	protected abstract InputStream getStdinStream();

	protected abstract OutputStream getFileOutputStream(String path) throws IOException;

	protected abstract InputStream getInputStream(String path) throws IOException;

}
