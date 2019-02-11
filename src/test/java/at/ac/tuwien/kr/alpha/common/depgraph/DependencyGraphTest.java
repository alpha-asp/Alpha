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
	@Ignore("Not compatible with current implementation, sorting of finished nodes is done externally")
	public void randomProgramDfsFinishOrderingTest() {
		Alpha system = new Alpha();
		Program prog = system.readProgramString(DependencyGraphTest.generateRandomProgram(10, 5, 3), null);
		ProgramAnalysis analysis = new ProgramAnalysis(prog);
		DependencyGraph dg = analysis.getDependencyGraph();
		List<Node> finishedNodes = DependencyGraphUtils.performDfs(dg.getNodes().keySet(), dg.getNodes()).getFinishedNodes();
		Assert.assertEquals(dg.getNodes().size(), finishedNodes.size());
		int finishLastNode = Integer.MAX_VALUE;
		for (Node n : finishedNodes) {
			Assert.assertTrue(n.getNodeInfo().getDfsFinishTime() < finishLastNode);
			finishLastNode = n.getNodeInfo().getDfsFinishTime();
		}
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
	public void stronglyConnectedComponentSimpleTest() {
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

}
