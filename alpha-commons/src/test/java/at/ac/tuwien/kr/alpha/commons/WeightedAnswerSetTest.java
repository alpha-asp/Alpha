package at.ac.tuwien.kr.alpha.commons;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import org.junit.jupiter.api.Test;

import static at.ac.tuwien.kr.alpha.commons.WeightedAnswerSet.weightPerLevelFromString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Copyright (c) 2021, the Alpha Team.
 */
public class WeightedAnswerSetTest {

	@Test
	public void compareWeightedAnswerSets() {
		AnswerSet answerSet1 = new AnswerSetBuilder().predicate("a").instance().predicate("p").instance(Terms.newSymbolicConstant("b")).build();
		AnswerSet answerSet2 = new AnswerSetBuilder().predicate("a").instance().build();
		WeightedAnswerSet was1 = new WeightedAnswerSet(answerSet1, weightPerLevelFromString("1@1"));
		WeightedAnswerSet was2 = new WeightedAnswerSet(answerSet2, weightPerLevelFromString("1@1, 2@3"));
		WeightedAnswerSet was1Same = new WeightedAnswerSet(answerSet1, weightPerLevelFromString("1@1"));

		assertFalse(was1.getPredicates().isEmpty());
		assertFalse(was2.getPredicates().isEmpty());
		assertNotEquals(was1, was2);
		assertEquals(was1Same, was1);
		assertEquals(was1.getPredicateInstances(), was1Same.getPredicateInstances());
	}

	@Test
	public void compareAnswerSetsSameAtoms() {
		AnswerSet answerSet = new AnswerSetBuilder().predicate("q").instance(Terms.newConstant(0), Terms.newConstant(1))
			.predicate("b").instance().predicate("foo").instance(Terms.newSymbolicConstant("a")).build();
		WeightedAnswerSet was3 = new WeightedAnswerSet(answerSet, weightPerLevelFromString("1@1, 2@3"));
		WeightedAnswerSet was3AtomsNotWeights = new WeightedAnswerSet(answerSet, weightPerLevelFromString("2@1, 2@3"));

		assertFalse(was3.getPredicates().isEmpty());
		assertNotEquals(was3, was3AtomsNotWeights);
		assertEquals(was3.getPredicateInstances(), was3AtomsNotWeights.getPredicateInstances());
	}

	@Test
	public void compareAnswerSetsSameWeights() {
		String was4Weights = "1@1, 4@3";
		AnswerSet answerSetBC = new AnswerSetBuilder().predicate("b").instance().predicate("c").instance().build();
		AnswerSet answerSetBD = new AnswerSetBuilder().predicate("b").instance().predicate("d").instance().build();
		WeightedAnswerSet was4 = new WeightedAnswerSet(answerSetBC, weightPerLevelFromString(was4Weights));
		WeightedAnswerSet was5 = new WeightedAnswerSet(answerSetBD, weightPerLevelFromString(was4Weights));

		assertFalse(was4.getPredicates().isEmpty());
		assertFalse(was5.getPredicates().isEmpty());
		assertEquals(0, was4.compareWeights(was5));
		assertNotEquals(was4, was5);
		assertNotEquals(was5, was4);
	}

	@Test
	public void compareWeights() {
		AnswerSet answerSet = new AnswerSetBuilder().predicate("a").instance().build();
		WeightedAnswerSet was6 = new WeightedAnswerSet(answerSet, weightPerLevelFromString("2@3"));
		WeightedAnswerSet was7 = new WeightedAnswerSet(answerSet, weightPerLevelFromString("1@1, 1@3"));
		WeightedAnswerSet was8 = new WeightedAnswerSet(answerSet, weightPerLevelFromString("1@-3, 1@1, 1@3"));
		assertEquals(-1, was7.compareWeights(was6));
		assertEquals(1, was6.compareWeights(was7));

		assertEquals(-1, was7.compareWeights(was8));
		assertEquals(1, was8.compareWeights(was7));

		assertEquals(-1, was8.compareWeights(was6));
		assertEquals(1, was6.compareWeights(was8));
	}

}