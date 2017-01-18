package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.common.Literals;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.Assignment;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.GroundConflictNoGoodLearner;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

import java.util.*;
import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.common.Atoms.isAtom;
import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;

/**
 * A BerkMin-like heuristics that uses activity of literals and a fixed-size queue instead of a stack of NoGoods.
 * Copyright (c) 2017, the Alpha Team.
 */
public class BerkMinLiteral extends BerkMin {

	private Map<Integer, Double> activityCounters = new HashMap<>();
	private Map<Integer, Integer> signCounters = new HashMap<>();
	private Deque<Integer> activeLiterals = new LinkedList<>();
	private static final int DEFAULT_QUEUE_SIZE = 32;
	private final int queueSize;
	private int decayAge;
	private double decayFactor;
	private int stepsSinceLastDecay;

	BerkMinLiteral(Assignment<ThriceTruth> assignment, ChoiceManager choiceManager, Random random, int decayAge, double decayFactor, int queueSize) {
		super(assignment, choiceManager, random, decayAge, decayFactor);
		this.queueSize = queueSize;
	}

	BerkMinLiteral(Assignment<ThriceTruth> assignment, ChoiceManager choiceManager, Random random) {
		this(assignment, choiceManager, random, DEFAULT_DECAY_AGE, DEFAULT_DECAY_FACTOR, DEFAULT_QUEUE_SIZE);
	}

	/**
	 * Gets the number of steps after which all counters are decayed (i.e. multiplied by {@link #getDecayFactor()}.
	 */
	public int getDecayAge() {
		return decayAge;
	}

	/**
	 * Sets the number of steps after which all counters are decayed (i.e. multiplied by {@link #getDecayFactor()}.
	 */
	public void setDecayAge(int decayAge) {
		this.decayAge = decayAge;
	}

	/**
	 * Gets the factor by which all counters are multiplied to decay after {@link #getDecayAge()}.
	 */
	public double getDecayFactor() {
		return decayFactor;
	}

	/**
	 * Sets the factor by which all counters are multiplied to decay after {@link #getDecayAge()}.
	 */
	public void setDecayFactor(double decayFactor) {
		this.decayFactor = decayFactor;
	}

	@Override
	public void violatedNoGood(NoGood violatedNoGood) {
		pushToStack(violatedNoGood);
	}

	@Override
	public void analyzedConflict(GroundConflictNoGoodLearner.ConflictAnalysisResult analysisResult) {
		pushToStack(analysisResult.learnedNoGood);
		for (NoGood noGood : analysisResult.noGoodsResponsibleForConflict) {
			for (Integer literal : noGood) {
				incrementActivityCounter(literal);
				incrementSignCounter(literal);
			}
		}
		decayAllIfTimeHasCome();
	}

	@Override
	public void newNoGood(NoGood newNoGood) {
		pushToStack(newNoGood);
		for (Integer literal : newNoGood) {
			incrementSignCounter(literal);
		}
	}

	@Override
	public void newNoGoods(Collection<NoGood> newNoGoods) {
		newNoGoods.forEach(this::newNoGood);
	}

	@Override
	public double getActivity(int literal) {
		return activityCounters.getOrDefault(atomOf(literal), DEFAULT_ACTIVITY);
	}

	/**
	 * {@inheritDoc}
	 * In BerkMin, the atom to choose on is the most active atom in the current top clause.
	 * Here, we can only consider atoms which are currently active choice points. If we do
	 * not find such an atom in the current top clause, we consider the next undefined
	 * nogood in the stack, then the one after that and so on.
	 */
	@Override
	public int chooseAtom() {
		return  getMostActiveChoosableAtom(activeLiterals.stream());
	}

	@Override
	public boolean chooseSign(int atom) {
		if (!isAtom(atom)) {
			throw new IllegalArgumentException("Atom must be a positive integer.");
		}

		if (assignment.getTruth(atom) == ThriceTruth.MBT) {
			return true;
		}

		int positiveCounter = signCounters.getOrDefault(+atom, DEFAULT_SIGN_COUNTER);
		int negativeCounter = signCounters.getOrDefault(-atom, DEFAULT_SIGN_COUNTER);

		if (positiveCounter > negativeCounter) {
			return false;
		} else if (negativeCounter > positiveCounter) {
			return true;
		} else {
			return rand.nextBoolean();
		}
	}

	private void pushToStack(Integer literal) {
		if (choiceManager.isAtomChoice(atomOf(literal))) {
			activeLiterals.addFirst(literal);
			// Restrict the size of the queue.
			if (activeLiterals.size() > queueSize) {
				activeLiterals.removeLast();
			}
		}
	}

	private void pushToStack(NoGood noGood) {
		if (noGood != null) {
			for (Integer literal : noGood) {
				pushToStack(literal);
			}
		}
	}

	private void incrementActivityCounter(int literal) {
		int atom = atomOf(literal);
		if (choiceManager.isAtomChoice(atom)) {
			activityCounters.compute(atom, (k, v) -> (v == null ? DEFAULT_ACTIVITY : v) + 1);
		}
	}

	private void incrementSignCounter(Integer literal) {
		if (choiceManager.isAtomChoice(atomOf(literal))) {
			signCounters.compute(literal, (k, v) -> (v == null ? DEFAULT_SIGN_COUNTER : v) + 1);
		}
	}

	private void decayAllIfTimeHasCome() {
		stepsSinceLastDecay++;
		if (stepsSinceLastDecay >= decayAge) {
			// Decay all:
			activityCounters.replaceAll((k, v) -> v * decayFactor);
			stepsSinceLastDecay = 0;
		}
	}

	private int getMostActiveChoosableAtom(Stream<Integer> streamOfLiterals) {
		return streamOfLiterals
			.map(Literals::atomOf)
			.filter(choiceManager::isActiveChoiceAtom)
			.max(Comparator.comparingDouble(this::getActivity))
			.orElse(DEFAULT_CHOICE_ATOM);
	}
}
