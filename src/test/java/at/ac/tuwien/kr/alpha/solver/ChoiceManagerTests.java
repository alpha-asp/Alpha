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

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static org.junit.Assert.assertTrue;

public class ChoiceManagerTests extends AbstractSolverTests {
	/**
	 * Sets the logging level to TRACE. Useful for debugging; call at beginning of test case.
	 */
	private static void enableTracing() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(ch.qos.logback.classic.Level.TRACE);
	}

	private static void enableDebugLog() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.DEBUG);
	}

	private Grounder grounder;
	private Assignment assignment;
	private ChoiceManager choiceManager;

	@Before
	public void setUp() throws IOException {
		String testProgram = "h :- b1, b2, not b3, not b4.";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		this.grounder = new NaiveGrounder(parsedProgram);
		this.assignment = new BasicAssignment();
		this.choiceManager = new ChoiceManager(assignment);
	}

	@Test
	public void testIsAtomChoice() {
		Collection<NoGood> noGoods = getNoGoods();
		choiceManager.addChoiceInformation(grounder.getChoiceAtoms());
		for (NoGood noGood : noGoods) {
			for (Integer literal : noGood) {
				int atom = atomOf(literal);
				String atomToString = grounder.atomToString(atom);
				if (atomToString.startsWith("_R_")) {
					assertTrue("Atom not choice: " + atomToString, choiceManager.isAtomChoice(atom));
				}
			}
		}
	}

	private Collection<NoGood> getNoGoods() {
		return grounder.getNoGoods().values();
	}

}
