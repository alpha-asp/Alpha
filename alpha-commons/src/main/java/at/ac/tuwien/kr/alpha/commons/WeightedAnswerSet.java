package at.ac.tuwien.kr.alpha.commons;

import at.ac.tuwien.kr.alpha.api.AnswerSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.TreeMap;

/**
 * Represents a weighted answer set, i.e., it extends a {@link BasicAnswerSet} with weights information.
 *
 * Weights are given as a mapping from levels to the respective weight. Note that negative levels and weights are allowed.
 * The highest level is the most important one and levels not contained in the map have a weight of zero.
 *
 * Copyright (c) 2020-2021, the Alpha Team.
 */
public class WeightedAnswerSet extends BasicAnswerSet {

	private final TreeMap<Integer, Integer> weightPerLevel;

	public WeightedAnswerSet(AnswerSet answerSet, TreeMap<Integer, Integer> weightPerLevel) {
		super(answerSet.getPredicates(), answerSet.getPredicateInstances());
		this.weightPerLevel = weightPerLevel;
	}

	/**
	 * Compares the weights of this {@link WeightedAnswerSet} with the weights of the other one.
	 * @param other the {@link WeightedAnswerSet} to compare with.
	 * @return -1, 0, or 1 depending on whether the weights of this are better, equal, or worse than the other.
	 */
	public int compareWeights(WeightedAnswerSet other) {
		Iterator<Integer> thisDescIterator = this.weightPerLevel.descendingKeySet().iterator();
		Iterator<Integer> otherDescIterator = other.weightPerLevel.descendingKeySet().iterator();
		while (true) {
			// Descend this level down to one with non-zero weight.
			Integer thisLevel = null;
			Integer thisWeight = null;
			while (thisDescIterator.hasNext()) {
				thisLevel = thisDescIterator.next();
				thisWeight = this.weightPerLevel.get(thisLevel);
				if (thisWeight != null && thisWeight != 0) {
					break;
				}
			}
			// Descend other level down to one with non-zero weight.
			Integer otherLevel = null;
			Integer otherWeight = null;
			while (otherDescIterator.hasNext()) {
				otherLevel = otherDescIterator.next();
				otherWeight = other.weightPerLevel.get(otherLevel);
				if (otherWeight != null && otherWeight != 0) {
					break;
				}
			}
			if (thisWeight == null && otherWeight == null) {
				return 0;
			}
			if (thisWeight == null) {
				return otherWeight < 0 ? 1 : -1;
			}
			if (otherWeight == null) {
				return thisWeight < 0 ? -1 : 1;
			}
			// Found level and weight for this and other WeightedAnswerSet.
			if (!thisLevel.equals(otherLevel)) {
				return thisLevel < otherLevel ? -1 : 1;
			}
			if (!thisWeight.equals(otherWeight)) {
				return thisWeight < otherWeight ? -1 : 1;
			}
			// Same level and same weight for both WeightedAnswerSet, now continue with next lower level.
		}
	}

	@Override
	public int compareTo(AnswerSet other) {
		if (!(other instanceof WeightedAnswerSet)) {
			return -1;
		}
		int basicComparison = super.compareTo(other);
		if (basicComparison != 0) {
			return basicComparison;
		}
		WeightedAnswerSet otherWeighted = (WeightedAnswerSet) other;
		return compareWeights(otherWeighted);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), weightPerLevel);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof WeightedAnswerSet)) {
			return false;
		}

		WeightedAnswerSet that = (WeightedAnswerSet) o;

		if (!getPredicates().equals(that.getPredicates())) {
			return false;
		}

		if (!getPredicateInstances().equals(that.getPredicateInstances())) {
			return false;
		}

		return weightPerLevel.equals(that.weightPerLevel);
	}

	public String getWeightsAsString() {
		StringJoiner joiner = new StringJoiner(", ", "[", "]");
		for (Map.Entry<Integer, Integer> weightPerLevel : weightPerLevel.entrySet()) {
			joiner.add(weightPerLevel.getValue() + "@" + weightPerLevel.getKey());
		}
		return joiner.toString();
	}

	/**
	 * Extract weights at levels from string of comma-separated pairs of the form weight@level.
	 * Multiple weights for the same level are summed-up.
	 * @param weightAtLevels
	 * @return a TreeMap containing
	 */
	public static TreeMap<Integer, Integer> weightPerLevelFromString(String weightAtLevels) {
		String[] weightsAtLevels = weightAtLevels.split(",");
		TreeMap<Integer, Integer> weightAtLevelsTreeMap = new TreeMap<>();
		for (String weightsAtLevel : weightsAtLevels) {
			String[] wAtL = weightsAtLevel.trim().split("@");
			if (wAtL.length != 2) {
				throw new IllegalArgumentException("Could not parse given comma-separated list of weight@level pairs. Given input was: " + weightAtLevels);
			}
			int weight = Integer.parseInt(wAtL[0]);
			int level = Integer.parseInt(wAtL[1]);
			if (weight == 0) {
				continue;	// Skip zero weights.
			}
			weightAtLevelsTreeMap.putIfAbsent(level, 0);
			weightAtLevelsTreeMap.put(level, weight + weightAtLevelsTreeMap.get(level));
		}
		return weightAtLevelsTreeMap;
	}

	@Override
	public String toString() {
		return super.toString() + getWeightsAsString();
	}
}
