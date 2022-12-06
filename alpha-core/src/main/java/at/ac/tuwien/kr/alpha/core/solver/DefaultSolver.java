/**
 * Copyright (c) 2016-2019, the Alpha Team.
 * All rights reserved.
 *
 * Additional changes made by Siemens.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.core.solver;

import static at.ac.tuwien.kr.alpha.commons.util.Util.oops;
import static at.ac.tuwien.kr.alpha.core.atoms.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.core.atoms.Literals.atomToLiteral;
import static at.ac.tuwien.kr.alpha.core.atoms.Literals.atomToNegatedLiteral;
import static at.ac.tuwien.kr.alpha.core.solver.NoGoodStore.LBD_NO_VALUE;
import static at.ac.tuwien.kr.alpha.core.solver.heuristics.BranchingHeuristic.DEFAULT_CHOICE_LITERAL;
import static at.ac.tuwien.kr.alpha.core.solver.learning.GroundConflictNoGoodLearner.ConflictAnalysisResult.UNSAT;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

import at.ac.tuwien.kr.alpha.core.common.Assignment;
import at.ac.tuwien.kr.alpha.core.grounder.RebootableGrounder;
import at.ac.tuwien.kr.alpha.core.solver.reboot.AtomizedChoice;
import at.ac.tuwien.kr.alpha.core.solver.reboot.RebootManager;
import at.ac.tuwien.kr.alpha.core.solver.reboot.stats.*;
import at.ac.tuwien.kr.alpha.core.solver.reboot.strategies.*;
import at.ac.tuwien.kr.alpha.core.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.StatisticsReportingSolver;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.core.atoms.RuleAtom;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.NoGood;
import at.ac.tuwien.kr.alpha.core.grounder.Grounder;
import at.ac.tuwien.kr.alpha.core.grounder.ProgramAnalyzingGrounder;
import at.ac.tuwien.kr.alpha.core.rules.CompiledRule;
import at.ac.tuwien.kr.alpha.core.solver.heuristics.BranchingHeuristic;
import at.ac.tuwien.kr.alpha.core.solver.heuristics.BranchingHeuristicFactory;
import at.ac.tuwien.kr.alpha.core.solver.heuristics.ChainedBranchingHeuristics;
import at.ac.tuwien.kr.alpha.core.solver.heuristics.HeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.core.solver.heuristics.NaiveHeuristic;
import at.ac.tuwien.kr.alpha.core.solver.learning.GroundConflictNoGoodLearner;
import at.ac.tuwien.kr.alpha.core.util.Substitutions;

/**
 * The new default solver employed in Alpha.
 *
 * Copyright (c) 2016-2021, the Alpha Team.
 */
public class DefaultSolver extends AbstractSolver implements StatisticsReportingSolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSolver.class);

	private final NoGoodStore store;
	private final ChoiceManager choiceManager;
	private final RebootManager rebootManager;
	private final WritableAssignment assignment;
	private final GroundConflictNoGoodLearner learner;
	private final BranchingHeuristic branchingHeuristic;
	private final RebootStrategy rebootStrategy;
	private boolean rebootEnabled;
	private final boolean disableRebootRepeat;

	private int mbtAtFixpoint;
	private int conflictsAfterClosing;
	private final boolean disableJustifications;
	private boolean disableJustificationAfterClosing = true;	// Keep disabled for now, case not fully worked out yet.
	private final boolean disableNoGoodDeletion;
	private static class SearchState {
		boolean hasBeenInitialized;
		boolean isSearchSpaceCompletelyExplored;
		/**
		 * True if search reached fixpoint and all remaining unassigned atoms have been set to false.
		 */
		boolean afterAllAtomsAssigned;
	}
	private final SearchState searchState = new SearchState();

	private final PerformanceLog performanceLog;

	private final StopWatch solverStopWatch;
	private final StopWatch grounderStopWatch;
	private final StopWatch propagationStopWatch;
	private final StopWatch mbtBacktrackingStopWatch;
	private final StopWatch epochStopWatch;
	private final SimpleCountingTracker iterationTracker;
	private final SimpleCountingTracker conflictTracker;
	private final SimpleCountingTracker decisionTracker;
	private final SimpleCountingTracker learnedNoGoodTracker;
	private final ResettableStatTracker learnEfficiencyTracker;

	public DefaultSolver(AtomStore atomStore, Grounder grounder, NoGoodStore store, WritableAssignment assignment, Random random, SystemConfig config, HeuristicsConfiguration heuristicsConfiguration) {
		super(atomStore, grounder);

		this.assignment = assignment;
		this.store = store;
		this.choiceManager = new ChoiceManager(assignment, store);
		this.choiceManager.setChecksEnabled(config.isDebugInternalChecks());
		this.rebootManager = new RebootManager(atomStore);
		this.learner = new GroundConflictNoGoodLearner(assignment, atomStore);
		this.branchingHeuristic = chainFallbackHeuristic(grounder, assignment, random, heuristicsConfiguration);
		this.disableJustifications = config.isDisableJustificationSearch();
		this.disableNoGoodDeletion = config.isDisableNoGoodDeletion();

		this.solverStopWatch = new StopWatch();
		this.grounderStopWatch = new StopWatch();
		this.propagationStopWatch = new StopWatch();
		this.mbtBacktrackingStopWatch = new StopWatch();
		this.epochStopWatch = new StopWatch();

		this.iterationTracker = new SimpleCountingTracker("iterations");
		this.conflictTracker = new SimpleCountingTracker("conflicts");
		this.decisionTracker = new SimpleCountingTracker("decisions");
		this.learnedNoGoodTracker = new SimpleCountingTracker("learned_nogoods");
		this.learnEfficiencyTracker = new ResettableQuotientTracker("learn_effic",
				learnedNoGoodTracker, new StopWatchTracker("solver_time", epochStopWatch));
		List<StatTracker> statTrackers = initStatTrackers();

		this.rebootEnabled = config.isRebootEnabled();
		this.disableRebootRepeat = config.isDisableRebootRepeat();
		DynamicLearnedIntervalRebootStrategy dynamicRebootStrategy = new DynamicLearnedIntervalRebootStrategy(
				learnEfficiencyTracker, 0.7, 5, config.getRebootIterations());
//		this.rebootStrategy = new FixedLearnedRebootStrategy(config.getRebootIterations());
//		this.rebootStrategy = new LubyLearnedRebootStrategy();
//		this.rebootStrategy = dynamicRebootStrategy;
		this.rebootStrategy = new GeometricLearnedRebootStrategy();
		statTrackers.add(dynamicRebootStrategy.getIntervalSizeTracker());

		this.performanceLog = new PerformanceLog(choiceManager, (TrailAssignment) assignment,
				store.getNoGoodCounter(), statTrackers, 1000);

		if (this.rebootEnabled && !(grounder instanceof RebootableGrounder)) {
			this.rebootEnabled = false;
			LOGGER.warn("Reboot was disabled since grounder does not support it.");
		}
	}

	private BranchingHeuristic chainFallbackHeuristic(Grounder grounder, WritableAssignment assignment, Random random, HeuristicsConfiguration heuristicsConfiguration) {
		BranchingHeuristic branchingHeuristic = BranchingHeuristicFactory.getInstance(heuristicsConfiguration, grounder, assignment, choiceManager, random);
		if (branchingHeuristic instanceof NaiveHeuristic) {
			return branchingHeuristic;
		}
		if (branchingHeuristic instanceof ChainedBranchingHeuristics && ((ChainedBranchingHeuristics)branchingHeuristic).getLastElement() instanceof NaiveHeuristic) {
			return branchingHeuristic;
		}
		return ChainedBranchingHeuristics.chainOf(branchingHeuristic, new NaiveHeuristic(choiceManager));
	}

	@Override
	protected boolean tryAdvance(Consumer<? super AnswerSet> action) {
		solverStopWatch.start();
		epochStopWatch.start();
		if (!searchState.hasBeenInitialized) {
			initializeSearch();
		} else {
			prepareForSubsequentAnswerSet();
		}
		// Try all assignments until grounder reports no more NoGoods and all of them are satisfied
		while (true) {
			rebootStrategy.nextIteration();
			iterationTracker.increment();
			performanceLog.writeIfTimeForLogging(LOGGER);
			if (searchState.isSearchSpaceCompletelyExplored) {
				LOGGER.debug("Search space has been fully explored, there are no more answer-sets.");
				logStats();
				stopStopWatchesAndLogRuntimes();
				return false;
			}
			ConflictCause conflictCause = propagate();
			if (conflictCause != null) {
				LOGGER.debug("Conflict encountered, analyzing conflict.");
				learnFromConflict(conflictCause);
				conflictTracker.increment();
				rebootStrategy.conflictEncountered();
			} else if (assignment.didChange()) {
				LOGGER.debug("Updating grounder with new assignments and (potentially) obtaining new NoGoods.");
				syncWithGrounder();
			} else if (rebootEnabled && rebootStrategy.isRebootScheduled()) {
				reboot();
				rebootStrategy.rebootPerformed();
				if (disableRebootRepeat) {
					rebootEnabled = false;
				}
			} else if (choose()) {
				LOGGER.debug("Did choice.");
				rebootStrategy.decisionMade();
			} else if (close()) {
				LOGGER.debug("Closed unassigned known atoms (assigning FALSE).");
			} else if (assignment.getMBTCount() == 0) {
				provideAnswerSet(action);
				rebootStrategy.answerSetFound();
				stopStopWatchesAndLogRuntimes();
				return true;
			} else {
				mbtBacktrackingStopWatch.start();
				backtrackFromMBTsRemaining();
				mbtBacktrackingStopWatch.stop();
				rebootStrategy.backtrackJustified();
			}
		}
	}

	private void initializeSearch() {
		// Initially, get NoGoods from grounder.
		performanceLog.initialize();
		getNoGoodsFromGrounderAndIngest();
		searchState.hasBeenInitialized = true;
	}

	/**
	 * Updates the assignment for the grounder and then gets new {@link NoGood}s from the grounder
	 * by calling {@link DefaultSolver#getNoGoodsFromGrounderAndIngest()}.
	 */
	private void syncWithGrounder() {
		grounder.updateAssignment(assignment.getNewPositiveAssignmentsIterator());
		getNoGoodsFromGrounderAndIngest();
	}

	private void prepareForSubsequentAnswerSet() {
		// We already found one Answer-Set and are requested to find another one.
		searchState.afterAllAtomsAssigned = false;
		if (assignment.getDecisionLevel() == 0) {
			// Solver is at decision level 0 again after finding some answer-set
			searchState.isSearchSpaceCompletelyExplored = true;
			return;
		}
		// Create enumeration NoGood to avoid finding the same Answer-Set twice.
		final NoGood enumerationNoGood = choiceManager.computeEnumeration();
		final int backjumpLevel = assignment.minimumConflictLevel(enumerationNoGood);
		if (backjumpLevel == -1) {
			throw oops("Enumeration nogood is not violated");
		}
		if (backjumpLevel == 0) {
			// Search space exhausted (only happens if first choice is for TRUE at decision level 1 for an atom that was MBT at decision level 0 already).
			searchState.isSearchSpaceCompletelyExplored = true;
			return;
		}
		// Backjump instead of backtrackSlow, enumerationNoGood will invert last choice.
		choiceManager.backjump(backjumpLevel - 1);
		LOGGER.debug("Adding enumeration nogood: {}", enumerationNoGood);
		rebootManager.newEnumerationNoGood(enumerationNoGood);
		rebootStrategy.newEnumerationNoGood(enumerationNoGood);
		if (!addAndBackjumpIfNecessary(grounder.register(enumerationNoGood), enumerationNoGood, Integer.MAX_VALUE)) {
			searchState.isSearchSpaceCompletelyExplored = true;
		}
	}

	private void getNoGoodsFromGrounderAndIngest() {
		grounderStopWatch.start();
		Map<Integer, NoGood> obtained = grounder.getNoGoods(assignment);
		grounderStopWatch.stop();
		rebootStrategy.newNoGoods(obtained.values());
		if (!ingest(obtained)) {
			searchState.isSearchSpaceCompletelyExplored = true;
		}
	}

	private void learnFromConflict(ConflictCause conflictCause) {
		LOGGER.debug("Violating assignment is: {}", assignment);
		Antecedent conflictAntecedent = conflictCause.getAntecedent();
		NoGood violatedNoGood = new NoGood(conflictAntecedent.getReasonLiterals().clone());
		// TODO: The violatedNoGood should not be necessary here, but this requires major type changes in heuristics.
		branchingHeuristic.violatedNoGood(violatedNoGood);
		if (searchState.afterAllAtomsAssigned) {
			LOGGER.debug("Assignment is violated after all unassigned atoms have been assigned false.");
			conflictsAfterClosing++;
			if (!treatConflictAfterClosing(conflictAntecedent)) {
				searchState.isSearchSpaceCompletelyExplored = true;
			}
			searchState.afterAllAtomsAssigned = false;
		} else {
			if (!learnBackjumpAddFromConflict(conflictCause)) {
				searchState.isSearchSpaceCompletelyExplored = true;
			}
		}
	}

	private ConflictCause propagate() {
		LOGGER.trace("Doing propagation step.");
		propagationStopWatch.start();
		ConflictCause conflictCause = store.propagate();
		propagationStopWatch.stop();
		LOGGER.trace("Assignment after propagation is: {}", assignment);
		if (!disableNoGoodDeletion && conflictCause == null) {
			// Run learned-NoGood deletion-strategy.
			store.cleanupLearnedNoGoods();
		}
		return conflictCause;
	}

	private void provideAnswerSet(Consumer<? super AnswerSet> action) {
		// NOTE: If we would do optimization, we would now have a guaranteed upper bound.
		AnswerSet as = translate(assignment.getTrueAssignments());
		LOGGER.debug("Answer-Set found: {}", as);
		action.accept(as);
		logStats();
	}

	private void backtrackFromMBTsRemaining() {
		LOGGER.debug("Backtracking from wrong choices ({} MBTs).", assignment.getMBTCount());
		searchState.afterAllAtomsAssigned = false;
		if (!justifyMbtAndBacktrack()) {
			searchState.isSearchSpaceCompletelyExplored = true;
		}
	}

	/**
	 * Adds a noGood to the store and in case of out-of-order literals causing another conflict, triggers further backjumping.
	 * @param noGoodId the unique identifier of the NoGood to add.
	 * @param noGood the NoGood to add.
	 * @param lbd the LBD (literal blocks distance) value of the NoGood.
	 */
	private boolean addAndBackjumpIfNecessary(int noGoodId, NoGood noGood, int lbd) {
		while (store.add(noGoodId, noGood, lbd) != null) {
			LOGGER.debug("Adding noGood (again) caused conflict, computing real backjumping level now.");
			int backjumpLevel = learner.computeConflictFreeBackjumpingLevel(noGood);
			if (backjumpLevel < 0) {
				return false;
			}
			choiceManager.backjump(backjumpLevel);
			propagationStopWatch.start();
			if (store.propagate() != null) {
				throw oops("Violated NoGood after backtracking.");
			}
			propagationStopWatch.stop();
		}
		return true;
	}

	/**
	 * Analyzes the conflict and learns a new NoGood (causing backjumping and addition to the NoGood store).
	 *
	 * @return false iff the analysis result shows that the set of NoGoods is unsatisfiable.
	 */
	private boolean learnBackjumpAddFromConflict(ConflictCause conflictCause) {
		GroundConflictNoGoodLearner.ConflictAnalysisResult analysisResult = learner.analyzeConflictingNoGood(conflictCause.getAntecedent());

		LOGGER.debug("Analysis result: {}", analysisResult);
		if (analysisResult == UNSAT) {
			// Halt if unsatisfiable.
			return false;
		}

		branchingHeuristic.analyzedConflict(analysisResult);

		if (analysisResult.learnedNoGood == null) {
			throw oops("Did not learn new NoGood from conflict.");
		}

		choiceManager.backjump(analysisResult.backjumpLevel);
		final NoGood learnedNoGood = analysisResult.learnedNoGood;
		rebootManager.newLearnedNoGood(learnedNoGood);
		rebootStrategy.newLearnedNoGood(learnedNoGood);
		learnedNoGoodTracker.increment();
		int noGoodId = grounder.register(learnedNoGood);
		return addAndBackjumpIfNecessary(noGoodId, learnedNoGood, analysisResult.lbd);
	}

	private boolean justifyMbtAndBacktrack() {
		mbtAtFixpoint++;
		// Run justification only if enabled and possible.
		if (disableJustifications || !(grounder instanceof ProgramAnalyzingGrounder)) {
			if (!backtrack()) {
				logStats();
				return false;
			}
			return true;
		}
		ProgramAnalyzingGrounder analyzingGrounder = (ProgramAnalyzingGrounder) grounder;
		// Justify one MBT assigned atom.
		int atomToJustify = assignment.getBasicAtomAssignedMBT();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Searching for justification of {} / {}", atomToJustify, atomStore.atomToString(atomToJustify));
			LOGGER.debug("Assignment is (TRUE part only): {}", translate(assignment.getTrueAssignments()));
		}
		Set<Literal> reasonsForUnjustified = analyzingGrounder.justifyAtom(atomToJustify, assignment);
		NoGood noGood = noGoodFromJustificationReasons(atomToJustify, reasonsForUnjustified);


		int noGoodID = grounder.register(noGood);
		Map<Integer, NoGood> obtained = new LinkedHashMap<>();
		obtained.put(noGoodID, noGood);
		rebootStrategy.newJustificationNoGood(noGood);
		LOGGER.debug("Learned NoGood is: {}", atomStore.noGoodToString(noGood));
		// Add NoGood and trigger backjumping.
		if (!ingest(obtained)) {
			logStats();
			return false;
		}
		return true;
	}

	private NoGood noGoodFromJustificationReasons(int atomToJustify, Set<Literal> reasonsForUnjustified) {
		// Turn the justification into a NoGood.
		int[] reasons = new int[reasonsForUnjustified.size() + 1];
		reasons[0] = atomToLiteral(atomToJustify);
		int arrpos = 1;
		for (Literal literal : reasonsForUnjustified) {
			reasons[arrpos++] = atomToLiteral(atomStore.get(literal.getAtom()), !literal.isNegated());
		}
		return NoGood.learnt(reasons);
	}

	private boolean treatConflictAfterClosing(Antecedent violatedNoGood) {
		if (disableJustificationAfterClosing || disableJustifications || !(grounder instanceof ProgramAnalyzingGrounder)) {
			// Will not learn from violated NoGood, do simple backtrack.
			LOGGER.debug("NoGood was violated after all unassigned atoms were assigned to false; will not learn from it; skipping.");
			if (!backtrack()) {
				logStats();
				return false;
			}
			return true;
		}
		ProgramAnalyzingGrounder analyzingGrounder = (ProgramAnalyzingGrounder) grounder;
		LOGGER.debug("Justifying atoms in violated nogood.");
		LinkedHashSet<Integer> toJustify = new LinkedHashSet<>();
		// Find those literals in violatedNoGood that were just assigned false.
		for (Integer literal : violatedNoGood.getReasonLiterals()) {
			if (assignment.getImpliedBy(atomOf(literal)) == TrailAssignment.CLOSING_INDICATOR_ANTECEDENT) {
				toJustify.add(literal);
			}
		}
		// Since the violatedNoGood may contain atoms other than BasicAtom, these have to be treated.
		Map<Integer, NoGood> obtained = new LinkedHashMap<>();
		Iterator<Integer> toJustifyIterator = toJustify.iterator();
		ArrayList<Integer> ruleAtomReplacements = new ArrayList<>();
		while (toJustifyIterator.hasNext()) {
			Integer literal = toJustifyIterator.next();
			Atom atom = atomStore.get(atomOf(literal));
			if (atom instanceof BasicAtom) {
				continue;
			}
			if (!(atom instanceof RuleAtom)) {
				// Ignore atoms other than RuleAtom.
				toJustifyIterator.remove();
				continue;
			}
			// Translate RuleAtom back to NonGroundRule + Substitution.
			RuleAtom ruleAtom = (RuleAtom) atom;
			CompiledRule nonGroundRule = Substitutions.getNonGroundRuleFromRuleAtom(ruleAtom, analyzingGrounder);
			Substitution groundingSubstitution = Substitutions.getSubstitutionFromRuleAtom(ruleAtom);
			// Find ground literals in the body that have been assigned false and justify those.
			for (Literal bodyLiteral : nonGroundRule.getBody()) {
				Atom groundAtom = bodyLiteral.getAtom().substitute(groundingSubstitution);
				if (groundAtom instanceof ComparisonAtom || analyzingGrounder.isFact(groundAtom)) {
					// Facts and ComparisonAtoms are always true, no justification needed.
					continue;
				}
				int groundAtomId = atomStore.get(groundAtom);
				Antecedent impliedBy = assignment.getImpliedBy(groundAtomId);
				// Check if atom was assigned to FALSE during the closing.
				if (impliedBy == TrailAssignment.CLOSING_INDICATOR_ANTECEDENT) {
					ruleAtomReplacements.add(atomToNegatedLiteral(groundAtomId));
				}
			}
			toJustifyIterator.remove();
		}
		toJustify.addAll(ruleAtomReplacements);
		for (Integer literalToJustify : toJustify) {
			LOGGER.debug("Searching for justification(s) of {} / {}", toJustify, atomStore.atomToString(atomOf(literalToJustify)));
			Set<Literal> reasonsForUnjustified = analyzingGrounder.justifyAtom(atomOf(literalToJustify), assignment);
			NoGood noGood = noGoodFromJustificationReasons(atomOf(literalToJustify), reasonsForUnjustified);
			int noGoodID = grounder.register(noGood);
			obtained.put(noGoodID, noGood);
			LOGGER.debug("Learned NoGood is: {}", atomStore.noGoodToString(noGood));
		}
		// Backtrack to remove the violation.
		if (!backtrack()) {
			logStats();
			return false;
		}
		// Add newly obtained noGoods.
		boolean success = ingest(obtained);
		rebootManager.newLearnedNoGoods(obtained.values());
		rebootStrategy.newLearnedNoGoods(obtained.values());
		learnedNoGoodTracker.incrementBy(obtained.values().size());
		if (!success) {
			logStats();
			return false;
		}
		return true;
	}

	private boolean close() {
		searchState.afterAllAtomsAssigned = true;
		return assignment.closeUnassignedAtoms();
	}

	/**
	 * Realizes chronological backtracking.
	 *
	 * @return {@code true} iff it is possible to backtrack even further, {@code false} otherwise
	 */
	private boolean backtrack() {
		while (assignment.getDecisionLevel() != 0) {
			// Backtrack highest decision level.
			final int previousDecisionLevel = assignment.getDecisionLevel();
			final Choice backtrackedChoice = choiceManager.backtrack();
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Backtracked choice atom is {}={}@{}.", backtrackedChoice.getAtom(),
					backtrackedChoice.getTruthValue() ? ThriceTruth.TRUE : ThriceTruth.FALSE, previousDecisionLevel);
			}

			// Construct inverse choice, if choice can be inverted.
			final Choice invertedChoice = Choice.getInverted(backtrackedChoice);
			if (invertedChoice == null) {
				LOGGER.debug("Backtracking further, because last choice was already backtracked/inverted.");
				continue;
			}
			// Choose inverse as long as the choice atom is not assigned.
			ThriceTruth currentTruthValue = assignment.getTruth(backtrackedChoice.getAtom());
			if (currentTruthValue == null) {
				LOGGER.debug("Choosing inverse, choice is: {}.", invertedChoice);
				choiceManager.choose(invertedChoice);
				break;
			}
			LOGGER.debug("Backtracking further, not inverting choice, because its value is implied now.");
		}
		return assignment.getDecisionLevel() != 0;
	}

	private boolean ingest(Map<Integer, NoGood> obtained) {
		growForMaxAtomId();
		branchingHeuristic.newNoGoods(obtained.values());

		LinkedList<Map.Entry<Integer, NoGood>> noGoodsToAdd = new LinkedList<>(obtained.entrySet());
		Map.Entry<Integer, NoGood> entry;
		while ((entry = noGoodsToAdd.poll()) != null) {
			if (NoGood.UNSAT.equals(entry.getValue())) {
				// Empty NoGood cannot be satisfied, program is unsatisfiable.
				return false;
			}

			final ConflictCause conflictCause = store.add(entry.getKey(), entry.getValue(), Integer.MAX_VALUE);
			if (conflictCause != null && !fixContradiction(entry, conflictCause)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Attempts to fix a given conflict that arose from adding a nogood.
	 * @param noGoodEntry the description of the NoGood that caused the conflict.
	 * @param conflictCause a description of the cause of the conflict.
	 * @return true if the contradiction could be resolved (by backjumping) and the NoGood was added.
	 * 	   False otherwise, i.e., iff the program is UNSAT.
	 */
	private boolean fixContradiction(Map.Entry<Integer, NoGood> noGoodEntry, ConflictCause conflictCause) {
		LOGGER.debug("Attempting to fix violation of {} caused by {}", noGoodEntry.getValue(), conflictCause);

		GroundConflictNoGoodLearner.ConflictAnalysisResult conflictAnalysisResult = learner.analyzeConflictFromAddingNoGood(conflictCause.getAntecedent());
		if (conflictAnalysisResult == UNSAT) {
			return false;
		}
		branchingHeuristic.analyzedConflict(conflictAnalysisResult);
		if (conflictAnalysisResult.learnedNoGood != null) {
			throw oops("Unexpectedly learned NoGood after addition of new NoGood caused a conflict.");
		}

		choiceManager.backjump(conflictAnalysisResult.backjumpLevel);

		// If NoGood was learned, add it to the store.
		// Note that the learned NoGood may cause further conflicts, since propagation on lower decision levels is lazy,
		// hence backtracking once might not be enough to remove the real conflict cause.
		return addAndBackjumpIfNecessary(noGoodEntry.getKey(), noGoodEntry.getValue(), LBD_NO_VALUE);
	}

	private boolean choose() {
		choiceManager.addChoiceInformation(grounder.getChoiceAtoms(), grounder.getHeadsToBodies());
		choiceManager.updateAssignments();

		// Hint: for custom heuristics, evaluate them here and pick a value if the heuristics suggests one.
		int literal;
		if ((literal = branchingHeuristic.chooseLiteral()) == DEFAULT_CHOICE_LITERAL) {
			LOGGER.debug("No choices!");
			return false;
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Branching heuristic chose literal {}", atomStore.literalToString(literal));
		}

		choiceManager.choose(new Choice(literal, false));
		decisionTracker.increment();
		return true;
	}

	/**
	 * Performs a reboot of the solving process while preserving the current assignment - and thus the progress made.
	 * {@link NoGoodStore} and {@link Grounder} are reset. Then the {@link AtomStore} is emptied and the current
	 * assignment is reconstructed.
	 */
	private void reboot() {
		LOGGER.info("Performing solver and grounder reboot.");

		RebootableGrounder rebootableGrounder = (RebootableGrounder) grounder;
		Stack<AtomizedChoice> atomizedChoiceStack = getAtomizedChoiceStack();

		store.reset();
		branchingHeuristic.reset();
		assignment.clear();
		rebootableGrounder.reboot(assignment);
		atomStore.reset();
		choiceManager.reset();

		syncWithGrounder();
		ingestNoGoodCollection(rebootManager.getEnumerationNoGoods());
		ingestNoGoodCollection(rebootManager.getLearnedNoGoods());
		ingestNoGoodsFromRulesOfRuleAtoms(rebootManager.getDiscoveredRuleAtoms());
		replayAtomizedChoiceStack(atomizedChoiceStack);

		LOGGER.info("Solver and grounder reboot finished.");
	}

	/**
	 * Extracts the current choice stack with atom ids replaced by the respective {@link Atom}s.
	 * @return the choice stack containing {@link Atom}s instead of atom ids.
	 */
	private Stack<AtomizedChoice> getAtomizedChoiceStack() {
		List<Choice> choiceList = choiceManager.getChoiceList();
		Stack<AtomizedChoice> choiceStack = new Stack<>();
		for (Choice choice : choiceList) {
			Atom atom = atomStore.get(choice.getAtom());
			choiceStack.push(new AtomizedChoice(atom, choice.getTruthValue(), choice.isBacktracked()));
		}
		return choiceStack;
	}

	/**
	 * Converts the given choice stack into one containing atom ids instead of atoms and applies
	 * the choices on the resulting stack using the choice manager.
	 * Propagation is performed initially and after each choice.
	 * @param atomizedChoiceStack the stack of choices containing atoms.
	 */
	private void replayAtomizedChoiceStack(Stack<AtomizedChoice> atomizedChoiceStack) {
		if (propagate() != null) {
			throw oops("Conflict in replay during reboot");
		}

		for (AtomizedChoice atomizedChoice : atomizedChoiceStack) {
			Atom atom = atomizedChoice.getAtom();
			atomStore.putIfAbsent(atom);
		}
		growForMaxAtomId();

		for (AtomizedChoice atomizedChoice : atomizedChoiceStack) {
			Atom atom = atomizedChoice.getAtom();
			int atomId = atomStore.get(atom);
			Choice choice = new Choice(atomId, atomizedChoice.getTruthValue(), atomizedChoice.isBacktracked());

			choiceManager.addChoiceInformation(grounder.getChoiceAtoms(), grounder.getHeadsToBodies());
			choiceManager.updateAssignments();
			boolean activeChoice = choiceManager.replayChoice(choice);

			if (activeChoice) {
				if (propagate() != null) {
					throw oops("Conflict in replay during reboot");
				}
				syncWithGrounder();
				if (propagate() != null) {
					throw oops("Conflict in replay during reboot");
				}
			}
		}
	}

	/**
	 * Grows the current {@link Assignment}, {@link NoGoodStore}, {@link ChoiceManager} and {@link BranchingHeuristic}
	 * by calling {@link Assignment#growForMaxAtomId()}, {@link NoGoodStore#growForMaxAtomId(int)},
	 * {@link ChoiceManager#growForMaxAtomId(int)} and {@link BranchingHeuristic#growForMaxAtomId(int)} respectively.
	 */
	private void growForMaxAtomId() {
		assignment.growForMaxAtomId();
		int maxAtomId = atomStore.getMaxAtomId();
		store.growForMaxAtomId(maxAtomId);
		choiceManager.growForMaxAtomId(maxAtomId);
		branchingHeuristic.growForMaxAtomId(maxAtomId);
	}

	/**
	 * Registers the given collection of {@link NoGood}s at the {@link Grounder}
	 * and calls {@link DefaultSolver#ingest(Map)} with the resulting ids as keys.
	 * @param noGoods the {@link NoGood}s to register and ingest.
	 */
	private void ingestNoGoodCollection(Collection<NoGood> noGoods) {
		Map<Integer, NoGood> newNoGoods = new LinkedHashMap<>();
		for (NoGood noGood : noGoods) {
			newNoGoods.put(grounder.register(noGood), noGood);
		}
		if (!ingest(newNoGoods)) {
			searchState.isSearchSpaceCompletelyExplored = true;
		}
	}

	/**
	 * Forces the {@link RebootableGrounder} to ground all rules corresponding to the given list of
	 * {@link RuleAtom}s. Then calls {@link DefaultSolver#ingest(Map)} with the {@link NoGood}s obtained this way.
	 * This method must not be called if reboots are disabled.
	 * @param ruleAtoms the {@link RuleAtom}s to ground and ingest the corresponding rules for.
	 */
	private void ingestNoGoodsFromRulesOfRuleAtoms(List<RuleAtom> ruleAtoms) {
		if (!rebootEnabled) {
			throw oops("Reboot is not enabled but nogood ingestion from rule atoms was called");
		}
		RebootableGrounder rebootableGrounder = (RebootableGrounder) grounder;
		Map<Integer, NoGood> newNoGoods = new LinkedHashMap<>();
		for (RuleAtom ruleAtom : ruleAtoms) {
			Map<Integer, NoGood> obtained = rebootableGrounder.forceRuleGrounding(ruleAtom);
			newNoGoods.putAll(obtained);
		}
		if (!ingest(newNoGoods)) {
			searchState.isSearchSpaceCompletelyExplored = true;
		}
	}

	/**
	 * Stops the {@link DefaultSolver#solverStopWatch} and {@link DefaultSolver#epochStopWatch}.
	 * Then logs the current total time of all {@link StopWatch}es kept by the {@link DefaultSolver}.
	 */
	private void stopStopWatchesAndLogRuntimes() {
		solverStopWatch.stop();
		epochStopWatch.stop();
		LOGGER.info("Solver runtime: {}", solverStopWatch.getNanoTime());
		LOGGER.info("Grounder runtime: {}", grounderStopWatch.getNanoTime());
		LOGGER.info("Propagation runtime: {}", propagationStopWatch.getNanoTime());
		LOGGER.info("Mbt-backtracking runtime: {}", mbtBacktrackingStopWatch.getNanoTime());
		LOGGER.info("Epoch runtime: {}", epochStopWatch.getNanoTime());
	}

	private List<StatTracker> initStatTrackers() {
		StaticNoGoodTracker staticNoGoodExtractingTracker = new StaticNoGoodTracker(getNoGoodCounter());
		LearnedNoGoodTracker learnedNoGoodExtractingTracker = new LearnedNoGoodTracker(getNoGoodCounter());
		TotalNoGoodTracker totalNoGoodExtractingTracker = new TotalNoGoodTracker(getNoGoodCounter());

		List<StatTracker> statTrackers = new LinkedList<>();
		statTrackers.add(iterationTracker);
		statTrackers.add(conflictTracker);
		statTrackers.add(decisionTracker);
		statTrackers.add(learnedNoGoodTracker);
		statTrackers.add(staticNoGoodExtractingTracker);
		statTrackers.add(learnedNoGoodExtractingTracker);
		statTrackers.add(totalNoGoodExtractingTracker);
		statTrackers.add(learnEfficiencyTracker);

		PropagationStatManager propagationStatManager = this.store.getPropagationStatManager();
		if (propagationStatManager != null) {
			List<ResettableStatTracker> propagationTrackers = propagationStatManager.getStatTrackerList();
			statTrackers.addAll(propagationTrackers);

			ResettableStatTracker propagationTracker = propagationStatManager.getPropagationTracker();
			ResettableStatTracker propagationConflictTracker = propagationStatManager.getPropagationConflictTracker();
			ResettableStatTracker nonbinPropagationTracker = propagationStatManager.getNonbinPropagationTracker();
			ResettableStatTracker nonbinPropagationConflictTracker = propagationStatManager.getNonbinPropagationConflictTracker();
			statTrackers.add(new ResettableQuotientTracker("prop_quot", propagationConflictTracker, propagationTracker));
			statTrackers.add(new QuotientTracker("prop_quot_nonbin", nonbinPropagationConflictTracker, nonbinPropagationTracker));
		}

		statTrackers.add(new QuotientTracker("conflict_quot", conflictTracker, iterationTracker));
		statTrackers.add(new QuotientTracker("nogood_quot", staticNoGoodExtractingTracker, totalNoGoodExtractingTracker));

		return statTrackers;
	}

	@Override
	public int getNumberOfChoices() {
		return choiceManager.getChoices();
	}

	@Override
	public int getNumberOfBacktracks() {
		return choiceManager.getBacktracks();
	}

	@Override
	public int getNumberOfBacktracksWithinBackjumps() {
		return choiceManager.getBacktracksWithinBackjumps();
	}

	@Override
	public int getNumberOfBackjumps() {
		return choiceManager.getBackjumps();
	}

	@Override
	public int getNumberOfBacktracksDueToRemnantMBTs() {
		return mbtAtFixpoint;
	}

	@Override
	public int getNumberOfConflictsAfterClosing() {
		return conflictsAfterClosing;
	}

	@Override
	public int getNumberOfDeletedNoGoods() {
		if (!(store instanceof NoGoodStoreAlphaRoaming)) {
			return 0;
		}
		return ((NoGoodStoreAlphaRoaming)store).getLearnedNoGoodDeletion().getNumberOfDeletedNoGoods();
	}

	public NoGoodCounter getNoGoodCounter() {
		return store.getNoGoodCounter();
	}

	private void logStats() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getStatisticsString());
			if (branchingHeuristic instanceof ChainedBranchingHeuristics) {
				LOGGER.debug("Decisions made by each heuristic:");
				for (Entry<BranchingHeuristic, Integer> heuristicToDecisionCounter : ((ChainedBranchingHeuristics)branchingHeuristic).getNumberOfDecisions().entrySet()) {
					LOGGER.debug("{}: {}", heuristicToDecisionCounter.getKey(), heuristicToDecisionCounter.getValue());
				}
			}
			NoGoodCounter noGoodCounter = store.getNoGoodCounter();
			LOGGER.debug("Number of NoGoods by type: {}", noGoodCounter.getStatsByType());
			LOGGER.debug("Number of NoGoods by cardinality: {}", noGoodCounter.getStatsByCardinality());
			AtomCounter atomCounter = atomStore.getAtomCounter();
			LOGGER.debug("Number of atoms by type: {}", atomCounter.getStatsByType());
		}
	}
}
