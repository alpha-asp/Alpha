package at.ac.tuwien.kr.alpha.grounder.transformation.impl;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;
import at.ac.tuwien.kr.alpha.common.depgraph.Node;
import at.ac.tuwien.kr.alpha.common.depgraph.StratificationAnalysis;
import at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram;
import at.ac.tuwien.kr.alpha.common.rule.impl.InternalRule;
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

	private WorkingMemory workingMemory = new WorkingMemory();
	
	@Override
	public InternalProgram apply(InternalProgram inputProgram) {
		// first, we calculate a component graph and stratification
		ComponentGraph componentGraph = ComponentGraph.fromDependencyGraph(inputProgram.getDependencyGraph());
		StratificationAnalysis stratification = componentGraph.calculateStratification();
		Map<Predicate, HashSet<InternalRule>> predicateDefiningRules = inputProgram.getPredicateDefiningRules();
		// set up list of atoms which are known to be true - we expand on this one
		Map<Predicate, Set<Instance>> knownFacts = new LinkedHashMap<>(inputProgram.getFactsByPredicate());
		for(Map.Entry<Predicate, Set<Instance>> entry : knownFacts.entrySet()) {
			this.workingMemory.initialize(entry.getKey());
			this.workingMemory.addInstances(entry.getKey(), true, entry.getValue());
		}
		List<SCComponent> componentHandlingList = stratification.getComponentHandlingOrder();
		for (SCComponent currComp : componentHandlingList) {
			this.evaluateComponent(currComp, predicateDefiningRules);
		}
		return null;
	}

	private void evaluateComponent(SCComponent comp, Map<Predicate, HashSet<InternalRule>> predicateDefiningRules) {
		Set<InternalRule> rulesToEvluate = comp.getNodes().stream().map(Node::getPredicate).map((p) -> predicateDefiningRules.getOrDefault(p, new HashSet<>()))
				.reduce(PartialEvaluation::mergeIfNotNull).orElse(new HashSet<>());
		boolean factsAdded;
		// delta = cumulated facts;
		// do while delta changes
		// 		for each atom : delta:
		// 			check if is starting literal from rule grounding order
		// 			if(rule.canFire) { delta.add(ruleHead); cumulFacts.add(ruleHead) }
		//			// for canFire copy/modify bindNextAtomInRule from grounder
		// 		re-init delta from found substitutions
		
		// potential changes f. bindNextAt...:
		// 		- check positive atoms really in facts (not in master, but not just call it, grounder my be more optimistic)
		//		- check negative body for absence! (doesn't occur in positive facts, works because program is stratified)	
		do {
			
			factsAdded = false;
			for (InternalRule r : rulesToEvluate) {
				factsAdded |= this.evaluateRule(r);
			}
		} while (factsAdded); // if size stays the same we got a fixed point
		
	}

	private boolean evaluateRule(InternalRule rule) {
		RuleGroundingOrder groundingOrder = rule.getGroundingOrder();
		// doSomething
		return this.workingMemory.modified().isEmpty(); // TODO return if evaluation added something
	}

	private static <T> HashSet<T> mergeIfNotNull(HashSet<T> a, HashSet<T> b) {
		a.addAll(b);
		return a;
	}

}
