package at.ac.tuwien.kr.alpha.core.parser.aspcore2;

import java.util.Map;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.core.parser.AbstractProgramParser;

public class ASPCore2ProgramParser extends AbstractProgramParser {

	public ASPCore2ProgramParser() {
		super();
	}

	public ASPCore2ProgramParser(Map<String, PredicateInterpretation> externals) {
		super(externals);
	}

	@Override
	protected ASPCore2ParseTreeVisitor createParseTreeVisitor(Map<String, PredicateInterpretation> externals) {
		return new ASPCore2ParseTreeVisitor(externals);
	}

}
