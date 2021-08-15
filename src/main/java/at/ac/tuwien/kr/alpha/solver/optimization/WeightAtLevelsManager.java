package at.ac.tuwien.kr.alpha.solver.optimization;

import at.ac.tuwien.kr.alpha.solver.Checkable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.TreeMap;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static java.lang.Math.max;

/**
 * Manages two sets of weights-at-levels (basically the valuation of two potential answer-sets). One set is the
 * valuation of the best-known answer-set at the time, the other is the (partial) valuation of the currently explored
 * (partial) answer-set.
 *
 * Weights-at-levels are stored in arrays in reverse order (array position 0 is most important level). Therefore, the
 * maximum level must be declared upfront. Furthermore, the array may grow if less-important levels are added later and
 * it is automatically trimmed if highest-level weights are 0 when marking the current valuation as the best-known one.
 *
 * Code using this class must therefore ensure not to add weights at levels beyond the current maximum one.
 *
 * Copyright (c) 2021, the Alpha Team.
 */
public class WeightAtLevelsManager implements Checkable {
	private static final Logger LOGGER = LoggerFactory.getLogger(WeightAtLevelsManager.class);
	private boolean checksEnabled;

	// Stores weights in inverted order: zero is highest violated level, may also grow to represent negative levels.
	private ArrayList<Integer> currentWeightAtLevels;
	private ArrayList<Integer> bestKnownWeightAtLevels;
	// The highest level for which holds: on all levels below (or equal), the current weights are the same as the weights for the best known.
	private int maxOffsetCurrentIsAllEqualBest;
	// Stores which actual level corresponds to array position 0 in bestKnownWeightAtLevels, i.e, the highest level that is violated. Value may decrease if better answer-sets are found.
	private int maxLevel;
	private boolean isCurrentBetterThanBest;
	private boolean hasBestKnown;

	public WeightAtLevelsManager() {
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
		if (!hasBestKnown) {
			throw oops("WeightAtLevelsManager has no best-known valuation yet.");
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

	/**
	 * Returns the current maximum level. Note that this may be smaller than a previously set one as the maximum
	 * level may decrease following the marking of another valuation as best-known.
	 * @return the current maximum level.
	 */
	public int getMaxLevel() {
		return maxLevel;
	}

	/**
	 * Increases the current valuation at the given level by the given weight.
	 * @param level the level to increase.
	 * @param weight the weight at the level.
	 */
	public void increaseCurrentWeight(int level, int weight) {
		if (checksEnabled) {
			runChecks();
		}
		if (level > maxLevel) {
			throw oops("WeightAtLevelsManager invoked to increase violation above maximum declared level.");
		}
		// Record the new weight.
		growForLevel(currentWeightAtLevels, level);
		int listOffset = levelToListOffset(level);
		int newWeight = currentWeightAtLevels.get(listOffset) + weight;
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
			moveUpwardsMaxOffsetCurrentIsAllEqual();
			recomputeIsCurrentBetterThanBest();
		} else {
			throw oops("Increasing weight of current answer reached unforeseen state.");
		}
		if (checksEnabled) {
			runChecks();
		}
	}

	/**
	 * Decreases the current valuation at the given level by the given weight.
	 * @param level the level to decrease.
	 * @param weight the weight at the level.
	 */
	public void decreaseCurrentWeight(int level, int weight) {
		if (checksEnabled) {
			runChecks();
		}
		if (level > maxLevel) {
			throw oops("WeightAtLevelsManager invoked to decrease violation above maximum declared level.");
		}
		// Record the new weight.
		int listOffset = levelToListOffset(level);
		int newWeight = currentWeightAtLevels.get(listOffset) - weight;
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
			moveUpwardsMaxOffsetCurrentIsAllEqual();
			recomputeIsCurrentBetterThanBest();
		} else {
			throw oops("Decreasing weight of current answer reached unforeseen state.");
		}
		if (checksEnabled) {
			runChecks();
		}
	}

	private void moveUpwardsMaxOffsetCurrentIsAllEqual() {
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
	 *
	 * May shrink the underlying maximum level if current highest level(s) are zero.
	 */
	public void markCurrentWeightAsBestKnown() {
		LOGGER.trace("Marking current answer-set as best known.");
		if (!hasBestKnown) {
			hasBestKnown = true;
			LOGGER.trace("  Initially, current/best known weights are: {}", currentWeightAtLevels);
		}
		trimZeroWeightLevels();
		bestKnownWeightAtLevels = new ArrayList<>(currentWeightAtLevels);
		maxOffsetCurrentIsAllEqualBest = bestKnownWeightAtLevels.size() - 1;
		LOGGER.trace("  Max offset current is all equal to best known is: {}", maxOffsetCurrentIsAllEqualBest);
		LOGGER.trace("  Current/Best known weights are: {}", bestKnownWeightAtLevels);
		isCurrentBetterThanBest = false;
		LOGGER.trace("End marking current answer-set as best known.");
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

	/**
	 * Initializes the maximum level that will ever be encountered while solving.
	 * This method may only be called once and must be called before the first weights are set/increased.
	 *
	 * Note that the maximum level may decrease automatically following calls to markCurrentWeightAsBestKnown.
	 *
	 * @param maxLevel the highest level ever to be encountered.
	 */
	public void setMaxLevel(int maxLevel) {
		if (currentWeightAtLevels != null) {
			throw oops("WeightAtLevelsManager.setMaxLevel called more than once.");
		}
		this.maxLevel = maxLevel;
		LOGGER.trace("Maximum level (0-offset) is set to: {} ({})", maxLevel, listOffsetToLevel(0));
		currentWeightAtLevels = new ArrayList<>();
	}

	private int levelToListOffset(int level) {
		int levelInList = maxLevel - level;
		if (levelInList < 0) {
			throw oops("Level optimisation in WeightAtLevelsManager is negative.");
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
			throw oops("WeightAtLevelsManager has best known answer set information but no answer set was found yet.");
		}
		checkBestKnownFreeOfLeadingZeroLevels();
		if (hasBestKnown) {
			// Check whether isCurrentBetterThanBest flag is consistent.
			if (isCurrentBetterThanBest != checkIsCurrentBetterThanBest()) {
				throw oops("WeightAtLevelsManager detected valuation of current assignment is inconsistent with state flag.");
			}
			checkLevelBar();
		}
		LOGGER.trace("Checks done.");
	}

	private void checkBestKnownFreeOfLeadingZeroLevels() {
		// Tolerate Zero weight if there is only one level.
		if (!hasBestKnown || bestKnownWeightAtLevels.size() == 1) {
			return;
		}
		if (!bestKnownWeightAtLevels.isEmpty() && bestKnownWeightAtLevels.get(0) == 0) {
			throw oops("WeightAtLevelsManager has best-known answer set with zero-weights at highest level.");
		}
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
			throw oops("WeightAtLevelsManager detected level bar at wrong level, it is " + maxOffsetCurrentIsAllEqualBest
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
