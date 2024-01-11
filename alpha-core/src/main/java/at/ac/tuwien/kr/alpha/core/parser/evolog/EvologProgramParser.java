package at.ac.tuwien.kr.alpha.core.parser.evolog;

import java.util.Collections;
import java.util.Map;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.core.actions.ActionImplementationProvider;
import at.ac.tuwien.kr.alpha.core.parser.AbstractProgramParser;

public class EvologProgramParser extends AbstractProgramParser {

	public EvologProgramParser(ActionImplementationProvider actionImplementationProvider, Map<String, PredicateInterpretation> externals) {
		super(externals);
		registerExternal("stdin", actionImplementationProvider.getStdinTerm());
		registerExternal("stdout", actionImplementationProvider.getStdoutTerm());
	}

	public EvologProgramParser(ActionImplementationProvider actionImplementationProvider) {
		this(actionImplementationProvider, Collections.emptyMap());
	}

	@Override
	protected EvologParseTreeVisitor createParseTreeVisitor(Map<String, PredicateInterpretation> externals) {
		return new EvologParseTreeVisitor(externals);
	}

}
