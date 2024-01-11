package at.ac.tuwien.kr.alpha.core.actions;

import java.util.Map;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.programs.actions.Action;

/**
 * Interface for types providing action implementations.
 */
public interface ActionImplementationProvider {

	/**
	 * Returns a map of all actions supported by this implementation provider.
	 */
	Map<String, Action> getSupportedActions();

	/**
	 * Returns a predicate interpretation specifying an external that takes no arguments 
	 * and returns a reference to the standard system output stream (stdout).
	 */
	PredicateInterpretation getStdoutTerm();

	/**
	 * Returns a predicate interpretation specifying an external that takes no arguments 
	 * and returns a reference to the standard system input stream (stdin).
	 */	
	PredicateInterpretation getStdinTerm();

}
