package at.ac.tuwien.kr.alpha.common.depgraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.Alpha;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;
import at.ac.tuwien.kr.alpha.grounder.structure.ProgramAnalysis;

public class ComponentGraphTest {

	private static void assertNodesMatchStratumNodes(List<SCComponent> stratum, Node... nodes) {
		ComponentGraphTest.assertNodesMatchStratumNodes(stratum, Arrays.asList(nodes));
	}

	private static void assertNodesMatchStratumNodes(List<SCComponent> stratum, Iterable<Node> nodes) {
		List<Node> allNodesInStratum = new ArrayList<>();
		for (SCComponent comp : stratum) {
			for (Node compNode : comp.getNodes()) {
				allNodesInStratum.add(compNode);
			}
		}

		for (Node n : nodes) {
			Assert.assertTrue(n.toString() + " is not contained in stratum, but should be!", allNodesInStratum.contains(n));
		}
		boolean isContained;
		for (Node n : allNodesInStratum) {
			isContained = false;
			for (Node n2 : nodes) {
				if (n2.equals(n)) {
					isContained = true;
				}
			}
			Assert.assertTrue(n.toString() + " contained in stratum and not in node list, but should be!", isContained);
		}
	}

	@Test
	public void stratifyOneRuleTest() throws IOException {
		Alpha system = new Alpha();
		Program prog = system.readProgramString("a :- b.", null);
		ProgramAnalysis pa = new ProgramAnalysis(prog);
		DependencyGraph dg = pa.getDependencyGraph();
		ComponentGraph cg = ComponentGraph.fromDependencyGraph(dg);
		Map<Integer, List<SCComponent>> strata = cg.calculateStratification();

		Assert.assertEquals(1, strata.size());
	}

	@Test
	public void stratifyTwoRulesTest() throws IOException {
		Alpha system = new Alpha();
		StringBuilder bld = new StringBuilder();
		bld.append("b :- a.").append("\n");
		bld.append("c :- b.").append("\n");
		Program prog = system.readProgramString(bld.toString(), null);
		ProgramAnalysis pa = new ProgramAnalysis(prog);
		DependencyGraph dg = pa.getDependencyGraph();
		ComponentGraph cg = ComponentGraph.fromDependencyGraph(dg);
		Map<Integer, List<SCComponent>> strata = cg.calculateStratification();

		Assert.assertEquals(1, strata.size());
		List<SCComponent> stratum0 = strata.get(0);
		Assert.assertEquals(3, stratum0.size());
	}

	@Test
	public void stratifyWithNegativeDependencyTest() throws IOException {
		Alpha system = new Alpha();
		StringBuilder bld = new StringBuilder();
		bld.append("b :- a.").append("\n");
		bld.append("c :- b.").append("\n");
		bld.append("d :- not c.").append("\n");
		bld.append("e :- d.").append("\n");
		Program prog = system.readProgramString(bld.toString(), null);
		ProgramAnalysis pa = new ProgramAnalysis(prog);
		DependencyGraph dg = pa.getDependencyGraph();
		ComponentGraph cg = ComponentGraph.fromDependencyGraph(dg);
		Map<Integer, List<SCComponent>> strata = cg.calculateStratification();

		Node a = dg.getNodeForPredicate(Predicate.getInstance("a", 0));
		Node b = dg.getNodeForPredicate(Predicate.getInstance("b", 0));
		Node c = dg.getNodeForPredicate(Predicate.getInstance("c", 0));
		Node d = dg.getNodeForPredicate(Predicate.getInstance("d", 0));
		Node e = dg.getNodeForPredicate(Predicate.getInstance("e", 0));

		Assert.assertEquals(2, strata.size());

		List<SCComponent> stratum0 = strata.get(0);
		Assert.assertEquals(3, stratum0.size());
		assertNodesMatchStratumNodes(stratum0, a, b, c);

		List<SCComponent> stratum1 = strata.get(1);
		Assert.assertEquals(2, stratum1.size());
		assertNodesMatchStratumNodes(stratum1, d, e);

	}

	@Test
	public void stratifyWithPositiveCycleTest() {
		Alpha system = new Alpha();
		StringBuilder bld = new StringBuilder();
		bld.append("ancestor_of(X, Y) :- parent_of(X, Y).");
		bld.append("ancestor_of(X, Z) :- parent_of(X, Y), ancestor_of(Y, Z).");
	}

}
