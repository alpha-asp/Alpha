package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages weak constraints: stores the value of the current partial assignment, the best known value of any answer-set
 * and handles callbacks/computations when atoms (that represent weak constraints) change their truth value.
 *
 * Copyright (c) 2020, the Alpha Team.
 */
public class WeakConstraintsManager {

	private ArrayList<Integer> currentWeightAtLevels;
	private ArrayList<Integer> bestKnownWeightAtLevels;
	private final WritableAssignment assignment;
	private boolean isCurrentWorseThanBest;

	public WeakConstraintsManager(WritableAssignment assignment) {
		this.assignment = assignment;
		this.currentWeightAtLevels = new ArrayList<>();
		this.bestKnownWeightAtLevels = new ArrayList<>();
		this.isCurrentWorseThanBest = false;
	}

	public void addWeakConstraintsInformation(List<Triple<Integer, Integer, Integer>> weakConstraintAtomWeightLevels) {
		// Register all newly obtained weak constraint atoms for callback at the assignment.
		for (Triple<Integer, Integer, Integer> weakConstraintAtomWeightLevel : weakConstraintAtomWeightLevels) {
			WeakConstraintAtom wcA = new WeakConstraintAtom(weakConstraintAtomWeightLevel.getLeft(), weakConstraintAtomWeightLevel.getMiddle(), weakConstraintAtomWeightLevel.getRight());
			assignment.getAtomCallbackManager().recordCallback(wcA.atom, wcA);
		}
	}

	/**
	 * Marks the current weight as the one of the currently best of any known answer-set.
	 */
	public void markCurrentWeightAsBestKnown() {
		bestKnownWeightAtLevels = currentWeightAtLevels;
	}

	/**
	 * Returns whether the current partial interpretation is already worse that the best-known answer-set.
	 * @return true if the current partial interpretation has worse weight than the best-known answer-set.
	 */
	public boolean isCurrentWorseThanBest() {
		return isCurrentWorseThanBest;
	}

	/**
	 * Generates a NoGood that prevents the current weight to be derived again.
	 * @return
	 */
	public NoGood generateExcludingNoGood() {
		return null;
	}

	class WeakConstraintAtom implements AtomCallbackManager.AtomCallback {
		private final int atom;
		private final int weight;
		private final int level;

		WeakConstraintAtom(int atom, int weight, int level) {
			this.atom = atom;
			this.weight = weight;
			this.level = level;
		}

		@Override
		public void processCallback() {
			// TODO: check if atom became true or unassigned/false, then increase or decrease weights.
			// TODO: for increase apply weight to respective level of current weightatlevels.
			// TODO: for decrease, remove weight from respective level of current weightatlevels.
			// TODO: also adapt isCurrentWorseThanBest.
		}
	}
}
