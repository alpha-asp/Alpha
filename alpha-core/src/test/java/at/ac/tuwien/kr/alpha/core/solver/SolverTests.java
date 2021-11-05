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
package at.ac.tuwien.kr.alpha.core.solver;

import static at.ac.tuwien.kr.alpha.core.test.util.TestUtils.assertRegressionTestAnswerSet;
import static at.ac.tuwien.kr.alpha.core.test.util.TestUtils.assertRegressionTestAnswerSets;
import static at.ac.tuwien.kr.alpha.core.test.util.TestUtils.assertRegressionTestAnswerSetsWithBase;
import static at.ac.tuwien.kr.alpha.core.test.util.TestUtils.buildSolverForRegressionTest;
import static at.ac.tuwien.kr.alpha.core.test.util.TestUtils.collectRegressionTestAnswerSets;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.Solver;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.commons.AnswerSetBuilder;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.core.grounder.GrounderMockWithChoice;
import at.ac.tuwien.kr.alpha.core.grounder.GrounderMockWithBasicProgram;
import at.ac.tuwien.kr.alpha.core.parser.InlineDirectivesImpl;
import at.ac.tuwien.kr.alpha.core.programs.InputProgram;
import at.ac.tuwien.kr.alpha.core.test.util.AnswerSetsParser;

// // TODO This is a functional test and should not be run with standard unit tests
public class SolverTests {
	
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

	@RegressionTest
	public void testObjectProgram(RegressionTestConfig cfg) {
		final Thingy thingy = new Thingy();

		final Atom fact = Atoms.newBasicAtom(Predicates.getPredicate("foo", 1), Terms.newConstant(thingy));

		final InputProgram program = new InputProgram(
			Collections.emptyList(),
			Collections.singletonList(fact),
			new InlineDirectivesImpl()
		);

		assertEquals(singleton(new AnswerSetBuilder()
			.predicate("foo").instance(thingy)
			.build()), collectRegressionTestAnswerSets(program, cfg));
	}

	@RegressionTest
	public void testFactsOnlyProgram(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
			cfg, 
			"p(a). p(b). foo(13). foo(16). q(a). q(c).",

			"q(a), q(c), p(a), p(b), foo(13), foo(16)"
		);
	}

	@RegressionTest
	public void testSimpleRule(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
			cfg, 
			"p(a). p(b). r(X) :- p(X).",

			"p(a), p(b), r(a), r(b)"
		);
	}

	@RegressionTest
	public void testSimpleRuleWithGroundPart(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
			cfg, 
			"p(1)." +
				"p(2)." +
				"q(X) :-  p(X), p(1).",

			"q(1), q(2), p(1), p(2)"
		);
	}

	@RegressionTest
	public void testProgramZeroArityPredicates(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
			cfg, 
			"a. p(X) :- b, r(X).",

		"a"
		);
	}

	@RegressionTest
	public void testChoiceGroundProgram(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(
			cfg,
			"a :- not b. b :- not a.",

			"a",
			"b"
		);
	}

	@RegressionTest
	public void testChoiceProgramNonGround(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSetsWithBase(
			cfg,
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

	@RegressionTest
	public void choiceProgram3Way(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(
			cfg,
			"a :- not b, not c." +
			"b :- not a, not c." +
			"c :- not a, not b.",

			"a",
			"b",
			"c"
		);
	}

	@RegressionTest
	public void emptyProgramYieldsEmptyAnswerSet(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(cfg, "", "");
	}

	@RegressionTest
	public void chooseMultipleAnswerSets(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(
			cfg,
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

	@RegressionTest
	public void builtinAtoms(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
			cfg,
			"dom(1). dom(2). dom(3). dom(4). dom(5)." +
			"p(X) :- dom(X), X = 4." +
			"r(Y) :- dom(Y), Y <= 2.",

			"dom(1), dom(2), dom(3), dom(4), dom(5), p(4), r(1), r(2)"
		);
	}

	@RegressionTest
	public void builtinAtomsGroundRule(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
			cfg,
			"a :- 13 != 4." +
			"b :- 2 != 3, 2 = 3." +
			"c :- 2 <= 3, not 2 > 3.",

			"a, c"
		);
	}

	
	@RegressionTest
	public void choiceProgramConstraintSimple(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
				cfg, 
				"fact(a).\n" + 
				"choice(either, X) :- fact(X), not choice(or, X).\n" + 
				"choice(or, X) :- fact(X), not choice(either, X).\n" + 
				":- choice(or, X).",
				
				"fact(a), choice(either, a)"
		);
	}
	
	@RegressionTest
	public void choiceProgramConstraintSimple2(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
				cfg, 
				"fact(a).\n" + 
				"desired(either).\n" + 
				"choice(either, X) :- fact(X), not choice(or, X).\n" + 
				"choice(or, X) :- fact(X), not choice(either, X).\n" + 
				":- choice(C, X), not desired(C).",
				
				"fact(a), desired(either), choice(either, a)"
		);
	}
	
	@RegressionTest
	public void choiceProgramConstraint(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSetsWithBase(
			cfg,
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

	@RegressionTest
	public void choiceProgramConstraintPermutation(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSetsWithBase(
		cfg,
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

	@RegressionTest
	public void simpleNoPropagation(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
			cfg,
			"val(1,1)." +
			"val(2,2)." +
			"something:- val(VAR1,VAL1), val(VAR2,VAL2), anything(VAL1,VAL2).",

			"val(1, 1), val(2, 2)"
		);
	}

	@RegressionTest
	public void choiceAndPropagationAfterwards(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSetsWithBase(
			cfg,
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

	@RegressionTest
	public void choiceAndConstraints(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSetsWithBase(
			cfg,
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

	@RegressionTest
	public void testUnsatisfiableProgram(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(cfg, "p(a). p(b). :- p(a), p(b).");
	}

	@RegressionTest
	public void testFunctionTermEquality(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
			cfg, 
			"r1(f(a,b)). r2(f(a,b)). a :- r1(X), r2(Y), X = Y.",

			"r1(f(a,b)), r2(f(a,b)), a"
		);
	}

	@RegressionTest
	public void builtinInequality(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSetsWithBase(
			cfg,
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

	@RegressionTest
	public void choiceConstraintsInequality(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSetsWithBase(
			cfg,
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
	
	@RegressionTest
	public void sameVariableTwiceInAtom(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(
			cfg,
			"p(a, a)." +
			"q(X) :- p(X, X).",

			"p(a,a), q(a)"
		);
	}

	@RegressionTest
	public void sameVariableTwiceInAtomConstraint(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(
			cfg,
			"p(a, a)." +
			":- p(X, X)."
		);
	}

	@RegressionTest
	public void noPositiveSelfFounding(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(
			cfg,
			"a :- b." +
			"b:- a." +
			":- not b."
		);
	}

	@RegressionTest
	public void noPositiveCycleSelfFoundingChoice(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(
			cfg,
			"c :- not d." +
			"d :- not c." +
			"a :- b, not c." +
			"b:- a." +
			":- not b."
		);
	}

	@RegressionTest
	public void conflictFromUnaryNoGood(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
			cfg, 
			"d(b)." +
			"sel(X) :- not nsel(X), d(X)." +
			"nsel(X) :- not sel(X), d(X)." +
			"t(a) :- sel(b)." +
			":- t(X).",

			"d(b), nsel(b)"
		);
	}

	@RegressionTest
	public void intervalsInFacts(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(
			cfg,
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

	@RegressionTest
	public void intervalInRules(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(
			cfg,
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

	@RegressionTest
	public void emptyIntervals(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(
			cfg,
			"p(3..1)." +
				"dom(5)." +
				"p(X) :- dom(X), X = 7..2 .",
			"dom(5)"
		);
	}

	@RegressionTest
	public void intervalInFunctionTermsInRules(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(
			cfg,
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

	@RegressionTest
	public void groundAtomInRule(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
			cfg, 
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

	@RegressionTest
	public void simpleChoiceRule(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSetsWithBase(
			cfg,
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

	@RegressionTest
	public void conditionalChoiceRule(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSetsWithBase(
			cfg,
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

	@RegressionTest
	public void doubleChoiceRule(RegressionTestConfig cfg) {
		Solver solver = buildSolverForRegressionTest("{ a }. { a }.", cfg);
		// Make sure that no superfluous answer sets that only differ on hidden atoms occur.
		List<AnswerSet> actual = solver.collectList();
		assertEquals(2, actual.size());
		assertEquals(AnswerSetsParser.parse("{} { a }"), new HashSet<>(actual));
	}

	@RegressionTest
	public void simpleArithmetics(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
			cfg, 
			"eight(X) :- X = 4 + 5 - 1." +
			"three(X) :- X = Z, Y = 1..10, Z = Y / 3, Z > 2, Z < 4.",

			"eight(8), three(3)");
	}

	@RegressionTest
	public void arithmeticsMultiplicationBeforeAddition(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSet(
			cfg, 
			"seven(X) :- 1+2 * 3 = X.",

			"seven(7)");
	}

	/**
	 * Tests the fix for issue #101
	 */
	@RegressionTest
	public void involvedUnsatisfiableProgram(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSets(
			cfg,	
			"x :- c1, c2, not x." +
			"c1 :- not a1." +
			"c1 :- not b1." +
			"c2 :- not a2." +
			"c2 :- not b2." +
			"a1 :- not b1." +
			"b1 :- not a1." +
			"a2 :- not b2." +
			"b2 :- not a2.");
	}

	@RegressionTest
	public void instanceEnumerationAtom(RegressionTestConfig cfg) {
		Set<AnswerSet> answerSets = buildSolverForRegressionTest("# enumeration_predicate_is enum." +
			"dom(1). dom(2). dom(3)." +
			"p(X) :- dom(X)." +
			"q(Y) :- p(Y)." +
			"unique_position(Term,Pos) :- q(Term), enum(id0,Term,Pos)." +
			"wrong_double_occurrence :- unique_position(T1,P), unique_position(T2,P), T1 != T2.", cfg).collectSet();
		// Since enumeration depends on evaluation, we do not know which unique_position is actually assigned.
		// Check manually that there is one answer set, wrong_double_occurrence has not been derived, and enum yielded a unique position for each term.
		assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.iterator().next();
		assertEquals(null, answerSet.getPredicateInstances(Predicates.getPredicate("wrong_double_occurrence", 0)));
		SortedSet<Atom> positions = answerSet.getPredicateInstances(Predicates.getPredicate("unique_position", 2));
		assertEnumerationPositions(positions, 3);
	}

	@RegressionTest
	public void instanceEnumerationArbitraryTerms(RegressionTestConfig cfg) {
		Set<AnswerSet> answerSets = buildSolverForRegressionTest("# enumeration_predicate_is enum." +
			"dom(a). dom(f(a,b)). dom(d)." +
			"p(X) :- dom(X)." +
			"q(Y) :- p(Y)." +
			"unique_position(Term,Pos) :- q(Term), enum(id0,Term,Pos)." +
			"wrong_double_occurrence :- unique_position(T1,P), unique_position(T2,P), T1 != T2.", cfg).collectSet();
		// Since enumeration depends on evaluation, we do not know which unique_position is actually assigned.
		// Check manually that there is one answer set, wrong_double_occurrence has not been derived, and enum yielded a unique position for each term.
		assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.iterator().next();
		assertPropositionalPredicateFalse(answerSet, Predicates.getPredicate("wrong_double_occurrence", 0));
		SortedSet<Atom> positions = answerSet.getPredicateInstances(Predicates.getPredicate("unique_position", 2));
		assertEnumerationPositions(positions, 3);
	}

	@RegressionTest
	public void instanceEnumerationMultipleIdentifiers(RegressionTestConfig cfg) {
		Set<AnswerSet> answerSets = buildSolverForRegressionTest("# enumeration_predicate_is enum." +
			"dom(a). dom(b). dom(c). dom(d)." +
			"p(X) :- dom(X)." +
			"unique_position1(Term,Pos) :- p(Term), enum(id,Term,Pos)." +
			"unique_position2(Term,Pos) :- p(Term), enum(otherid,Term,Pos)." +
			"wrong_double_occurrence :- unique_position(T1,P), unique_position(T2,P), T1 != T2." +
			"wrong_double_occurrence :- unique_position2(T1,P), unique_position(T2,P), T1 != T2.", cfg).collectSet();
		// Since enumeration depends on evaluation, we do not know which unique_position is actually assigned.
		// Check manually that there is one answer set, wrong_double_occurrence has not been derived, and enum yielded a unique position for each term.
		assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.iterator().next();
		assertPropositionalPredicateFalse(answerSet, Predicates.getPredicate("wrong_double_occurrence", 0));
		SortedSet<Atom> positions = answerSet.getPredicateInstances(Predicates.getPredicate("unique_position1", 2));
		assertEnumerationPositions(positions, 4);
		SortedSet<Atom> positions2 = answerSet.getPredicateInstances(Predicates.getPredicate("unique_position2", 2));
		assertEnumerationPositions(positions2, 4);
	}

	private void assertPropositionalPredicateFalse(AnswerSet answerSet, Predicate predicate) {
		assertEquals(null, answerSet.getPredicateInstances(predicate));
	}

	private void assertEnumerationPositions(SortedSet<Atom> positions, int numPositions) {
		assertEquals(numPositions, positions.size());
		boolean usedPositions[] = new boolean[numPositions];
		for (Atom position : positions) {
			@SuppressWarnings("unchecked")
			Integer atomPos = ((ConstantTerm<Integer>) position.getTerms().get(1)).getObject() - 1;
			assertTrue(atomPos < numPositions);
			usedPositions[atomPos] = true;
		}
		for (int i = 0; i < numPositions; i++) {
			assertTrue(usedPositions[i]);
		}
	}

	@RegressionTest
	public void smallCardinalityAggregate(RegressionTestConfig cfg) {
		assertRegressionTestAnswerSetsWithBase(
			cfg,
			"dom(1..3)." +
				"bound(1..4)." +
				"{ value(X) : dom(X) }." +
				"num(K) :-  K <= #count {X : value(X) }, bound(K).",

			"dom(1), dom(2), dom(3), bound(1), bound(2), bound(3), bound(4)",
			"",
			"",
			"value(1), num(1)",
			"value(1), value(2), num(1), num(2)",
			"value(1), value(2), value(3), num(1), num(2), num(3)",
			"value(1), value(3), num(1), num(2)",
			"value(2), num(1)",
			"value(2), value(3), num(1), num(2)",
			"value(3), num(1)"
		);
	}

	@RegressionTest
	public void dummyGrounder(RegressionTestConfig cfg) {
		AtomStore atomStore = new AtomStoreImpl();
		assertEquals(GrounderMockWithBasicProgram.EXPECTED, buildSolverForRegressionTest(atomStore, new GrounderMockWithBasicProgram(atomStore), cfg).collectSet());
	}

	@RegressionTest
	public void choiceGrounder(RegressionTestConfig cfg) {
		AtomStore atomStore = new AtomStoreImpl();
		assertEquals(GrounderMockWithChoice.EXPECTED, buildSolverForRegressionTest(atomStore, new GrounderMockWithChoice(atomStore), cfg).collectSet());
	}

}
