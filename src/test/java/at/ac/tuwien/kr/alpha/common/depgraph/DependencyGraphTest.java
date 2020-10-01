package at.ac.tuwien.kr.alpha.common.depgraph;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.program.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.NormalProgram;
import at.ac.tuwien.kr.alpha.test.util.DependencyGraphUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(a, a, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(b, a, dg));
		Assert.assertFalse(DependencyGraphUtils.isReachableFrom(a, b, dg));
		Assert.assertFalse(DependencyGraphUtils.isReachableFrom(nonExistent, a, dg));
		Assert.assertFalse(DependencyGraphUtils.isReachableFrom(nonExistent, b, dg));
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

		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(d, a, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(c, a, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(b, a, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(a, a, dg));

		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(d, b, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(c, b, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(b, b, dg));

		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(d, c, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(c, c, dg));

		Assert.assertFalse(DependencyGraphUtils.isReachableFrom(a, d, dg));
		Assert.assertFalse(DependencyGraphUtils.isReachableFrom(a, c, dg));
		Assert.assertFalse(DependencyGraphUtils.isReachableFrom(a, b, dg));
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

		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(d, a, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(c, a, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(b, a, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(a, a, dg));

		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(d, b, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(c, b, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(b, b, dg));

		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(d, c, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(c, c, dg));

		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(a, d, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(a, c, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(a, b, dg));

		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(x, f1, dg));
		Assert.assertTrue(DependencyGraphUtils.isReachableFrom(c, f1, dg));

		Assert.assertFalse(DependencyGraphUtils.isReachableFrom(notInGraph, a, dg));
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
		Assert.assertTrue(DependencyGraphUtils.areStronglyConnected(componentA, dg));
		Assert.assertFalse(DependencyGraphUtils.isStronglyConnectedComponent(componentA, dg));

		List<Node> componentB = new ArrayList<>();
		componentB.add(b);
		Assert.assertTrue(DependencyGraphUtils.areStronglyConnected(componentB, dg));
		Assert.assertFalse(DependencyGraphUtils.isStronglyConnectedComponent(componentB, dg));

		List<Node> componentAll = new ArrayList<>();
		componentAll.add(a);
		componentAll.add(b);
		Assert.assertTrue(DependencyGraphUtils.areStronglyConnected(componentAll, dg));
		Assert.assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(componentAll, dg));
	}

	@Test
	public void stronglyConnectedComponentsMultipleComponentsTest() {
		String inputProgram = "f0.\n" +
			"f1.\n" +
			"f2.\n" +
			"f3.\n" +
			"a :- f0, f1, not b.\n" +
			"b :- f0, f1, not a.\n" +
			"c :- f2, f3, not d.\n" +
			"d :- f2, f3, not c.\n" +
			"x :- a, c, y.\n" +
			"y :- b, d, x.\n" +
			"z :- x, y, z.";

		Alpha system = new Alpha();
		InputProgram prog = system.readProgramString(inputProgram);
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

		StronglyConnectedComponentsAlgorithm.SccResult sccResult = StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg);
		Map<Node, Integer> nodesByComponent = sccResult.nodesByComponentId;
		List<List<Node>> stronglyConnectedComponents = sccResult.stronglyConnectedComponents;
		Assert.assertEquals(8, stronglyConnectedComponents.size());

		for (int i = 0; i < stronglyConnectedComponents.size(); i++) {
			List<Node> stronglyConnectedComponent = stronglyConnectedComponents.get(i);
			Assert.assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(stronglyConnectedComponent, dg));
			for (Node node : stronglyConnectedComponent) {
				Assert.assertEquals(Integer.valueOf(i), nodesByComponent.get(node));
			}
		}

		List<Node> c1 = new ArrayList<>();
		c1.add(a);
		c1.add(b);
		Assert.assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c1, dg));
		Assert.assertEquals(nodesByComponent.get(a), nodesByComponent.get(b));

		List<Node> c2 = new ArrayList<>();
		c2.add(c);
		c2.add(d);
		Assert.assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c2, dg));
		Assert.assertEquals(nodesByComponent.get(c), nodesByComponent.get(d));

		List<Node> c3 = new ArrayList<>();
		c3.add(x);
		c3.add(y);
		Assert.assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c3, dg));
		Assert.assertEquals(nodesByComponent.get(x), nodesByComponent.get(y));

		List<Node> c4 = new ArrayList<>();
		c4.add(z);
		Assert.assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c4, dg));

		List<Node> c5 = new ArrayList<>();
		c5.add(f0);
		Assert.assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c5, dg));

		List<Node> c6 = new ArrayList<>();
		c6.add(f1);
		Assert.assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c6, dg));

		List<Node> c7 = new ArrayList<>();
		c7.add(f2);
		Assert.assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c7, dg));

		List<Node> c8 = new ArrayList<>();
		c8.add(f3);
		Assert.assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c8, dg));
	}

}
