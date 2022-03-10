package at.ac.tuwien.kr.alpha.core.actions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.api.programs.actions.Action;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.commons.util.IntIdGenerator;

public class Actions {

	public static Map<String, Action> getDefaultActionBindings() {
		Map<String, Action> actions = new HashMap<>();
		actions.put("printLine", Actions::printLine);
		actions.put("fileOutputStream", Actions::fileOpenOutputStream);
		actions.put("streamWrite", Actions::outputStreamWrite);
		actions.put("outputStreamClose", Actions::outputStreamClose);

		actions.put("fileInputStream", Actions::fileOpenInputStream);
		actions.put("streamReadLine", Actions::inputStreamReadLine);
		actions.put("inputStreamClose", Actions::inputStreamClose);
		return actions;
	}

	// TODO this needs to be encapsulated and made thread-safe!
	private static final IntIdGenerator ID_GEN = new IntIdGenerator();

	public static FunctionTerm printLine(List<Term> input) {
		if (input.size() != 1) {
			// TODO do properly
			throw new RuntimeException("Incorrect arity!");
		}
		// TODO this should only work on ConstantTerm<String>
		System.out.println(input.get(0).toString());
		return Terms.newFunctionTerm("ok");
	}

	public static FunctionTerm fileOpenOutputStream(List<Term> input) {
		if (input.size() != 1) {
			// TODO do properly
			throw new IllegalArgumentException("Incorrect arity!");
		}
		Term inTerm = input.get(0);
		if (!(inTerm instanceof ConstantTerm)) {
			throw new IllegalArgumentException("Input must be a string constant!");
		}
		ConstantTerm<?> inConst = (ConstantTerm<?>) inTerm;
		if (!(inConst.getObject() instanceof String) || inConst.isSymbolic()) {
			throw new IllegalArgumentException("Input must be a string constant!");
		}
		String path = (String) inConst.getObject();
		try {
			OutputStreamHandle streamHandle = new OutputStreamHandle(ID_GEN.getNextId(), new FileOutputStream(path, true));
			return Terms.newFunctionTerm("stream", Terms.newConstant(streamHandle));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static FunctionTerm outputStreamWrite(List<Term> input) {
		if (input.size() != 2) {
			// TODO do properly
			throw new IllegalArgumentException("Incorrect arity!");
		}
		if (!(input.get(0) instanceof ConstantTerm) || !(((ConstantTerm<?>) input.get(0)).getObject() instanceof OutputStreamHandle)) {
			throw new IllegalArgumentException("First input term must be an output stream handle!");
		}
		OutputStreamHandle dstHandle = ((ConstantTerm<OutputStreamHandle>) input.get(0)).getObject();
		if (!(input.get(1) instanceof ConstantTerm) || !(((ConstantTerm<?>) input.get(1)).getObject() instanceof String)
				|| ((ConstantTerm<?>) input.get(1)).isSymbolic()) {
			throw new IllegalArgumentException("Second input term must be a string constant!");
		}
		String str = ((ConstantTerm<String>) input.get(1)).getObject();
		// TODO this needs some built-in conversion function
		byte[] data = str.getBytes();
		OutputStream dst = dstHandle.getStream();
		try {
			dst.write(data);
			return Terms.newFunctionTerm("writeResult", Terms.newSymbolicConstant("ok"));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public static FunctionTerm outputStreamClose(List<Term> input) {
		if (input.size() != 1) {
			// TODO do properly
			throw new IllegalArgumentException("Incorrect arity!");
		}
		if (!(input.get(0) instanceof ConstantTerm) || !(((ConstantTerm<?>) input.get(0)).getObject() instanceof OutputStreamHandle)) {
			throw new IllegalArgumentException("First input term must be an output stream handle!");
		}
		OutputStreamHandle handle = ((ConstantTerm<OutputStreamHandle>) input.get(0)).getObject();
		try {
			handle.getStream().close();
			return Terms.newFunctionTerm("closeResult", Terms.newSymbolicConstant("ok"));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static FunctionTerm fileOpenInputStream(List<Term> input) {
		if (input.size() != 1) {
			// TODO do properly
			throw new IllegalArgumentException("Incorrect arity!");
		}
		Term inTerm = input.get(0);
		if (!(inTerm instanceof ConstantTerm)) {
			throw new IllegalArgumentException("Input must be a string constant!");
		}
		ConstantTerm<?> inConst = (ConstantTerm<?>) inTerm;
		if (!(inConst.getObject() instanceof String) || inConst.isSymbolic()) {
			throw new IllegalArgumentException("Input must be a string constant!");
		}
		String path = (String) inConst.getObject();
		try {
			InputStreamHandle streamHandle = new InputStreamHandle(ID_GEN.getNextId(), Files.newBufferedReader(Paths.get(path)));
			return Terms.newFunctionTerm("stream", Terms.newConstant(streamHandle));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public static FunctionTerm inputStreamReadLine(List<Term> input) {
		if (input.size() != 1) {
			// TODO do properly
			throw new IllegalArgumentException("Incorrect arity!");
		}
		if (!(input.get(0) instanceof ConstantTerm) || !(((ConstantTerm<?>) input.get(0)).getObject() instanceof InputStreamHandle)) {
			throw new IllegalArgumentException("First input term must be an input stream handle!");
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
			return Terms.newFunctionTerm("ok", lineTerm);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public static FunctionTerm inputStreamClose(List<Term> input) {
		if (input.size() != 1) {
			// TODO do properly
			throw new IllegalArgumentException("Incorrect arity!");
		}
		if (!(input.get(0) instanceof ConstantTerm) || !(((ConstantTerm<?>) input.get(0)).getObject() instanceof InputStreamHandle)) {
			throw new IllegalArgumentException("First input term must be an input stream handle!");
		}
		InputStreamHandle handle = ((ConstantTerm<InputStreamHandle>) input.get(0)).getObject();
		try {
			handle.getStream().close();
			return Terms.newFunctionTerm("closeResult", Terms.newSymbolicConstant("ok"));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
