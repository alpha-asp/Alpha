package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.ResolutionSequence;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class GroundConflictNoGoodLearner {

	private final Assignment assignment;
	private final NoGoodStore<ThriceTruth> noGoodStore;
	private NoGood learnedNoGood;

	public GroundConflictNoGoodLearner(Assignment assignment, NoGoodStore<ThriceTruth> noGoodStore) {
		this.assignment = assignment;
		this.noGoodStore = noGoodStore;
	}

	public void analyzeConflict(NoGood violatedNoGood) {
		learnedNoGood = null;
		NoGood currentResolutionNoGood = new NoGood(violatedNoGood.getLiteralsClone(), -1);	// Clone violated NoGood and remove potential head.
		throw new NotImplementedException("Method not yet implemented.");
	}

	public NoGood obtainLearnedNoGood() {
		return learnedNoGood;
	}

	public ResolutionSequence obtainResolutionSequence() {
		throw new NotImplementedException("Method not yet implemented.");
	}
}
