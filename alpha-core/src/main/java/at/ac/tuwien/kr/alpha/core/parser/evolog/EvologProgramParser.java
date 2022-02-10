package at.ac.tuwien.kr.alpha.core.parser.evolog;

import java.util.Map;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.core.parser.AbstractProgramParser;

public class EvologProgramParser extends AbstractProgramParser {

	public EvologProgramParser(Map<String, PredicateInterpretation> externals) {
		super(externals);
	}

	public EvologProgramParser() {
		super();
	}

	@Override
	protected EvologParseTreeVisitor createParseTreeVisitor(Map<String, PredicateInterpretation> externals) {
		return new EvologParseTreeVisitor(externals);
	}

}
