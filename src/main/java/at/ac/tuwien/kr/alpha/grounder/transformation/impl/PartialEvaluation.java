package at.ac.tuwien.kr.alpha.grounder.transformation.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;
import at.ac.tuwien.kr.alpha.common.depgraph.StratificationAnalysis;
import at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.transformation.ProgramTransformation;

/**
 * Evaluates the stratifiable part (if any) of the given program
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
// TODO maybe use NormalProgram as input type, could do dependency graph right here
public class PartialEvaluation extends ProgramTransformation<InternalProgram, InternalProgram> {

	@Override
	public InternalProgram apply(InternalProgram inputProgram) {
		// first, we calculate a component graph and stratification
		ComponentGraph componentGraph = ComponentGraph.fromDependencyGraph(inputProgram.getDependencyGraph());
		StratificationAnalysis stratification = componentGraph.calculateStratification();
		// set up list of atoms which are known to be true - we expand on this one
		Map<Predicate, Set<Instance>> knownFacts = new LinkedHashMap<>(inputProgram.getFactsByPredicate());
		List<SCComponent> componentHandlingList = stratification.getComponentHandlingOrder();
		for (SCComponent currComp : componentHandlingList) {
			this.evaluateComponent(currComp, knownFacts);
		}
		return null;
	}

	private void evaluateComponent(SCComponent comp, Map<Predicate, Set<Instance>> factStore) {

	}

}
