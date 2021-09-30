package at.ac.tuwien.kr.alpha.api.config;

import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.ProgramTransformation;

public interface ProgramTransformationFactory {
	
	ProgramTransformation<InputProgram,NormalProgram> createProgramNormalizationTransformation();

}
