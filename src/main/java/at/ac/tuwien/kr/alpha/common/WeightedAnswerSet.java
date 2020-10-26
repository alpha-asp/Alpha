package at.ac.tuwien.kr.alpha.common;

import java.util.ArrayList;

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
