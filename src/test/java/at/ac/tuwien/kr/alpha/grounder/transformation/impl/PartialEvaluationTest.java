package at.ac.tuwien.kr.alpha.grounder.transformation.impl;

import org.junit.Test;

import at.ac.tuwien.kr.alpha.Alpha;
import at.ac.tuwien.kr.alpha.common.program.impl.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram;

public class PartialEvaluationTest {
	
	@Test
	public void smokeTest1() {
		String aspStr = "p(a). q(X) :- p(X).";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		InternalProgram internalProgram = system.performProgramPreprocessing(prg);
		InternalProgram evaluated = new PartialEvaluation().apply(internalProgram);
	}

	@Test
	public void smokeTest2() {
		String aspStr = "p(a). q(b). p(c). q(d). s(X, Y):- p(X), q(Y).";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		InternalProgram internalProgram = system.performProgramPreprocessing(prg);
		InternalProgram evaluated = new PartialEvaluation().apply(internalProgram);
	}	
	
	@Test
	public void smokeTest3() {
		String aspStr = "p(a). q(b). p(c). q(d). r(c). s(X, Y):- p(X), q(Y), not r(X).";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		InternalProgram internalProgram = system.performProgramPreprocessing(prg);
		InternalProgram evaluated = new PartialEvaluation().apply(internalProgram);
	}
	
	@Test
	public void smokeTest4() {
		String aspStr = "p(a). q(b). p(c). q(d). r(c). p(e). q(f). u(e, f). s(X, Y):- p(X), q(Y), not r(X), not u(X, Y).";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		InternalProgram internalProgram = system.performProgramPreprocessing(prg);
		InternalProgram evaluated = new PartialEvaluation().apply(internalProgram);
	}
	
}
