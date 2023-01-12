package at.ac.tuwien.kr.alpha.core.solver.optimization;

import at.ac.tuwien.kr.alpha.core.solver.WritableAssignment;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Enables the valuation of an answer-set based on weak constraints.
 * This implementation is not based on callbacks whenever atoms change their assignment but rather checks all
 * {@link at.ac.tuwien.kr.alpha.grounder.atoms.WeakConstraintAtom}s sequentially whenever a valuation is requested.
 * This implementation is intended to provide valuations only for full answer-sets.
 * Copyright (c) 2023, the Alpha Team.
 */
public class WeakConstraintsManagerForUnboundedEnumeration implements WeakConstraintsManager {

	private final List<Triple<Integer, Integer, Integer>> knownAtomValuations = new ArrayList<>();
	private final WritableAssignment assignment;

	public WeakConstraintsManagerForUnboundedEnumeration(WritableAssignment assignment) {
		this.assignment = assignment;
	}

	@Override
	public void setChecksEnabled(boolean checksEnabled) {

	}

	@Override
	public void addWeakConstraintsInformation(List<Triple<Integer, Integer, Integer>> weakConstraintAtomWeightLevels) {
		knownAtomValuations.addAll(weakConstraintAtomWeightLevels);
	}

	@Override
	public TreeMap<Integer, Integer> getCurrentWeightAtLevels() {
		TreeMap<Integer, Integer> currentWeights = new TreeMap<>();
		for (Triple<Integer, Integer, Integer> atomWeightLevel : knownAtomValuations) {
			if (assignment.getTruth(atomWeightLevel.getLeft()).toBoolean()) {
				Integer atomLevel = atomWeightLevel.getRight();
				Integer currentWeight = currentWeights.get(atomLevel);
				if (currentWeight == null) {
					currentWeights.put(atomLevel, atomWeightLevel.getMiddle());
				} else {
					currentWeights.put(atomLevel, currentWeight + atomWeightLevel.getMiddle());
				}
			}
		}
		return currentWeights;
	}
}
