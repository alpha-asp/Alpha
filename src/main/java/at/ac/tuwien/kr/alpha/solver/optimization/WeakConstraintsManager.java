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

	public WeakConstraintsManager(WritableAssignment assignment) {
		this.assignment = assignment;
		this.weightAtLevelsManager = new WeightAtLevelsManager(this);
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
			return;
		}
		LOGGER.trace("Current truth value is: {}", currentAtomTruth);
		if (lastTruthValue == null) {
			// Change from unassigned to some truth value.
			if (currentAtomTruth == MBT) {
				weightAtLevelsManager.increaseCurrentWeight(atomCallback);
			}
			// Note: for assignment to FALSE or MBT->TRUE, no change needed.
		} else {
			if (lastTruthValue.toBoolean() && currentAtomTruth == null) {
				// Change from TRUE/MBT to unassigned.
				weightAtLevelsManager.decreaseCurrentWeight(atomCallback);
			}
			// Note: for backtracking from TRUE to MBT no change is needed.
			if (currentAtomTruth != null && lastTruthValue.toBoolean() && !currentAtomTruth.toBoolean()) {
				throw oops("Unexpected case of weak constraint atom directly changing truth value from MBT/TRUE to FALSE encountered. Atom/LastTruth/CurrentTruth: " + atom + "/" + lastTruthValue + "/" + currentAtomTruth);
			}
		}
		atomCallback.lastTruthValue = currentAtomTruth;
	}

	/**
	 * For the first answer-set returns all those {@link WeakConstraintAtomCallback}s that are true.
	 * @return a list of all {@link WeakConstraintAtomCallback}s whose atom is MBT/TRUE.
	 * Throws an exception if called after the first answer-set has been found.
	 */
	public List<WeakConstraintAtomCallback> getTrueWeakConstraintAtomCallbacksOfFirstAnswerSet() {
		if (foundFirstAnswerSet) {
			throw oops("Requesting true WeakConstraintAtomCallbacks after first answer-set has been computed.");
		}
		return knownAtomCallbacksForFirstAnswerSet.stream().filter(n -> {
			ThriceTruth truth = assignment.getTruth(n.atom);
			return truth != null && truth.toBoolean();
		}).collect(Collectors.toList());
	}

	/**
	 * Returns whether the current partial interpretation is already worse (or equal) than the best-known answer-set.
	 * @return true if the current partial interpretation has worse (or equal) weight than the best-known answer-set.
	 */
	public boolean isCurrentBetterThanBest() {
		boolean isCurrentBetterThanBest = weightAtLevelsManager.isCurrentBetterThanBest();
		LOGGER.trace("Is current better than best? {}", isCurrentBetterThanBest);
		return isCurrentBetterThanBest;
	}

	/**
	 * Mark the current weight as being the best of all currently known answer-sets.
	 */
	public void markCurrentWeightAsBestKnown() {
		weightAtLevelsManager.markCurrentWeightAsBestKnown();
		foundFirstAnswerSet = true;
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

	@Override
	public void setChecksEnabled(boolean checksEnabled) {
		weightAtLevelsManager.setChecksEnabled(checksEnabled);
	}

	/**
	 * Returns the current weights and their levels.
	 * @return a TreeMap mapping levels to weights.
	 */
	public TreeMap<Integer, Integer> getCurrentWeightAtLevels() {
		return weightAtLevelsManager.getCurrentWeightAtLevels();
	}
}
