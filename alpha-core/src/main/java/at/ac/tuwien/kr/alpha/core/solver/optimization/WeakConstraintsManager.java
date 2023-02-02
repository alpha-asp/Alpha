package at.ac.tuwien.kr.alpha.core.solver.optimization;

import at.ac.tuwien.kr.alpha.core.solver.Checkable;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.TreeMap;

/**
 * A {@link WeakConstraintsManager} enables the solver to compute the valuation of answer-sets if weak constraints are
 * present in the input program.
 * Copyright (c) 2023, the Alpha Team.
 */
public interface WeakConstraintsManager extends Checkable {

	/**
	 * Registers newly obtained weak constraint atoms to be respected by the {@link WeakConstraintsManager}.
	 * @param weakConstraintAtomWeightLevels a list of triples (a,b,c) representing weak constraint atoms, where a is
	 *                                       the atom, b the weight and c the level of the respective weak constraint.
	 */
	void addWeakConstraintsInformation(List<Triple<Integer, Integer, Integer>> weakConstraintAtomWeightLevels);

	/**
	 * Returns the valuation of the current assignment as weights and their levels.
	 * @return a TreeMap mapping levels to weight, only levels with weight != 0 are guaranteed to be reported.
	 */
	TreeMap<Integer, Integer> getCurrentWeightAtLevels();
}
