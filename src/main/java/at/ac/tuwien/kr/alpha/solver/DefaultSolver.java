package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

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

	private int decisionLevel;
	private boolean initialize = true;

	private boolean didChange;

	public DefaultSolver(Grounder grounder, java.util.function.Predicate<Predicate> filter) {
		super(grounder, filter);

		this.assignment = new BasicAssignment();
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
			} else if (store.getViolatedNoGood() != null) {
				LOGGER.trace("Backtracking from wrong choices ({} violated): {}", store.getViolatedNoGood(), choiceStack);
				doBacktrack();
				if (isSearchSpaceExhausted()) {
					return false;
				}
			} else if ((nextChoice = computeChoice()) != 0) {
				doChoice(nextChoice);
			} else if (assignment.getMBTCount() == 0) {
				AnswerSet as = translate(assignment.getTrueAssignments());

				if (as == null) {
					return true;
				}

				LOGGER.debug("Answer-Set found: {}", as);
				LOGGER.trace("Choices: {}", choiceStack);
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

	private void doBacktrack() {
		if (decisionLevel <= 0) {
			return;
		}

		assignment.backtrack(decisionLevel - 1);

		int lastGuessedAtom = choiceStack.peekAtom();
		boolean lastGuessedValue = choiceStack.peekValue();
		choiceStack.remove();

		if (lastGuessedValue) {
			store.assign(lastGuessedAtom, FALSE);
			choiceStack.push(lastGuessedAtom, false);
			didChange = true;
		} else {
			decisionLevel--;
			store.setDecisionLevel(decisionLevel);
			doBacktrack();
		}
	}

	private void updateGrounderAssignment() {
		final Map<Integer, ThriceTruth> changedAssignments = store.getChangedAssignments();
		int[] atomIds = new int[changedAssignments.size()];
		boolean[] truthValues = new boolean[changedAssignments.size()];
		int i = 0;
		for (Map.Entry<Integer, ThriceTruth> assignment : changedAssignments.entrySet()) {
			atomIds[i] = assignment.getKey();
			truthValues[i] = assignment.getValue().toBoolean();
			i++;
		}
		grounder.updateAssignment(atomIds, truthValues);
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
		return decisionLevel == 0;
	}

	private boolean propagationFixpointReached() {
		// Check if anything changed: didChange is updated in places of change.
		boolean changeCopy = didChange;
		didChange = false;
		return !changeCopy;
	}

	private void doChoice(int nextChoice) {
		decisionLevel++;
		store.setDecisionLevel(decisionLevel);
		// We guess true for any unassigned choice atom (backtrack tries false)
		store.assign(nextChoice, TRUE);
		choiceStack.push(nextChoice, true);
		// Record change to compute propagation fixpoint again.
		didChange = true;
	}

	private int computeChoice() {
		// Check if there is an enabled choice that is not also disabled
		// HINT: tracking changes of ChoiceOn, ChoiceOff directly could
		// increase performance (analyze store.getChangedAssignments()).
		for (Integer enablerAtom : choiceOn.keySet()) {
			if (FALSE.equals(assignment.getTruth(enablerAtom))) {
				continue;
			}

			Integer nextChoiceCandidate = choiceOn.get(enablerAtom);

			// Only consider unassigned choices
			if (assignment.getTruth(nextChoiceCandidate) != null) {
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
