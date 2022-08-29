package at.ac.tuwien.kr.alpha.core.programs.transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.analysis.ComponentGraph;
import at.ac.tuwien.kr.alpha.api.programs.analysis.DependencyGraph;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.RuleInstantiator;
import at.ac.tuwien.kr.alpha.api.rules.heads.ActionHead;
import at.ac.tuwien.kr.alpha.api.rules.heads.InstantiableHead;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.substitutions.BasicSubstitution;
import at.ac.tuwien.kr.alpha.commons.substitutions.Instance;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.actions.ActionContext;
import at.ac.tuwien.kr.alpha.core.actions.ActionWitness;
import at.ac.tuwien.kr.alpha.core.depgraph.StratificationAlgorithm;
import at.ac.tuwien.kr.alpha.core.grounder.IndexedInstanceStorage;
import at.ac.tuwien.kr.alpha.core.grounder.RuleGroundingInfo;
import at.ac.tuwien.kr.alpha.core.grounder.RuleGroundingOrder;
import at.ac.tuwien.kr.alpha.core.grounder.WorkingMemory;
import at.ac.tuwien.kr.alpha.core.grounder.instantiation.AssignmentStatus;
import at.ac.tuwien.kr.alpha.core.grounder.instantiation.LiteralInstantiationResult;
import at.ac.tuwien.kr.alpha.core.grounder.instantiation.LiteralInstantiator;
import at.ac.tuwien.kr.alpha.core.grounder.instantiation.WorkingMemoryBasedInstantiationStrategy;
import at.ac.tuwien.kr.alpha.core.programs.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.core.programs.InternalProgram;
import at.ac.tuwien.kr.alpha.core.rules.CompiledRule;

/**
 * Evaluates the stratifiable part of a given (analyzed) ASP program.
 * 
 * Copyright (c) 2019-2020, the Alpha Team.
 */
public class StratifiedEvaluation extends ProgramTransformation<AnalyzedProgram, InternalProgram> implements RuleInstantiator {

	private static final Logger LOGGER = LoggerFactory.getLogger(StratifiedEvaluation.class);

	private WorkingMemory workingMemory = new WorkingMemory();
	private Map<Predicate, LinkedHashSet<CompiledRule>> predicateDefiningRules;

	private Map<Predicate, Set<Instance>> modifiedInLastEvaluationRun = new HashMap<>();

	private Set<Atom> outputFacts = new HashSet<>(); // The additional facts derived by stratified evaluation. Note that it may contain duplicates.
	private Set<Integer> solvedRuleIds = new HashSet<>(); // Set of rules that have been completely evaluated.

	private LiteralInstantiator literalInstantiator;
	private ActionContext actionContext;
	private final boolean generateActionWitnesses;

	public StratifiedEvaluation(ActionContext actionContext, boolean generateActionWitnesses) {
		this.actionContext = actionContext;
		this.generateActionWitnesses = generateActionWitnesses;
	}

	@Override
	// Note: ideally this returns a "PartiallyEvaluatedProgram" such that the grounder can directly use the working
	// memories created here rather than re-initialize everything.
	public InternalProgram apply(AnalyzedProgram inputProgram) {
		// Calculate a stratification and initialize the working memory.
		ComponentGraph componentGraph = inputProgram.getComponentGraph();
		List<ComponentGraph.SCComponent> strata = StratificationAlgorithm.calculateStratification(componentGraph);
		predicateDefiningRules = inputProgram.getPredicateDefiningRules();

		// Set up list of atoms which are known to be true - these will be expand by the evaluation.
		Map<Predicate, Set<Instance>> knownFacts = new LinkedHashMap<>(inputProgram.getFactsByPredicate());
		for (Map.Entry<Predicate, Set<Instance>> entry : knownFacts.entrySet()) {
			workingMemory.initialize(entry.getKey());
			workingMemory.addInstances(entry.getKey(), true, entry.getValue());
		}

		// Create working memories for all predicates occurring in each rule.
		for (CompiledRule nonGroundRule : inputProgram.getRulesById().values()) {
			for (Predicate predicate : nonGroundRule.getOccurringPredicates()) {
				workingMemory.initialize(predicate);
			}
		}

		workingMemory.reset();

		// Set up set of facts to which we'll add everything derived during stratified evaluation.
		outputFacts = new HashSet<>(inputProgram.getFacts());

		// Set up literal instantiator.
		literalInstantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(workingMemory));

		// Evaluate the program part covered by the calculated stratification.
		for (ComponentGraph.SCComponent currComponent : strata) {
			evaluateComponent(currComponent);
		}

		// Build the program resulting from evaluating the stratified part.
		List<CompiledRule> outputRules = new ArrayList<>();
		inputProgram.getRulesById().entrySet().stream().filter((entry) -> !solvedRuleIds.contains(entry.getKey()))
				.forEach((entry) -> outputRules.add(entry.getValue()));

		// NOTE: if InternalProgram requires solved rules, they should be added here.
		return new InternalProgram(outputRules, new ArrayList<>(outputFacts));
	}

	private void evaluateComponent(ComponentGraph.SCComponent comp) {
		LOGGER.debug("Evaluating component {}", comp);
		ComponentEvaluationInfo evaluationInfo = getRulesToEvaluate(comp);
		if (evaluationInfo.isEmpty()) {
			LOGGER.debug("No rules to evaluate for component {}", comp);
			return;
		}

		// Rules outside of dependency cycles only need to be evaluated once.
		if (!evaluationInfo.nonRecursiveRules.isEmpty()) {
			prepareInitialEvaluation(evaluationInfo.nonRecursiveRules);
			evaluateRules(evaluationInfo.nonRecursiveRules, true);
			for (IndexedInstanceStorage instanceStorage : workingMemory.modified()) {
				// Directly record all newly derived instances as additional facts.
				recordRecentlyAddedInstances(instanceStorage);
				instanceStorage.markRecentlyAddedInstancesDone();
			}
		}
		boolean isInitialRun = true;
		if (!evaluationInfo.recursiveRules.isEmpty()) {
			do {
				// Now do the rules that cyclically depend on each other,
				// evaluate these until nothing new can be derived any more.
				if (isInitialRun) {
					prepareInitialEvaluation(evaluationInfo.recursiveRules);
				}
				evaluateRules(evaluationInfo.recursiveRules, isInitialRun);
				isInitialRun = false;
				modifiedInLastEvaluationRun = new HashMap<>();
				// Since we are stratified we never have to backtrack, therefore just collect the added instances.
				for (IndexedInstanceStorage instanceStorage : workingMemory.modified()) {
					// Directly record all newly derived instances as additional facts.
					recordRecentlyAddedInstances(instanceStorage);
					modifiedInLastEvaluationRun.putIfAbsent(instanceStorage.getPredicate(), new LinkedHashSet<>());
					modifiedInLastEvaluationRun.get(instanceStorage.getPredicate()).addAll(instanceStorage.getRecentlyAddedInstances());
					instanceStorage.markRecentlyAddedInstancesDone();
				}
				// If the evaluation of rules did not modify the working memory we have a fixed-point.
			} while (!workingMemory.modified().isEmpty());
		}
		LOGGER.debug("Evaluation done - reached a fixed point on component {}", comp);
		SetUtils.union(evaluationInfo.nonRecursiveRules, evaluationInfo.recursiveRules)
				.forEach((rule) -> solvedRuleIds.add(rule.getRuleId()));
	}

	private void recordRecentlyAddedInstances(IndexedInstanceStorage instanceStorage) {
		for (Instance recentlyAddedInstance : instanceStorage.getRecentlyAddedInstances()) {
			outputFacts.add(Atoms.newBasicAtom(instanceStorage.getPredicate(), recentlyAddedInstance.terms));
		}
	}

	private void evaluateRules(Set<CompiledRule> rules, boolean isInitialRun) {
		workingMemory.reset();
		LOGGER.debug("Starting component evaluation run...");
		for (CompiledRule r : rules) {
			evaluateRule(r, !isInitialRun);
		}
	}

	/**
	 * To be called at the start of evaluateComponent. Adds all known instances of the predicates occurring in the given set
	 * of rules to the "modifiedInLastEvaluationRun" map in order to "bootstrap" incremental grounding, i.e. making sure
	 * that those instances are taken into account for ground substitutions by evaluateRule.
	 */
	private void prepareInitialEvaluation(Set<CompiledRule> rulesToEvaluate) {
		modifiedInLastEvaluationRun = new HashMap<>();
		for (CompiledRule rule : rulesToEvaluate) {
			// Register rule head instances.
			Predicate headPredicate = rule.getHeadAtom().getPredicate();
			IndexedInstanceStorage headInstances = workingMemory.get(headPredicate, true);
			modifiedInLastEvaluationRun.putIfAbsent(headPredicate, new LinkedHashSet<>());
			if (headInstances != null) {
				modifiedInLastEvaluationRun.get(headPredicate).addAll(headInstances.getAllInstances());
			}
			// Register positive body literal instances.
			for (Literal lit : rule.getPositiveBody()) {
				Predicate bodyPredicate = lit.getPredicate();
				IndexedInstanceStorage bodyInstances = workingMemory.get(bodyPredicate, true);
				modifiedInLastEvaluationRun.putIfAbsent(bodyPredicate, new LinkedHashSet<>());
				if (bodyInstances != null) {
					modifiedInLastEvaluationRun.get(bodyPredicate).addAll(bodyInstances.getAllInstances());
				}
			}
		}
	}

	private void evaluateRule(CompiledRule rule, boolean checkAllStartingLiterals) {
		LOGGER.debug("Evaluating rule {}", rule);
		List<Substitution> satisfyingSubstitutions = calculateSatisfyingSubstitutionsForRule(rule, checkAllStartingLiterals);
		for (Substitution subst : satisfyingSubstitutions) {
			fireRule(rule, subst);
		}
	}

	private List<Substitution> calculateSatisfyingSubstitutionsForRule(CompiledRule rule, boolean checkAllStartingLiterals) {
		LOGGER.debug("Grounding rule {}", rule);
		RuleGroundingInfo groundingOrders = rule.getGroundingInfo();

		// Treat rules with fixed instantiation first.
		LOGGER.debug("Is fixed rule? {}", rule.getGroundingInfo().hasFixedInstantiation());
		if (groundingOrders.hasFixedInstantiation()) {
			RuleGroundingOrder fixedGroundingOrder = groundingOrders.getFixedGroundingOrder();
			return calcSubstitutionsWithGroundingOrder(fixedGroundingOrder, Collections.singletonList(new BasicSubstitution()));
		}

		List<Literal> startingLiterals = groundingOrders.getStartingLiterals();
		// Check only one starting literal if indicated by the parameter.
		if (!checkAllStartingLiterals) {
			// If this is the first evaluation run, it suffices to start from the first starting literal only.
			Literal lit = startingLiterals.get(0);
			return calcSubstitutionsWithGroundingOrder(groundingOrders.orderStartingFrom(lit), substituteFromRecentlyAddedInstances(lit));
		}

		// Ground from all starting literals.
		List<Substitution> groundSubstitutions = new ArrayList<>(); // Collection of full ground substitutions for the given rule.
		for (Literal lit : startingLiterals) {
			List<Substitution> substitutionsForStartingLiteral = calcSubstitutionsWithGroundingOrder(groundingOrders.orderStartingFrom(lit),
					substituteFromRecentlyAddedInstances(lit));
			groundSubstitutions.addAll(substitutionsForStartingLiteral);
		}
		return groundSubstitutions;
	}

	/**
	 * Use this to find initial substitutions for a starting literal when grounding a rule.
	 * In order to avoid finding the same ground instantiations of rules again, only look at
	 * <code>modifiedInLastEvaluationRun</code> to obtain instances.
	 * 
	 * @param lit the literal to substitute.
	 * @return valid ground substitutions for the literal based on the recently added instances (i.e. instances derived in
	 *         the last evaluation run).
	 */
	private List<Substitution> substituteFromRecentlyAddedInstances(Literal lit) {
		List<Substitution> retVal = new ArrayList<>();
		Set<Instance> instances = modifiedInLastEvaluationRun.get(lit.getPredicate());
		if (instances == null) {
			return Collections.emptyList();
		}
		for (Instance instance : instances) {
			Substitution unifyingSubstitution = BasicSubstitution.specializeSubstitution(lit, instance, BasicSubstitution.EMPTY_SUBSTITUTION);
			if (unifyingSubstitution != null) {
				retVal.add(unifyingSubstitution);
			}
		}
		return retVal;
	}

	private List<Substitution> calcSubstitutionsWithGroundingOrder(RuleGroundingOrder groundingOrder, List<Substitution> startingSubstitutions) {
		// Iterate through the grounding order and whenever instantiation of a Literal with a given substitution
		// causes a result with a type other than CONTINUE, discard that substitution.

		// Note that this function uses a stack of partial substitutions to simulate a recursive function.
		Stack<ArrayList<Substitution>> substitutionStack = new Stack<>(); // For speed, we really want ArrayLists on the stack.
		if (startingSubstitutions instanceof ArrayList) {
			substitutionStack.push((ArrayList<Substitution>) startingSubstitutions);
		} else {
			substitutionStack.push(new ArrayList<>(startingSubstitutions)); // Copy startingSubstitutions into ArrayList. Note: mostly happens for empty or
																			// singleton lists.
		}
		int currentOrderPosition = 0;
		List<Substitution> fullSubstitutions = new ArrayList<>();
		while (!substitutionStack.isEmpty()) {
			List<Substitution> currentSubstitutions = substitutionStack.peek();
			// If no more substitutions remain at current position, all have been processed, continue on next lower level.
			if (currentSubstitutions.isEmpty()) {
				substitutionStack.pop();
				currentOrderPosition--;
				continue;
			}
			// In case the full grounding order has been worked on, all current substitutions are full substitutions, add them to
			// result.
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
			Substitution currentSubstitution = currentSubstitutions.remove(currentSubstitutions.size() - 1); // Work on last element (removing last element is
																												// O(1) for ArrayList).
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

	private void fireRule(CompiledRule rule, Substitution substitution) {
		// BasicAtom newAtom = this.instantiate(rule.getHead(), substitution);
		BasicAtom newAtom;
		if (rule.getHead() instanceof ActionHead) {
			newAtom = instantiateActionHead((ActionHead) rule.getHead(), substitution, rule);
		} else {
			newAtom = instantiateNormalHead(rule.getHead(), substitution);
		}
		if (!newAtom.isGround()) {
			throw new IllegalStateException("Trying to fire rule " + rule.toString() + " with incompatible substitution " + substitution.toString());
		}
		LOGGER.debug("Firing rule - got head atom: {}", newAtom);
		workingMemory.addInstance(newAtom, true);
	}

	@Override
	public BasicAtom instantiate(InstantiableHead ruleHead, Substitution substitution) {
		return ruleHead.instantiate(this, substitution);
	}

	// FIXME should be dispatched via visitor pattern
	public BasicAtom instantiateNormalHead(NormalHead head, Substitution substitution) {
		return head.getAtom().substitute(substitution);
	}

	// FIXME should be dispatched via visitor pattern
	public BasicAtom instantiateActionHead(ActionHead head, Substitution substitution, CompiledRule rule) {
		List<Term> actionInput = head.getActionInputTerms();
		List<Term> substitutedInput = new ArrayList<>();
		// Substitute all variables in action input so that all input terms are ground.
		for (Term inputTerm : actionInput) {
			substitutedInput.add(inputTerm.substitute(substitution));
		}
		// Delegate action execution to respective backend.
		ActionWitness witness = actionContext.execute(head.getActionName(), rule.getRuleId(), substitution, substitutedInput);
		// If the according debug flag is set, convert witness to atom and add to facts.
		if (generateActionWitnesses) {
			BasicAtom witnessAtom = buildActionWitnessAtom(witness, rule);
			// Note that this is a rather "sneaky" side-effect,
			// but seems like overkill to do this structurally proper just for a debug feature.
			workingMemory.addInstance(witnessAtom, true);
		}
		// We have an action result. Add it to the substitution as the substitute for the variable bound to the action so we're able to obtain the
		// ground BasicAtom derived by the rule
		substitution.put(head.getActionOutputTerm(), witness.getActionResult());
		return head.getAtom().substitute(substitution);
	}

	private BasicAtom buildActionWitnessAtom(ActionWitness witness, CompiledRule rule) {
		// Note that this methods should only ever be used for debugging!
		// While action witnesses are used as a semantic concept in the evolog specification,
		// they normally only exist implicitly.

		// Construct state term: create function terms from ground body literals.
		List<Term> functionalizedBody = new ArrayList<>();
		for (Literal lit : rule.getBody()) {
			Literal groundLit = lit.substitute(witness.getGroundSubstitution());
			FunctionTerm functionalizedLiteral = Terms.newFunctionTerm(groundLit.getPredicate().getName(), groundLit.getTerms());
			functionalizedBody.add(functionalizedLiteral);
		}
		FunctionTerm stateTerm = Terms.newFunctionTerm("state", functionalizedBody);

		// Construct input term: wrap action input terms into one function term.
		FunctionTerm inputTerm = Terms.newFunctionTerm("input", witness.getActionInput());

		// Return witness atom: put state and input terms together.
		return Atoms.newBasicAtom(Predicates.getPredicate("action_witness", 4),
				Terms.newConstant(witness.getActionName()),
				stateTerm,
				inputTerm,
				witness.getActionResult());
	}

	private ComponentEvaluationInfo getRulesToEvaluate(ComponentGraph.SCComponent comp) {
		Set<CompiledRule> nonRecursiveRules = new HashSet<>();
		Set<CompiledRule> recursiveRules = new HashSet<>();

		// Collect all predicates occurring in heads of rules of the given component.
		Set<Predicate> headPredicates = new HashSet<>();
		for (DependencyGraph.Node node : comp.getNodes()) {
			headPredicates.add(node.getPredicate());
		}
		// Check each predicate whether its defining rules depend on some of the head predicates, i.e., whether there is a
		// cycle.
		for (Predicate headPredicate : headPredicates) {
			HashSet<CompiledRule> definingRules = predicateDefiningRules.get(headPredicate);
			if (definingRules == null) {
				// Predicate only occurs in facts, skip.
				continue;
			}
			// Note: here we assume that all rules defining a predicate belong to the same SC component.
			for (CompiledRule rule : definingRules) {
				boolean isRuleRecursive = false;
				for (Literal lit : rule.getPositiveBody()) {
					if (headPredicates.contains(lit.getPredicate())) {
						// The rule body contains a predicate that is defined in the same
						// component, the rule is therefore part of a cyclic dependency within
						// this component.
						isRuleRecursive = true;
					}
				}
				if (isRuleRecursive) {
					recursiveRules.add(rule);
				} else {
					nonRecursiveRules.add(rule);
				}
			}
		}
		return new ComponentEvaluationInfo(nonRecursiveRules, recursiveRules);
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
		final Set<CompiledRule> nonRecursiveRules;
		final Set<CompiledRule> recursiveRules;

		ComponentEvaluationInfo(Set<CompiledRule> nonRecursive, Set<CompiledRule> recursive) {
			nonRecursiveRules = Collections.unmodifiableSet(nonRecursive);
			recursiveRules = Collections.unmodifiableSet(recursive);
		}

		boolean isEmpty() {
			return nonRecursiveRules.isEmpty() && recursiveRules.isEmpty();
		}

	}

}
