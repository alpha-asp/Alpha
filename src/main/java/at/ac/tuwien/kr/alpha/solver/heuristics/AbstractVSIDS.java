package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.BinaryNoGoodPropagationEstimation;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.heuristics.activity.BodyActivityProvider;

import java.util.ArrayList;
import java.util.Collection;

import static at.ac.tuwien.kr.alpha.common.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.solver.learning.GroundConflictNoGoodLearner.ConflictAnalysisResult;

/**
 * Combines fields and methods common to several VSIDS variants.
 *
 * Copyright (c) 2021, the Alpha Team.
 */
public abstract class AbstractVSIDS implements ActivityBasedBranchingHeuristic {
	protected static final int DEFAULT_DECAY_PERIOD = 1;
	/**
	 * The default factor by which VSID's activity increment will be multiplied when the decay period has expired.
	 * The value is taken from clasp's tweety configuration which clasp uses by default.
	 */
	protected static final double DEFAULT_DECAY_FACTOR = 1 / 0.92;
	protected final Assignment assignment;
	protected final ChoiceManager choiceManager;
	protected final HeapOfActiveAtoms heapOfActiveAtoms;
	protected final Collection<NoGood> bufferedNoGoods = new ArrayList<>();

	public AbstractVSIDS(Assignment assignment, ChoiceManager choiceManager, HeapOfActiveAtoms heapOfActiveAtoms, BinaryNoGoodPropagationEstimation.Strategy momsStrategy) {
		this.assignment = assignment;
		this.choiceManager = choiceManager;
		this.heapOfActiveAtoms = heapOfActiveAtoms;
		this.heapOfActiveAtoms.setMOMsStrategy(momsStrategy);
	}

	public void growForMaxAtomId(int maxAtomId) {
		heapOfActiveAtoms.growForMaxAtomId(maxAtomId);
	}

	@Override
	public void violatedNoGood(NoGood violatedNoGood) {
	}

	@Override
	public void newNoGood(NoGood newNoGood) {
		bufferedNoGoods.add(newNoGood);
	}

	@Override
	public void newNoGoods(Collection<NoGood> newNoGoods) {
		bufferedNoGoods.addAll(newNoGoods);
	}

	protected void ingestBufferedNoGoods() {
		heapOfActiveAtoms.newNoGoods(bufferedNoGoods);
		bufferedNoGoods.clear();
	}

	/**
	 * {@link VSIDS} manages a stack of nogoods in the fashion of {@link BerkMin}
	 * and starts by looking at the most active atom <code>a</code> in the nogood currently at the top of the stack.
	 * If <code>a</code> is an active choice point (i.e. representing the body of an applicable rule), it is immediately chosen;
	 * else the most active choice point dependent on <code>a</code> is.
	 * If there is no such atom, we continue further down the stack.
	 * When choosing between dependent atoms, a {@link BodyActivityProvider} is employed to define the activity of a choice point.
	 */
	@Override
	public int chooseLiteral() {
		int atom = chooseAtom();
		if (atom == DEFAULT_CHOICE_ATOM) {
			return DEFAULT_CHOICE_LITERAL;
		}
		boolean sign = chooseSign(atom);
		return atomToLiteral(atom, sign);
	}

	@Override
	public void analyzedConflict(ConflictAnalysisResult analysisResult) {
		ingestBufferedNoGoods();	// The analysisResult may contain new atoms whose activity must be initialized.
		for (int resolutionAtom : analysisResult.resolutionAtoms) {
			incrementActivityResolutionAtom(resolutionAtom);
		}
		if (analysisResult.learnedNoGood != null) {
			for (int literal : analysisResult.learnedNoGood) {
				incrementActivityLearnedNoGood(literal);
			}
		}
		heapOfActiveAtoms.decayIfTimeHasCome();
	}

	protected abstract void incrementActivityLearnedNoGood(int literal);

	protected abstract void incrementActivityResolutionAtom(int resolutionAtom);

	protected abstract int chooseAtom();

	protected abstract boolean chooseSign(int atom);
}
