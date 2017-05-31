package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.solver.Assignment;
import at.ac.tuwien.kr.alpha.solver.Choices;
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
	Map<Integer, NoGood> getNoGoods(ReadableAssignment assignment);

	Map<Integer, NoGood> getHexNoGoods(Assignment assignment);

	/**
	 * Return choice points and their enablers and disablers.
	 * Must be preceeded by a call to getNoGoods().
	 * @return a pair (choiceOn, choiceOff) of two maps from atomIds to atomIds,
	 * choiceOn maps atoms (choice points) to their enabling atoms
	 * and choiceOff maps atoms (choice points) to their disabling atoms.
	 */
	Choices getChoices();

	void updateAssignment(Iterator<ReadableAssignment.Entry> it);

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

	/**
	 * Returns the highest atomId in use.
	 * @return the highest atomId in use.
	 */
	int getMaxAtomId();
}
