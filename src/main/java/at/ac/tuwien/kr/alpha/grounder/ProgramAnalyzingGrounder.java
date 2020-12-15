package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.InternalRule;

import java.util.List;
import java.util.Set;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public interface ProgramAnalyzingGrounder extends Grounder {

	/**
	 * Justifies the absence of an atom, i.e., returns reasons why the atom is not TRUE given the assignment.
	 * @param atomToJustify the atom to justify.
	 * @param currentAssignment the current assignment.
	 * @return a set of literals who jointly imply the atomToJustify not being TRUE.
	 */
	Set<Literal> justifyAtom(int atomToJustify, Assignment currentAssignment);

	/**
	 * Returns true iff the given atom is known to the grounder as a fact (hence not occurring in any assignment).
	 * @param atom the atom.
	 * @return true iff atom is a fact.
	 */
	boolean isFact(Atom atom);

	/**
	 * Returns the {@link InternalRule} identified by the given id.
	 * @param ruleId the id of the rule.
	 * @return the corresponding {@link InternalRule}.
	 */
	InternalRule getNonGroundRule(Integer ruleId);

	/**
	 * Computes the completion of the given atom and grounds rules deriving it, if that is possible.
	 * @param atom the atom to complete.
	 * @return an empty list if completion is not possible, otherwise the completion nogood and all nogoods resulting from rules deriving the atom.
	 */
	List<NoGood> completeAndGroundRulesFor(int atom);

	/**
	 * Returns a set of atomIds that have been completed (or attempted to be completed) during recent grounding.
	 * Newly completed atoms are only reported once. Calling completeAndGroundRulesFor for an atomId that got
	 * reported as newly completed previously is useless.
	 * @return a set of atomIds previously completed.
	 */
	Set<Integer> getNewlyCompletedAtoms();
}
