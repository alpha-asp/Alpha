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

import at.ac.tuwien.kr.alpha.AnswerSetsParser;
import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.ChoiceGrounder;
import at.ac.tuwien.kr.alpha.grounder.DummyGrounder;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

public class SolverTests extends AbstractSolverTests {
	private final ProgramParser parser = new ProgramParser();

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
		final Thingy thingy = new Thingy();

		final Atom fact = new BasicAtom(new BasicPredicate("foo", 1), ConstantTerm.getInstance(thingy));

		final Program program = new Program(
			Collections.emptyList(),
			Collections.singletonList(fact)
		);

		assertEquals(singleton(new AnswerSetBuilder()
			.predicate("foo").instance(thingy)
			.build()), solve(program));
	}

	@Test
	public void testFactsOnlyProgram() throws IOException {
		assertAnswerSet(
			"p(a). p(b). foo(13). foo(16). q(a). q(c).",

			"q(a), q(c), p(a), p(b), foo(13), foo(16)"
		);
	}

	@Test
	public void testSimpleRule() throws Exception {
		assertAnswerSet(
			"p(a). p(b). r(X) :- p(X).",

			"p(a), p(b), r(a), r(b)"
		);
	}

	@Test
	public void testSimpleRuleWithGroundPart() throws Exception {
		assertAnswerSet(
			"p(1)." +
				"p(2)." +
				"q(X) :-  p(X), p(1).",

			"q(1), q(2), p(1), p(2)"
		);
	}

	@Test
	public void testProgramZeroArityPredicates() throws Exception {
		assertAnswerSet(
			"a. p(X) :- b, r(X).",

		"a"
		);
	}

	@Test
	public void testGuessingGroundProgram() throws Exception {
		assertAnswerSets(
			"a :- not b. b :- not a.",

			"a",
			"b"
		);
	}


	@Test
	public void testGuessingProgramNonGround() throws Exception {
		assertAnswerSetsWithBase(
			"dom(1). dom(2). dom(3)." +
			"p(X) :- dom(X), not q(X)." +
			"q(X) :- dom(X), not p(X).",

			"dom(1), dom(2), dom(3)",

			"q(1), q(2), p(3)",
			"q(1), p(2), p(3)",
			"p(1), q(2), p(3)",
			"p(1), p(2), p(3)",
			"q(1), q(2), q(3)",
			"q(1), p(2), q(3)",
			"p(1), q(2), q(3)",
			"p(1), p(2), q(3)"
		);
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
		assertAnswerSets(
			"a :- not b, not c." +
			"b :- not a, not c." +
			"c :- not a, not b.",

			"a",
			"b",
			"c"
		);
	}

	@Test
	public void emptyProgramYieldsEmptyAnswerSet() throws IOException {
		assertOnlyEmptyAnswerSet("");
	}

	@Test
	public void guessingMultipleAnswerSets() throws IOException {
		assertAnswerSets(
			"a :- not nota." +
			"nota :- not a." +
			"b :- not notb." +
			"notb :- not b." +
			"c :- not notc." +
			"notc :- not c." +
			":- nota,notb,notc.",

			"a, b, c",
			"nota, b, c",
			"a, notb, c",
			"nota, notb, c",
			"a, b, notc",
			"nota, b, notc",
			"a, notb, notc"
		);
	}

	@Test
	public void builtinAtoms() throws IOException {
		assertAnswerSet(
			"dom(1). dom(2). dom(3). dom(4). dom(5)." +
			"p(X) :- dom(X), X = 4." +
			"r(Y) :- dom(Y), Y <= 2.",

			"dom(1), dom(2), dom(3), dom(4), dom(5), p(4), r(1), r(2)"
		);
	}

	@Test
	public void builtinAtomsGroundRule() throws IOException {
		assertAnswerSet(
			"a :- 13 != 4." +
			"b :- 2 != 3, 2 = 3." +
			"c :- 2 <= 3, not 2 > 3.",

			"a, c"
		);
	}

	@Test
	public void guessingProgramConstraint() throws IOException {
		assertAnswerSetsWithBase(
			"eq(1,1)." +
			"eq(2,2)." +
			"eq(3,3)." +
			"var(1)." +
			"var(2)." +
			"var(3)." +
			"val(VAR,1):-var(VAR),not val(VAR,2),not val(VAR,3)." +
			"val(VAR,2):-var(VAR),not val(VAR,1),not val(VAR,3)." +
			"val(VAR,3):-var(VAR),not val(VAR,1),not val(VAR,2)." +
			":- eq(VAL1,VAL2), not eq(VAR1,VAR2), val(VAR1,VAL1), val(VAR2,VAL2).",

			"eq(1, 1), eq(2, 2), eq(3, 3), var(1), var(2), var(3)",

			"val(1, 1), val(2, 2), val(3, 3)",
			"val(1, 1), val(3, 2), val(2, 3)",
			"val(2, 1), val(1, 2), val(3, 3)",
			"val(2, 1), val(3, 2), val(1, 3)",
			"val(3, 1), val(1, 2), val(2, 3)",
			"val(3, 1), val(2, 2), val(1, 3)"
		);
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


		Program parsedProgram = parser.parse(testProgram);

		NaiveGrounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = getInstance(grounder);

		final AnswerSetBuilder base = new AnswerSetBuilder()
			.predicate("eq")
			.instance(1, 1)
			.instance(2, 2)
			.instance(3, 3)
			.predicate("var")
			.instance(1)
			.instance(2)
			.instance(3);

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new AnswerSetBuilder(base)
				.predicate("val")
				.instance(1, 1)
				.instance(2, 2)
				.instance(3, 3)
				.build(),
			new AnswerSetBuilder(base)
				.predicate("val")
				.instance(1, 1)
				.instance(3, 2)
				.instance(2, 3)
				.build(),
			new AnswerSetBuilder(base)
				.predicate("val")
				.instance(2, 1)
				.instance(1, 2)
				.instance(3, 3)
				.build(),
			new AnswerSetBuilder(base)
				.predicate("val")
				.instance(2, 1)
				.instance(3, 2)
				.instance(1, 3)
				.build(),
			new AnswerSetBuilder(base)
				.predicate("val")
				.instance(3, 1)
				.instance(1, 2)
				.instance(2, 3)
				.build(),
			new AnswerSetBuilder(base)
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
		assertAnswerSet(
			"val(1,1)." +
			"val(2,2)." +
			"something:- val(VAR1,VAL1), val(VAR2,VAL2), anything(VAL1,VAL2).",

			"val(1, 1), val(2, 2)"
		);
	}

	@Test
	public void guessingAndPropagationAfterwards() throws IOException {
		String testProgram = "node(a).\n" +
			"node(b).\n" +
			"in(X) :- not out(X), node(X).\n" +
			"out(X) :- not in(X), node(X).\n" +
			"pair(X,Y) :- in(X), in(Y).";

		Program parsedProgram = parser.parse(testProgram);

		NaiveGrounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = getInstance(grounder);

		final AnswerSetBuilder base = new AnswerSetBuilder()
			.predicate("node")
			.instance(Symbol.getInstance("a"))
			.instance(Symbol.getInstance("b"));

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new AnswerSetBuilder(base)
				.predicate("in")
				.instance(Symbol.getInstance("a"))
				.instance(Symbol.getInstance("b"))
				.predicate("pair")
				.instance(Symbol.getInstance("a"), Symbol.getInstance("a"))
				.instance(Symbol.getInstance("a"), Symbol.getInstance("b"))
				.instance(Symbol.getInstance("b"), Symbol.getInstance("a"))
				.instance(Symbol.getInstance("b"), Symbol.getInstance("b"))
				.build(),
			new AnswerSetBuilder(base)
				.predicate("in")
				.instance(Symbol.getInstance("b"))
				.predicate("out")
				.instance(Symbol.getInstance("a"))
				.predicate("pair")
				.instance(Symbol.getInstance("b"), Symbol.getInstance("b"))
				.build(),
			new AnswerSetBuilder(base)
				.predicate("in")
				.instance(Symbol.getInstance("a"))
				.predicate("out")
				.instance(Symbol.getInstance("b"))
				.predicate("pair")
				.instance(Symbol.getInstance("a"), Symbol.getInstance("a"))
				.build(),
			new AnswerSetBuilder(base)
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

		Program parsedProgram = parser.parse(testProgram);

		NaiveGrounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = getInstance(grounder);

		final AnswerSetBuilder base = new AnswerSetBuilder()
			.predicate("node")
			.instance(Symbol.getInstance("a"))
			.instance(Symbol.getInstance("b"));

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new AnswerSetBuilder(base)
				.predicate("in")
				.instance(Symbol.getInstance("b"))
				.predicate("out")
				.instance(Symbol.getInstance("a"))
				.predicate("edge")
				.instance(Symbol.getInstance("b"), Symbol.getInstance("a"))
				.build(),
			new AnswerSetBuilder(base)
				.predicate("in")
				.instance(Symbol.getInstance("a"))
				.predicate("out")
				.instance(Symbol.getInstance("b"))
				.predicate("edge")
				.instance(Symbol.getInstance("b"), Symbol.getInstance("a"))
				.build(),
			new AnswerSetBuilder(base)
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
		assertNoAnswerSets("p(a). p(b). :- p(a), p(b).");
	}

	@Test
	public void testFunctionTermEquality() throws IOException {
		assertAnswerSet(
			"r1(f(a,b)). r2(f(a,b)). a :- r1(X), r2(Y), X = Y.",

			"r1(f(a,b)), r2(f(a,b)), a"
		);
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


		Program parsedProgram = parser.parse(program);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = getInstance(grounder);

		final AnswerSetBuilder base = new AnswerSetBuilder()
			.predicate("location")
			.instance(Symbol.getInstance("a1"))
			.predicate("region")
			.instance(Symbol.getInstance("r1"))
			.instance(Symbol.getInstance("r2"))
			.predicate("aux_ext_assign")
			.instance(Symbol.getInstance("a1"), Symbol.getInstance("r1"))
			.instance(Symbol.getInstance("a1"), Symbol.getInstance("r2"));

		Set<AnswerSet> expected = new HashSet<>(Arrays.asList(
			new AnswerSetBuilder(base)
				.predicate("assign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r2"))
				.predicate("nassign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r1"))
				.predicate("aux_not_assign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r1"))
				.build(),
			new AnswerSetBuilder(base)
				.predicate("assign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r1"))
				.predicate("nassign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r2"))
				.predicate("aux_not_assign")
				.instance(Symbol.getInstance("a1"), Symbol.getInstance("r2"))
				.build(),
			new AnswerSetBuilder(base)
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


		Program parsedProgram = parser.parse(program);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = getInstance(grounder);

		final AnswerSetBuilder base = new AnswerSetBuilder()
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
			new AnswerSetBuilder(base)
				.predicate("assign")
				.symbolicInstance("l4", "r1")
				.symbolicInstance("l9", "r4")
				.predicate("nassign")
				.symbolicInstance("l4", "r3")
				.symbolicInstance("l9", "r1")
				.build(),
			new AnswerSetBuilder(base)
				.predicate("assign")
				.symbolicInstance("l4", "r1")
				.symbolicInstance("l9", "r1")
				.predicate("nassign")
				.symbolicInstance("l4", "r3")
				.symbolicInstance("l9", "r4")
				.build(),
			new AnswerSetBuilder(base)
				.predicate("assign")
				.symbolicInstance("l4", "r3")
				.symbolicInstance("l9", "r4")
				.predicate("nassign")
				.symbolicInstance("l4", "r1")
				.symbolicInstance("l9", "r1")
				.build(),
			new AnswerSetBuilder(base)
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
		assertAnswerSets(
			"p(a, a)." +
			"q(X) :- p(X, X).",

			"p(a,a), q(a)"
		);
	}

	@Test
	public void sameVariableTwiceInAtomConstraint() throws IOException {
		assertNoAnswerSets(
			"p(a, a)." +
			":- p(X, X)."
		);
	}

	@Test
	public void noPositiveSelfFounding() throws IOException {
		assertNoAnswerSets(
			"a :- b." +
			"b:- a." +
			":- not b."
		);
	}

	@Test
	public void noPositiveCycleSelfFoundingGuess() throws IOException {
		assertNoAnswerSets(
			"c :- not d." +
			"d :- not c." +
			"a :- b, not c." +
			"b:- a." +
			":- not b."
		);
	}

	@Test
	public void conflictFromUnaryNoGood() throws IOException {
		assertAnswerSet(
			"d(b)." +
			"sel(X) :- not nsel(X), d(X)." +
			"nsel(X) :- not sel(X), d(X)." +
			"t(a) :- sel(b)." +
			":- t(X).",

			"d(b), nsel(b)"
		);
	}

	@Test
	public void intervalsInFacts() throws IOException {
		assertAnswerSets(
			"a." +
			"facta(1..3)." +
			"factb(t, 5..8, u)." +
			"factc(1..3, w, 2 .. 4)." +
			"b(1,2)." +
			"b(3,4).",

			"facta(1), " +
			"facta(2), " +
			"facta(3), " +

			"factb(t, 5, u)," +
			"factb(t, 6, u)," +
			"factb(t, 7, u)," +
			"factb(t, 8, u)," +

			"factc(1, w, 2)," +
			"factc(2, w, 2)," +
			"factc(3, w, 2)," +
			"factc(1, w, 3)," +
			"factc(2, w, 3)," +
			"factc(3, w, 3)," +
			"factc(1, w, 4)," +
			"factc(2, w, 4)," +
			"factc(3, w, 4)," +

			"a," +

			"b(1, 2),"+
			"b(3, 4)"
		);
	}

	@Test
	public void intervalInRules() throws IOException {
		assertAnswerSets(
			"a :- 3 = 1..4 ." +
			"p(X, 1..X) :- dom(X), X != 2." +
			"dom(1). dom(2). dom(3).",

			"dom(1), dom(2), dom(3), p(1, 1), p(3, 1), p(3, 2), p(3, 3), a"
		);
	}

	@Test
	public void intervalInFunctionTermsInRules() throws IOException {
		assertAnswerSets(
			"a :- q(f(1..3,g(4..5)))." +
			"q(f(2,g(4)))." +
			"q(f(1,g(5)))." +
			"p(f(1..3,g(4..5))) :- b." +
			"b.",

			"a, " +
			"b, " +

			"q(f(2,g(4))), " +
			"q(f(1,g(5))), " +

			"p(f(1,g(4))), " +
			"p(f(1,g(5))), " +
			"p(f(2,g(4))), " +
			"p(f(2,g(5))), " +
			"p(f(3,g(4))), " +
			"p(f(3,g(5)))"
		);
	}

	@Test
	public void groundAtomInRule() throws IOException {
		assertAnswerSet(
			"p :- dom(X), q, q2." +
				"dom(1)." +
				"q :- not nq." +
				"nq :- not q." +
				"q2 :- not nq2." +
				"nq2 :- not q2." +
				":- not p.",

			"dom(1), p, q, q2"
		);
	}

	private Set<AnswerSet> solve(String program) throws IOException {
		return solve(parser.parse(program));
	}

	private Set<AnswerSet> solve(Program program) throws IOException {
		return getInstance(new NaiveGrounder(program)).collectSet();
	}

	private void assertAnswerSets(String program, String... answerSets) throws IOException {
		StringJoiner joiner = new StringJoiner("} {", "{", "}");
		Arrays.stream(answerSets).forEach(joiner::add);
		assertAnswerSets(program, AnswerSetsParser.parse(joiner.toString()));
	}

	private void assertAnswerSet(String program, String answerSet) throws IOException {
		assertAnswerSets(program, AnswerSetsParser.parseSingleton(answerSet));
	}

	private void assertAnswerSetsWithBase(String program, String base, String... answerSets) throws IOException {
		assertAnswerSets(program, AnswerSetsParser.parseWithBase(base, answerSets));
	}

	private void assertAnswerSets(String program, Set<AnswerSet> answerSets) throws IOException {
		assertEquals(answerSets, solve(program));
	}

	private void assertOnlyEmptyAnswerSet(String program) throws IOException {
		assertEquals(singleton(BasicAnswerSet.EMPTY), solve(program));
	}

	private void assertNoAnswerSets(String program) throws IOException {
		assertEquals(emptySet(), solve(program));
	}
}
