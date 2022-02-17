package at.ac.tuwien.kr.alpha.api.programs.literals;

import java.util.function.Function;

public interface BasicLiteral extends Literal {

	BasicLiteral renameVariables(Function<String, String> mapping);

}
