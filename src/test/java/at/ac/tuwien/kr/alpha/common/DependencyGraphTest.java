package at.ac.tuwien.kr.alpha.common;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.CharStreams;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.common.depgraph.DependencyGraph;
import at.ac.tuwien.kr.alpha.common.depgraph.Edge;
import at.ac.tuwien.kr.alpha.common.depgraph.Node;
import at.ac.tuwien.kr.alpha.common.depgraph.io.DependencyGraphWriter;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.structure.ProgramAnalysis;

public class DependencyGraphTest {

	@Test
	@Ignore("Not a real test, rather a playground for local testing while changing stuff")
	public void dependencyGraphSmokeTest() throws IOException {
		InputStream is = DependencyGraphTest.class.getResourceAsStream("/map_austria.asp");
		Program p = new ProgramParser().parse(CharStreams.fromStream(is));
		ProgramAnalysis pa = new ProgramAnalysis(p);
		DependencyGraph dg = DependencyGraph.buildDependencyGraph(pa.getNonGroundRules());
		DependencyGraphWriter dgw = new DependencyGraphWriter();
		dgw.writeAsDotfile(dg, "/tmp/map_austria.asp.dg.dot");
	}

	@Test
	public void edgesEqualTest() {
		Predicate testPredicate;
		testPredicate = new Predicate("test", 2, false, false);
		DependencyGraph dg = new DependencyGraph();
		Edge e1 = new Edge(new Node(testPredicate, testPredicate.toString()), true, null);
		Edge e2 = new Edge(new Node(testPredicate, testPredicate.toString()), true, null);
		Assert.assertEquals(e1, e2);
	}

}
