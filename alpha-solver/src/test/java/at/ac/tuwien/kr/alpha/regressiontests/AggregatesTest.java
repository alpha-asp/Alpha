/**
 * Copyright (c) 2018-2019 Siemens AG
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
package at.ac.tuwien.kr.alpha.regressiontests;

import static at.ac.tuwien.kr.alpha.regressiontests.RegressionTestUtils.assertRegressionTestAnswerSets;
import static at.ac.tuwien.kr.alpha.regressiontests.RegressionTestUtils.assertRegressionTestAnswerSetsWithBase;
import static at.ac.tuwien.kr.alpha.regressiontests.RegressionTestUtils.ignoreTestForNaiveSolver;
import static at.ac.tuwien.kr.alpha.regressiontests.RegressionTestUtils.ignoreTestForSimplifiedSumAggregates;

import at.ac.tuwien.kr.alpha.api.config.SystemConfig;

/**
 * Tests if correct answer sets for programs containing aggregates are computed.
 */
// TODO This is a functional test and should not be run with standard unit tests
public class AggregatesTest {

	private static final String LS = System.lineSeparator();
	
	@AggregateRegressionTest
	public void aggregateCountLeGroundPositive(SystemConfig cfg) {
		String program = "a." + LS
				+ "b :- 1 <= #count { 1 : a }.";
		assertRegressionTestAnswerSets(cfg, program, "a,b");
	}

	@AggregateRegressionTest
	public void aggregateCountEqSingleElementPositive(SystemConfig cfg) {
		String program = "thing(1..3)."
				+ "cnt_things(N) :- N = #count{X : thing(X)}.";
		assertRegressionTestAnswerSets(cfg, program, "thing(1), thing(2), thing(3), cnt_things(3)");
	}

	@AggregateRegressionTest
	public void aggregateCountEqEmptySetPositive(SystemConfig cfg) {
		String program = "cnt_things(N) :- N = #count{X : thing(X)}.";
		assertRegressionTestAnswerSets(cfg, program, "cnt_things(0)");
	}

	@AggregateRegressionTest
	public void aggregateCountLeEmptySetPositive(SystemConfig cfg) {
		String program = "zero_leq_cnt :- 0 <= #count{X : thing(X)}.";
		assertRegressionTestAnswerSets(cfg, program, "zero_leq_cnt");
	}

	@AggregateRegressionTest
	public void aggregateSumEqEmptySetPositive(SystemConfig cfg) {
		String program = "sum_things(S) :- S = #sum{X : thing(X)}.";
		assertRegressionTestAnswerSets(cfg, program, "sum_things(0)");
	}
	
	@AggregateRegressionTest
	public void aggregateSumEqNegativeSum(SystemConfig cfg) {
		ignoreTestForSimplifiedSumAggregates(cfg);
		String program = "thing(-1). thing(-2). thing(-3). sum_things(S) :- S = #sum{X : thing(X)}.";
		assertRegressionTestAnswerSets(cfg, program, "thing(-1), thing(-2), thing(-3), sum_things(-6)");
	}
	
	@AggregateRegressionTest
	public void aggregateSumEqMixedElementsSum(SystemConfig cfg) {
		ignoreTestForSimplifiedSumAggregates(cfg);
		String program = "thing(-1). thing(6). thing(-3). sum_things(S) :- S = #sum{X : thing(X)}.";
		assertRegressionTestAnswerSets(cfg, program, "thing(-1), thing(6), thing(-3), sum_things(2)");		
	}

	@AggregateRegressionTest
	public void aggregateSumLeEmptySetPositive(SystemConfig cfg) {
		String program = "zero_leq_sum :- 0 <= #sum{X : thing(X)}.";
		assertRegressionTestAnswerSets(cfg, program, "zero_leq_sum");
	}
	
	@AggregateRegressionTest
	public void aggregateSumLeNegativeSum(SystemConfig cfg) {
		ignoreTestForSimplifiedSumAggregates(cfg);
		String program = "thing(-3). thing(4). "
				+ "minus_three_leq_sum :- -3 <= #sum{X : thing(X)}."
				+ "two_gt_sum :- 2 > #sum{X : thing(X)}.";
		assertRegressionTestAnswerSets(cfg, program, "thing(-3), thing(4), minus_three_leq_sum, two_gt_sum");
	}

	@AggregateRegressionTest
	public void aggregateSumLeNegativeElementsWithChoice(SystemConfig cfg) {
		ignoreTestForSimplifiedSumAggregates(cfg);
		String program = "thing(3). thing(-5). thing(5). "
				+ "{summed_up_thing(X) : thing(X)}. "
				+ "seven_le :- 7 <= #sum{X : summed_up_thing(X)}.";
		assertRegressionTestAnswerSetsWithBase(cfg, program,
				"thing(-5), thing(3), thing(5)",
				"seven_le, summed_up_thing(3), summed_up_thing(5)",
				"summed_up_thing(5)",
				"summed_up_thing(3)",
				"summed_up_thing(-5)",
				"summed_up_thing(-5), summed_up_thing(3), summed_up_thing(5)",
				"summed_up_thing(-5), summed_up_thing(3)",
				"summed_up_thing(-5), summed_up_thing(5)",
				"");
	}
	
	@AggregateRegressionTest
	public void aggregateCountLeWithChoicePositive(SystemConfig cfg) {
		String program = "potential_thing(1..4). "
				+ "{ thing(N) : potential_thing(N)}."
				+ "two_things_chosen :- thing(N1), thing(N2), N1 != N2."
				+ "three_things_chosen :- thing(N1), thing(N2), thing(N3), N1 != N2, N1 != N3, N2 != N3."
				+ ":- not two_things_chosen."
				+ ":- three_things_chosen."
				+ "two_leq_cnt :- 2 <= #count{ X : thing(X) }.";
		assertRegressionTestAnswerSetsWithBase(cfg, program,
				"potential_thing(1), potential_thing(2), potential_thing(3), potential_thing(4), two_things_chosen, two_leq_cnt",
				"thing(1), thing(2)",
				"thing(1), thing(3)",
				"thing(1), thing(4)",
				"thing(2), thing(3)",
				"thing(2), thing(4)",
				"thing(3), thing(4)");
	}

	@AggregateRegressionTest
	public void aggregateCountEqWithChoicePositive(SystemConfig cfg) {
		String program = "potential_thing(1..4). "
				+ "{ thing(N) : potential_thing(N)}."
				+ "two_things_chosen :- thing(N1), thing(N2), N1 != N2."
				+ "three_things_chosen :- thing(N1), thing(N2), thing(N3), N1 != N2, N1 != N3, N2 != N3."
				+ ":- not two_things_chosen."
				+ ":- three_things_chosen."
				+ "cnt_things(CNT) :- CNT = #count{ X : thing(X) }.";
		assertRegressionTestAnswerSetsWithBase(cfg, program,
				"potential_thing(1), potential_thing(2), potential_thing(3), potential_thing(4), two_things_chosen, cnt_things(2)",
				"thing(1), thing(2)",
				"thing(1), thing(3)",
				"thing(1), thing(4)",
				"thing(2), thing(3)",
				"thing(2), thing(4)",
				"thing(3), thing(4)");
	}

	@AggregateRegressionTest
	public void aggregateSumEqWithChoicePositive(SystemConfig cfg) {
		String program = "potential_thing(1..4). "
				+ "{ thing(N) : potential_thing(N)}."
				+ "two_things_chosen :- thing(N1), thing(N2), N1 != N2."
				+ "three_things_chosen :- thing(N1), thing(N2), thing(N3), N1 != N2, N1 != N3, N2 != N3."
				+ ":- not two_things_chosen."
				+ ":- three_things_chosen."
				+ "sum_things(SUM) :- SUM = #sum{ X : thing(X) }.";
		assertRegressionTestAnswerSetsWithBase(cfg, program,
				"potential_thing(1), potential_thing(2), potential_thing(3), potential_thing(4), two_things_chosen",
				"thing(1), thing(2), sum_things(3)",
				"thing(1), thing(3), sum_things(4)",
				"thing(1), thing(4), sum_things(5)",
				"thing(2), thing(3), sum_things(5)",
				"thing(2), thing(4), sum_things(6)",
				"thing(3), thing(4), sum_things(7)");
	}
	
	@AggregateRegressionTest
	public void aggregateSumEqOverMixedValuesWithChoicePositive(SystemConfig cfg) {
		ignoreTestForSimplifiedSumAggregates(cfg);
		String program = "potential_thing(-2). potential_thing(-1). potential_thing(0). potential_thing(1). "
				+ "{ thing(N) : potential_thing(N)}."
				+ "two_things_chosen :- thing(N1), thing(N2), N1 != N2."
				+ "three_things_chosen :- thing(N1), thing(N2), thing(N3), N1 != N2, N1 != N3, N2 != N3."
				+ ":- not two_things_chosen."
				+ ":- three_things_chosen."
				+ "sum_things(SUM) :- SUM = #sum{ X : thing(X) }.";
		assertRegressionTestAnswerSetsWithBase(cfg, program,
				"potential_thing(-2), potential_thing(-1), potential_thing(0), potential_thing(1), two_things_chosen",
				"thing(-2), thing(-1), sum_things(-3)",
				"thing(-2), thing(0), sum_things(-2)",
				"thing(-2), thing(1), sum_things(-1)",
				"thing(-1), thing(0), sum_things(-1)",
				"thing(-1), thing(1), sum_things(0)",
				"thing(0), thing(1), sum_things(1)");
	}

	@AggregateRegressionTest
	public void aggregateSumBetweenNegative(SystemConfig cfg) {
		String program = "potential_thing(1..4). "
				+ "{ thing(N) : potential_thing(N)}."
				+ "two_things_chosen :- thing(N1), thing(N2), N1 != N2."
				+ "three_things_chosen :- thing(N1), thing(N2), thing(N3), N1 != N2, N1 != N3, N2 != N3."
				+ ":- not two_things_chosen."
				+ ":- three_things_chosen."
				+ "sum_not_five :- not 4 < #sum{ X : thing(X) } < 6."
				+ ":- not sum_not_five."
				+ "sum_things(SUM) :- SUM = #sum{ X : thing(X) }.";
		assertRegressionTestAnswerSetsWithBase(cfg, program,
				"potential_thing(1), potential_thing(2), potential_thing(3), potential_thing(4), two_things_chosen, sum_not_five",
				"thing(1), thing(2), sum_things(3)",
				"thing(1), thing(3), sum_things(4)",
				"thing(2), thing(4), sum_things(6)",
				"thing(3), thing(4), sum_things(7)");
	}

	@AggregateRegressionTest
	public void aggregateMaxNegative(SystemConfig cfg) {
		String program = "potential_thing(1..4). "
				+ "{ thing(N) : potential_thing(N) }."
				+ "one_chosen :- thing(_)."
				+ ":- not one_chosen."
				+ ":- thing(N1), thing(N2), N1 != N2."
				+ "max_chosen :- thing(X), not X < #max{M : potential_thing(M)}."
				+ ":- not max_chosen.";
		assertRegressionTestAnswerSets(cfg, program,
				"potential_thing(1), potential_thing(2), potential_thing(3), potential_thing(4), one_chosen, max_chosen, thing(4)");
	}

	@AggregateRegressionTest
	public void aggregateCountGroundNegative(SystemConfig cfg) {
		String program = "{a}." + LS
				+ "b :- not c." + LS
				+ "c :- 1 <= #count { 1 : a }.";
		assertRegressionTestAnswerSets(cfg, program, "a,c", "b");
	}
	
	@AggregateRegressionTest
	public void aggregateCountNonGroundPositive(SystemConfig cfg) {
		String program = "n(1..3)." + LS
				+ "{x(N)} :- n(N)." + LS
				+ "min(3)." + LS
				+ "ok :- min(M), M <= #count { N : n(N), x(N) }.";
		assertRegressionTestAnswerSetsWithBase(cfg, program, "n(1), n(2), n(3), min(3)",
				"", "x(1)", "x(2)", "x(3)", "x(1), x(2)", "x(1), x(3)",
				"x(2), x(3)", "x(1), x(2), x(3), ok");
	}
	
	@AggregateRegressionTest
	public void aggregateCountNonGroundLowerAndUpper(SystemConfig cfg) {
		String program = "n(1..3)." + LS
				+ "{x(N)} :- n(N)." + LS
				+ "min(2)." + LS
				+ "max(2)." + LS
				+ "ok :- min(M), M <= #count { N : n(N), x(N) }, not exceedsMax." + LS
				+ "exceedsMax :- max(M), M1 = M + 1, M1 <= #count { N : n(N), x(N) }.";
		System.out.println(program);
		assertRegressionTestAnswerSetsWithBase(cfg, program, "n(1), n(2), n(3), min(2), max(2)",
				"", "x(1)", "x(2)", "x(3)", "x(1), x(2), ok", "x(1), x(3), ok",
				"x(2), x(3), ok", "x(1), x(2), x(3), exceedsMax");
	}
	
	@AggregateRegressionTest
	public void aggregateSumGroundLower(SystemConfig cfg) {
		String program = "a." + LS
				+ "b :- 5 <= #sum { 2 : a; 3 }.";
		assertRegressionTestAnswerSets(cfg, program, "a,b");
	}
	
	@AggregateRegressionTest
	public void aggregateSumNonGroundLowerAndUpper(SystemConfig cfg) {
		String program = "n(1..3)." + LS
				+ "{x(N)} :- n(N)." + LS
				+ "min(3)." + LS
				+ "max(4)." + LS
				+ "ok :- min(M), M <= #sum { N : n(N), x(N) }, not exceedsMax." + LS
				+ "exceedsMax :- max(M), M1 = M + 1, M1 <= #sum { N : n(N), x(N) }.";
		System.out.println(program);
		assertRegressionTestAnswerSetsWithBase(cfg, program, "n(1), n(2), n(3), min(3), max(4)",
				"", "x(1)", "x(2)", "x(3), ok", "x(1), x(2), ok", "x(1), x(3), ok",
				"x(2), x(3), exceedsMax", "x(1), x(2), x(3), exceedsMax");
	}
	
	@AggregateRegressionTest
	public void aggregateSumNonGroundLower(SystemConfig cfg) {
		String program = "n(1..3)." + LS
				+ "{x(N)} :- n(N)." + LS
				+ "min(3)." + LS
				+ "ok :- min(M), M <= #sum { N : n(N), x(N) }.";
		System.out.println(program);
		assertRegressionTestAnswerSetsWithBase(cfg, program, "n(1), n(2), n(3), min(3)",
				"", "x(1)", "x(2)", "x(3), ok", "x(1), x(2), ok", "x(1), x(3), ok",
				"x(2), x(3), ok", "x(1), x(2), x(3), ok");
	}

	@AggregateRegressionTest
	public void aggregateSumComputed(SystemConfig cfg) {
		ignoreTestForNaiveSolver(cfg); // Do not run this test case with the naive solver.
		String program = "n(1..3)." + LS
				+ "{x(N)} :- n(N)." + LS
				+ "potential_sum(0..6)." + LS
				+ "min(S) :- S <= #sum { N : n(N), x(N) }, potential_sum(S)." + LS
				+ "sum(S) :- min(S), not min(Sp1), Sp1 = S+1.";
		assertRegressionTestAnswerSetsWithBase(cfg, program, "n(1), n(2), n(3), potential_sum(0), potential_sum(1), "
				+ "potential_sum(2), potential_sum(3), potential_sum(4), potential_sum(5), potential_sum(6)",
				"min(0), sum(0)",
				"x(1), min(0), min(1), sum(1)",
				"x(2), min(0), min(1), min(2), sum(2)",
				"x(3), min(0), min(1), min(2), min(3), sum(3)",
				"x(1), x(2), min(0), min(1), min(2), min(3), sum(3)",
				"x(1), x(3), min(0), min(1), min(2), min(3), min(4), sum(4)",
				"x(2), x(3), min(0), min(1), min(2), min(3), min(4), min(5), sum(5)",
				"x(1), x(2), x(3), min(0), min(1), min(2), min(3), min(4), min(5), min(6), sum(6)");
	}
	
	@AggregateRegressionTest
	public void aggregateCountGlobalVariable(SystemConfig cfg) {
		String program = "box(1..2)." + LS
				+ "in(1,1)." + LS
				+ "in(1,2)." + LS
				+ "in(2,2)." + LS
				+ "full(B) :- box(B), 2 <= #count { I : in(I,B) }.";
		assertRegressionTestAnswerSetsWithBase(cfg, program, "box(1), box(2), in(1,1), in(1,2), in(2,2)",
				"full(2)");
	}
	
	@AggregateRegressionTest
	public void aggregateSumGlobalVariable(SystemConfig cfg) {
		String program = "box(1..2)." + LS
				+ "item_size(I,I) :- I=1..2." + LS
				+ "in(1,1)." + LS
				+ "in(1,2)." + LS
				+ "in(2,2)." + LS
				+ "full(B) :- box(B), 3 <= #sum { S : item_size(I,S), in(I,B) }.";
		assertRegressionTestAnswerSetsWithBase(cfg, program, "box(1), box(2), item_size(1,1), item_size(2,2), in(1,1), in(1,2), in(2,2)",
				"full(2)");
	}

	@AggregateRegressionTest
	public void aggregateRightHandTermCountGreater(SystemConfig cfg) {
		String program = "p(1)." + 
			"p(2)." + 
			"q :- #count { N : p(N) } > 1.";
		assertRegressionTestAnswerSets(cfg, program, "p(1), p(2), q");
	}

	@AggregateRegressionTest
	public void aggregateRightHandTermCountEqAssigning(SystemConfig cfg) {
		String program = "p(1)." + 
			"p(2)." + 
			"q :- #count { N : p(N) } = 2.";
		assertRegressionTestAnswerSets(cfg, program, "p(1), p(2), q");
	}

	@AggregateRegressionTest
	public void aggregateRightHandTermCountNeqAssigning(SystemConfig cfg) {
		String program = "p(1)." + 
			"p(2)." + 
			"q :- #count { N : p(N) } != 1.";
		assertRegressionTestAnswerSets(cfg, program, "p(1), p(2), q");
	}

	@AggregateRegressionTest
	public void aggregateRightHandTermCountLt(SystemConfig cfg) {
		String program = "p(1)." + 
			"p(2)." + 
			"q :- #count { N : p(N) } < 3.";
		assertRegressionTestAnswerSets(cfg, program, "p(1), p(2), q");
	}

}
