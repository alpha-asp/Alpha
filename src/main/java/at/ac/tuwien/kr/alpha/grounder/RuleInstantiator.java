package at.ac.tuwien.kr.alpha.grounder;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.ExternalLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder.BindingResult;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.IntervalLiteral;
import at.ac.tuwien.kr.alpha.grounder.heuristics.GrounderHeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

/**
 * Provides ground instantiations for rules.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public class RuleInstantiator {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleInstantiator.class);
	
	private final static int NEITHER_TERMINATE_BINDING_NOR_DECREMENT_TOLERANCE = 0;
	private final static int TERMINATE_BINDING = 1;
	private final static int DECREMENT_TOLERANCE = 2;

	private WorkingMemory workingMemory = new WorkingMemory();
	private final Map<Predicate, LinkedHashSet<Instance>> factsFromProgram = new LinkedHashMap<>(); // FIXME do we need this? should be working memory lookup

	BindingResult getGroundInstantiations(GrounderHeuristicsConfiguration heuristicsConfiguration, NonGroundRule rule, RuleGroundingOrder groundingOrder,
			Substitution partialSubstitution, Assignment currentAssignment) {
		int tolerance = heuristicsConfiguration.getTolerance(rule.isConstraint());
		if (tolerance < 0) {
			tolerance = Integer.MAX_VALUE;
		}
		BindingResult bindingResult = bindNextAtomInRule(groundingOrder, 0, tolerance, tolerance, partialSubstitution, currentAssignment);
		if (LOGGER.isDebugEnabled()) {
			for (int i = 0; i < bindingResult.size(); i++) {
				Integer numberOfUnassignedPositiveBodyAtoms = bindingResult.numbersOfUnassignedPositiveBodyAtoms.get(i);
				if (numberOfUnassignedPositiveBodyAtoms > 0) {
					LOGGER.debug("Grounded rule in which {} positive atoms are still unassigned: {} (substitution: {})", numberOfUnassignedPositiveBodyAtoms,
							rule, bindingResult.generatedSubstitutions.get(i));
				}
			}
		}
		return bindingResult;
	}

	private BindingResult bindNextAtomInRule(RuleGroundingOrder groundingOrder, int orderPosition, int originalTolerance, int remainingTolerance,
			Substitution partialSubstitution, Assignment currentAssignment) {
		boolean permissiveGrounderHeuristic = originalTolerance > 0;

		Literal currentLiteral = groundingOrder.getLiteralAtOrderPosition(orderPosition);
		if (currentLiteral == null) {
			return BindingResult.singleton(partialSubstitution, originalTolerance - remainingTolerance);
		}

		Atom currentAtom = currentLiteral.getAtom();
		if (currentLiteral instanceof FixedInterpretationLiteral) {
			// Generate all substitutions for the builtin/external/interval atom.
			FixedInterpretationLiteral substitutedLiteral = (FixedInterpretationLiteral) currentLiteral.substitute(partialSubstitution);
			if (shallPushBackFixedInterpretationLiteral(substitutedLiteral)) {
				return pushBackAndBindNextAtomInRule(groundingOrder, orderPosition, originalTolerance, remainingTolerance, partialSubstitution,
						currentAssignment);
			}
			final List<Substitution> substitutions = substitutedLiteral.getSatisfyingSubstitutions(partialSubstitution);

			if (substitutions.isEmpty()) {
				// if FixedInterpretationLiteral cannot be satisfied now, it will never be
				return BindingResult.empty();
			}

			final BindingResult bindingResult = new BindingResult();
			for (Substitution substitution : substitutions) {
				// Continue grounding with each of the generated values.
				bindingResult.add(
						advanceAndBindNextAtomInRule(groundingOrder, orderPosition, originalTolerance, remainingTolerance, substitution, currentAssignment));
			}
			return bindingResult;
		}
		if (currentAtom instanceof EnumerationAtom) {
			// Get the enumeration value and add it to the current partialSubstitution.
			((EnumerationAtom) currentAtom).addEnumerationToSubstitution(partialSubstitution);
			return advanceAndBindNextAtomInRule(groundingOrder, orderPosition, originalTolerance, remainingTolerance, partialSubstitution, currentAssignment);
		}

		Collection<Instance> instances = null;

		// check if partialVariableSubstitution already yields a ground atom
		final Atom substitute = currentAtom.substitute(partialSubstitution);
		if (substitute.isGround()) {
			// Substituted atom is ground, in case it is positive, only ground if it also
			// holds true
			if (currentLiteral.isNegated()) {
				// Atom occurs negated in the rule: continue grounding
				return advanceAndBindNextAtomInRule(groundingOrder, orderPosition, originalTolerance, remainingTolerance, partialSubstitution,
						currentAssignment);
			}

			if (!groundingOrder.isGround() && remainingTolerance <= 0
					&& !workingMemory.get(currentAtom.getPredicate(), true).containsInstance(new Instance(substitute.getTerms()))) {
				// Generate no variable substitution.
				return BindingResult.empty();
			}

			instances = singletonList(new Instance(substitute.getTerms()));
		}

		// substituted atom contains variables
		if (currentLiteral.isNegated()) {
			if (permissiveGrounderHeuristic) {
				return pushBackAndBindNextAtomInRule(groundingOrder, orderPosition, originalTolerance, remainingTolerance, partialSubstitution,
						currentAssignment);
			} else {
				throw oops("Current atom should be positive at this point but is not");
			}
		}

		if (instances == null) {
			instances = getInstancesForSubstitute(substitute, partialSubstitution);
		}

		if (permissiveGrounderHeuristic && instances.isEmpty()) {
			// we have reached a point where we have to terminate binding,
			// but it might be possible that a different grounding order would allow us to
			// continue binding
			// under the presence of a permissive grounder heuristic
			return pushBackAndBindNextAtomInRule(groundingOrder, orderPosition, originalTolerance, remainingTolerance, partialSubstitution, currentAssignment);
		}

		return createBindings(groundingOrder, orderPosition, originalTolerance, remainingTolerance, partialSubstitution, currentAssignment, instances,
				substitute);
	}

	private BindingResult pushBackAndBindNextAtomInRule(RuleGroundingOrder groundingOrder, int orderPosition, int originalTolerance, int remainingTolerance,
			Substitution partialSubstitution, Assignment currentAssignment) {
		RuleGroundingOrder modifiedGroundingOrder = groundingOrder.pushBack(orderPosition);
		if (modifiedGroundingOrder == null) {
			return BindingResult.empty();
		}
		return bindNextAtomInRule(modifiedGroundingOrder, orderPosition + 1, originalTolerance, remainingTolerance, partialSubstitution, currentAssignment);
	}

	private BindingResult advanceAndBindNextAtomInRule(RuleGroundingOrder groundingOrder, int orderPosition, int originalTolerance, int remainingTolerance,
			Substitution partialSubstitution, Assignment currentAssignment) {
		groundingOrder.considerUntilCurrentEnd();
		return bindNextAtomInRule(groundingOrder, orderPosition + 1, originalTolerance, remainingTolerance, partialSubstitution, currentAssignment);
	}

	/**
	 * Uses {@code instances} to create ground instantiations for {@code substitute}
	 * and then proceeds in the {@groundingOrder}.
	 * 
	 * @param groundingOrder      the grounding order to follow while grounding the
	 *                            corresponding rule.
	 * @param orderPosition       the current position in the
	 *                            {@code groundingOrder}.
	 * @param originalTolerance   the original number of tolerated unassigned
	 *                            positive body literals.
	 * @param remainingTolerance  the remaining number of tolerated unassigned
	 *                            positive body literals.
	 * @param partialSubstitution the partial substitution created so far while
	 *                            grounding the current rule.
	 * @param currentAssignment   the current assignment.
	 * @param instances           instances used to ground the current atom.
	 * @param substitute          the current atom, to which
	 *                            {@code partialSubstitution} has already been
	 *                            applied.
	 * @return
	 */
	private BindingResult createBindings(RuleGroundingOrder groundingOrder, int orderPosition, int originalTolerance, int remainingTolerance,
			Substitution partialSubstitution, Assignment currentAssignment, Collection<Instance> instances, Atom substitute) {
		BindingResult bindingResult = new BindingResult();
		for (Instance instance : instances) {
			int remainingToleranceForThisInstance = remainingTolerance;
			// Check each instance if it matches with the atom.
			Substitution unified = Substitution.unify(substitute, instance, new Substitution(partialSubstitution));
			if (unified == null) {
				continue;
			}

			// Check if atom is also assigned true.
			Atom substituteClone = new BasicAtom(substitute.getPredicate(), substitute.getTerms());
			Atom substitutedAtom = substituteClone.substitute(unified);
			if (!substitutedAtom.isGround()) {
				throw oops("Grounded atom should be ground but is not");
			}

			if (factsFromProgram.get(substitutedAtom.getPredicate()) == null
					|| !factsFromProgram.get(substitutedAtom.getPredicate()).contains(new Instance(substitutedAtom.getTerms()))) {
				final int terminateOrDecrement = storeAtomAndTerminateIfAtomDoesNotHold(substitutedAtom, currentAssignment, remainingToleranceForThisInstance);
				if (terminateOrDecrement == TERMINATE_BINDING) {
					continue;
				}
				if (terminateOrDecrement == DECREMENT_TOLERANCE) {
					remainingToleranceForThisInstance--;
				}
			}
			bindingResult.add(advanceAndBindNextAtomInRule(groundingOrder, orderPosition, originalTolerance, remainingToleranceForThisInstance, unified,
					currentAssignment));
		}

		return bindingResult;
	}

	// FIXME this seems like grounder workflow - should stay in grounder and work against instantiator interface
	/**
	 * Does nothing if {@code currentAssignment == null}, which means we are in bootstrapping.
	 * Otherwise, stores {@code substitute} in the atom store if it is not yet stored.
	 * Afterwards, the truth value currently assigned to this atom and the observation whether the atom is in the
	 * grounder's working memory or not are used to determine whether binding the current rule shall be terminated or not,
	 * and whether or not the remaining tolerance (for permissive grounding) shall be decremented.
	 * <p/>
	 * Binding shall not be terminated if accumulator is enabled and the atom is in the working memory.
	 * Otherwise, binding shall be terminated if either the atom is not assigned and tolerance is exhausted,
	 * or if the atom is assigned false.
	 * <p/>
	 * Tolerance shall be decremented if the atom is unassigned (and also not in the working memory, if accumulator
	 * is enabled) and binding is not terminated.
	 * <p/>
	 * If the atom is assigned false and accumulator is not enabled, the atom is also added to {@link #removeAfterObtainingNewNoGoods}
	 * to trigger lazy update of the working memory.
	 *
	 * @param substitute         the atom to store.
	 * @param currentAssignment  the current assignment.
	 * @param remainingTolerance the remaining number of positive body atoms tolerated not to be assigned.
	 * @return {@link #TERMINATE_BINDING} if binding shall be terminated; {@link #DECREMENT_TOLERANCE} if remaining
	 * tolerance shall be decremented; {@link #NEITHER_TERMINATE_BINDING_NOR_DECREMENT_TOLERANCE} if neither shall be done.
	 */
	private int storeAtomAndTerminateIfAtomDoesNotHold(final Atom substitute, final Assignment currentAssignment, final int remainingTolerance) {
		if (currentAssignment == null) { // if we are in bootstrapping
			return NEITHER_TERMINATE_BINDING_NOR_DECREMENT_TOLERANCE;
		}

		int decrementedTolerance = remainingTolerance;
		final int atomId = atomStore.putIfAbsent(substitute);
		currentAssignment.growForMaxAtomId();
		ThriceTruth truth = currentAssignment.isAssigned(atomId) ? currentAssignment.getTruth(atomId) : null;

		if (heuristicsConfiguration.isAccumulatorEnabled()) {
			// special handling for the accumulator variants of lazy-grounding strategies
			final Instance instance = new Instance(substitute.getTerms());
			boolean isInWorkingMemory = workingMemory.get(substitute, true).containsInstance(instance);
			if (isInWorkingMemory) {
				// the atom is in the working memory, so we need neither terminate nor decrement tolerance
				return NEITHER_TERMINATE_BINDING_NOR_DECREMENT_TOLERANCE;
			}
		} else if (truth == null || !truth.toBoolean()) {
			// no accumulator and the atom currently does not hold, so the working memory needs to be updated
			removeAfterObtainingNewNoGoods.add(substitute);
		}

		if (truth == null && --decrementedTolerance < 0) {
			// terminate if more positive atoms are unsatisfied as tolerated by the heuristic
			return TERMINATE_BINDING;
		}
		// terminate if positive body atom is assigned false
		if (truth != null && !truth.toBoolean()) {
			return TERMINATE_BINDING;
		} else if (decrementedTolerance < remainingTolerance) {
			return DECREMENT_TOLERANCE;
		}
		return NEITHER_TERMINATE_BINDING_NOR_DECREMENT_TOLERANCE;
	}	
	
	private Collection<Instance> getInstancesForSubstitute(Atom substitute, Substitution partialSubstitution) {
		Collection<Instance> instances;
		IndexedInstanceStorage storage = workingMemory.get(substitute.getPredicate(), true);
		if (partialSubstitution.isEmpty()) {
			// No variables are bound, but first atom in the body became recently true,
			// consider all instances now.
			instances = storage.getAllInstances();
		} else {
			instances = storage.getInstancesFromPartiallyGroundAtom(substitute);
		}
		return instances;
	}

	/**
	 * Any {@link FixedInterpretationLiteral} that does <emph>not</emph> fulfil any
	 * of the following conditions is
	 * "pushed back" in the grounding order because it cannot be used to generate
	 * substitutions now but maybe later:
	 * <ul>
	 * <li>the literal is ground</li>
	 * <li>the literal is a {@link ComparisonLiteral} that is left-assigning or
	 * right-assigning</li>
	 * <li>the literal is an {@link IntervalLiteral} representing a ground interval
	 * term</li>
	 * <li>the literal is an {@link ExternalLiteral}.</li>
	 * </ul>
	 * 
	 * @param substitutedLiteral
	 * @return
	 */
	private boolean shallPushBackFixedInterpretationLiteral(FixedInterpretationLiteral substitutedLiteral) {
		return !(substitutedLiteral.isGround() ||
				(substitutedLiteral instanceof ComparisonLiteral && ((ComparisonLiteral) substitutedLiteral).isLeftOrRightAssigning()) ||
				(substitutedLiteral instanceof IntervalLiteral && substitutedLiteral.getTerms().get(0).isGround()) ||
				(substitutedLiteral instanceof ExternalLiteral));
	}

}
