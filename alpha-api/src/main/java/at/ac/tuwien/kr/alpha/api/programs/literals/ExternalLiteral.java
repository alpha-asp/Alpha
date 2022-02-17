package at.ac.tuwien.kr.alpha.api.programs.literals;

import java.util.function.Function;

import at.ac.tuwien.kr.alpha.api.programs.atoms.ExternalAtom;

public interface ExternalLiteral extends FixedInterpretationLiteral {

	@Override
	ExternalAtom getAtom();

	ExternalLiteral renameVariables(Function<String, String> mapping);
	
}
