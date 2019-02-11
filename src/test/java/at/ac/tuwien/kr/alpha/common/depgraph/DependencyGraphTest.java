package at.ac.tuwien.kr.alpha.common.depgraph;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.CharStreams;
import org.junit.Assert;
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
			tmpAtom = predicates[((int) (Math.random() * predicates.length))];
			prgBuilder.append(tmpAtom).append(" :- ");
			for (int j = 0; j < tmpBodyLiterals; j++) {
				tmpAtom = predicates[((int) (Math.random() * predicates.length))];
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
	// @Ignore("Not a real test, rather a playground for local testing while
	// changing stuff")
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
	public void randomProgramDfsFinishOrderingTest() {
		Alpha system = new Alpha();
		Program prog = system.readProgramString(DependencyGraphTest.generateRandomProgram(10, 5, 3), null);
		ProgramAnalysis analysis = new ProgramAnalysis(prog);
		DependencyGraph dg = analysis.getDependencyGraph();
		Set<Node> finishedNodes = DependencyGraphUtils.performDfs(dg.getNodes());
		Assert.assertEquals(dg.getNodes().size(), finishedNodes.size());
		int finishLastNode = Integer.MAX_VALUE;
		for (Node n : finishedNodes) {
			Assert.assertTrue(n.getNodeInfo().getDfsFinishTime() < finishLastNode);
			finishLastNode = n.getNodeInfo().getDfsFinishTime();
		}
	}

}
