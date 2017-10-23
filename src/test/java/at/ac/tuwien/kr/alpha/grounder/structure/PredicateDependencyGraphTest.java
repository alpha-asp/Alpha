package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class PredicateDependencyGraphTest {

	@Test
	public void simpleProgramDependency() throws IOException {
		Program program = parseVisit("a :- b. d:- c. b :- c. ");
		PredicateDependencyGraph dependencyGraph = PredicateDependencyGraph.buildFromProgram(program);
		Set<Predicate> aDependsOn = dependencyGraph.getDependencies(new BasicPredicate("a", 0));
		assertEquals(2, aDependsOn.size());
		assertTrue(aDependsOn.containsAll(Arrays.asList(new BasicPredicate("b",0), new BasicPredicate("c", 0))));
		assertTrue(dependencyGraph.getDependencies(new BasicPredicate("d", 0)).contains(new BasicPredicate("c", 0)));
	}

}