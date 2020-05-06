package at.ac.tuwien.kr.alpha.common.depgraph;

import org.antlr.v4.runtime.CharStreams;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.graphio.DependencyGraphWriter;
import at.ac.tuwien.kr.alpha.common.program.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.NormalProgram;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.test.util.DependencyGraphUtils;

public class DependencyGraphTest {

	// Currently not used anywhere, but keep as it might come in handy
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
		Alpha system = new Alpha();
		InputStream is = DependencyGraphTest.class.getResourceAsStream("/partial-eval/components-test.asp");
		InputProgram p = new ProgramParser().parse(CharStreams.fromStream(is));
		NormalProgram normalProg = system.normalizeProgram(p);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		DependencyGraphWriter dgw = new DependencyGraphWriter();
		dgw.writeAsDotfile(dg, "/tmp/components-test.asp.dg.dot");
	}

	@Test
	public void edgesEqualTest() {
		Predicate testPredicate = Predicate.getInstance("test", 2, false, false);
		Edge e1 = new Edge(new Node(testPredicate), true);
		Edge e2 = new Edge(new Node(testPredicate), true);
		Assert.assertEquals(e1, e2);
	}

	@Test
	public void reachabilityCheckSimpleTest() {
		Alpha system = new Alpha();

		InputProgram prog = system.readProgramString("b :- a.", null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();

		Node a = dg.getNodeForPredicate(Predicate.getInstance("a", 0));
		Node b = dg.getNodeForPredicate(Predicate.getInstance("b", 0));

		Node nonExistent = new Node(Predicate.getInstance("notHere", 0));

		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(a, a, dg));
		Assert.assertEquals(true, DependencyGraphUtils.isReachableFrom(b, a, dg));
		Assert.assertEquals(false, DependencyGraphUtils.isReachableFrom(a, b, dg));
		Assert.assertEquals(false, DependencyGraphUtils.isReachableFrom(nonExistent, a, dg));
		Assert.assertEquals(false, DependencyGraphUtils.isReachableFrom(nonExistent, b, dg));
	}

	@Test
	public void reachabilityCheckWithHopsTest() {
		StringBuilder bld = new StringBuilder();
		bld.append("b :- a.").append("\n");
		bld.append("c :- b.").append("\n");
		bld.append("d :- c.").append("\n");
		Alpha system = new Alpha();
		InputProgram prog = system.readProgramString(bld.toString(), null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		Node a = dg.getNodeForPredicate(Predicate.getInstance("a", 0));
		Node b = dg.getNodeForPredicate(Predicate.getInstance("b", 0));
		Node c = dg.getNodeForPredicate(Predicate.getInstance("c", 0));
		Node d = dg.getNodeForPredicate(Predicate.getInstance("d", 0));

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
		InputProgram prog = system.readProgramString(bld.toString(), null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		Node a = dg.getNodeForPredicate(Predicate.getInstance("a", 0));
		Node b = dg.getNodeForPredicate(Predicate.getInstance("b", 0));
		Node c = dg.getNodeForPredicate(Predicate.getInstance("c", 0));
		Node d = dg.getNodeForPredicate(Predicate.getInstance("d", 0));
		Node f1 = dg.getNodeForPredicate(Predicate.getInstance("f1", 0));
		Node x = dg.getNodeForPredicate(Predicate.getInstance("x", 0));
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
		InputProgram prog = system.readProgramString(bld.toString(), null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		Node a = dg.getNodeForPredicate(Predicate.getInstance("a", 0));
		Node b = dg.getNodeForPredicate(Predicate.getInstance("b", 0));

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
	public void stronglyConnectedComponentsMultipleComponentsTest() throws IOException {
		Alpha system = new Alpha();
		InputProgram prog = new ProgramParser()
				.parse(CharStreams.fromStream(DependencyGraphTest.class.getResourceAsStream("/partial-eval/components-test.asp")));
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();

		Node f0 = dg.getNodeForPredicate(Predicate.getInstance("f0", 0));
		Node f1 = dg.getNodeForPredicate(Predicate.getInstance("f1", 0));
		Node f2 = dg.getNodeForPredicate(Predicate.getInstance("f2", 0));
		Node f3 = dg.getNodeForPredicate(Predicate.getInstance("f3", 0));
		Node a = dg.getNodeForPredicate(Predicate.getInstance("a", 0));
		Node b = dg.getNodeForPredicate(Predicate.getInstance("b", 0));
		Node c = dg.getNodeForPredicate(Predicate.getInstance("c", 0));
		Node d = dg.getNodeForPredicate(Predicate.getInstance("d", 0));
		Node x = dg.getNodeForPredicate(Predicate.getInstance("x", 0));
		Node y = dg.getNodeForPredicate(Predicate.getInstance("y", 0));
		Node z = dg.getNodeForPredicate(Predicate.getInstance("z", 0));

		StronglyConnectedComponentsHelper componentHelper = new StronglyConnectedComponentsHelper();
		SccResult sccResult = componentHelper.findStronglyConnectedComponents(dg);
		Map<Node, Integer> nodesByComponent = sccResult.getNodesByComponentId();
		Map<Integer, List<Node>> stronglyConnectedComponents = sccResult.getStronglyConnectedComponents();
		Assert.assertEquals(8, stronglyConnectedComponents.size());

		for (Map.Entry<Integer, List<Node>> sccEntry : stronglyConnectedComponents.entrySet()) {
			Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(sccEntry.getValue(), dg));
			for (Node node : sccEntry.getValue()) {
				Assert.assertEquals(sccEntry.getKey(), nodesByComponent.get(node));
			}
		}

		List<Node> c1 = new ArrayList<>();
		c1.add(a);
		c1.add(b);
		Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(c1, dg));
		Assert.assertEquals(nodesByComponent.get(a), nodesByComponent.get(b));

		List<Node> c2 = new ArrayList<>();
		c2.add(c);
		c2.add(d);
		Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(c2, dg));
		Assert.assertEquals(nodesByComponent.get(c), nodesByComponent.get(d));

		List<Node> c3 = new ArrayList<>();
		c3.add(x);
		c3.add(y);
		Assert.assertEquals(true, DependencyGraphUtils.isStronglyConnectedComponent(c3, dg));
		Assert.assertEquals(nodesByComponent.get(x), nodesByComponent.get(y));

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
