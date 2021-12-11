package at.ac.tuwien.kr.alpha.api;

import java.io.PrintStream;

/**
 * A {@link Solver} that collects statistics about the solving process while working.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface StatisticsReportingSolver extends Solver {


	/**
	 * Returns how often the solver made a choice (i.e., the number of times it was guessing a truth value for a
	 * grounded rule).
	 */
	int getNumberOfChoices();

	/**
	 * Returns the total number of backtracks (i.e., the number of decision levels that were rolled back).
	 * This includes all decision levels backtracked via backjumps.
	 *
	 * The number of backtracks excluding those within backjumps is
	 * {@link #getNumberOfBacktracks()} ()} minus {@link #getNumberOfBacktracksWithinBackjumps()} ()}.
	 */
	int getNumberOfBacktracks();

	/**
	 * Returns the total number of backtracks executed during backjumping.
	 * That is, the total number of decision levels that were rolled back due to backjumps.
	 */
	int getNumberOfBacktracksWithinBackjumps();

	/**
	 * Returns the number of times a backjump was executed, regardless of how many decision levels were rolled back
	 * by each backjump.
	 */
	int getNumberOfBackjumps();

	/**
	 * Returns the number of times a complete assignment was found which contained some atoms assigned to
	 * must-be-true. Those assignments are no answer-sets and subsequently backtracking (or justification analysis)
	 * is triggered.
	 */
	int getNumberOfBacktracksDueToRemnantMBTs();

	/**
	 * Returns the number of learned nogoods that were deleted again due to not being active enough.
	 * Learned-nogood deletion removes nogoods that were once learned but in the long run turned out to be no longer
	 * valuable for search, i.e., (large) nogoods that no longer appear in conflicts and do not propagate.
	 */
	int getNumberOfDeletedNoGoods();
	
	/**
	 * Returns the number of times that a conflict was reached when the solver closed all atoms.
	 * Closing is the last step to make an assignment complete after search reaches a fixpoint (i.e., the lazy
	 * grounder instantiates no more nogoods and there are no more ground instances of rules to guess on), hence all
	 * atoms not yet assigned a truth value are therefore false, but this may still violate some nogoods and lead to
	 * a conflict.
	 */
	int getNumberOfConflictsAfterClosing();

	default String getStatisticsString() {
		return "g=" + getNumberOfChoices() + ", bt=" + getNumberOfBacktracks() + ", bj=" + getNumberOfBackjumps() + ", bt_within_bj="
				+ getNumberOfBacktracksWithinBackjumps() + ", mbt=" + getNumberOfBacktracksDueToRemnantMBTs() + ", cac=" + getNumberOfConflictsAfterClosing()
				+ ", del_ng=" + getNumberOfDeletedNoGoods();
	}
	
	default String getStatisticsCSV() {
		return String.format("%d,%d,%d,%d,%d,%d,%d", getNumberOfChoices(), getNumberOfBacktracks(), getNumberOfBackjumps(), getNumberOfBacktracksWithinBackjumps(), getNumberOfBacktracksDueToRemnantMBTs(), getNumberOfConflictsAfterClosing(), getNumberOfDeletedNoGoods());
	}

	default void printStatistics(PrintStream out) {
		out.println(getStatisticsString());
	}

	default void printStatistics() {
		printStatistics(System.out);
	}

}
