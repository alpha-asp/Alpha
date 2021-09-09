package at.ac.tuwien.kr.alpha.core.parser.evolog;

import java.util.Map;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.rules.Head;
import at.ac.tuwien.kr.alpha.core.antlr.ASPCore2Parser;
import at.ac.tuwien.kr.alpha.core.parser.aspcore2.ASPCore2ParseTreeVisitor;

public class EvologParseTreeVisitor extends ASPCore2ParseTreeVisitor {

	public EvologParseTreeVisitor(Map<String, PredicateInterpretation> externals, boolean acceptVariables) {
		super(externals, acceptVariables);
	}

	public EvologParseTreeVisitor(Map<String, PredicateInterpretation> externals) {
		this(externals, true);
	}

	@Override
	public Head visitAction(ASPCore2Parser.ActionContext ctx) {
		return null;
	}
	
}
