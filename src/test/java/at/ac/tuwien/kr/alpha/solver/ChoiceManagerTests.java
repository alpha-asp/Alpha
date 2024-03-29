/**
 * Copyright (c) 2017 Siemens AG
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.solver;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.InternalProgram;
import at.ac.tuwien.kr.alpha.common.program.NormalProgram;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

public class ChoiceManagerTests {
	private Grounder grounder;
	private ChoiceManager choiceManager;
	private AtomStore atomStore;

	@BeforeEach
	public void setUp() {
		Alpha system = new Alpha();
		String testProgram = "h :- b1, b2, not b3, not b4.";
		InputProgram parsedProgram = new ProgramParser().parse(testProgram);
		NormalProgram normalProgram = system.normalizeProgram(parsedProgram);
		InternalProgram internalProgram = InternalProgram.fromNormalProgram(normalProgram);
		atomStore = new AtomStoreImpl();
		grounder = new NaiveGrounder(internalProgram, atomStore, true);
		WritableAssignment assignment = new TrailAssignment(atomStore);
		NoGoodStore store = new NoGoodStoreAlphaRoaming(assignment);
		choiceManager = new ChoiceManager(assignment, store);
	}

	@Test
	public void testIsAtomChoice() {
		Collection<NoGood> noGoods = getNoGoods();
		choiceManager.addChoiceInformation(grounder.getChoiceAtoms(), grounder.getHeadsToBodies());
		for (NoGood noGood : noGoods) {
			for (Integer literal : noGood) {
				int atom = atomOf(literal);
				String atomToString = atomStore.atomToString(atom);
				if (atomToString.startsWith(RuleAtom.PREDICATE.getName())) {
					assertTrue(choiceManager.isAtomChoice(atom), "Atom not choice: " + atomToString);
				}
			}
		}
	}

	private Collection<NoGood> getNoGoods() {
		return grounder.getNoGoods(null).values();
	}
}
