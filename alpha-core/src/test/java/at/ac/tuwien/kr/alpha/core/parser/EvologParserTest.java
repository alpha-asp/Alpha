package at.ac.tuwien.kr.alpha.core.parser;

import at.ac.tuwien.kr.alpha.core.actions.DefaultActionImplementationProvider;
import at.ac.tuwien.kr.alpha.core.parser.evolog.EvologProgramParser;

public class EvologParserTest extends ParserTest {

	protected EvologParserTest() {
		super(new EvologProgramParser(new DefaultActionImplementationProvider()));
	}
	
	

}
