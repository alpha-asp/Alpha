package at.ac.tuwien.kr.alpha.common.depgraph;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.Alpha;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.depgraph.io.DependencyGraphWriter;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.structure.ProgramAnalysis;

public class DependencyGraphTest {

	@SuppressWarnings("unused")
	private static String generateRandomProgram(int numRules, int numPredicates, int maxRuleBodyLiterals) {
		String[] predicates = new String[numPredicates];
		for (int i = 0; i < predicates.length; i++) {
			predicates[i] = "p" + Integer.toString(i + 1);
		}

		StringBuilder prgBuilder = new StringBuilder();
		String tmpAtom;
		int tmpBodyLiterals;
		for (int i = 0; i < numRules; i++) {
			tmpBodyLiterals = 1 + ((int) (Math.random() * maxRuleBodyLiterals));
			tmpAtom = predicates[(int) (Math.random() * predicates.length)];
			prgBuilder.append(tmpAtom).append(" :- ");
			for (int j = 0; j < tmpBodyLiterals; j++) {
				tmpAtom = predicates[(int) (Math.random() * predicates.length)];
				prgBuilder.append(tmpAtom);
				if (j < (tmpBodyLiterals - 1)) {
					prgBuilder.append(", ");
				}
			}
			prgBuilder.append(".");
			prgBuilder.append("\n");
		}
		return prgBuilder.toString();
	}

	@Test
	@Ignore("Not a real test, rather a playground for local testing while changing stuff")
	public void dependencyGraphSmokeTest() throws IOException {
		InputStream is = DependencyGraphTest.class.getResourceAsStream("/map_austria.asp");
		Program p = new ProgramParser().parse(CharStreams.fromStream(is));
		ProgramAnalysis pa = new ProgramAnalysis(p);
		DependencyGraph dg = DependencyGraph.buildDependencyGraph(pa.getNonGroundRules());
		DependencyGraphWriter dgw = new DependencyGraphWriter();
		dgw.writeAsDotfile(dg, "/tmp/map_austria.asp.dg.dot", true);
	}

	@Test
	public void dependencyGraphTransposeSimpleTest() {
		Alpha system = new Alpha();
		Program prog = system.readProgramString("b :- a.", null);
		ProgramAnalysis analysis = new ProgramAnalysis(prog);
		DependencyGraph dg = analysis.getDependencyGraph();
		Map<Node, List<Edge>> dgNodes = dg.getNodes();
		Map<Node, List<Edge>> dgTransposed = dg.getTransposedNodes();

		// first check structure of dependency graph
		Assert.assertEquals(2, dgNodes.size());
		Assert.assertTrue(dgNodes.containsKey(new Node(Predicate.getInstance("b", 0), "")));
		Assert.assertTrue(dgNodes.containsKey(new Node(Predicate.getInstance("a", 0), "")));
		List<Edge> aEdgeList = dgNodes.get(new Node(Predicate.getInstance("a", 0), ""));
		Assert.assertEquals(1, aEdgeList.size());
		Edge aToB = aEdgeList.get(0);
		Assert.assertEquals(Predicate.getInstance("b", 0), aToB.getTarget().getPredicate());
		Assert.assertEquals(0, dgNodes.get(new Node(Predicate.getInstance("b", 0), "")).size());

		// now check the transposed structure
		Assert.assertEquals(2, dgTransposed.size());
		Assert.assertTrue(dgTransposed.containsKey(new Node(Predicate.getInstance("b", 0), "")));
		Assert.assertTrue(dgTransposed.containsKey(new Node(Predicate.getInstance("a", 0), "")));
	}

	@Test
	public void edgesEqualTest() {
		Predicate testPredicate = Predicate.getInstance("test", 2, false, false);
		Edge e1 = new Edge(new Node(testPredicate, testPredicate.toString()), true, null);
		Edge e2 = new Edge(new Node(testPredicate, testPredicate.toString()), true, null);
		Assert.assertEquals(e1, e2);
	}

	@Test
	public void reachabilityCheckSimpleTest() {
		Alpha system = new Alpha();
		Program prog = system.readProgramString("b :- a.", null);
		ProgramAnalysis pa = new ProgramAnalysis(prog);
		DependencyGraph dg = pa.getDependencyGraph();
		Node a = new Node(Predicate.getInstance("a", 0));
		Node b = new Node(Predicate.getInstance("b", 0));
		Node c = new Node(Predicate.getInstance("c", 0));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(a, a, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(b, a, dg));
		Assert.assertEquals(false, DependencyGraphUtils.isReachableFrom(a, b, dg));
		Assert.assertEquals(false, DependencyGraphUtils.isReachableFrom(c, a, dg));
		Assert.assertEquals(false, DependencyGraphUtils.isReachableFrom(c, b, dg));
	}

	@Test
	public void reachabilityCheckWithHopsTest() {
		StringBuilder bld = new StringBuilder();
		bld.append("b :- a.").append("\n");
		bld.append("c :- b.").append("\n");
		bld.append("d :- c.").append("\n");
		Alpha system = new Alpha();
		Program prog = system.readProgramString(bld.toString(), null);
		ProgramAnalysis pa = new ProgramAnalysis(prog);
		DependencyGraph dg = pa.getDependencyGraph();
		Node a = new Node(Predicate.getInstance("a", 0));
		Node b = new Node(Predicate.getInstance("b", 0));
		Node c = new Node(Predicate.getInstance("c", 0));
		Node d = new Node(Predicate.getInstance("d", 0));

		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(d, a, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(c, a, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(b, a, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(a, a, dg));

		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(d, b, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(c, b, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(b, b, dg));

		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(d, c, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(c, c, dg));

		Assert.assertEquals(false, DependencyGraphUtils.isReachableFrom(a, d, dg));
		Assert.assertEquals(false, DependencyGraphUtils.isReachableFrom(a, c, dg));
		Assert.assertEquals(false, DependencyGraphUtils.isReachableFrom(a, b, dg));
	}

	@Test
	public void reachabilityWithCyclesTest() {
		StringBuilder bld = new StringBuilder();
		bld.append("b :- a, f1.").append("\n");
		bld.append("c :- b.").append("\n");
		bld.append("d :- c.").append("\n");
		bld.append("a :- d.").append("\n");
		bld.append("x :- d, f1.");
		Alpha system = new Alpha();
		Program prog = system.readProgramString(bld.toString(), null);
		ProgramAnalysis pa = new ProgramAnalysis(prog);
		DependencyGraph dg = pa.getDependencyGraph();
		Node a = new Node(Predicate.getInstance("a", 0));
		Node b = new Node(Predicate.getInstance("b", 0));
		Node c = new Node(Predicate.getInstance("c", 0));
		Node d = new Node(Predicate.getInstance("d", 0));
		Node f1 = new Node(Predicate.getInstance("f1", 0));
		Node x = new Node(Predicate.getInstance("x", 0));
		Node notInGraph = new Node(Predicate.getInstance("notInGraph", 0));

		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(d, a, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(c, a, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(b, a, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(a, a, dg));

		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(d, b, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(c, b, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(b, b, dg));

		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(d, c, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(c, c, dg));

		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(a, d, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(a, c, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(a, b, dg));

		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(x, f1, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(c, f1, dg));

		Assert.assertEquals(false, DependencyGraphUtils.isReachableFrom(notInGraph, a, dg));
	}

	@Test
	public void stronglyConnectedComponentsSimpleTest() {
		StringBuilder bld = new StringBuilder();
		bld.append("b :- a.").append("\n");
		bld.append("a :- b.").append("\n");
		Alpha system = new Alpha();
		Program prog = system.readProgramString(bld.toString(), null);
		ProgramAnalysis pa = new ProgramAnalysis(prog);
		DependencyGraph dg = pa.getDependencyGraph();
		Node a = new Node(Predicate.getInstance("a", 0));
		Node b = new Node(Predicate.getInstance("b", 0));

		List<Node> componentA = new ArrayList<>();
		componentA.add(a);
		Assert.assertEquals(true, DependencyGraphUtils.areStronglyConnected(componentA, dg));
		Assert.assertEquals(false, DependencyGraphUtils.isStronglyConnectedComponent(componentA, dg));

		List<Node> componentB = new ArrayList<>();
		componentB.add(b);
		Assert.assertEquals(true, DependencyGraphUtils.areStronglyConnected(componentB, dg));
		Assert.assertEquals(false, DependencyGraphUtils.isStronglyConnectedComponent(componentB, dg));

		List<Node> componentAll = new ArrayList<>();
		componentAll.add(a);
		componentAll.add(b);
		Assert.assertEquals(true, DependencyGraphUtils.areStronglyConnected(componentAll, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(componentAll, dg));
	}

	@Test
	public void stronglyConnectedComponentsMultipleComponentsTest() {
		StringBuilder bld = new StringBuilder();
		bld.append("f0.\n").append("f1.\n").append("f2.\n").append("f3.\n");
		bld.append("a :- f0, f1, not b.").append("\n");
		bld.append("b :- f0, f1, not a.").append("\n");
		bld.append("c :- f2, f3, not d.").append("\n");
		bld.append("d :- f2, f3, not c.").append("\n");
		bld.append("x :- a, c, y.").append("\n");
		bld.append("y :- b, d, x.").append("\n");
		bld.append("z :- x, y, z.").append("\n");
		Alpha system = new Alpha();
		Program prog = system.readProgramString(bld.toString(), null);
		ProgramAnalysis pa = new ProgramAnalysis(prog);
		DependencyGraph dg = pa.getDependencyGraph();

		Node f0 = new Node(Predicate.getInstance("f0", 0));
		Node f1 = new Node(Predicate.getInstance("f1", 0));
		Node f2 = new Node(Predicate.getInstance("f2", 0));
		Node f3 = new Node(Predicate.getInstance("f3", 0));
		Node a = new Node(Predicate.getInstance("a", 0));
		Node b = new Node(Predicate.getInstance("b", 0));
		Node c = new Node(Predicate.getInstance("c", 0));
		Node d = new Node(Predicate.getInstance("d", 0));
		Node x = new Node(Predicate.getInstance("x", 0));
		Node y = new Node(Predicate.getInstance("y", 0));
		Node z = new Node(Predicate.getInstance("z", 0));

		Map<Integer, List<Node>> stronglyConnectedComponents = dg.getStronglyConnectedComponents();
		Assert.assertEquals(8, stronglyConnectedComponents.size());

		for (List<Node> scc : stronglyConnectedComponents.values()) {
			Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(scc, dg));
		}

		List<Node> c1 = new ArrayList<>();
		c1.add(a);
		c1.add(b);
		Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(c1, dg));

		List<Node> c2 = new ArrayList<>();
		c2.add(c);
		c2.add(d);
		Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(c2, dg));

		List<Node> c3 = new ArrayList<>();
		c3.add(x);
		c3.add(y);
		Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(c3, dg));

		List<Node> c4 = new ArrayList<>();
		c4.add(z);
		Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(c4, dg));

		List<Node> c5 = new ArrayList<>();
		c5.add(f0);
		Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(c5, dg));

		List<Node> c6 = new ArrayList<>();
		c6.add(f1);
		Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(c6, dg));

		List<Node> c7 = new ArrayList<>();
		c7.add(f2);
		Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(c7, dg));

		List<Node> c8 = new ArrayList<>();
		c8.add(f3);
		Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(c8, dg));
	}

}
