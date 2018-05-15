package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
		assertEquals(3, aDependsOn.size());
		assertTrue(aDependsOn.containsAll(Arrays.asList(Predicate.getInstance("b", 0), Predicate.getInstance("c", 0))));
		assertTrue(dependencyGraph.getDependencies(Predicate.getInstance("d", 0)).contains(Predicate.getInstance("c", 0)));
	}

	@Test
	public void oneDependency() throws IOException {
		String program = "a(Z) :- b(Z).";
		Program parsedProgram = new ProgramParser(new HashMap<>()).parse(program);
		PredicateDependencyGraph dependencyGraph = PredicateDependencyGraph.buildFromProgram(parsedProgram);

		Set<Predicate> b = new HashSet<>(Arrays.asList(Predicate.getInstance("b", 1)));
		Set<Predicate> ab = new HashSet<>(Arrays.asList(Predicate.getInstance("a", 1), Predicate.getInstance("b", 1)));

		Set<Predicate> aDependsOn = dependencyGraph.getDependencies(Predicate.getInstance("a", 1));
		assertEquals(ab, aDependsOn);

		Set<Predicate> bDependsOn = dependencyGraph.getDependencies(Predicate.getInstance("b", 1));
		assertEquals(b, bDependsOn);
	}

	@Test
	public void stronglyConnectedComponent() throws IOException {
		String program = "p(X) :- p(X), q(X)." +
			"q(X) :- not o(1)." +
			"o(X) :- p(X)." +
			"a(V1,V3) :- b(V3,V1,V2)." +
			"r(Y) :- p(a).";
		Program parsedProgram = new ProgramParser(new HashMap<>()).parse(program);
		PredicateDependencyGraph dependencyGraph = PredicateDependencyGraph.buildFromProgram(parsedProgram);
		Set<Predicate> opq = new HashSet<>(Arrays.asList(
			Predicate.getInstance("p", 1),
			Predicate.getInstance("q", 1),
			Predicate.getInstance("o", 1)));
		Set<Predicate> r = new HashSet<>(opq);
		r.add(Predicate.getInstance("r", 1));
		Set<Predicate> ab = new HashSet<>(Arrays.asList(Predicate.getInstance("a", 2), Predicate.getInstance("b", 3)));
		Set<Predicate> b = new HashSet<>(Arrays.asList(Predicate.getInstance("b", 3)));

		Set<Predicate> aDependsOn = dependencyGraph.getDependencies(Predicate.getInstance("a", 2));
		assertEquals(ab, aDependsOn);
		Set<Predicate> bDependsOn = dependencyGraph.getDependencies(Predicate.getInstance("b", 3));
		assertEquals(b, bDependsOn);
		Set<Predicate> rDependsOn = dependencyGraph.getDependencies(Predicate.getInstance("r", 1));
		assertEquals(r, rDependsOn);
		Set<Predicate> oDependsOn = dependencyGraph.getDependencies(Predicate.getInstance("o", 1));
		assertEquals(opq, oDependsOn);
		Set<Predicate> pDependsOn = dependencyGraph.getDependencies(Predicate.getInstance("p", 1));
		assertEquals(opq, pDependsOn);
		Set<Predicate> qDependsOn = dependencyGraph.getDependencies(Predicate.getInstance("q", 1));
		assertEquals(opq, qDependsOn);

	}

}