package at.ac.tuwien.kr.alpha.grounder.transformation.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;
import at.ac.tuwien.kr.alpha.common.depgraph.Node;
import at.ac.tuwien.kr.alpha.common.depgraph.StratificationHelper;
import at.ac.tuwien.kr.alpha.common.depgraph.StronglyConnectedComponentsHelper;
import at.ac.tuwien.kr.alpha.common.depgraph.StronglyConnectedComponentsHelper.SCCResult;
import at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram;
import at.ac.tuwien.kr.alpha.common.rule.impl.InternalRule;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.RuleGroundingOrder;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;
import at.ac.tuwien.kr.alpha.grounder.transformation.ProgramTransformation;

/**
 * Evaluates the stratifiable part (if any) of the given program
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
// TODO maybe use NormalProgram as input type, could do dependency graph right here
// TODO ideally return "PartiallyEvaluatedProgram" here, grounder can use working memories created here rather than re-initialize everything
public class PartialEvaluation extends ProgramTransformation<InternalProgram, InternalProgram> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartialEvaluation.class);

	private StronglyConnectedComponentsHelper sccHelper = new StronglyConnectedComponentsHelper();
	private StratificationHelper stratificationHelper = new StratificationHelper();

	private WorkingMemory workingMemory = new WorkingMemory();
	private Map<Predicate, HashSet<InternalRule>> predicateDefiningRules;

	@Override
	public InternalProgram apply(InternalProgram inputProgram) {
		// first, we calculate a component graph and stratification
		SCCResult sccResult = this.sccHelper.findStronglyConnectedComponents(inputProgram.getDependencyGraph());
		ComponentGraph componentGraph = ComponentGraph.buildComponentGraph(inputProgram.getDependencyGraph(), sccResult);
		Map<Integer, List<SCComponent>> strata = this.stratificationHelper.calculateStratification(componentGraph);
		this.predicateDefiningRules = inputProgram.getPredicateDefiningRules();
		// set up list of atoms which are known to be true - we expand on this one
		Map<Predicate, Set<Instance>> knownFacts = new LinkedHashMap<>(inputProgram.getFactsByPredicate());
		for (Map.Entry<Predicate, Set<Instance>> entry : knownFacts.entrySet()) {
			this.workingMemory.initialize(entry.getKey());
			this.workingMemory.addInstances(entry.getKey(), true, entry.getValue());
		}

		ComponentEvaluationOrder evaluationOrder = new ComponentEvaluationOrder(strata);
		for (SCComponent currComponent : evaluationOrder) {
			this.evaluateComponent(currComponent);
		}
		return inputProgram;
	}

	private void evaluateComponent(SCComponent comp) {
		LOGGER.debug("Evaluating component {}", comp);
		Set<InternalRule> rulesToEvaluate = this.getRulesToEvaluate(comp);	
		boolean factsAdded;
		// delta = cumulated facts;
		// do while delta changes
		// for each atom : delta:
		// check if is starting literal from rule grounding order
		// if(rule.canFire) { delta.add(ruleHead); cumulFacts.add(ruleHead) }
		// // for canFire copy/modify bindNextAtomInRule from grounder
		// re-init delta from found substitutions

		// potential changes f. bindNextAt...:
		// - check positive atoms really in facts (not in master, but not just call it, grounder my be more optimistic)
		// - check negative body for absence! (doesn't occur in positive facts, works because program is stratified)
		do {
			LOGGER.debug("Starting component evaluation run...");
			factsAdded = false;
			for (InternalRule r : rulesToEvaluate) {
				factsAdded |= this.evaluateRule(r);
			}
		} while (factsAdded); // if size stays the same we got a fixed point

	}

	private Set<InternalRule> getRulesToEvaluate(SCComponent comp) {
		Set<InternalRule> retVal = new HashSet<>();
		HashSet<InternalRule> tmpPredicateRules;
		for (Node node : comp.getNodes()) {
			tmpPredicateRules = this.predicateDefiningRules.get(node.getPredicate());
			if (tmpPredicateRules != null) {
				retVal.addAll(tmpPredicateRules);
			}
		}
		return retVal;
	}

	private boolean evaluateRule(InternalRule rule) {
		LOGGER.debug("Evaluating rule {}", rule);
		RuleGroundingOrder groundingOrder = rule.getGroundingOrder();
		Collection<Literal> startingLiterals = groundingOrder.getStartingLiterals();
		for (Literal lit : startingLiterals) {
			LOGGER.debug("Got starting literal {} from grounding order", lit);
			Set<VariableTerm> bindingVars = lit.getBindingVariables();
			for (VariableTerm term : bindingVars) {
				LOGGER.debug("Got binding var: {}", term);
			}
		}
		return this.workingMemory.modified().isEmpty();
	}

	private class ComponentEvaluationOrder implements Iterable<SCComponent> {

		private Map<Integer, List<SCComponent>> stratification;
		private Iterator<Entry<Integer, List<SCComponent>>> strataIterator;
		private Iterator<SCComponent> componentIterator;

		private ComponentEvaluationOrder(Map<Integer, List<SCComponent>> stratification) {
			this.stratification = stratification;
			this.strataIterator = this.stratification.entrySet().iterator();
			this.startNextStratum();
		}

		private boolean startNextStratum() {
			if (!this.strataIterator.hasNext()) {
				return false;
			}
			this.componentIterator = this.strataIterator.next().getValue().iterator();
			return true;
		}

		@Override
		public Iterator<SCComponent> iterator() {
			return new Iterator<SCComponent>() {

				@Override
				public boolean hasNext() {
					if (ComponentEvaluationOrder.this.componentIterator.hasNext()) {
						return true;
					} else {
						if (!ComponentEvaluationOrder.this.startNextStratum()) {
							return false;
						} else {
							return this.hasNext();
						}
					}
				}

				@Override
				public SCComponent next() {
					return ComponentEvaluationOrder.this.componentIterator.next();
				}

			};
		}
	}

}
