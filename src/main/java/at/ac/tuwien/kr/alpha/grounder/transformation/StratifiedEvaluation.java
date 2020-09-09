package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;
import at.ac.tuwien.kr.alpha.common.depgraph.Node;
import at.ac.tuwien.kr.alpha.common.depgraph.StratificationHelper;
import at.ac.tuwien.kr.alpha.common.program.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.common.program.InternalProgram;
import at.ac.tuwien.kr.alpha.common.rule.InternalRule;
import at.ac.tuwien.kr.alpha.grounder.IndexedInstanceStorage;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.RuleGroundingOrder;
import at.ac.tuwien.kr.alpha.grounder.RuleGroundingOrders;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;
import at.ac.tuwien.kr.alpha.grounder.instantiation.AssignmentStatus;
import at.ac.tuwien.kr.alpha.grounder.instantiation.LiteralInstantiationResult;
import at.ac.tuwien.kr.alpha.grounder.instantiation.LiteralInstantiator;
import at.ac.tuwien.kr.alpha.grounder.instantiation.WorkingMemoryBasedInstantiationStrategy;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

/**
 * Evaluates the stratifiable part (if any) of the given program
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
// TODO ideally return "PartiallyEvaluatedProgram" here, grounder can use working memories created here rather than re-initialize everything
// TODO add solved rules to internal program (in extra list)
public class StratifiedEvaluation extends ProgramTransformation<AnalyzedProgram, InternalProgram> {

	private static final Logger LOGGER = LoggerFactory.getLogger(StratifiedEvaluation.class);

	private StratificationHelper stratificationHelper = new StratificationHelper();

	private WorkingMemory workingMemory = new WorkingMemory();
	private Map<Predicate, HashSet<InternalRule>> predicateDefiningRules;

	private Map<Predicate, Set<Instance>> modifiedInLastEvaluationRun = new HashMap<>();

	private Set<Atom> additionalFacts = new HashSet<>();
	private Set<Integer> solvedRuleIds = new HashSet<>();

	private LiteralInstantiator literalInstantiator;

	@Override
	public InternalProgram apply(AnalyzedProgram inputProgram) {
		// Calculate a stratification and initialize working memory.
		ComponentGraph componentGraph = inputProgram.getComponentGraph();
		Map<Integer, List<SCComponent>> strata = stratificationHelper.calculateStratification(componentGraph);
		predicateDefiningRules = inputProgram.getPredicateDefiningRules();
		// set up list of atoms which are known to be true - we expand on this one
		Map<Predicate, Set<Instance>> knownFacts = new LinkedHashMap<>(inputProgram.getFactsByPredicate());
		for (Map.Entry<Predicate, Set<Instance>> entry : knownFacts.entrySet()) {
			workingMemory.initialize(entry.getKey());
			workingMemory.addInstances(entry.getKey(), true, entry.getValue());
		}

		for (InternalRule nonGroundRule : inputProgram.getRulesById().values()) {
			// Create working memories for all predicates occurring in the rule
			for (Predicate predicate : nonGroundRule.getOccurringPredicates()) {
				workingMemory.initialize(predicate);
			}
		}

		workingMemory.reset();

		// Set up literal instantiator.
		literalInstantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(workingMemory));

		// Evaluate the program part covered by the calculated stratification.
		ComponentEvaluationOrder evaluationOrder = new ComponentEvaluationOrder(strata);
		for (SCComponent currComponent : evaluationOrder) {
			evaluateComponent(currComponent);
		}

		// Build the program resulting from evaluating the stratified part.
		List<Atom> outputFacts = buildOutputFacts(inputProgram.getFacts(), additionalFacts);
		List<InternalRule> outputRules = new ArrayList<>();
		inputProgram.getRulesById().entrySet().stream().filter((entry) -> !solvedRuleIds.contains(entry.getKey()))
				.forEach((entry) -> outputRules.add(entry.getValue()));
		return new InternalProgram(outputRules, outputFacts);
	}

	// extra method is better visible in CPU traces when profiling
	private List<Atom> buildOutputFacts(List<Atom> initialFacts, Set<Atom> newFacts) {
		Set<Atom> atomSet = new LinkedHashSet<>(initialFacts);
		atomSet.addAll(newFacts);
		return new ArrayList<>(atomSet);
	}

	private void evaluateComponent(SCComponent comp) {
		LOGGER.debug("Evaluating component {}", comp);
		ComponentEvaluationInfo evaluationInfo = getRulesToEvaluate(comp);
		if (evaluationInfo.isEmpty()) {
			LOGGER.debug("No rules to evaluate for component {}", comp);
			return;
		}
		prepareComponentEvaluation(SetUtils.union(evaluationInfo.nonRecursiveRules, evaluationInfo.recursiveRules));
		// Rules outside of dependency cycles only need to be evaluated once.
		if (!evaluationInfo.nonRecursiveRules.isEmpty()) {
			addFactsToProgram(evaluateRules(evaluationInfo.nonRecursiveRules, true));
		}
		boolean isInitialRun = true;
		if (!evaluationInfo.recursiveRules.isEmpty()) {
			do {
				// Now do the rules that cyclically depend on each other,
				// evaluate these until nothing new can be derived any more.
				addFactsToProgram(evaluateRules(evaluationInfo.recursiveRules, isInitialRun));
				isInitialRun = false;
				// If evaluation of rules doesn't modify the working memory we have a fixed point.
			} while (!workingMemory.modified().isEmpty());
		}
		LOGGER.debug("Evaluation done - reached a fixed point on component {}", comp);
		SetUtils.union(evaluationInfo.nonRecursiveRules, evaluationInfo.recursiveRules)
				.forEach((rule) -> solvedRuleIds.add(rule.getRuleId()));
	}

	private Map<Predicate, List<Instance>> evaluateRules(Set<InternalRule> rules, boolean isInitialRun) {
		Map<Predicate, List<Instance>> addedInstances = new HashMap<>();
		workingMemory.reset();
		LOGGER.debug("Starting component evaluation run...");
		for (InternalRule r : rules) {
			evaluateRule(r, !isInitialRun);
		}
		modifiedInLastEvaluationRun = new HashMap<>();
		// Since we're stratified we never have to backtrack, therefore just collect the added instances.
		for (IndexedInstanceStorage instanceStorage : workingMemory.modified()) {
			// NOTE: We're only dealing with positive instances.
			addedInstances.putIfAbsent(instanceStorage.getPredicate(), new ArrayList<>());
			addedInstances.get(instanceStorage.getPredicate()).addAll(instanceStorage.getRecentlyAddedInstances());
			modifiedInLastEvaluationRun.putIfAbsent(instanceStorage.getPredicate(), new LinkedHashSet<>());
			modifiedInLastEvaluationRun.get(instanceStorage.getPredicate()).addAll(instanceStorage.getRecentlyAddedInstances());
			instanceStorage.markRecentlyAddedInstancesDone();
		}
		return addedInstances;
	}

	/**
	 * To be called at the start of evaluateComponent. Adds all known instances of the predicates occurring in the given set
	 * of rules to the "modifiedInLastEvaluationRun" map in order to "bootstrap" incremental grounding, i.e. making sure
	 * that those instances are taken into account for ground substitutions by evaluateRule.
	 */
	private void prepareComponentEvaluation(Set<InternalRule> rulesToEvaluate) {
		modifiedInLastEvaluationRun = new HashMap<>();
		Predicate tmpPredicate;
		IndexedInstanceStorage tmpInstances;
		for (InternalRule rule : rulesToEvaluate) {
			// register rule head instances
			tmpPredicate = rule.getHeadAtom().getPredicate();
			tmpInstances = workingMemory.get(tmpPredicate, true);
			modifiedInLastEvaluationRun.putIfAbsent(tmpPredicate, new LinkedHashSet<>());
			if (tmpInstances != null) {
				modifiedInLastEvaluationRun.get(tmpPredicate).addAll(tmpInstances.getAllInstances());
			}
			// register positive body instances
			for (Literal lit : rule.getPositiveBody()) {
				tmpPredicate = lit.getPredicate();
				tmpInstances = workingMemory.get(tmpPredicate, true);
				modifiedInLastEvaluationRun.putIfAbsent(tmpPredicate, new LinkedHashSet<>());
				if (tmpInstances != null) {
					modifiedInLastEvaluationRun.get(tmpPredicate).addAll(tmpInstances.getAllInstances());
				}
			}
		}
	}

	private void evaluateRule(InternalRule rule, boolean checkAllStartingLiterals) {
		LOGGER.debug("Evaluating rule {}", rule);
		List<Substitution> satisfyingSubstitutions = calculateSatisfyingSubstitutionsForRule(rule, checkAllStartingLiterals);
		for (Substitution subst : satisfyingSubstitutions) {
			fireRule(rule, subst);
		}
	}

	private List<Substitution> calculateSatisfyingSubstitutionsForRule(InternalRule rule, boolean checkAllStartingLiterals) {
		LOGGER.debug("Grounding rule {}", rule);
		RuleGroundingOrders groundingOrders = rule.getGroundingOrders();
		List<Substitution> groundSubstitutions = new ArrayList<>(); // the actual full ground substitutions for the rule
		LOGGER.debug("Is fixed rule? {}", rule.getGroundingOrders().fixedInstantiation());
		if (groundingOrders.fixedInstantiation()) { 
			// Note: Representation of fixed grounding orders should be refactored in RuleGroundingOrders.
			RuleGroundingOrder fixedGroundingOrder = groundingOrders.getFixedGroundingOrder();
			groundSubstitutions.addAll(calcSubstitutionsWithGroundingOrder(fixedGroundingOrder,
				Collections.singletonList(new Substitution())));
		} else {
			List<Literal> startingLiterals = groundingOrders.getStartingLiterals();
			List<Substitution> substitutionsForStartingLiteral;
			if (!checkAllStartingLiterals) {
				// If we don't have to check all literals, i.e. we're in the first evaluation run, just use the first one
				Literal lit = startingLiterals.get(0);
				substitutionsForStartingLiteral = calcSubstitutionsWithGroundingOrder(groundingOrders.orderStartingFrom(lit),
					substituteFromRecentlyAddedInstances(lit));
				groundSubstitutions.addAll(substitutionsForStartingLiteral);
			} else {
				for (Literal lit : startingLiterals) {
					substitutionsForStartingLiteral = calcSubstitutionsWithGroundingOrder(groundingOrders.orderStartingFrom(lit),
						substituteFromRecentlyAddedInstances(lit));
					groundSubstitutions.addAll(substitutionsForStartingLiteral);
				}
			}
		}
		return groundSubstitutions;
	}

	/**
	 * Use this to find initial substitutions for a starting literal when grounding a rule.
	 * In order to avoid finding the same ground instantiations of rules again, only look at
	 * <code>modifiedInLastEvaluationRun</code> to obtain instances.
	 * 
	 * @param lit the literal to substitute
	 * @return valid ground substitutions for the literal based on the recently added instances (i.e. instances derived in
	 *         the last evaluation run)
	 */
	private List<Substitution> substituteFromRecentlyAddedInstances(Literal lit) {
		List<Substitution> retVal = new ArrayList<>();
		Set<Instance> instances = modifiedInLastEvaluationRun.get(lit.getPredicate());
		if (instances == null) {
			return Collections.emptyList();
		}
		Substitution initialSubstitutionForCurrentInstance;
		for (Instance instance : instances) {
			initialSubstitutionForCurrentInstance = Substitution.unify(lit, instance, new Substitution());
			if (initialSubstitutionForCurrentInstance != null) {
				retVal.add(initialSubstitutionForCurrentInstance);
			}
		}
		return retVal;
	}

	private List<Substitution> calcSubstitutionsWithGroundingOrderOld(RuleGroundingOrder groundingOrder, List<Substitution> startingSubstitutions) {
		// Iterate through the grounding order starting at index startFromOrderPosition.
		// Whenever instantiation of a Literal with a given substitution causes
		// a result with a type other than CONTINUE, discard that substitution.
		List<Substitution> currentSubstitutions = startingSubstitutions;
		List<Substitution> updatedSubstitutions = new ArrayList<>();
		Literal currentLiteral;
		LiteralInstantiationResult currentLiteralResult;
		int curentOrderPosition = 0;
		while ((currentLiteral = groundingOrder.getLiteralAtOrderPosition(curentOrderPosition)) != null) {
			for (Substitution subst : currentSubstitutions) {
				currentLiteralResult = literalInstantiator.instantiateLiteral(currentLiteral, subst);
				if (currentLiteralResult.getType() == LiteralInstantiationResult.Type.CONTINUE) {
					for (ImmutablePair<Substitution, AssignmentStatus> pair : currentLiteralResult.getSubstitutions()) {
						updatedSubstitutions.add(pair.left);
					}
				}
			}
			if (updatedSubstitutions.isEmpty()) {
				// In this case it doesn't make any sense to advance further in the grounding order.
				return Collections.emptyList();
			}
			currentSubstitutions = updatedSubstitutions;
			updatedSubstitutions = new ArrayList<>();
			curentOrderPosition++;
		}
		return currentSubstitutions;
	}

	private List<Substitution> calcSubstitutionsWithGroundingOrder(RuleGroundingOrder groundingOrder, List<Substitution> startingSubstitutions) {
		// Iterate through the grounding order starting at index startFromOrderPosition.
		// Whenever instantiation of a Literal with a given substitution causes
		// a result with a type other than CONTINUE, discard that substitution.

		List<Substitution> fullSubstitutions = new ArrayList<>();
		Stack<ArrayList<Substitution>> substitutionStack = new Stack<>();	// For speed, we really want ArrayLists on the stack.
		if (startingSubstitutions instanceof ArrayList) {
			substitutionStack.push((ArrayList<Substitution>) startingSubstitutions);
		} else {
			substitutionStack.push(new ArrayList<>(startingSubstitutions));	// Copy startingSubstitutions into ArrayList. Note: mostly happens for empty or singleton lists.
		}
		int currentOrderPosition = 0;
		while (!substitutionStack.isEmpty()) {
			List<Substitution> currentSubstitutions = substitutionStack.peek();
			// If no more substitutions remain at current position, all have been processed, continue on next lower level.
			if (currentSubstitutions.isEmpty()) {
				substitutionStack.pop();
				currentOrderPosition--;
				continue;
			}
			// In case the full grounding order has been worked on, all current substitutions are full substitutions, add them to result.
			Literal currentLiteral = groundingOrder.getLiteralAtOrderPosition(currentOrderPosition);
			if (currentLiteral == null) {
				fullSubstitutions.addAll(currentSubstitutions);
				currentSubstitutions.clear();
				// Continue on next lower level.
				substitutionStack.pop();
				currentOrderPosition--;
				continue;
			}
			// Take one substitution from the top-list of the stack and try extending it.
			Substitution currentSubstitution = currentSubstitutions.remove(currentSubstitutions.size() - 1);        // Work on last element (removing last element is O(1) for ArrayList).
			LiteralInstantiationResult currentLiteralResult = literalInstantiator.instantiateLiteral(currentLiteral, currentSubstitution);
			if (currentLiteralResult.getType() == LiteralInstantiationResult.Type.CONTINUE) {
				// The currentSubstitution could be extended, push the extensions on the stack and continue working on them.
				ArrayList<Substitution> furtheredSubstitutions = new ArrayList<>();
				for (ImmutablePair<Substitution, AssignmentStatus> resultSubstitution : currentLiteralResult.getSubstitutions()) {
					furtheredSubstitutions.add(resultSubstitution.left);
				}
				substitutionStack.push(furtheredSubstitutions);
				// Continue work on the higher level.
				currentOrderPosition++;
			}
		}
		return fullSubstitutions;
	}

	private void fireRule(InternalRule rule, Substitution substitution) {
		Atom newAtom = rule.getHeadAtom().substitute(substitution);
		if (!newAtom.isGround()) {
			throw new IllegalStateException("Trying to fire rule " + rule.toString() + " with incompatible substitution " + substitution.toString());
		}
		LOGGER.debug("Firing rule - got head atom: {}", newAtom);
		workingMemory.addInstance(newAtom, true);
	}

	private ComponentEvaluationInfo getRulesToEvaluate(SCComponent comp) {
		Set<InternalRule> nonRecursiveRules = new HashSet<>();
		Set<InternalRule> recursiveRules = new HashSet<>();
		HashSet<InternalRule> definingRules;
		Set<Predicate> headPredicates = new HashSet<>();
		for (Node node : comp.getNodes()) {
			headPredicates.add(node.getPredicate());
		}
		for (Predicate headPredicate : headPredicates) {
			definingRules = predicateDefiningRules.get(headPredicate);
			if (definingRules == null) {
				// predicate only occurs in facts
				continue;
			}
			for (InternalRule rule : definingRules) {
				for (Literal lit : rule.getPositiveBody()) {
					if (headPredicates.contains(lit.getPredicate())) {
						// rule body contains a predicate that is defined in the same component,
						// rule is therefore part of a dependency chain within this component and must be evaluated repeatedly
						recursiveRules.add(rule);
					} else {
						nonRecursiveRules.add(rule);
					}
				}
			}
		}
		return new ComponentEvaluationInfo(nonRecursiveRules, recursiveRules);
	}

	private void addFactsToProgram(Map<Predicate, List<Instance>> instances) {
		for (Entry<Predicate, List<Instance>> entry : instances.entrySet()) {
			for (Instance inst : entry.getValue()) {
				additionalFacts.add(new BasicAtom(entry.getKey(), inst.terms));
			}
		}
	}

	private class ComponentEvaluationOrder implements Iterable<SCComponent> {

		private Iterator<Entry<Integer, List<SCComponent>>> strataIterator;
		private Iterator<SCComponent> componentIterator;

		private ComponentEvaluationOrder(Map<Integer, List<SCComponent>> stratification) {
			strataIterator = stratification.entrySet().iterator();
			startNextStratum();
		}

		private boolean startNextStratum() {
			if (!strataIterator.hasNext()) {
				return false;
			}
			componentIterator = strataIterator.next().getValue().iterator();
			return true;
		}

		@Override
		public Iterator<SCComponent> iterator() {
			return new Iterator<SCComponent>() {

				@Override
				public boolean hasNext() {
					if (componentIterator == null) {
						// can happen when there are actually no components, as is the case for empty programs or programs just consisting of
						// facts
						return false;
					}
					if (componentIterator.hasNext()) {
						return true;
					} else {
						if (!startNextStratum()) {
							return false;
						} else {
							return hasNext();
						}
					}
				}

				@Override
				public SCComponent next() {
					return componentIterator.next();
				}

			};
		}
	}

	/**
	 * Internal helper class to group rules within an {@SCComponent} into rules that are recursive, i.e. part of some cyclic
	 * dependency chain within that component, and non-recursive rules, i.e. rules where all body predicates occur in lower
	 * strata. The reason for this grouping is that, when evaluating rules within a component, non-recursive rules only need
	 * to be evaluated once, while recursive rules need to be evaluated until a fixed-point has been reached.
	 * 
	 * Copyright (c) 2020, the Alpha Team.
	 */
	private class ComponentEvaluationInfo {
		final Set<InternalRule> nonRecursiveRules;
		final Set<InternalRule> recursiveRules;

		ComponentEvaluationInfo(Set<InternalRule> nonRecursive, Set<InternalRule> recursive) {
			nonRecursiveRules = Collections.unmodifiableSet(nonRecursive);
			recursiveRules = Collections.unmodifiableSet(recursive);
		}

		boolean isEmpty() {
			return nonRecursiveRules.isEmpty() && recursiveRules.isEmpty();
		}

	}

}
