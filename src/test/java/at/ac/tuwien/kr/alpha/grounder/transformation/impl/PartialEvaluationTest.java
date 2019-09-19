package at.ac.tuwien.kr.alpha.grounder.transformation.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.Alpha;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.impl.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram;

public class PartialEvaluationTest {
	
	@Test
	public void testDuplicateFacts() {
		String aspStr = "p(a). p(b). q(b). q(X) :- p(X).";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		InternalProgram internalProgram = system.performProgramPreprocessing(prg);
		InternalProgram evaluated = new PartialEvaluation().apply(internalProgram);
		BasicAtom qOfB = BasicAtom.newInstance("q", "b");
		List<Atom> facts = evaluated.getFacts();
		int numQOfB = 0;
		for(Atom at : facts) {
			if(at.equals(qOfB)) {
				numQOfB++;
			}
		}
		Assert.assertEquals(1, numQOfB);
	}

}
