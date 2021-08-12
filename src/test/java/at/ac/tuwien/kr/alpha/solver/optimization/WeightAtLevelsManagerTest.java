package at.ac.tuwien.kr.alpha.solver.optimization;

import org.junit.Test;

import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2021, the Alpha Team.
 */
public class WeightAtLevelsManagerTest {

	@Test
	public void simpleWeightAtTwoLevels() {
		WeightAtLevelsManager weightAtLevelsManager = new WeightAtLevelsManager();
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
		weightAtLevelsManager.setMaxLevel(0);
		assertTrue(weightAtLevelsManager.getCurrentWeightAtLevels().isEmpty());
		weightAtLevelsManager.increaseCurrentWeight(0, 2);
		weightAtLevelsManager.increaseCurrentWeight(0, 2);
		weightAtLevelsManager.markCurrentWeightAsBestKnown();

		assertFalse(weightAtLevelsManager.isCurrentBetterThanBest());

		weightAtLevelsManager.decreaseCurrentWeight(0, 2);
		assertTrue(weightAtLevelsManager.isCurrentBetterThanBest());
	}

	@Test(expected = RuntimeException.class)
	public void addAtomOnHigherThanMaxLevel() {
		WeightAtLevelsManager weightAtLevelsManager = new WeightAtLevelsManager();
		weightAtLevelsManager.setMaxLevel(3);
		assertTrue(weightAtLevelsManager.getCurrentWeightAtLevels().isEmpty());
		weightAtLevelsManager.increaseCurrentWeight(3, 1);
		weightAtLevelsManager.increaseCurrentWeight(0, 2);
		weightAtLevelsManager.markCurrentWeightAsBestKnown();

		assertFalse(weightAtLevelsManager.isCurrentBetterThanBest());

		weightAtLevelsManager.decreaseCurrentWeight(0, 2);
		assertTrue(weightAtLevelsManager.isCurrentBetterThanBest());
		// Now increase some weight at a level beyond the declared maxLevel, so an Exception is expected here.
		weightAtLevelsManager.increaseCurrentWeight(4, 3);
	}

	@Test
	public void markingCurrentWeightAsBestDecreasesMaximumLevel() {
		WeightAtLevelsManager weightAtLevelsManager = new WeightAtLevelsManager();
		weightAtLevelsManager.setMaxLevel(3);
		assertTrue(weightAtLevelsManager.getCurrentWeightAtLevels().isEmpty());
		weightAtLevelsManager.increaseCurrentWeight(1, 1);

		assertEquals(3, weightAtLevelsManager.getMaxLevel());
		weightAtLevelsManager.markCurrentWeightAsBestKnown();
		assertTrue(weightAtLevelsManager.getMaxLevel() < 3);
	}
}