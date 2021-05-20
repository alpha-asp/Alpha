package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

/**
 * Manages weak constraints: stores the value of the current partial assignment, the best known value of any answer-set,
 * and handles callbacks/computations when certain atoms (that represent weak constraints) change their truth value.
 *
 * Copyright (c) 2020, the Alpha Team.
 */
public class WeakConstraintsManager implements Checkable {

	private ArrayList<Integer> currentWeightAtLevels;
	private ArrayList<Integer> bestKnownWeightAtLevels;
	private final WritableAssignment assignment;
	private boolean foundFirstAnswerSet;
	private int maxLevelCurrentIsAllEqual;	// The highest level for which holds: on all levels below (or equal), the current weights are the same as the weights for the best known.
	private boolean isCurrentBetterThanBest;
	private final ArrayList<Integer> knownCallbackAtoms;

	private boolean checksEnabled;

	public WeakConstraintsManager(WritableAssignment assignment) {
		this.assignment = assignment;
		this.currentWeightAtLevels = new ArrayList<>();
		this.bestKnownWeightAtLevels = new ArrayList<>();
		this.foundFirstAnswerSet = false;
		this.maxLevelCurrentIsAllEqual = -1;
		this.isCurrentBetterThanBest = true;
		this.knownCallbackAtoms = new ArrayList<>();
	}

	public void addWeakConstraintsInformation(List<Triple<Integer, Integer, Integer>> weakConstraintAtomWeightLevels) {
		// Register all newly obtained weak constraint atoms for callback at the assignment.
		for (Triple<Integer, Integer, Integer> weakConstraintAtomWeightLevel : weakConstraintAtomWeightLevels) {
			WeakConstraintAtomCallback wcA = new WeakConstraintAtomCallback(weakConstraintAtomWeightLevel.getLeft(), weakConstraintAtomWeightLevel.getMiddle(), weakConstraintAtomWeightLevel.getRight());
			assignment.registerCallbackOnChange(wcA.atom);
			assignment.getAtomCallbackManager().recordCallback(wcA.atom, wcA);
			knownCallbackAtoms.add(wcA.atom);
		}
	}

	/**
	 * Mark the current weight as being the best of all currently known answer-sets.
	 */
	public void markCurrentWeightAsBestKnown() {
		foundFirstAnswerSet = true;
		bestKnownWeightAtLevels = new ArrayList<>(currentWeightAtLevels);
		maxLevelCurrentIsAllEqual = bestKnownWeightAtLevels.size() - 1;
		isCurrentBetterThanBest = false;
	}

	/**
	 * Returns whether the current partial interpretation is already worse (or equal) than the best-known answer-set.
	 * @return true if the current partial interpretation has worse (or equal) weight than the best-known answer-set.
	 */
	public boolean isCurrentBetterThanBest() {
		return isCurrentBetterThanBest;
	}

	/**
	 * Generates a NoGood that prevents the current weight to be derived again.
	 * @return a NoGood which excludes the exact weight of the current assignment.
	 */
	public NoGood generateExcludingNoGood() {
		// Collect all currently true/mbt weights and put their respective literals in one nogood.
		// Note: alternative to searching all known callback atoms would be maintaining a list of true ones, but this is probably less efficient.
		ArrayList<Integer> trueCallbackAtoms = new ArrayList<>();
		for (Integer callbackAtom : knownCallbackAtoms) {
			ThriceTruth truth = assignment.getTruth(callbackAtom);
			if (truth != null && truth.toBoolean()) {
				trueCallbackAtoms.add(atomToLiteral(callbackAtom));
			}
		}
		return NoGood.fromConstraint(trueCallbackAtoms, Collections.emptyList());
	}

	public ArrayList<Integer> getCurrentWeightAtLevels() {
		return currentWeightAtLevels;
	}

	private void increaseCurrentWeight(WeakConstraintAtomCallback atom) {
		// Ignore increase by 0 weight.
		if (atom.weight == 0) {
			return;
		}
		// Record the new weight.
		expandLevels(atom.level);
		int newWeight = currentWeightAtLevels.get(atom.level) + atom.weight;
		currentWeightAtLevels.set(atom.level, newWeight);

		// Now check whether current is worse than best known.
		if (atom.level > maxLevelCurrentIsAllEqual + 1) {
			// Weight in some level where differences do not (yet) matter increased, but on lower level current partial assignment is still better than best found answer-set.
			// Nothing to do here.
			return;
		} else if (atom.level <= maxLevelCurrentIsAllEqual) {
			// Weight in level with previously equal weights increased, current is now worse than best.
			isCurrentBetterThanBest = false;
			maxLevelCurrentIsAllEqual = atom.level - 1;
		} else if (atom.level == maxLevelCurrentIsAllEqual + 1) {
			// Weight increased for the first level where current and best-known are not equal.
			moveUpwardsMaxLevelCurrentIsAllEqual();
			recomputeIsCurrentBetterThanBest();
		} else {
			throw oops("Increasing weight of current answer reached unforeseen state.");
		}
	}

	private void decreaseCurrentWeight(WeakConstraintAtomCallback atom) {
		// Ignore decrease by 0 weight.
		if (atom.weight == 0) {
			return;
		}
		// Record the new weight.
		int newWeight = currentWeightAtLevels.get(atom.level) - atom.weight;
		currentWeightAtLevels.set(atom.level, newWeight);

		// Now check whether current is better than best known and adapt maxLevelCurrentIsAllEqual.
		if (atom.level <= maxLevelCurrentIsAllEqual) {
			// Improved below the point where current is equal to best, so current is better than best now.
			isCurrentBetterThanBest = true;
			maxLevelCurrentIsAllEqual = atom.level - 1;        // All weights below the change are equal.
		} else if (atom.level > maxLevelCurrentIsAllEqual + 1) {
			// Decrease in such a high level, that there is at least one level (maxLevel+1) where weights are
			// different and whose difference still dominates whether current is better than best known.
			return;
		} else if (atom.level == maxLevelCurrentIsAllEqual + 1) {
			// Decrease might make current better than best known and change level of equals.
			moveUpwardsMaxLevelCurrentIsAllEqual();
			recomputeIsCurrentBetterThanBest();
		} else {
			throw oops("Decreasing weight of current answer reached unforeseen state.");
		}
	}

	private void moveUpwardsMaxLevelCurrentIsAllEqual() {
		for (int i = maxLevelCurrentIsAllEqual; i < currentWeightAtLevels.size(); i++) {
			int currentLevelWeight = currentWeightAtLevels.get(i);
			int bestLevelWeight = bestKnownWeightAtLevels.get(i);
			if (currentLevelWeight != bestLevelWeight) {
				// Level is the first where values differ, previous level was maximum one where values are equal.
				maxLevelCurrentIsAllEqual = i - 1;
				return;
			}
		}
		// Iterated all levels, so weights are the same on all levels.
		maxLevelCurrentIsAllEqual = currentWeightAtLevels.size() - 1;
	}

	private void recomputeIsCurrentBetterThanBest() {
		// The weights at the first level above maxLevelCurrentIsAllEqual dominate whether current is worse than best known.
		if (maxLevelCurrentIsAllEqual == currentWeightAtLevels.size() - 1) {
			// Current and best known are equal even up to the last level.
			isCurrentBetterThanBest = false;
			return;
		}
		int currentLevelWeight = currentWeightAtLevels.get(maxLevelCurrentIsAllEqual + 1);
		int bestLevelWeight = bestKnownWeightAtLevels.get(maxLevelCurrentIsAllEqual + 1);
		isCurrentBetterThanBest = currentLevelWeight < bestLevelWeight;
	}

	private void expandLevels(int newHighestLevel) {
		while (currentWeightAtLevels.size() <= newHighestLevel) {
			currentWeightAtLevels.add(0);
		}
		while (bestKnownWeightAtLevels.size() <= newHighestLevel) {
			bestKnownWeightAtLevels.add(foundFirstAnswerSet ? 0 : Integer.MAX_VALUE);	// Before first answer set is found, best known is maximally bad.
		}
	}

	@Override
	public void setChecksEnabled(boolean checksEnabled) {
		this.checksEnabled = checksEnabled;
	}

	private void runChecks() {
		if (currentWeightAtLevels.size() > bestKnownWeightAtLevels.size()) {
			throw oops("WeakConstraintManager has more levels for current valuation than for best known.");
		}
		// Check whether isCurrentBetterThanBest flag is consistent.
		if (isCurrentBetterThanBest != checkIsCurrentBetterThanBest()) {
			throw oops("WeakConstraintManager detected valuation of current assignment is inconsistent with state flag.");
		}
		checkLevelBar();
	}

	private void checkLevelBar() {
		int highestLevelWhereLowerLevelsAreEqual = -1;
		for (int i = 0; i < currentWeightAtLevels.size(); i++) {
			if (currentWeightAtLevels.get(i).intValue() != bestKnownWeightAtLevels.get(i).intValue()) {
				break;
			}
			highestLevelWhereLowerLevelsAreEqual = i;
		}
		// The level bar should be at highestLevelWhereLowerLevelsAreEqual+1.
		if (maxLevelCurrentIsAllEqual != highestLevelWhereLowerLevelsAreEqual) {
			throw oops("WeakConstraintManager detected level bar at wrong level, it is " + maxLevelCurrentIsAllEqual
				+ "and should be " + highestLevelWhereLowerLevelsAreEqual);
		}
	}

	private boolean checkIsCurrentBetterThanBest() {
		for (int i = 0; i < currentWeightAtLevels.size(); i++) {
			if (currentWeightAtLevels.get(i) < bestKnownWeightAtLevels.get(i)) {
				return true;
			}
			if (currentWeightAtLevels.get(i) > bestKnownWeightAtLevels.get(i)) {
				return false;
			}
		}
		// All levels have same weight for current as for best known, so at this point usually current is not better than best.
		return !foundFirstAnswerSet;	// If no answer set has been found, however, the current is always better than best known.
	}

	class WeakConstraintAtomCallback implements AtomCallbackManager.AtomCallback {
		private final int atom;
		private final int weight;
		private final int level;
		private ThriceTruth lastTruthValue;

		WeakConstraintAtomCallback(int atom, int weight, int level) {
			this.atom = atom;
			this.weight = weight;
			this.level = level;
		}

		@Override
		public void processCallback() {
			if (checksEnabled) {
				runChecks();
			}
			ThriceTruth currentAtomTruth = assignment.getTruth(atom);
			if (lastTruthValue == null) {
				// Change from unassigned to some truth value.
				if (currentAtomTruth == MBT) {
					increaseCurrentWeight(this);
				}
				// Note: for assignment to FALSE or MBT->TRUE, no change needed.
				lastTruthValue = currentAtomTruth;
			} else {
				if ((lastTruthValue == MBT || lastTruthValue == TRUE) && currentAtomTruth == null) {
					// Change from TRUE/MBT to unassigned.
					decreaseCurrentWeight(this);
				}
				// Note: for backtracking from TRUE to MBT no change is needed.
				lastTruthValue = currentAtomTruth;
				if (currentAtomTruth != null && lastTruthValue != TRUE && currentAtomTruth != MBT) {
					throw oops("Unexpected case of weak constraint atom changing truth value encountered. Atom/LastTruth/CurrentTruth: " + atom + "/" + lastTruthValue + "/" + currentAtomTruth);
				}
			}
			if (checksEnabled) {
				runChecks();
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			WeakConstraintAtomCallback that = (WeakConstraintAtomCallback) o;
			return atom == that.atom &&
				weight == that.weight &&
				level == that.level;
		}

		@Override
		public int hashCode() {
			return Objects.hash(atom, weight, level);
		}
	}
}
