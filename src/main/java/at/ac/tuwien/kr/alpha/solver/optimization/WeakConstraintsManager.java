package at.ac.tuwien.kr.alpha.solver.optimization;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.Checkable;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.WritableAssignment;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;

/**
 * Manages weak constraints: stores the value of the current partial assignment, the best known value of any answer-set,
 * and handles callbacks/computations when certain atoms (that represent weak constraints) change their truth value.
 *
 * Copyright (c) 2020-2021, the Alpha Team.
 */
public class WeakConstraintsManager implements Checkable {
	private static final Logger LOGGER = LoggerFactory.getLogger(WeakConstraintsManager.class);

	final WritableAssignment assignment;
	final WeightAtLevelsManager weightAtLevelsManager;
	private final ArrayList<Integer> knownCallbackAtoms;
	private final ArrayList<WeakConstraintAtomCallback> knownAtomCallbacksForFirstAnswerSet = new ArrayList<>();
	private boolean foundFirstAnswerSet;
	private final HashSet<WeakConstraintAtomCallback> aboveMaxLevelTrueAtoms = new HashSet<>();

	public WeakConstraintsManager(WritableAssignment assignment) {
		this.assignment = assignment;
		this.weightAtLevelsManager = new WeightAtLevelsManager();
		this.knownCallbackAtoms = new ArrayList<>();
	}

	/**
	 * Registers newly obtained weak constraint atoms for callback at the assignment.
	 * @param weakConstraintAtomWeightLevels a set of triples (a,b,c) representing weak constraint atoms, where a is
	 *                                       the atom, b the weight and c the level of the respective weak constraint.
	 */
	public void addWeakConstraintsInformation(List<Triple<Integer, Integer, Integer>> weakConstraintAtomWeightLevels) {
		for (Triple<Integer, Integer, Integer> weakConstraintAtomWeightLevel : weakConstraintAtomWeightLevels) {
			if (weakConstraintAtomWeightLevel.getMiddle() == 0) {
				// Skip weak constraints with weight 0 entirely.
				continue;
			}
			WeakConstraintAtomCallback wcA = new WeakConstraintAtomCallback(this, weakConstraintAtomWeightLevel.getLeft(), weakConstraintAtomWeightLevel.getMiddle(), weakConstraintAtomWeightLevel.getRight());
			assignment.registerCallbackOnChange(wcA.atom);
			assignment.getAtomCallbackManager().recordCallback(wcA.atom, wcA);
			knownCallbackAtoms.add(wcA.atom);
			if (assignment.getTruth(wcA.atom) != null) {
				throw oops("Adding weak constraints information and atom callback already has a truth value assigned.");
			}
			if (!foundFirstAnswerSet) {
				knownAtomCallbacksForFirstAnswerSet.add(wcA);
			}
		}
	}

	/**
	 * Process the the changed truth value of an atom representing a weak constraint.
	 *
	 * @param atomCallback the {@link WeakConstraintAtomCallback} whose assigned truth value did change.
	 */
	public void processCallback(WeakConstraintAtomCallback atomCallback) {
		int atom = atomCallback.atom;
		int level = atomCallback.level;
		int weight = atomCallback.weight;
		ThriceTruth lastTruthValue = atomCallback.lastTruthValue;
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Processing callback for atom {} with weight at level {}@{}, last truth value was {}.", atom, weight, level, lastTruthValue);
			LOGGER.trace("Current atom truth value is: {}", assignment.getTruth(atom));
		}
		ThriceTruth currentAtomTruth = assignment.getTruth(atom);
		if (!foundFirstAnswerSet) {
			// Record old truth value and return if no answer set has been found yet.
			atomCallback.lastTruthValue = currentAtomTruth;
			LOGGER.trace("End processing callback as no first answer set has been found.");
			return;
		}
		LOGGER.trace("Current truth value is: {}", currentAtomTruth);
		atomCallback.lastTruthValue = currentAtomTruth;
		if (lastTruthValue == null) {
			// Change from unassigned to some truth value.
			if (currentAtomTruth == MBT) {
				increaseWeight(atomCallback);
			}
			// Note: for assignment to FALSE or MBT->TRUE, no change needed.
		} else {
			// Decrease if change from TRUE/MBT to unassigned.
			if (lastTruthValue.toBoolean() && currentAtomTruth == null) {
				decreaseWeight(atomCallback);
			}
			// Note: for backtracking from TRUE to MBT no change is needed.
			if (currentAtomTruth != null && lastTruthValue.toBoolean() && !currentAtomTruth.toBoolean()) {
				throw oops("Unexpected case of weak constraint atom directly changing truth value from MBT/TRUE to FALSE encountered. Atom/LastTruth/CurrentTruth: " + atom + "/" + lastTruthValue + "/" + currentAtomTruth);
			}
		}
		LOGGER.trace("End Processing callback for atom {} with weight at level {}@{}, last truth value was {}, current is {}.", atom, weight, level, lastTruthValue, currentAtomTruth);
	}

	private void increaseWeight(WeakConstraintAtomCallback atomCallback) {
		if (weightAtLevelsManager.getMaxLevel() < atomCallback.level) {
			LOGGER.info("Adding higher level than possible with weightAtLevelsManager: level={} and maxLevel={}, callback is: {}.", atomCallback.level, weightAtLevelsManager.getMaxLevel(), atomCallback);
			aboveMaxLevelTrueAtoms.add(atomCallback);
		} else {
			weightAtLevelsManager.increaseCurrentWeight(atomCallback.level, atomCallback.weight);
		}
	}

	private void decreaseWeight(WeakConstraintAtomCallback atomCallback) {
		if (weightAtLevelsManager.getMaxLevel() < atomCallback.level) {
			LOGGER.info("Removing higher level than possible with weightAtLevelsManager: level={} and maxLevel={}, callback is: {}.", atomCallback.level, weightAtLevelsManager.getMaxLevel(), atomCallback);
			aboveMaxLevelTrueAtoms.remove(atomCallback);
		} else {
			weightAtLevelsManager.decreaseCurrentWeight(atomCallback.level, atomCallback.weight);
		}
	}

	/**
	 * Returns whether the current partial interpretation is already worse (or equal) than the best-known answer-set.
	 * @return true if the current partial interpretation has worse (or equal) weight than the best-known answer-set.
	 */
	public boolean isCurrentBetterThanBest() {
		if (!foundFirstAnswerSet) {
			return true;
		}
		boolean isCurrentBetterThanBest = aboveMaxLevelTrueAtoms.isEmpty() && weightAtLevelsManager.isCurrentBetterThanBest();
		LOGGER.trace("Is current better than best? {}", isCurrentBetterThanBest);
		return isCurrentBetterThanBest;
	}

	/**
	 * Mark the current weight as being the best of all currently known answer-sets.
	 */
	public void markCurrentWeightAsBestKnown() {
		if (!foundFirstAnswerSet) {
			initializeFirstWeightsAtLevel();
			foundFirstAnswerSet = true;
		} else {
			if (!isCurrentBetterThanBest()) {
				throw oops("WeakConstraintsManager instructed to mark current valuation as best-known, but there is a better one already.");
			}
		}
		if (!aboveMaxLevelTrueAtoms.isEmpty()) {
			throw oops("WeakConstraintsManager has aboveMaxLevelTrueAtoms but is called to markCurrentWeightsAsBestKnown.");
		}
		weightAtLevelsManager.markCurrentWeightAsBestKnown();
	}

	private void initializeFirstWeightsAtLevel() {
		// First answer set has been found, find its maximum level and inform WeightAtLevelsManager.
		LOGGER.trace("Initializing for first answer-set.");
		int highestLevel = 0;
		List<WeakConstraintAtomCallback> trueWeakConstraintAtomCallbacks = knownAtomCallbacksForFirstAnswerSet
			.stream()
			.filter(wca -> assignment.getTruth(wca.atom).toBoolean())
			.collect(Collectors.toList());
		for (WeakConstraintAtomCallback weakConstraintAtomCallback : trueWeakConstraintAtomCallbacks) {
			int level = weakConstraintAtomCallback.level;
			if (highestLevel < level) {
				highestLevel = level;
			}
		}
		weightAtLevelsManager.setMaxLevel(highestLevel);
		// Now inform WeightAtLevelsManager about all true weak constraint atoms.
		for (WeakConstraintAtomCallback weakConstraintAtomCallback : trueWeakConstraintAtomCallbacks) {
			weightAtLevelsManager.increaseCurrentWeight(weakConstraintAtomCallback.level, weakConstraintAtomCallback.weight);
		}
	}

	/**
	 * Generates a NoGood that prevents the current weight to be derived again.
	 * @return a NoGood which excludes the exact weight of the current assignment.
	 */
	public NoGood generateExcludingNoGood() {
		LOGGER.trace("Generating excluding NoGood.");
		// Collect all currently true/mbt weights and put their respective literals in one nogood.
		// Note: alternative to searching all known callback atoms would be maintaining a list of true ones, but this is probably less efficient.
		ArrayList<Integer> trueCallbackAtoms = new ArrayList<>();
		for (Integer callbackAtom : knownCallbackAtoms) {
			ThriceTruth truth = assignment.getTruth(callbackAtom);
			if (truth != null && truth.toBoolean()) {
				trueCallbackAtoms.add(atomToLiteral(callbackAtom));
			}
		}
		LOGGER.trace("True weak constraint representing atoms are: {}", trueCallbackAtoms);
		return NoGood.fromConstraint(trueCallbackAtoms, Collections.emptyList());
	}

	@Override
	public void setChecksEnabled(boolean checksEnabled) {
		weightAtLevelsManager.setChecksEnabled(checksEnabled);
	}

	/**
	 * Returns the current weights and their levels.
	 * @return a TreeMap mapping levels to weights.
	 */
	public TreeMap<Integer, Integer> getCurrentWeightAtLevels() {
		LOGGER.trace("Reporting current weight at levels.");
		TreeMap<Integer, Integer> weightAtLevels = weightAtLevelsManager.getCurrentWeightAtLevels();
		if (aboveMaxLevelTrueAtoms.isEmpty()) {
			LOGGER.trace("Current weight at levels: {}", weightAtLevels);
			return weightAtLevels;
		}
		// Add weights above the maximum level stored in the WeightAtLevelsManager.
		for (WeakConstraintAtomCallback aboveMaxLevelTrueAtom : aboveMaxLevelTrueAtoms) {
			Integer weightAtLevel = weightAtLevels.get(aboveMaxLevelTrueAtom.level);
			weightAtLevel = weightAtLevel == null ? 0 : weightAtLevel;
			weightAtLevels.putIfAbsent(aboveMaxLevelTrueAtom.level, weightAtLevel + aboveMaxLevelTrueAtom.weight);
		}
		LOGGER.trace("Current weight at levels: {}", weightAtLevels);
		return weightAtLevels;
	}
}
