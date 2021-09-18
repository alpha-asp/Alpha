package at.ac.tuwien.kr.alpha.core.parser.aspcore2;

import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;

public class ASPCore2ProgramParser extends AbstractProgramParser {

	public ASPCore2ProgramParser() {
		super();
	}
	
	public ASPCore2ProgramParser(Map<String, PredicateInterpretation> externals) {
		super(externals);
	}
	
	@Override
	protected ParseTreeVisitor<Object> createParseTreeVisitor() {
		return new ASPCore2ParseTreeVisitor(this.getPreloadedExternals());
	}

}
