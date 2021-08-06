package at.ac.tuwien.kr.alpha.solver.optimization;

import at.ac.tuwien.kr.alpha.solver.Checkable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static java.lang.Math.max;

/**
 * Manages two sets of weights-at-levels (basically the valuation of two potential answer-sets). One set is the
 * valuation of the best-known answer-set at the time, the other is the (partial) valuation of the currently explored
 * (partial) answer-set.
 *
 * Copyright (c) 2021, the Alpha Team.
 */
public class WeightAtLevelsManager implements Checkable {
	private static final Logger LOGGER = LoggerFactory.getLogger(WeightAtLevelsManager.class);
	private boolean checksEnabled;

	private final WeakConstraintsManager weakConstraintsManager;
	// Stores weights in inverted order: zero is highest violated level, may also grow to represent negative levels.
	private ArrayList<Integer> currentWeightAtLevels;
	private ArrayList<Integer> bestKnownWeightAtLevels;
	// The highest level for which holds: on all levels below (or equal), the current weights are the same as the weights for the best known.
	private int maxOffsetCurrentIsAllEqualBest;
	// Stores which actual level corresponds to array position 0 in bestKnownWeightAtLevels, i.e, the highest level that is violated. Value may decrease if better answer-sets are found.
	private int maxLevel;
	private boolean isCurrentBetterThanBest;
	private boolean hasBestKnown;

	public WeightAtLevelsManager(WeakConstraintsManager weakConstraintsManager) {
		this.weakConstraintsManager = weakConstraintsManager;
		currentWeightAtLevels = new ArrayList<>();
		bestKnownWeightAtLevels = new ArrayList<>();
		maxOffsetCurrentIsAllEqualBest = -1;
		isCurrentBetterThanBest = true;
	}

	@Override
	public void setChecksEnabled(boolean checksEnabled) {
		this.checksEnabled = checksEnabled;
	}

	/**
	 * Returns whether the current valuation is (still) better than the best-known one.
	 * @return true iff the current weights-at-levels are better than the best-known.
	 */
	public boolean isCurrentBetterThanBest() {
		if (checksEnabled) {
			runChecks();
		}
		return isCurrentBetterThanBest;
	}

	/**
	 * Returns the current weights and their levels.
	 * @return a TreeMap mapping levels to weights. Note that values may be non-continuous and negative levels are possible.
	 */
	public TreeMap<Integer, Integer> getCurrentWeightAtLevels() {
		TreeMap<Integer, Integer> weightPerLevels = new TreeMap<>();
		for (int i = 0; i < currentWeightAtLevels.size(); i++) {
			Integer weightAti = currentWeightAtLevels.get(i);
			if (weightAti == 0) {
				continue;
			}
			weightPerLevels.put(listOffsetToLevel(i), weightAti);
		}
		return weightPerLevels;
	}

	public void increaseCurrentWeight(WeakConstraintAtomCallback atom) {
		if (checksEnabled) {
			runChecks();
		}
		// Record the new weight.
		growForLevel(currentWeightAtLevels, atom.level);
		int listOffset = levelToListOffset(atom.level);
		int newWeight = currentWeightAtLevels.get(listOffset) + atom.weight;
		currentWeightAtLevels.set(listOffset, newWeight);

		// Now check whether current is worse than best known.
		if (listOffset > maxOffsetCurrentIsAllEqualBest + 1) {
			// Weight in some level where differences do not (yet) matter increased, but on lower level current partial assignment is still better than best found answer-set.
			// Nothing to do here.
			return;
		} else if (listOffset <= maxOffsetCurrentIsAllEqualBest) {
			// Weight in level with previously equal weights increased, current is now worse than best.
			isCurrentBetterThanBest = false;
			maxOffsetCurrentIsAllEqualBest = listOffset - 1;
		} else if (listOffset == maxOffsetCurrentIsAllEqualBest + 1) {
			// Weight increased for the first level where current and best-known are not equal.
			moveUpwardsMaxLevelCurrentIsAllEqual();
			recomputeIsCurrentBetterThanBest();
		} else {
			throw oops("Increasing weight of current answer reached unforeseen state.");
		}
	}

	public void decreaseCurrentWeight(WeakConstraintAtomCallback atom) {
		if (checksEnabled) {
			runChecks();
		}
		// Record the new weight.
		int listOffset = levelToListOffset(atom.level);
		int newWeight = currentWeightAtLevels.get(listOffset) - atom.weight;
		currentWeightAtLevels.set(listOffset, newWeight);

		// Now check whether current is better than best known and adapt maxOffsetCurrentIsAllEqualBest.
		if (listOffset <= maxOffsetCurrentIsAllEqualBest) {
			// Improved below the point where current is equal to best, so current is better than best now.
			isCurrentBetterThanBest = true;
			// All weights below the change are equal.
			maxOffsetCurrentIsAllEqualBest = listOffset - 1;
		} else if (listOffset > maxOffsetCurrentIsAllEqualBest + 1) {
			// Decrease in such a high level, that there is at least one level (maxLevel+1) where weights are
			// different and whose difference still dominates whether current is better than best known.
			return;
		} else if (listOffset == maxOffsetCurrentIsAllEqualBest + 1) {
			// Decrease might make current better than best known and change level of equals.
			moveUpwardsMaxLevelCurrentIsAllEqual();
			recomputeIsCurrentBetterThanBest();
		} else {
			throw oops("Decreasing weight of current answer reached unforeseen state.");
		}
	}

	private void moveUpwardsMaxLevelCurrentIsAllEqual() {
		for (int i = max(maxOffsetCurrentIsAllEqualBest, 0); i < currentWeightAtLevels.size(); i++) {
			int currentLevelWeight = currentWeightAtLevels.get(i);
			int bestLevelWeight = bestKnownWeightAtLevels.size() > i ? bestKnownWeightAtLevels.get(i) : 0;
			if (currentLevelWeight != bestLevelWeight) {
				// Level is the first where values differ, previous level was maximum one where values are equal.
				maxOffsetCurrentIsAllEqualBest = i - 1;
				return;
			}
		}
		// Iterated all levels, so weights are the same on all levels.
		maxOffsetCurrentIsAllEqualBest = currentWeightAtLevels.size() - 1;
	}

	private void recomputeIsCurrentBetterThanBest() {
		// The weights at the first level above maxOffsetCurrentIsAllEqualBest dominate whether current is worse than best known.
		if (maxOffsetCurrentIsAllEqualBest == currentWeightAtLevels.size() - 1) {
			// Current and best known are equal even up to the last level.
			isCurrentBetterThanBest = false;
			return;
		}
		int comparisonOffset = maxOffsetCurrentIsAllEqualBest + 1;
		int currentLevelWeight = currentWeightAtLevels.get(comparisonOffset);
		int bestLevelWeight = bestKnownWeightAtLevels.size() > comparisonOffset ? bestKnownWeightAtLevels.get(comparisonOffset) : 0;
		isCurrentBetterThanBest = currentLevelWeight < bestLevelWeight;
	}

	private int listOffsetToLevel(int offset) {
		return maxLevel - offset;
	}

	private void growForLevel(ArrayList<Integer> listToGrow, int level) {
		int listOffset = levelToListOffset(level);
		while (listOffset >= listToGrow.size()) {
			listToGrow.add(0);
		}
	}

	/**
	 * Marks the current valuation/weights-at-levels as being the best-known one.
	 */
	public void markCurrentWeightAsBestKnown() {
		LOGGER.trace("Marking current answer-set as best known.");
		if (!hasBestKnown) {
			initializeFirstWeightsAtLevel();
		} else {
			trimZeroWeightLevels();
			bestKnownWeightAtLevels = new ArrayList<>(currentWeightAtLevels);
		}
		maxOffsetCurrentIsAllEqualBest = bestKnownWeightAtLevels.size() - 1;
		LOGGER.trace("Max offset current is all equal to best known is: {}", maxOffsetCurrentIsAllEqualBest);
		isCurrentBetterThanBest = false;
	}

	/**
	 * Removes unnecessary high levels (with weights 0) from current valuation.
	 */
	private void trimZeroWeightLevels() {
		int trim = 0;
		for (int i = 0; i < currentWeightAtLevels.size(); i++) {
			if (currentWeightAtLevels.get(i) != 0) {
				trim = i;
				break;
			}
		}
		if (trim > 0) {
			currentWeightAtLevels = new ArrayList<>(currentWeightAtLevels.subList(trim, currentWeightAtLevels.size()));
			maxLevel -= trim;
		}
	}

	private void initializeFirstWeightsAtLevel() {
		// First answer set has been found, take its maximum level as zero-level.
		LOGGER.trace("Initializing for first answer-set.");
		HashMap<Integer, Integer> sumWeightsAtLevel = new HashMap<>();
		int highestLevel = 0;
		int lowestLevel = 0;
		for (WeakConstraintAtomCallback atomCallback : weakConstraintsManager.getTrueWeakConstraintAtomCallbacksOfFirstAnswerSet()) {
			int level = atomCallback.level;
			// Record if level is highest or lowest so far.
			if (highestLevel < level) {
				highestLevel = level;
			}
			if (lowestLevel > level) {
				lowestLevel = level;
			}
			// Update weight information.
			if (sumWeightsAtLevel.get(level) == null) {
				sumWeightsAtLevel.put(level, atomCallback.weight);
			} else {
				int newWeight = sumWeightsAtLevel.get(level) + atomCallback.weight;
				sumWeightsAtLevel.put(level, newWeight);
			}
		}
		maxLevel = highestLevel;
		LOGGER.trace("Maximum recorded level (0-offset) is: {} ({})", maxLevel, listOffsetToLevel(0));
		bestKnownWeightAtLevels = new ArrayList<>();
		growForLevel(bestKnownWeightAtLevels, lowestLevel);
		for (Map.Entry<Integer, Integer> levelWeight : sumWeightsAtLevel.entrySet()) {
			bestKnownWeightAtLevels.set(maxLevel - levelWeight.getKey(), levelWeight.getValue());
		}
		currentWeightAtLevels = new ArrayList<>(bestKnownWeightAtLevels);
		hasBestKnown = true;
		LOGGER.trace("Initially, current/best known weights are: {}", bestKnownWeightAtLevels);
	}

	private int levelToListOffset(int level) {
		int levelInList = maxLevel - level;
		if (levelInList < 0) {
			throw oops("Level optimisation in WeakConstraintsManager is negative.");
		}
		return levelInList;
	}

	private void runChecks() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Running checks.");
			LOGGER.trace("Maximum recorded level (0-offset) is: {} ({})", maxLevel, listOffsetToLevel(0));
			LOGGER.trace("Currently best known weights are: {}", bestKnownWeightAtLevels);
			LOGGER.trace("Current weights are: {}", currentWeightAtLevels);
			LOGGER.trace("Max offset current is all equal to best known is: {}", maxOffsetCurrentIsAllEqualBest);
			LOGGER.trace("Is current better than best-known: {}", isCurrentBetterThanBest);

		}
		if (!hasBestKnown && !bestKnownWeightAtLevels.isEmpty()) {
			throw oops("WeakConstraintManager has best known answer set information but no answer set was found yet.");
		}
		if (hasBestKnown && bestKnownWeightAtLevels.get(0) == 0) {
			throw oops("WeakConstraintManager has best-known answer set with zero-weights at highest level.");
		}
		// Check whether isCurrentBetterThanBest flag is consistent.
		if (isCurrentBetterThanBest != checkIsCurrentBetterThanBest()) {
			throw oops("WeakConstraintManager detected valuation of current assignment is inconsistent with state flag.");
		}
		checkLevelBar();
		LOGGER.trace("Checks done.");
	}

	private void checkLevelBar() {
		int highestLevelWhereLowerLevelsAreEqual = -1;
		for (int i = 0; i < currentWeightAtLevels.size(); i++) {
			int levelValue = i < bestKnownWeightAtLevels.size() ? bestKnownWeightAtLevels.get(i) : 0;
			if (currentWeightAtLevels.get(i) != levelValue) {
				break;
			}
			highestLevelWhereLowerLevelsAreEqual = i;
		}
		// The level bar should be at highestLevelWhereLowerLevelsAreEqual+1.
		if (maxOffsetCurrentIsAllEqualBest != highestLevelWhereLowerLevelsAreEqual) {
			throw oops("WeakConstraintManager detected level bar at wrong level, it is " + maxOffsetCurrentIsAllEqualBest
				+ "and should be " + highestLevelWhereLowerLevelsAreEqual);
		}
	}

	private boolean checkIsCurrentBetterThanBest() {
		if (!hasBestKnown) {
			// If no answer set has been found, the current is always better than best known.
			return true;
		}
		for (int i = 0; i < currentWeightAtLevels.size(); i++) {
			if (i >= bestKnownWeightAtLevels.size()) {
				// Current has violations at level not violated by best.
				return false;
			}
			if (currentWeightAtLevels.get(i) < bestKnownWeightAtLevels.get(i)) {
				return true;
			}
			if (currentWeightAtLevels.get(i) > bestKnownWeightAtLevels.get(i)) {
				return false;
			}
		}
		// All levels have same weight for current as for best known, so at this point usually current is not better than best.
		return false;
	}
}
