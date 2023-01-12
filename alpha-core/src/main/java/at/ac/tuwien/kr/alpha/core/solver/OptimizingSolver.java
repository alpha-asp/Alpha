package at.ac.tuwien.kr.alpha.core.solver;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.NoGood;
import at.ac.tuwien.kr.alpha.commons.WeightedAnswerSet;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.core.grounder.Grounder;
import at.ac.tuwien.kr.alpha.core.solver.heuristics.HeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.core.solver.optimization.WeakConstraintsManagerForBoundedOptimality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * A solver providing optimization via weak constraints and a simpla branch-and-bound algorithm.
 * The basis of this solver is the {@link DefaultSolver} and its algorithms.
 *
 * Copyright (c) 2021, the Alpha Team.
 */
public class OptimizingSolver extends DefaultSolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(OptimizingSolver.class);

	public OptimizingSolver(AtomStore atomStore, Grounder grounder, NoGoodStore store, WritableAssignment assignment, Random random, SystemConfig config, HeuristicsConfiguration heuristicsConfiguration) {
		super(atomStore, grounder, store, assignment, random, config, heuristicsConfiguration);
		this.weakConstraintsManager = new WeakConstraintsManagerForBoundedOptimality(assignment, config.getAnswerSetsMaxWeightAtLevels());
		this.weakConstraintsManager.setChecksEnabled(config.isDebugInternalChecks());
	}

	@Override
	protected boolean tryAdvance(Consumer<? super AnswerSet> action) {
		if (!searchState.hasBeenInitialized) {
			initializeSearch();
		} else {
			prepareForSubsequentAnswerSet();
		}
		// Try all assignments until grounder reports no more NoGoods and all of them are satisfied
		while (true) {
			performanceLog.writeIfTimeForLogging(LOGGER);
			if (searchState.isSearchSpaceCompletelyExplored) {
				LOGGER.debug("Search space has been fully explored, there are no more answer-sets.");
				logStats();
				return false;
			}
			ConflictCause conflictCause = propagate();
			if (conflictCause != null) {
				LOGGER.debug("Conflict encountered, analyzing conflict.");
				learnFromConflict(conflictCause);
			} else if (!((WeakConstraintsManagerForBoundedOptimality)weakConstraintsManager).isCurrentBetterThanBest()) {
				LOGGER.debug("Current assignment is worse than previously found answer set, backjumping now.");
				backtrackFromBound();
			} else if (assignment.didChange()) {
				LOGGER.debug("Updating grounder with new assignments and (potentially) obtaining new NoGoods.");
				grounder.updateAssignment(assignment.getNewPositiveAssignmentsIterator());
				getNoGoodsFromGrounderAndIngest();
			} else if (choose()) {
				LOGGER.debug("Did choice.");
			} else if (close()) {
				LOGGER.debug("Closed unassigned known atoms (assigning FALSE).");
			} else if (assignment.getMBTCount() == 0) {
				provideAnswerSet(action);
				return true;
			} else {
				backtrackFromMBTsRemaining();
			}
		}
	}

	private void backtrackFromBound() {
		NoGood excludingNoGood = ((WeakConstraintsManagerForBoundedOptimality)weakConstraintsManager).generateExcludingNoGood();
		Map<Integer, NoGood> obtained = new LinkedHashMap<>();
		obtained.put(grounder.register(excludingNoGood), excludingNoGood);
		if (!ingest(obtained)) {
			searchState.isSearchSpaceCompletelyExplored = true;
		}
	}

	private void provideAnswerSet(Consumer<? super WeightedAnswerSet> action) {
		// Enrich answer-set with weights information and record current-best upper bound.
		AnswerSet as = translate(assignment.getTrueAssignments());
		((WeakConstraintsManagerForBoundedOptimality)weakConstraintsManager).markCurrentWeightAsBestKnown();
		WeightedAnswerSet was = new WeightedAnswerSet(as, weakConstraintsManager.getCurrentWeightAtLevels());
		LOGGER.debug("Answer-Set found: {}", was);
		action.accept(was);
		logStats();
	}
}
