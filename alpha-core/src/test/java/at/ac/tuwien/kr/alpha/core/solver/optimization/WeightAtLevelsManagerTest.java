package at.ac.tuwien.kr.alpha.solver.optimization;

import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Copyright (c) 2021, the Alpha Team.
 */
public class WeightAtLevelsManagerTest {

	@Test
	public void simpleWeightAtTwoLevels() {
		WeightAtLevelsManager weightAtLevelsManager = new WeightAtLevelsManager();
		weightAtLevelsManager.setChecksEnabled(true);
		weightAtLevelsManager.setMaxLevel(3);
		assertTrue(weightAtLevelsManager.getCurrentWeightAtLevels().isEmpty());
		weightAtLevelsManager.increaseCurrentWeight(0, 2);
		weightAtLevelsManager.increaseCurrentWeight(3, 1);
		TreeMap<Integer, Integer> currentWeightsAfterIncrease = weightAtLevelsManager.getCurrentWeightAtLevels();
		assertEquals(1, currentWeightsAfterIncrease.get(3).longValue());
		assertEquals(2, currentWeightsAfterIncrease.get(0).longValue());
		assertEquals(2, currentWeightsAfterIncrease.size());

		weightAtLevelsManager.decreaseCurrentWeight(0, 2);

		TreeMap<Integer, Integer> currentWeightsAfterDecrease = weightAtLevelsManager.getCurrentWeightAtLevels();
		assertEquals(1, currentWeightsAfterDecrease.get(3).longValue());
		assertNull(currentWeightsAfterDecrease.get(0));
		assertEquals(1, currentWeightsAfterDecrease.size());
	}

	@Test
	public void currentIsBestAfterDecrease() {
		WeightAtLevelsManager weightAtLevelsManager = new WeightAtLevelsManager();
		weightAtLevelsManager.setChecksEnabled(true);
		weightAtLevelsManager.setMaxLevel(0);
		assertTrue(weightAtLevelsManager.getCurrentWeightAtLevels().isEmpty());
		weightAtLevelsManager.increaseCurrentWeight(0, 2);
		weightAtLevelsManager.increaseCurrentWeight(0, 2);
		weightAtLevelsManager.markCurrentWeightAsBestKnown();

		assertFalse(weightAtLevelsManager.isCurrentBetterThanBest());

		weightAtLevelsManager.decreaseCurrentWeight(0, 2);
		assertTrue(weightAtLevelsManager.isCurrentBetterThanBest());
	}

	@Test
	public void addAtomOnHigherThanMaxLevel() {
		WeightAtLevelsManager weightAtLevelsManager = new WeightAtLevelsManager();
		weightAtLevelsManager.setChecksEnabled(true);
		weightAtLevelsManager.setMaxLevel(3);
		assertTrue(weightAtLevelsManager.getCurrentWeightAtLevels().isEmpty());
		weightAtLevelsManager.increaseCurrentWeight(3, 1);
		weightAtLevelsManager.increaseCurrentWeight(0, 2);
		weightAtLevelsManager.markCurrentWeightAsBestKnown();

		assertFalse(weightAtLevelsManager.isCurrentBetterThanBest());

		weightAtLevelsManager.decreaseCurrentWeight(0, 2);
		assertTrue(weightAtLevelsManager.isCurrentBetterThanBest());
		// Now increase some weight at a level beyond the declared maxLevel, so an Exception is expected here.
		assertThrows(RuntimeException.class, () -> {
			weightAtLevelsManager.increaseCurrentWeight(4, 3);
		});
	}

	@Test
	public void markingCurrentWeightAsBestDecreasesMaximumLevel() {
		WeightAtLevelsManager weightAtLevelsManager = new WeightAtLevelsManager();
		weightAtLevelsManager.setChecksEnabled(true);
		weightAtLevelsManager.setMaxLevel(3);
		assertTrue(weightAtLevelsManager.getCurrentWeightAtLevels().isEmpty());
		weightAtLevelsManager.increaseCurrentWeight(1, 1);

		assertEquals(3, weightAtLevelsManager.getMaxLevel());
		weightAtLevelsManager.markCurrentWeightAsBestKnown();
		assertTrue(weightAtLevelsManager.getMaxLevel() < 3);
	}

	@Test
	public void indirectTrimmingCheck() {
		WeightAtLevelsManager weightAtLevelsManager = new WeightAtLevelsManager();
		weightAtLevelsManager.setChecksEnabled(true);
		weightAtLevelsManager.setMaxLevel(4);
		assertTrue(weightAtLevelsManager.getCurrentWeightAtLevels().isEmpty());
		weightAtLevelsManager.increaseCurrentWeight(4, 1);
		weightAtLevelsManager.markCurrentWeightAsBestKnown();

		weightAtLevelsManager.decreaseCurrentWeight(4, 1);
		weightAtLevelsManager.increaseCurrentWeight(2, 1);
		weightAtLevelsManager.increaseCurrentWeight(1, 3);
		weightAtLevelsManager.markCurrentWeightAsBestKnown();
		weightAtLevelsManager.decreaseCurrentWeight(1, 3);
		// Note: the below assertion relies on the implementation of WeightAtLevelsManager doing trimming.
		assertTrue(weightAtLevelsManager.getMaxLevel() < 3);
	}

	@Test
	public void trimmingAtLevelZero() {
		WeightAtLevelsManager weightAtLevelsManager = new WeightAtLevelsManager();
		weightAtLevelsManager.setChecksEnabled(true);
		weightAtLevelsManager.setMaxLevel(3);
		assertTrue(weightAtLevelsManager.getCurrentWeightAtLevels().isEmpty());
		weightAtLevelsManager.increaseCurrentWeight(2, 1);
		weightAtLevelsManager.markCurrentWeightAsBestKnown();

		weightAtLevelsManager.decreaseCurrentWeight(2, 1);
		weightAtLevelsManager.increaseCurrentWeight(1, 1);
		weightAtLevelsManager.markCurrentWeightAsBestKnown();
		weightAtLevelsManager.decreaseCurrentWeight(1, 1);

		weightAtLevelsManager.markCurrentWeightAsBestKnown();
		assertFalse(weightAtLevelsManager.isCurrentBetterThanBest());
	}

	@Test
	public void initializationWithMaxBound() {
		WeightAtLevelsManager weightAtLevelsManager = new WeightAtLevelsManager("1@1,5@6,3@2");
		weightAtLevelsManager.setChecksEnabled(true);
		assertTrue(weightAtLevelsManager.isCurrentBetterThanBest());
	}

	@Test
	public void initializationWithMaxBoundAllowsBetterUntilBound() {
		WeightAtLevelsManager weightAtLevelsManager = new WeightAtLevelsManager("1@2,4@3");
		weightAtLevelsManager.setChecksEnabled(true);
		TreeMap<Integer, Integer> initialWeights = weightAtLevelsManager.getCurrentWeightAtLevels();
		assertEquals(initialWeights.size(), 0);
		assertTrue(weightAtLevelsManager.isCurrentBetterThanBest());
		weightAtLevelsManager.increaseCurrentWeight(2, 1);
		assertTrue(weightAtLevelsManager.isCurrentBetterThanBest());
		weightAtLevelsManager.increaseCurrentWeight(3, 3);
		assertTrue(weightAtLevelsManager.isCurrentBetterThanBest());
		weightAtLevelsManager.increaseCurrentWeight(3, 1);
		assertFalse(weightAtLevelsManager.isCurrentBetterThanBest());
	}

	@Test
	public void initializationWithEmptyMaxBoundThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> new WeightAtLevelsManager(""));
	}
}