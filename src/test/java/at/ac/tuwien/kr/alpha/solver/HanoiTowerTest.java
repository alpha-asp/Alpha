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

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedFact;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.SortedSet;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link AbstractSolver} using some hanoi tower test cases (see https://en.wikipedia.org/wiki/Tower_of_Hanoi).
 *
 */
@Ignore("disabled to save resources during CI")
public class HanoiTowerTest extends AbstractSolverTests {
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

	@Before
	public void printSolverName() {
		System.out.println(solverName);
	}

	@Test(timeout = 10000)
	public void testInstance1() throws IOException {
		testHanoiTower(1);
	}

	@Test(timeout = 10000)
	public void testInstance2() throws IOException {
		testHanoiTower(2);
	}

	@Test(timeout = 10000)
	public void testInstance3() throws IOException {
		testHanoiTower(3);
	}

	@Test(timeout = 10000)
	public void testInstance4() throws IOException {
		testHanoiTower(4);
	}

	@Test(timeout = 60000)
	public void testSimple() throws IOException {
		testHanoiTower("simple");
	}

	private void testHanoiTower(int instance) throws IOException {
		testHanoiTower(String.valueOf(instance));
	}

	private void testHanoiTower(String instance) throws IOException {
		ANTLRFileStream programInputStream = new ANTLRFileStream(Paths.get("src", "test", "resources", "HanoiTower_Alpha.asp").toString());
		ANTLRFileStream instanceInputStream = new ANTLRFileStream(Paths.get("src", "test", "resources", "HanoiTower_instances", instance + ".asp").toString());
		ParsedProgram parsedProgram = parseVisit(programInputStream);
		parsedProgram.accumulate(parseVisit(instanceInputStream));
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = getInstance(grounder);
		Optional<AnswerSet> answerSet = solver.stream().findFirst();
		System.out.println(answerSet);
		checkGoal(parsedProgram, answerSet.get());
	}

	/**
	 * Conducts a very simple, non-comprehensive goal check (i.e. it may classify answer sets as correct that are actually wrong) by checking if for every goal/3
	 * fact in the input there is a corresponding on/3 atom in the output.
	 */
	private void checkGoal(ParsedProgram parsedProgram, AnswerSet answerSet) {
		Predicate ongoal = new BasicPredicate("ongoal", 2);
		Predicate on = new BasicPredicate("on", 3);
		int steps = getSteps(parsedProgram);
		SortedSet<Atom> onInstancesInAnswerSet = answerSet.getPredicateInstances(on);
		for (ParsedFact fact : parsedProgram.facts) {
			ParsedAtom atom = fact.getFact();
			if (atom.getPredicate().equals(ongoal.getPredicateName()) && atom.getArity() == ongoal.getArity()) {
				Term expectedTop = ConstantTerm.getInstance(atom.getTerms().get(0).toString());
				Term expectedBottom = ConstantTerm.getInstance(atom.getTerms().get(1).toString());
				Term expectedSteps = ConstantTerm.getInstance(String.valueOf(steps));
				Atom expectedAtom = new BasicAtom(on, expectedSteps, expectedBottom, expectedTop);
				assertTrue("Answer set does not contain " + expectedAtom, onInstancesInAnswerSet.contains(expectedAtom));
			}
		}
	}

	private int getSteps(ParsedProgram parsedProgram) {
		Predicate steps = new BasicPredicate("steps", 1);
		for (ParsedFact fact : parsedProgram.facts) {
			ParsedAtom atom = fact.getFact();
			if (atom.getPredicate().equals(steps.getPredicateName()) && atom.getArity() == steps.getArity()) {
				return Integer.valueOf(atom.getTerms().get(0).toTerm().toString());
			}
		}
		throw new IllegalArgumentException("No steps atom found in input program.");
	}

}
