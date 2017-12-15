package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.List;

/**
 * Represents any element that may occur in the body of a rule.
 * Copyright (c) 2017, the Alpha Team.
 */
public interface BodyElement {

	/**
	 * List of all variables occurring in the {@link BodyElement} that are potentially binding, e.g, variables in positive atoms, bound by =, etc.
	 * @return the list of all variables that can be bound by this BodyElement.
	 */
	List<VariableTerm> getBindingVariables();

	/**
	 * List of all variables occurring in the {@link BodyElement} that are not binding e.g., variables in negative literals, intervals, or most built-in atoms.
	 * @return the list of all variables that must be bound by other BodyElements.
	 */
	List<VariableTerm> getNonBindingVariables();

	/**
	 * Returns whether this element is ground.
	 * @return true iff no variables occur in the element.
	 */
	boolean isGround();

	/**
	 * This method applies a substitution to a potentially non-substitute atom.
	 * The resulting atom may be non-substitute.
	 * @param substitution the variable substitution to apply.
	 * @return the atom resulting from the applying the substitution.
	 */
	BodyElement substitute(Substitution substitution);
}
