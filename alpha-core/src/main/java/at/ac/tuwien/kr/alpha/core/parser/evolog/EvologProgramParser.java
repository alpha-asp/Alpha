package at.ac.tuwien.kr.alpha.core.parser.evolog;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

import at.ac.tuwien.kr.alpha.core.parser.aspcore2.AbstractProgramParser;

public class EvologProgramParser extends AbstractProgramParser {

	@Override
	protected ParseTreeVisitor<Object> createParseTreeVisitor() {
		return new EvologParseTreeVisitor(this.getPreloadedExternals());
	}

}
