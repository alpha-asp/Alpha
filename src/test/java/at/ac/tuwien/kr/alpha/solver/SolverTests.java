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
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.AnswerSetBuilder;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.ChoiceGrounder;
import at.ac.tuwien.kr.alpha.grounder.DummyGrounder;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

public class SolverTests extends AbstractSolverTests {
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

		final Atom fact = new BasicAtom(new Predicate("foo", 1), ConstantTerm.getInstance(thingy));

		final Program program = new Program(
			Collections.emptyList(),
			Collections.singletonList(fact)
		);

		assertEquals(singleton(new AnswerSetBuilder()
			.predicate("foo").instance(thingy)
			.build()), collectSet(program));
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
	public void testChoiceGroundProgram() throws Exception {
		assertAnswerSets(
			"a :- not b. b :- not a.",

			"a",
			"b"
		);
	}

	@Test
	public void testChoiceProgramNonGround() throws Exception {
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
	public void choiceProgram3Way() throws IOException {
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
		assertAnswerSets("", "");
	}

	@Test
	public void chooseMultipleAnswerSets() throws IOException {
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
	public void choiceProgramConstraint() throws IOException {
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
	public void choiceProgramConstraintPermutation() throws IOException {
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
			":- val(VAR1,VAL1), val(VAR2,VAL2), eq(VAL1,VAL2), not eq(VAR1,VAR2).",

			"eq(1,1), eq(2,2), eq(3,3), var(1), var(2), var(3)",

			"val(1,1), val(2,2), val(3,3)",
			"val(1,1), val(3,2), val(2,3)",
			"val(2,1), val(1,2), val(3,3)",
			"val(2,1), val(3,2), val(1,3)",
			"val(3,1), val(1,2), val(2,3)",
			"val(3,1), val(2,2), val(1,3)"
		);
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
	public void choiceAndPropagationAfterwards() throws IOException {
		assertAnswerSetsWithBase(
			"node(a)." +
			"node(b)." +
			"in(X) :- not out(X), node(X)." +
			"out(X) :- not in(X), node(X)." +
			"pair(X,Y) :- in(X), in(Y).",

			"node(a), node(b)",

			"in(a), in(b), pair(a,a), pair(a,b), pair(b,a), pair(b,b)",
			"in(b), out(a), pair(b,b)",
			"in(a), out(b), pair(a,a)",
			"out(a), out(b)"
		);
	}

	@Test
	public void choiceAndConstraints() throws IOException {
		assertAnswerSetsWithBase(
			"node(a)." +
			"node(b)." +
			"edge(b,a)." +
			"in(X) :- not out(X), node(X)." +
			"out(X) :- not in(X), node(X)." +
			":- in(X), in(Y), edge(X,Y).",

			"node(a), node(b), edge(b,a)",

			"in(b), out(a)",
			"in(a), out(b)",
			"out(a), out(b)"
		);
	}

	@Test
	public void testUnsatisfiableProgram() throws IOException {
		assertAnswerSets("p(a). p(b). :- p(a), p(b).");
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
		assertAnswerSetsWithBase(
			"location(a1)." +
			"region(r1)." +
			"region(r2)." +
			"assign(L,R) :- location(L), region(R), not nassign(L,R)." +
			"nassign(L,R) :- location(L), region(R), not assign(L,R)." +
			":- assign(L,R1), assign(L,R2), R1 != R2." +
			"aux_ext_assign(a1,r1)." +
			"aux_ext_assign(a1,r2)." +
			"aux_not_assign(L,R) :- aux_ext_assign(L,R), not assign(L,R)." +
			":- aux_not_assign(L,R), assign(L,R).",

			"location(a1), region(r1), region(r2), aux_ext_assign(a1,r1), aux_ext_assign(a1,r2)",

			"assign(a1,r2), nassign(a1,r1), aux_not_assign(a1,r1)",
			"assign(a1,r1), nassign(a1,r2), aux_not_assign(a1,r2)",
			"nassign(a1,r1), nassign(a1,r2), aux_not_assign(a1,r1), aux_not_assign(a1,r2)"
		);
	}

	@Test
	public void choiceConstraintsInequality() throws IOException {
		assertAnswerSetsWithBase(
			"assign(L, R) :- not nassign(L, R), possible(L, R)." +
			"nassign(L, R) :- not assign(L, R), possible(L, R)." +
			"assigned(L) :- assign(L, R)." +
			":- possible(L,_), not assigned(L)." +
			":- assign(L, R1), assign(L, R2), R1 != R2." +
			"possible(l1, r1). possible(l3, r3). possible(l4, r1). possible(l4, r3). possible(l5, r4). possible(l6, r2). possible(l7, r3). possible(l8, r2). possible(l9, r1). possible(l9, r4).",

			"possible(l1,r1), " +
			"possible(l3,r3), " +
			"possible(l4,r1), " +
			"possible(l4,r3), " +
			"possible(l5,r4), " +
			"possible(l6,r2), " +
			"possible(l7,r3), " +
			"possible(l8,r2), " +
			"possible(l9,r1), " +
			"possible(l9,r4), " +
			"assign(l1,r1), " +
			"assign(l3,r3), " +
			"assign(l5,r4), " +
			"assign(l6,r2), " +
			"assign(l7,r3), " +
			"assign(l8,r2), " +
			"assigned(l1), " +
			"assigned(l3), " +
			"assigned(l4), " +
			"assigned(l5), " +
			"assigned(l6), " +
			"assigned(l7), " +
			"assigned(l8), " +
			"assigned(l9)",

			"assign(l4,r1), " +
			"assign(l9,r4), " +
			"nassign(l4,r3), " +
			"nassign(l9,r1)",

			"assign(l4,r1), " +
			"assign(l9,r1), " +
			"nassign(l4,r3), " +
			"nassign(l9,r4)",

			"assign(l4,r3), " +
			"assign(l9,r4), " +
			"nassign(l4,r1), " +
			"nassign(l9,r1)",

			"assign(l4,r3), " +
			"assign(l9,r1), " +
			"nassign(l4,r1), " +
			"nassign(l9,r4)"
		);
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
		assertAnswerSets(
			"p(a, a)." +
			":- p(X, X)."
		);
	}

	@Test
	public void noPositiveSelfFounding() throws IOException {
		assertAnswerSets(
			"a :- b." +
			"b:- a." +
			":- not b."
		);
	}

	@Test
	public void noPositiveCycleSelfFoundingChoice() throws IOException {
		assertAnswerSets(
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

			"b(1, 2)," +
			"b(3, 4)"
		);
	}

	@Test
	public void intervalInRules() throws IOException {
		assertAnswerSets(
			"a :- 3 = 1..4 ." +
			"p(X, 1..X) :- dom(X), X != 2." +
			"dom(1). dom(2). dom(3).",

			"dom(1)," +
				"dom(2)," +
				"dom(3)," +

				"p(1, 1)," +
				"p(3, 1)," +
				"p(3, 2)," +
				"p(3, 3)," +

				"a"
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

	@Test
	public void simpleChoiceRule() throws IOException {
		assertAnswerSetsWithBase(
			"{ a; b; c} :- d." +
				"d.",

			"d",
			"",
			"a",
			"a, b",
			"a, c",
			"a, b, c",
			"b",
			"b, c",
			"c"
		);
	}

	@Test
	public void conditionalChoiceRule() throws IOException {
		assertAnswerSetsWithBase(
			"dom(1..3)." +
				"{ p(X): not q(X); r(Y): p(Y)} :- dom(X), q(Y)." +
				"q(2).",

			"dom(1)," +
				"dom(2)," +
				"dom(3)," +
				"q(2)",

			"p(1)," +
				"p(3)",

			"",

			"p(3)",

			"p(1)"
		);
	}

	@Test
	public void doubleChoiceRule() throws IOException {
		Solver solver = getInstance("{ a }. { a }.");
		// Make sure that no superfluous answer sets that only differ on hidden atoms occur.
		List<AnswerSet> actual = solver.collectList();
		assertEquals(2, actual.size());
		assertEquals(AnswerSetsParser.parse("{} { a }"), new HashSet<>(actual));
	}

	@Test
	public void dummyGrounder() {
		TestCase.assertEquals(DummyGrounder.EXPECTED, getInstance(new DummyGrounder()).collectSet());
	}

	@Test
	public void choiceGrounder() {
		TestCase.assertEquals(ChoiceGrounder.EXPECTED, getInstance(new ChoiceGrounder()).collectSet());
	}
}
