/**
 * Copyright (c) 2016, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
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
import at.ac.tuwien.kr.alpha.common.BasicAnswerSet;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Symbol;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.ChoiceGrounder;
import at.ac.tuwien.kr.alpha.grounder.DummyGrounder;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SolverTests extends AbstractSolverTests {
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

	private static class Thingy implements Comparable<Thingy> {
		@Override
		public String toString() {
			return "thingy";
		}

		@Override
		public int compareTo(Thingy o) {
			return 0;
		}
	}

	@Test
	public void testObjectProgram() throws IOException {
		Thingy thingy = new Thingy();

		Atom fact = new BasicAtom(new BasicPredicate("foo", 1), ConstantTerm.getInstance(thingy));

		Program program = new Program(
			Collections.singletonList(fact),
			Collections.emptyList(),
			Collections.emptyList()
		);

		Grounder grounder = new NaiveGrounder(program);
		Solver solver = getInstance(grounder);

		List<AnswerSet> answerSets = solver.collectList();

		assertEquals(1, answerSets.size());

		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("foo").instance(thingy)
			.build();

		AnswerSet actual = answerSets.get(0);
		assertEquals(expected, actual);
	}

	@Test
	public void testFactsOnlyProgram() throws IOException {
		String testProgram = "p(a). p(b). foo(13). foo(16). q(a). q(c).";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		Grounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		List<AnswerSet> answerSets = solver.collectList();

		assertEquals(1, answerSets.size());

		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("q").symbolicInstance("a").symbolicInstance("c")
			.predicate("p").symbolicInstance("a").symbolicInstance("b")
			.predicate("foo").instance(13).instance(16)
			.build();

		assertEquals(expected, answerSets.get(0));
	}

	@Test
	public void testSimpleRule() throws Exception {
		String testProgram = "p(a). p(b). r(X) :- p(X).";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		Grounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		List<AnswerSet> answerSets = solver.collectList();

		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("p").instance(Symbol.getInstance("a")).instance(Symbol.getInstance("b"))
			.predicate("r").instance(Symbol.getInstance("a")).instance(Symbol.getInstance("b"))
			.build();

		assertEquals(1, answerSets.size());
		assertEquals(expected, answerSets.get(0));
	}

	@Test
	public void testSimpleRuleWithGroundPart() throws Exception {
		String testProgram =
			"p(1)." +
				"p(2)." +
				"q(X) :-  p(X), p(1).";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		Grounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		List<AnswerSet> answerSets = solver.collectList();

		assertEquals(1, answerSets.size());
		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("q").instance(1).instance(2)
			.predicate("p").instance(1).instance(2)
			.build();

		assertEquals(expected, answerSets.get(0));
	}

	@Test
	public void testProgramZeroArityPredicates() throws Exception {
		String testProgram = "a. p(X) :- b, r(X).";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		Grounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		List<AnswerSet> answerSets = solver.collectList();

		assertEquals(1, answerSets.size());

		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("a")
			.build();

		assertEquals(expected, answerSets.get(0));
	}

	@Test
	public void testGuessingGroundProgram() throws Exception {
		Solver solver = getInstance(new NaiveGrounder(parseVisit("a :- not b. b :- not a.").toProgram()));

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new BasicAnswerSet.Builder().predicate("a").build(),
			new BasicAnswerSet.Builder().predicate("b").build()
		));

		assertEquals(expected, solver.collectSet());
	}

	@Test
	public void testGuessingProgramNonGround() throws Exception {
		String testProgram = "dom(1). dom(2). dom(3)." +
			"p(X) :- dom(X), not q(X)." +
			"q(X) :- dom(X), not p(X).";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		final BasicAnswerSet.Builder base = new BasicAnswerSet.Builder()
			.predicate("dom").instance(1).instance(2).instance(3);

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new BasicAnswerSet.Builder(base)
				.predicate("q").instance(1).instance(2)
				.predicate("p").instance(3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("q").instance(1)
				.predicate("p").instance(2).instance(3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("q").instance(2)
				.predicate("p").instance(1).instance(3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("p").instance(1).instance(2).instance(3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("q").instance(1).instance(2).instance(3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("q").instance(1).instance(3)
				.predicate("p").instance(2)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("q").instance(2).instance(3)
				.predicate("p").instance(1)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("q").instance(3)
				.predicate("p").instance(1).instance(2)
				.build()
		));

		Set<AnswerSet> actual = solver.collectSet();

		assertEquals(expected, actual);
	}

	@Test
	public void dummyGrounder() {
		assertEquals(DummyGrounder.EXPECTED, getInstance(new DummyGrounder()).collectSet());
	}

	@Test
	public void choiceGrounder() {
		assertEquals(ChoiceGrounder.EXPECTED, getInstance(new ChoiceGrounder()).collectSet());
	}

	@Test
	public void guessingProgram3Way() throws IOException {
		String testProgram = "a :- not b, not c." +
			"b :- not a, not c." +
			"c :- not a, not b.";

		ParsedProgram parsedProgram = parseVisit(testProgram);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());

		Solver solver = getInstance(grounder);

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new BasicAnswerSet.Builder()
				.predicate("a")
				.build(),
			new BasicAnswerSet.Builder()
				.predicate("b")
				.build(),
			new BasicAnswerSet.Builder()
				.predicate("c")
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(expected, answerSets);
	}

	@Test
	public void emptyProgramYieldsEmptyAnswerSet() throws IOException {
		ParsedProgram parsedProgram = parseVisit("");
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		List<AnswerSet> answerSets = getInstance(grounder).collectList();
		assertEquals(1, answerSets.size());
		assertEquals(BasicAnswerSet.EMPTY, answerSets.get(0));
	}

	@Test
	public void guessingMultipleAnswerSets() throws IOException {
		String testProgram = "a :- not nota.\n" +
			"nota :- not a.\n" +
			"b :- not notb.\n" +
			"notb :- not b.\n" +
			"c :- not notc.\n" +
			"notc :- not c.\n" +
			":- nota,notb,notc.";

		ParsedProgram parsedProgram = parseVisit(testProgram);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());

		Solver solver = getInstance(grounder);

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new BasicAnswerSet.Builder()
				.predicate("a")
				.predicate("b")
				.predicate("c")
				.build(),
			new BasicAnswerSet.Builder()
				.predicate("nota")
				.predicate("b")
				.predicate("c")
				.build(),
			new BasicAnswerSet.Builder()
				.predicate("a")
				.predicate("notb")
				.predicate("c")
				.build(),
			new BasicAnswerSet.Builder()
				.predicate("nota")
				.predicate("notb")
				.predicate("c")
				.build(),
			new BasicAnswerSet.Builder()
				.predicate("a")
				.predicate("b")
				.predicate("notc")
				.build(),
			new BasicAnswerSet.Builder()
				.predicate("nota")
				.predicate("b")
				.predicate("notc")
				.build(),
			new BasicAnswerSet.Builder()
				.predicate("a")
				.predicate("notb")
				.predicate("notc")
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(expected, answerSets);
	}

	@Test
	public void builtinAtoms() throws IOException {
		String testProgram = "dom(1). dom(2). dom(3). dom(4). dom(5)." +
			"p(X) :- dom(X), X = 4." +
			"r(Y) :- dom(Y), Y <= 2.";

		ParsedProgram parsedProgram = parseVisit(testProgram);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());

		Solver solver = getInstance(grounder);

		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(
			new BasicAnswerSet.Builder()
				.predicate("dom")
				.instance(1)
				.instance(2)
				.instance(3)
				.instance(4)
				.instance(5)
				.predicate("p")
				.instance(4)
				.predicate("r")
				.instance(1)
				.instance(2)
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(expected, answerSets);
	}

	@Test
	public void builtinAtomsGroundRule() throws IOException {
		String testProgram = "a :- 13 != 4." +
			"b :- 2 != 3, 2 = 3." +
			"c :- 2 <= 3, not 2 > 3.";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());

		Solver solver = getInstance(grounder);

		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(
			new BasicAnswerSet.Builder()
				.predicate("a")
				.predicate("c")
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(expected, answerSets);
	}

	@Test
	public void guessingProgramConstraint() throws IOException {
		String testProgram =
			"eq(1,1).\n" +
			"eq(2,2).\n" +
			"eq(3,3).\n" +
			"var(1).\n" +
			"var(2).\n" +
			"var(3).\n" +
			"val(VAR,1):-var(VAR),not val(VAR,2),not val(VAR,3).\n" +
			"val(VAR,2):-var(VAR),not val(VAR,1),not val(VAR,3).\n" +
			"val(VAR,3):-var(VAR),not val(VAR,1),not val(VAR,2).\n" +
			"%:- val(VAR1,VAL1), val(VAR2,VAL2), eq(VAL1,VAL2), not eq(VAR1,VAR2).\n" +
			":- eq(VAL1,VAL2), not eq(VAR1,VAR2), val(VAR1,VAL1), val(VAR2,VAL2).";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		final BasicAnswerSet.Builder base = new BasicAnswerSet.Builder()
			.predicate("eq")
			.instance(1, 1)
			.instance(2, 2)
			.instance(3, 3)
			.predicate("var")
			.instance(1)
			.instance(2)
			.instance(3);

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new BasicAnswerSet.Builder(base)
				.predicate("val")
				.instance(1, 1)
				.instance(2, 2)
				.instance(3, 3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("val")
				.instance(1, 1)
				.instance(3, 2)
				.instance(2, 3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("val")
				.instance(2, 1)
				.instance(1, 2)
				.instance(3, 3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("val")
				.instance(2, 1)
				.instance(3, 2)
				.instance(1, 3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("val")
				.instance(3, 1)
				.instance(1, 2)
				.instance(2, 3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("val")
				.instance(3, 1)
				.instance(2, 2)
				.instance(1, 3)
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(expected, answerSets);
	}

	@Test
	public void guessingProgramConstraintPermutation() throws IOException {
		String testProgram =
			"eq(1,1).\n" +
				"eq(2,2).\n" +
				"eq(3,3).\n" +
				"var(1).\n" +
				"var(2).\n" +
				"var(3).\n" +
				"val(VAR,1):-var(VAR),not val(VAR,2),not val(VAR,3).\n" +
				"val(VAR,2):-var(VAR),not val(VAR,1),not val(VAR,3).\n" +
				"val(VAR,3):-var(VAR),not val(VAR,1),not val(VAR,2).\n" +
				":- val(VAR1,VAL1), val(VAR2,VAL2), eq(VAL1,VAL2), not eq(VAR1,VAR2).\n" +
				"%:- eq(VAL1,VAL2), not eq(VAR1,VAR2), val(VAR1,VAL1), val(VAR2,VAL2).";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		final BasicAnswerSet.Builder base = new BasicAnswerSet.Builder()
			.predicate("eq")
			.instance(1, 1)
			.instance(2, 2)
			.instance(3, 3)
			.predicate("var")
			.instance(1)
			.instance(2)
			.instance(3);

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new BasicAnswerSet.Builder(base)
				.predicate("val")
				.instance(1, 1)
				.instance(2, 2)
				.instance(3, 3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("val")
				.instance(1, 1)
				.instance(3, 2)
				.instance(2, 3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("val")
				.instance(2, 1)
				.instance(1, 2)
				.instance(3, 3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("val")
				.instance(2, 1)
				.instance(3, 2)
				.instance(1, 3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("val")
				.instance(3, 1)
				.instance(1, 2)
				.instance(2, 3)
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("val")
				.instance(3, 1)
				.instance(2, 2)
				.instance(1, 3)
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(expected, answerSets);
	}

	@Test
	public void simpleNoPropagation() throws IOException {
		String testProgram = "val(1,1).\n" +
			"val(2,2).\n" +
			"something:- val(VAR1,VAL1), val(VAR2,VAL2), anything(VAL1,VAL2).";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(
			new BasicAnswerSet.Builder()
				.predicate("val")
				.instance(1, 1)
				.instance(2, 2)
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(expected, answerSets);
	}

	@Test
	public void guessingAndPropagationAfterwards() throws IOException {
		String testProgram = "node(a).\n" +
			"node(b).\n" +
			"in(X) :- not out(X), node(X).\n" +
			"out(X) :- not in(X), node(X).\n" +
			"pair(X,Y) :- in(X), in(Y).";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		final BasicAnswerSet.Builder base = new BasicAnswerSet.Builder()
			.predicate("node")
			.instance(Symbol.getInstance("a"))
			.instance(Symbol.getInstance("b"));

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new BasicAnswerSet.Builder(base)
				.predicate("in")
				.instance(Symbol.getInstance("a"))
				.instance(Symbol.getInstance("b"))
				.predicate("pair")
				.instance(Symbol.getInstance("a"), Symbol.getInstance("a"))
				.instance(Symbol.getInstance("a"), Symbol.getInstance("b"))
				.instance(Symbol.getInstance("b"), Symbol.getInstance("a"))
				.instance(Symbol.getInstance("b"), Symbol.getInstance("b"))
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("in")
				.instance(Symbol.getInstance("b"))
				.predicate("out")
				.instance(Symbol.getInstance("a"))
				.predicate("pair")
				.instance(Symbol.getInstance("b"), Symbol.getInstance("b"))
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("in")
				.instance(Symbol.getInstance("a"))
				.predicate("out")
				.instance(Symbol.getInstance("b"))
				.predicate("pair")
				.instance(Symbol.getInstance("a"), Symbol.getInstance("a"))
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("out")
				.instance(Symbol.getInstance("a"))
				.instance(Symbol.getInstance("b"))
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(expected, answerSets);
	}

	@Test
	public void guessingAndConstraints() throws IOException {
		String testProgram = "node(a).\n" +
			"node(b).\n" +
			"edge(b,a).\n" +
			"in(X) :- not out(X), node(X).\n" +
			"out(X) :- not in(X), node(X).\n" +
			":- in(X), in(Y), edge(X,Y).";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		final BasicAnswerSet.Builder base = new BasicAnswerSet.Builder()
			.predicate("node")
			.instance(Symbol.getInstance("a"))
			.instance(Symbol.getInstance("b"));

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new BasicAnswerSet.Builder(base)
				.predicate("in")
				.instance(Symbol.getInstance("b"))
				.predicate("out")
				.instance(Symbol.getInstance("a"))
				.predicate("edge")
				.instance(Symbol.getInstance("b"), Symbol.getInstance("a"))
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("in")
				.instance(Symbol.getInstance("a"))
				.predicate("out")
				.instance(Symbol.getInstance("b"))
				.predicate("edge")
				.instance(Symbol.getInstance("b"), Symbol.getInstance("a"))
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("out")
				.instance(Symbol.getInstance("a"))
				.instance(Symbol.getInstance("b"))
				.predicate("edge")
				.instance(Symbol.getInstance("b"), Symbol.getInstance("a"))
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(expected, answerSets);
	}

	@Test
	public void testUnsatisfiableProgram() throws IOException {
		String testProgram = "p(a). p(b). :- p(a), p(b).";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		Grounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		List<AnswerSet> answerSets = solver.collectList();

		assertEquals(0, answerSets.size());
	}

	@Test
	public void testFunctionTermEquality() throws IOException {
		String testProgram = "r1(f(a,b)). r2(f(a,b)). a :- r1(X), r2(Y), X = Y.";
		ParsedProgram parsedProgram = parseVisit(testProgram);
		Grounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(
			new BasicAnswerSet.Builder()
				.predicate("r1")
				.parseInstance("f(a,b)")
				.predicate("r2")
				.parseInstance("f(a,b)")
				.predicate("a")
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();

		assertEquals(expected, answerSets);
	}

	@Test
	public void builtinInequality() throws IOException {
		String program = "location(a1).\n" +
			"region(r1).\n" +
			"region(r2).\n" +
			"\n" +
			"assign(L,R) :- location(L), region(R), not nassign(L,R).\n" +
			"nassign(L,R) :- location(L), region(R), not assign(L,R).\n" +
			"\n" +
			":- assign(L,R1), assign(L,R2), R1 != R2.\n" +
			"\n" +
			"aux_ext_assign(a1,r1).\n" +
			"aux_ext_assign(a1,r2).\n" +
			"\n" +
			"aux_not_assign(L,R) :- aux_ext_assign(L,R), not assign(L,R).\n" +
			":- aux_not_assign(L,R), assign(L,R).";

		ParsedProgram parsedProgram = parseVisit(program);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());

		Solver solver = getInstance(grounder);

		final BasicAnswerSet.Builder base = new BasicAnswerSet.Builder()
			.predicate("location")
			.instance(Symbol.getInstance("a1"))
			.predicate("region")
			.instance(Symbol.getInstance("r1"))
			.instance(Symbol.getInstance("r2"))
			.predicate("aux_ext_assign")
			.instance(Symbol.getInstance("a1"), Symbol.getInstance("r1"))
			.instance(Symbol.getInstance("a1"), Symbol.getInstance("r2"));

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new BasicAnswerSet.Builder(base)
				.predicate("assign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r2"))
				.predicate("nassign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r1"))
				.predicate("aux_not_assign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r1"))
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("assign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r1"))
				.predicate("nassign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r2"))
				.predicate("aux_not_assign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r2"))
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("nassign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r1"))
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r2"))
				.predicate("aux_not_assign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r1"))
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r2"))
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(expected, answerSets);
	}

	@Test
	public void guessingConstraintsInequality() throws IOException {
		String program = "assign(L, R) :- not nassign(L, R), possible(L, R).\n" +
			"nassign(L, R) :- not assign(L, R), possible(L, R).\n" +
			"\n" +
			"assigned(L) :- assign(L, R).\n" +
			":- possible(L,_), not assigned(L).\n" +
			":- assign(L, R1), assign(L, R2), R1 != R2.\n" +
			"\n" +
			"possible(l1, r1). possible(l3, r3). possible(l4, r1). possible(l4, r3). possible(l5, r4). possible(l6, r2). possible(l7, r3). possible(l8, r2). possible(l9, r1). possible(l9, r4).\n";

		ParsedProgram parsedProgram = parseVisit(program);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());

		Solver solver = getInstance(grounder);

		final BasicAnswerSet.Builder base = new BasicAnswerSet.Builder()
			.predicate("possible")
			.symbolicInstance("l1", "r1")
			.symbolicInstance("l3", "r3")
			.symbolicInstance("l4", "r1")
			.symbolicInstance("l4", "r3")
			.symbolicInstance("l5", "r4")
			.symbolicInstance("l6", "r2")
			.symbolicInstance("l7", "r3")
			.symbolicInstance("l8", "r2")
			.symbolicInstance("l9", "r1")
			.symbolicInstance("l9", "r4")
			.predicate("assign")
			.symbolicInstance("l1", "r1")
			.symbolicInstance("l3", "r3")
			.symbolicInstance("l5", "r4")
			.symbolicInstance("l6", "r2")
			.symbolicInstance("l7", "r3")
			.symbolicInstance("l8", "r2")
			.predicate("assigned")
			.symbolicInstance("l1")
			.symbolicInstance("l3")
			.symbolicInstance("l4")
			.symbolicInstance("l5")
			.symbolicInstance("l6")
			.symbolicInstance("l7")
			.symbolicInstance("l8")
			.symbolicInstance("l9");

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new BasicAnswerSet.Builder(base)
				.predicate("assign")
				.symbolicInstance("l4", "r1")
				.symbolicInstance("l9", "r4")
				.predicate("nassign")
				.symbolicInstance("l4", "r3")
				.symbolicInstance("l9", "r1")
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("assign")
				.symbolicInstance("l4", "r1")
				.symbolicInstance("l9", "r1")
				.predicate("nassign")
				.symbolicInstance("l4", "r3")
				.symbolicInstance("l9", "r4")
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("assign")
				.symbolicInstance("l4", "r3")
				.symbolicInstance("l9", "r4")
				.predicate("nassign")
				.symbolicInstance("l4", "r1")
				.symbolicInstance("l9", "r1")
				.build(),
			new BasicAnswerSet.Builder(base)
				.predicate("assign")
				.symbolicInstance("l4", "r3")
				.symbolicInstance("l9", "r1")
				.predicate("nassign")
				.symbolicInstance("l4", "r1")
				.symbolicInstance("l9", "r4")
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(expected, answerSets);
	}
	@Test
	public void sameVariableTwiceInAtom() throws IOException {
		String program = "p(a, a).\n" +
			"q(X) :- p(X, X).\n";

		ParsedProgram parsedProgram = parseVisit(program);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());

		Solver solver = getInstance(grounder);

		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(
			new BasicAnswerSet.Builder()
				.predicate("p")
				.instance(Symbol.getInstance("a"), Symbol.getInstance("a"))
				.predicate("q")
				.instance(Symbol.getInstance("a"))
				.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(expected, answerSets);
	}

	@Test
	public void sameVariableTwiceInAtomConstraint() throws IOException {
		String program = "p(a, a).\n" +
			":- p(X, X).\n";

		ParsedProgram parsedProgram = parseVisit(program);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());

		Solver solver = getInstance(grounder);

		Set<AnswerSet> answerSets = solver.collectSet();
		assertTrue(answerSets.isEmpty());
	}

	@Test
	public void noPositiveSelfFounding() throws IOException {
		String program = "a :- b.\n" +
			"b:- a.\n" +
			":- not b.";

		ParsedProgram parsedProgram = parseVisit(program);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		Set<AnswerSet> answerSets = solver.collectSet();
		assertTrue(answerSets.isEmpty());
	}

	@Test
	public void noPositiveCycleSelfFoundingGuess() throws IOException {
		String program =
			"c :- not d.\n" +
			"d :- not c." +
			"a :- b, not c.\n" +
			"b:- a.\n" +
			":- not b.";

		ParsedProgram parsedProgram = parseVisit(program);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		Set<AnswerSet> answerSets = solver.collectSet();
		assertTrue(answerSets.isEmpty());
	}

	@Test
	public void conflictFromUnaryNoGood() throws IOException {
		String program =
			"d(b).\n" +
			"sel(X) :- not nsel(X), d(X).\n" +
			"nsel(X) :- not sel(X), d(X).\n" +
			"t(a) :- sel(b).\n" +
			":- t(X).\n";

		ParsedProgram parsedProgram = parseVisit(program);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(
				new BasicAnswerSet.Builder()
						.predicate("d")
						.instance(Symbol.getInstance("b"))
						.predicate("nsel")
						.instance(Symbol.getInstance("b"))
						.build()
		));

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(expected, answerSets);
	}
}
