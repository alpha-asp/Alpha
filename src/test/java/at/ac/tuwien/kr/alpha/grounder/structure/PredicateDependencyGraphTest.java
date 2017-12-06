package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class PredicateDependencyGraphTest {

	@Test
	public void simpleProgramDependency() throws IOException {
		Program program = new ProgramParser(new HashMap<>()).parse("a :- b. d:- c. b :- c. ");
		PredicateDependencyGraph dependencyGraph = PredicateDependencyGraph.buildFromProgram(program);
		Set<Predicate> aDependsOn = dependencyGraph.getDependencies(Predicate.getInstance("a", 0));
		assertEquals(2, aDependsOn.size());
		assertTrue(aDependsOn.containsAll(Arrays.asList(Predicate.getInstance("b", 0), Predicate.getInstance("c", 0))));
		assertTrue(dependencyGraph.getDependencies(Predicate.getInstance("d", 0)).contains(Predicate.getInstance("c", 0)));
	}

}