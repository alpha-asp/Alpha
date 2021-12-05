package at.ac.tuwien.kr.alpha.core.solver.heuristics;

import at.ac.tuwien.kr.alpha.api.config.BinaryNoGoodPropagationEstimationStrategy;
import at.ac.tuwien.kr.alpha.core.common.Assignment;
import at.ac.tuwien.kr.alpha.core.grounder.structure.AtomChoiceRelation;
import at.ac.tuwien.kr.alpha.core.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.core.solver.ThriceTruth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static at.ac.tuwien.kr.alpha.commons.util.Util.oops;
import static at.ac.tuwien.kr.alpha.core.atoms.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.core.atoms.Literals.atomToLiteral;

/**
 * This implementation is similar to {@link VSIDS} but uses the saved phase for the truth of the chosen atom.
 *
 * Copyright (c) 2019-2021, the Alpha Team.
 */
public class VSIDSWithPhaseSaving extends AbstractVSIDS {
	protected static final Logger LOGGER = LoggerFactory.getLogger(VSIDSWithPhaseSaving.class);

	private final AtomChoiceRelation atomChoiceRelation;
	private double activityDecrease;
	private long numThrownAway;
	private long numNoChoicePoint;
	private long numNotActiveChoicePoint;

	private VSIDSWithPhaseSaving(Assignment assignment, ChoiceManager choiceManager, AtomChoiceRelation atomChoiceRelation, HeapOfActiveAtoms heapOfActiveAtoms, BinaryNoGoodPropagationEstimationStrategy momsStrategy) {
		super(assignment, choiceManager, heapOfActiveAtoms, momsStrategy);
		this.atomChoiceRelation = atomChoiceRelation;
	}

	private VSIDSWithPhaseSaving(Assignment assignment, ChoiceManager choiceManager, AtomChoiceRelation atomChoiceRelation, int decayPeriod, double decayFactor, BinaryNoGoodPropagationEstimationStrategy momsStrategy) {
		this(assignment, choiceManager, atomChoiceRelation, new HeapOfRelatedChoiceAtoms(decayPeriod, decayFactor, choiceManager, atomChoiceRelation),  momsStrategy);
	}

	VSIDSWithPhaseSaving(Assignment assignment, ChoiceManager choiceManager, AtomChoiceRelation atomChoiceRelation, BinaryNoGoodPropagationEstimationStrategy momsStrategy) {
		this(assignment, choiceManager, atomChoiceRelation, DEFAULT_DECAY_PERIOD, DEFAULT_DECAY_FACTOR,  momsStrategy);
	}

	@Override
	protected void incrementActivityResolutionAtom(int resolutionAtom) {
		incrementActivityOfRelatedChoiceAtoms(resolutionAtom);
	}

	@Override
	protected void incrementActivityLearnedNoGood(int literal) {
		incrementActivityOfRelatedChoiceAtoms(atomOf(literal));
	}

	private void incrementActivityOfRelatedChoiceAtoms(int toAtom) {
		if (atomChoiceRelation == null) {
			heapOfActiveAtoms.incrementActivity(toAtom);
			throw new RuntimeException("Condition met: atomChoiceRelation is null.");
		}
		for (Integer relatedChoiceAtom : atomChoiceRelation.getRelatedChoiceAtoms(toAtom)) {
			if (!choiceManager.isAtomChoice(relatedChoiceAtom)) {
				throw oops("Related atom is no choice.");
			}
			heapOfActiveAtoms.incrementActivity(relatedChoiceAtom);
		}
	}

	public double getActivityDecrease() {
		return activityDecrease;
	}

	public long getNumThrownAway() {
		return numThrownAway;
	}

	public long getNumNoChoicePoint() {
		return numNoChoicePoint;
	}

	public long getNumNotActiveChoicePoint() {
		return numNotActiveChoicePoint;
	}

	public long getNumAddedToHeapByActivity() {
		return heapOfActiveAtoms.getNumAddedToHeapByActivity();
	}

	@Override
	protected int chooseAtom() {
		ingestBufferedNoGoods();
		Integer mostActiveAtom;
		double maxActivity = 0.0f;
		while ((mostActiveAtom = heapOfActiveAtoms.getMostActiveAtom()) != null) {
			double activity = heapOfActiveAtoms.getActivity(atomToLiteral(mostActiveAtom));
			if (activity > maxActivity) {
				maxActivity = activity;
			}
			if (choiceManager.isActiveChoiceAtom(mostActiveAtom)) {
				if (maxActivity > activity) {
					double lostActivityNormalized = (maxActivity - activity) / heapOfActiveAtoms.getCurrentActivityIncrement();
					activityDecrease += lostActivityNormalized;
				}
				return mostActiveAtom;
			}
			if (choiceManager.isAtomChoice(mostActiveAtom)) {
				numNotActiveChoicePoint++;
			} else {
				numNoChoicePoint++;
			}
			numThrownAway++;
		}
		return DEFAULT_CHOICE_ATOM;
	}

	/**
	 * Chooses a sign (truth value) to assign to the given atom;
	 * uses the last value (saved phase) to determine its truth value.
	 * 
	 * @param atom
	 *          the chosen atom
	 * @return the truth value to assign to the given atom
	 */
	@Override
	protected boolean chooseSign(int atom) {
		if (assignment.getTruth(atom) == ThriceTruth.MBT) {
			return true;
		}
		return assignment.getLastValue(atom);
	}

	public void growForMaxAtomId(int maxAtomId) {
		super.growForMaxAtomId(maxAtomId);
		if (atomChoiceRelation != null) {
			atomChoiceRelation.growForMaxAtomId(maxAtomId);
		}
	}

	@Override
	public double getActivity(int literal) {
		return heapOfActiveAtoms.getActivity(literal);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
