package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.OrdinaryAssignment;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;

/**
 * The new default solver employed in Alpha.
 * Copyright (c) 2016, the Alpha Team.
 */
public class DefaultSolver extends AbstractSolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSolver.class);

	private final NoGoodStore<ThriceTruth> store;
	private final Map<Integer, Integer> choiceOn = new LinkedHashMap<>();
	private final Map<Integer, Integer> choiceOff = new HashMap<>();
	private final ChoiceStack choiceStack;
	private final Assignment assignment;
	private final Iterator<OrdinaryAssignment> assignmentIterator;

	private boolean initialize = true;

	private boolean didChange;

	private int decisionCounter;
	private List<Integer> unassignedAtoms;

	public DefaultSolver(Grounder grounder) {
		super(grounder);

		this.assignment = new BasicAssignment(grounder);
		this.assignmentIterator = this.assignment.ordinaryIterator();
		this.store = new BasicNoGoodStore(assignment, grounder);
		this.choiceStack = new ChoiceStack(grounder);
	}

	@Override
	protected boolean tryAdvance(Consumer<? super AnswerSet> action) {
		// Get basic rules and facts from grounder
		if (initialize) {
			obtainNoGoodsFromGrounder();
			initialize = false;
		} else {
			// We already found one Answer-Set and are requested to find another one
			doBacktrack();
			if (isSearchSpaceExhausted()) {
				return false;
			}
		}

		int nextChoice;

		// Try all assignments until grounder reports no more NoGoods and all of them are satisfied
		while (true) {
			if (!propagationFixpointReached()) {
				// After a choice, it would be more efficient to propagate first and only then ask the grounder.
				updateGrounderAssignment();
				obtainNoGoodsFromGrounder();
				if (store.propagate()) {
					didChange = true;
				}
				LOGGER.debug("Assignment after propagation is: {}", assignment);
			} else if (store.getViolatedNoGood() != null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Backtracking from wrong choices ({} violated): {}", grounder.noGoodToString(store.getViolatedNoGood()), choiceStack);
				}
				LOGGER.debug("Violating assignment is: {}", assignment);
				doBacktrack();
				if (isSearchSpaceExhausted()) {
					return false;
				}
			} else if ((nextChoice = computeChoice()) != 0) {
				LOGGER.debug("Doing choice.");
				doChoice(nextChoice);
			} else if (!allAtomsAssigned()) {
				LOGGER.debug("Closing unassigned known atoms (assigning FALSE).");
				assignUnassignedToFalse();
				didChange = true;
			} else if (assignment.getMBTCount() == 0) {
				AnswerSet as = translate(assignment.getTrueAssignments());
				LOGGER.debug("Answer-Set found: {}", as);
				LOGGER.debug("Choices of Answer-Set were: {}", choiceStack);
				action.accept(as);
				return true;
			} else {
				LOGGER.debug("Backtracking from wrong choices ({} MBTs): {}", assignment.getMBTCount(), choiceStack);
				doBacktrack();
				if (isSearchSpaceExhausted()) {
					return false;
				}
			}
		}
	}

	private void assignUnassignedToFalse() {
		for (Integer atom : unassignedAtoms) {
			assignment.assign(atom, FALSE, null);
		}
	}

	private boolean allAtomsAssigned() {
		unassignedAtoms = grounder.getUnassignedAtoms(assignment);
		return unassignedAtoms.isEmpty();
	}

	private void doBacktrack() {
		boolean repeatBacktrack;	// Iterative implementation of recursive backtracking.
		do {
			repeatBacktrack = false;
			if (isSearchSpaceExhausted()) {
				return;
			}

			store.backtrack();
			LOGGER.debug("Backtrack: Removing last choice, setting decision level to {}.", assignment.getDecisionLevel());

			int lastGuessedAtom = choiceStack.peekAtom();
			boolean lastGuessedValue = choiceStack.peekValue();
			choiceStack.remove();
			LOGGER.debug("Backtrack: choice stack size: {}, choice stack: {}", choiceStack.size(), choiceStack);

			if (lastGuessedValue) {
				// Chronological backtracking: guess FALSE now.
				// Guess FALSE if the previous guess was for TRUE and the atom was not already MBT at that time.
				if (MBT.equals(assignment.getTruth(lastGuessedAtom))) {
					LOGGER.debug("Backtrack: inverting last guess not possible, atom was MBT before guessing TRUE.");
					LOGGER.debug("Recursive backtracking.");
					repeatBacktrack = true;
					continue;
					//doBacktrack();
					//return;
				}
				decisionCounter++;
				assignment.guess(lastGuessedAtom, FALSE);
				LOGGER.debug("Backtrack: setting decision level to {}.", assignment.getDecisionLevel());
				LOGGER.debug("Backtrack: inverting last guess. Now: {}=FALSE@{}", grounder.atomToString(lastGuessedAtom), assignment.getDecisionLevel());
				choiceStack.push(lastGuessedAtom, false);
				didChange = true;
				LOGGER.debug("Backtrack: choice stack size: {}, choice stack: {}", choiceStack.size(), choiceStack);
				LOGGER.debug("Backtrack: {} choices so far.", decisionCounter);
			} else {
				LOGGER.debug("Recursive backtracking.");
				repeatBacktrack = true;
				//doBacktrack();
			}
		} while (repeatBacktrack);
	}

	private void updateGrounderAssignment() {
		grounder.updateAssignment(assignmentIterator);
	}

	private void obtainNoGoodsFromGrounder() {
		Map<Integer, NoGood> obtained = grounder.getNoGoods();

		if (!obtained.isEmpty()) {
			// Record to detect propagation fixpoint, checking if new NoGoods were reported would be better here.
			didChange = true;
		}

		store.addAll(obtained);

		// Record choice atoms.
		final Pair<Map<Integer, Integer>, Map<Integer, Integer>> choiceAtoms = grounder.getChoiceAtoms();
		choiceOn.putAll(choiceAtoms.getKey());
		choiceOff.putAll(choiceAtoms.getValue());
	}

	private boolean isSearchSpaceExhausted() {
		return assignment.getDecisionLevel() == 0;
	}

	private boolean propagationFixpointReached() {
		// Check if anything changed: didChange is updated in places of change.
		boolean changeCopy = didChange;
		didChange = false;
		return !changeCopy;
	}

	private void doChoice(int nextChoice) {
		decisionCounter++;
		// We guess true for any unassigned choice atom (backtrack tries false)
		assignment.guess(nextChoice, TRUE);
		choiceStack.push(nextChoice, true);
		// Record change to compute propagation fixpoint again.
		didChange = true;
		LOGGER.debug("Choice: guessing {}=TRUE@{}", grounder.atomToString(nextChoice), assignment.getDecisionLevel());
		LOGGER.debug("Choice: stack size: {}, choice stack: {}", choiceStack.size(), choiceStack);
		LOGGER.debug("Choice: {} choices so far.", decisionCounter);
	}

	private int computeChoice() {
		// Check if there is an enabled choice that is not also disabled
		// HINT: tracking changes of ChoiceOn, ChoiceOff directly could
		// increase performance (analyze store.getChangedAssignments()).
		for (Integer enablerAtom : choiceOn.keySet()) {
			if (assignment.getTruth(enablerAtom) == null || FALSE.equals(assignment.getTruth(enablerAtom))) {
				continue;
			}

			Integer nextChoiceCandidate = choiceOn.get(enablerAtom);

			// Only consider unassigned choices or choices currently MBT (and changing to TRUE following the guess)
			if (assignment.getTruth(nextChoiceCandidate) != null && !MBT.equals(assignment.getTruth(nextChoiceCandidate))) {
				continue;
			}

			// Check that candidate is not disabled already
			boolean isDisabled = false;
			for (Map.Entry<Integer, Integer> disablerAtom : choiceOff.entrySet()) {
				if (nextChoiceCandidate.equals(disablerAtom.getValue())
					&& assignment.getTruth(disablerAtom.getKey()) != null
					&& !(FALSE.equals(assignment.getTruth(disablerAtom.getKey())))) {
					isDisabled = true;
					break;
				}
			}

			if (!isDisabled) {
				return nextChoiceCandidate;
			}
		}
		return 0;
	}
}
