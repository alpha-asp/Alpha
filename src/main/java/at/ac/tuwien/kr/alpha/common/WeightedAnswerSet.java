package at.ac.tuwien.kr.alpha.common;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a weighted answer sets, i.e., it extends a {@link BasicAnswerSet} with weights information.
 *
 * Copyright (c) 2020, the Alpha Team.
 */
public class WeightedAnswerSet extends BasicAnswerSet {

	private final ArrayList<Integer> weightsAtLevel;

	public WeightedAnswerSet(BasicAnswerSet basicAnswerSet, ArrayList<Integer> weightsAtLevel) {
		super(basicAnswerSet.getPredicates(), basicAnswerSet.getPredicateInstances());
		this.weightsAtLevel = weightsAtLevel;
	}

	public ArrayList<Integer> getWeightsAtLevel() {
		return weightsAtLevel;
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
		if (weightsAtLevel.size() != otherWeighted.weightsAtLevel.size()) {
			return weightsAtLevel.size() - otherWeighted.weightsAtLevel.size();
		}
		for (int i = 0; i < weightsAtLevel.size(); i++) {
			Integer thisWeightAtI = weightsAtLevel.get(i);
			Integer otherWeightAtI = otherWeighted.weightsAtLevel.get(i);
			if (!thisWeightAtI.equals(otherWeightAtI)) {
				return thisWeightAtI - otherWeightAtI;
			}
		}
		return 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), weightsAtLevel);
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

		return weightsAtLevel.equals(that.weightsAtLevel);
	}

	public String getWeightsAsString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < weightsAtLevel.size(); i++) {
			if (i != 0) {
				sb.append(",");
			}
			Integer weight = weightsAtLevel.get(i);
			sb.append(weight);
			sb.append("@");
			sb.append(i);
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public String toString() {
		return super.toString() + getWeightsAsString();
	}
}
