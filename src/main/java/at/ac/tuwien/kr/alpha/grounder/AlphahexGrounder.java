package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;

public class AlphahexGrounder extends NaiveGrounder {
	public AlphahexGrounder(ParsedProgram program) {
		super(program);
	}

	public AlphahexGrounder(ParsedProgram program, java.util.function.Predicate<Predicate> filter) {
		super(program, filter);
	}
}
