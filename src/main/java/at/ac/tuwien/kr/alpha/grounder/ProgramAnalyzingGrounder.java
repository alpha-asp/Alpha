package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;

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
	 * Returns the NonGroundRule identified by the given id.
	 * @param ruleId the id of the rule.
	 * @return the corresponding NonGroundRule.
	 */
	NonGroundRule getNonGroundRule(Integer ruleId);
}
