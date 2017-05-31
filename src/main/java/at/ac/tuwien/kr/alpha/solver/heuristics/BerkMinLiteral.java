package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.Assignment;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.learning.GroundConflictNoGoodLearner;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.atoms.Atoms.isAtom;

/**
 * A BerkMin-like heuristics that uses activity of literals and a fixed-size queue instead of a stack of NoGoods.
 * Copyright (c) 2017, the Alpha Team.
 */
public class BerkMinLiteral extends BerkMin {

	private Deque<Integer> activeLiterals = new LinkedList<>();
	private static final int DEFAULT_QUEUE_SIZE = 32;
	private final int queueSize;

	BerkMinLiteral(Assignment assignment, ChoiceManager choiceManager, int decayAge, double decayFactor, Random random, int queueSize) {
		super(assignment, choiceManager, decayAge, decayFactor, random);
		this.queueSize = queueSize;
	}

	BerkMinLiteral(Assignment assignment, ChoiceManager choiceManager, Random random) {
		this(assignment, choiceManager, DEFAULT_DECAY_AGE, DEFAULT_DECAY_FACTOR, random, DEFAULT_QUEUE_SIZE);
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

	private void pushToStack(Integer literal) {
		if (choiceManager.isAtomChoice(atomOf(literal))) {
			activeLiterals.addFirst(literal);
			// Restrict the size of the queue.
			if (activeLiterals.size() > queueSize) {
				activeLiterals.removeLast();
			}
		}
	}

	@Override
	protected void pushToStack(NoGood noGood) {
		if (noGood != null) {
			for (Integer literal : noGood) {
				pushToStack(literal);
			}
		}
	}
}
