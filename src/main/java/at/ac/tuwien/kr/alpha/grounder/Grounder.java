package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.solver.Assignment;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface Grounder extends AtomTranslator {
	/**
	 * Translates an answer-set represented by true atom IDs into its logical representation.
	 * @param trueAtoms
	 * @return
	 */
	AnswerSet assignmentToAnswerSet(Iterable<Integer> trueAtoms);

	/**
	 * Applies lazy grounding and returns all newly derived (fully ground) NoGoods.
	 * @return a mapping of nogood IDs to NoGoods.
	 */
	Map<Integer, NoGood> getNoGoods();

	/**
	 * Return choice points and their enablers and disablers.
	 * Must be preceeded by a call to getNoGoods().
	 * @return a pair (choiceOn, choiceOff) of two maps from atomIds to atomIds,
	 * choiceOn maps atoms (choice points) to their enabling atoms
	 * and choiceOff maps atoms (choice points) to their disabling atoms.
	 */
	Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms();

	void updateAssignment(Iterator<Assignment.Entry> it);

	void forgetAssignment(int[] atomIds);

	/**
	 * Returns a list of currently known but unassigned.
	 * @param assignment the current assignment.
	 * @return a list of atoms not having assigned a truth value.
	 */
	List<Integer> getUnassignedAtoms(Assignment assignment);

	/**
	 * Registers the given NoGood and returns the identifier of it.
	 * @param noGood
	 * @return
	 */
	int registerOutsideNoGood(NoGood noGood);

	/**
	 * Returns true whenever the atom is a valid choice point (i.e., it represents a rule body).
	 * @param atom
	 * @return
	 */
	boolean isAtomChoicePoint(int atom);
}
