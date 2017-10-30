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
package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.ArrayAssignment;
import at.ac.tuwien.kr.alpha.solver.TestableChoiceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Predicate;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests assumptions made by {@link AlphaHeuristic}. Even if these test cases do not test {@link AlphaHeuristic} directly, it will break if these test cases
 * break.
 * 
 * Copyright (c) 2017 Siemens AG
 *
 */
public class AlphaHeuristicTestAssumptions {
	private Grounder grounder;
	private Assignment assignment;
	private TestableChoiceManager choiceManager;
	
	@BeforeEach
	public void setUp() throws IOException {
		String testProgram = "h :- b1, b2, not b3, not b4.";
		Program parsedProgram = new ProgramParser().parse(testProgram);
		this.grounder = new NaiveGrounder(parsedProgram);
		this.assignment = new ArrayAssignment();
		this.choiceManager = new TestableChoiceManager(assignment);
	}

	@Test
	@Disabled("Test strictly depends on generated NoGoods. Grounder optimisations change generated NoGoods.")
	public void testNumbersOfNoGoods_GrounderIsAtomChoicePoint() {
		testNumbersOfNoGoods(grounder::isAtomChoicePoint);
	}

	@Test
	@Disabled("Test strictly depends on generated NoGoods. Grounder optimisations change generated NoGoods.")
	public void testNumbersOfNoGoods_ChoiceManagerIsAtomChoice() {
		testNumbersOfNoGoods(choiceManager::isAtomChoice);
	}

	private void testNumbersOfNoGoods(Predicate<? super Integer> isRuleBody) {
		int n = 0;
		int bodyNotHead = 0;
		int bodyElementsNotBody = 0;
		int noHead = 0;
		int other = 0;

		Collection<NoGood> noGoods = getNoGoods();
		choiceManager.addChoiceInformation(grounder.getChoiceAtoms());
		for (NoGood noGood : noGoods) {
			n++;
			boolean knownType = false;
			if (noGood.isBodyNotHead(isRuleBody)) {
				bodyNotHead++;
				knownType = true;
			}
			if (noGood.isBodyElementsNotBody(isRuleBody)) {
				bodyElementsNotBody++;
				knownType = true;
			}
			if (!noGood.hasHead()) {
				noHead++;
				knownType = true;
			}
			if (!knownType) {
				other++;
			}
		}

		assertEquals(1, bodyNotHead, "Unexpected number of bodyNotHead nogoods");
		assertEquals(1, bodyElementsNotBody, "Unexpected number of bodyElementsNotBody nogoods");
		assertGreaterThan( 4, noHead, "Unexpected number of nogoods without head");

		// there may be other nogoods (e.g. for ChoiceOn, ChoiceOff) which we do not care for here
		System.out.println("Total number of NoGoods: " + n);
		System.out.println("Number of NoGoods of unknown type: " + other);
	}

	@Test
	void testIsAtomChoice_GrounderIsAtomChoicePoint() {
		testIsAtomChoice(grounder::isAtomChoicePoint);
	}

	@Test
	void testIsAtomChoice_ChoiceManagerIsAtomChoice() {
		testIsAtomChoice(choiceManager::isAtomChoice);
	}

	private void testIsAtomChoice(Predicate<? super Integer> isRuleBody) {
		Collection<NoGood> noGoods = getNoGoods();
		choiceManager.addChoiceInformation(grounder.getChoiceAtoms());
		for (NoGood noGood : noGoods) {
			for (Integer literal : noGood) {
				int atom = atomOf(literal);
				String atomToString = grounder.atomToString(atom);
				if (atomToString.startsWith("_R_")) {
					assertTrue(isRuleBody.test(atom), "Atom not choice: " + atomToString);
				}
			}
		}
	}

	private Collection<NoGood> getNoGoods() {
		return grounder.getNoGoods(null).values();
	}

	private void assertGreaterThan(long expected, long actual, String message) {
		assertTrue(actual > expected, message);
	}
}
