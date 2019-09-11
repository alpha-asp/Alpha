package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Assignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;

/**
 * Realizes a learned NoGood deletion strategy based on LBD and activity of NoGoods.
 *
 * Copyright (c) 2019, the Alpha Team.
 */
class LearnedNoGoodDeletion {
	private static final Logger LOGGER = LoggerFactory.getLogger(LearnedNoGoodDeletion.class);
	private static final int RESET_SEQUENCE_AFTER = 20;
	private static final int RUN_AFTER_AT_LEAST = 2000;
	private static final int GROWTH_FACTOR = 100;
	private final ArrayList<WatchedNoGood> learnedNoGoods = new ArrayList<>();	// List of learned NoGoods that can be removed again. Note: should only contain NoGoods of size > 2.
	private final NoGoodStoreAlphaRoaming store;
	private final Assignment assignment;
	private int conflictCounter;
	private int cleanupCounter;

	LearnedNoGoodDeletion(NoGoodStoreAlphaRoaming store, Assignment assignment) {
		this.store = store;
		this.assignment = assignment;
	}

	void recordLearnedNoGood(WatchedNoGood learnedWatchedNoGood) {
		learnedNoGoods.add(learnedWatchedNoGood);
	}

	void increaseConflictCounter() {
		conflictCounter++;
	}

	boolean needToRunNoGoodDeletion() {
		return conflictCounter > RUN_AFTER_AT_LEAST + (GROWTH_FACTOR * cleanupCounter);
	}

	void runNoGoodDeletion() {
		conflictCounter = 0;
		cleanupCounter++;
		// Reset the sequence after enough growth cycles.
		if (cleanupCounter > RESET_SEQUENCE_AFTER) {
			cleanupCounter = 0;
		}
		int deletedNoGoods = 0;
		int originalSize = learnedNoGoods.size();
		if (originalSize == 0) {
			return;
		}
		int toDeleteMax = originalSize / 2;
		long activitySum = 0;
		for (WatchedNoGood learnedNoGood : learnedNoGoods) {
			activitySum += learnedNoGood.getActivity();
		}
		double avgActivity = activitySum / originalSize;
		double scoreThreshold = avgActivity * 1.5;
		for (Iterator<WatchedNoGood> iterator = learnedNoGoods.iterator(); iterator.hasNext();) {
			WatchedNoGood learnedNoGood = iterator.next();
			if (deletedNoGoods >= toDeleteMax) {
				break;
			}
			boolean keepNoGood = isLocked(learnedNoGood, assignment)
				|| learnedNoGood.getActivity() > scoreThreshold
				|| learnedNoGood.isLbdLessOrEqual2();
			if (!keepNoGood) {
				iterator.remove();
				store.removeFromWatches(learnedNoGood);
				learnedNoGood.decreaseActivity();
				deletedNoGoods++;
				LOGGER.trace("Removed from store the NoGood: {}", learnedNoGood);
			}
		}
		LOGGER.debug("Removed {} NoGoods from store.", deletedNoGoods);
	}

	private boolean isLocked(WatchedNoGood noGood, Assignment assignment) {
		int watchedAtom1 = atomOf(noGood.getLiteral(0));
		int watchedAtom2 = atomOf(noGood.getLiteral(1));
		if (!assignment.isAssigned(watchedAtom1) || !assignment.isAssigned(watchedAtom2)) {
			return false;
		}
		return noGood == assignment.getImpliedBy(watchedAtom1)
			|| noGood == assignment.getImpliedBy(watchedAtom2);
	}
}
