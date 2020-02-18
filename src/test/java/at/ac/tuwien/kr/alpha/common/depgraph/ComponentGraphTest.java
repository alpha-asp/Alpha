package at.ac.tuwien.kr.alpha.common.depgraph;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;
import at.ac.tuwien.kr.alpha.common.program.impl.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.NormalProgram;

public class ComponentGraphTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComponentGraphTest.class);

	private StronglyConnectedComponentsHelper componentHelper = new StronglyConnectedComponentsHelper();
	private StratificationHelper stratificationHelper = new StratificationHelper();

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

	private static List<Node> extractNodesFromStratum(List<SCComponent> stratum) {
		List<Node> retVal = new ArrayList<>();
		for (SCComponent comp : stratum) {
			retVal.addAll(comp.getNodes());
		}
		return retVal;
	}

	@Test
	public void stratifyOneRuleTest() throws IOException {
		Alpha system = new Alpha();
		InputProgram prog = system.readProgramString("a :- b.", null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, this.componentHelper.findStronglyConnectedComponents(dg));
		Map<Integer, List<SCComponent>> strata = this.stratificationHelper.calculateStratification(cg);

		Assert.assertEquals(1, strata.size());
	}

	@Test
	public void stratifyTwoRulesTest() throws IOException {
		Alpha system = new Alpha();
		StringBuilder bld = new StringBuilder();
		bld.append("b :- a.").append("\n");
		bld.append("c :- b.").append("\n");
		InputProgram prog = system.readProgramString(bld.toString(), null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, this.componentHelper.findStronglyConnectedComponents(dg));
		Map<Integer, List<SCComponent>> strata = this.stratificationHelper.calculateStratification(cg);

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
		InputProgram prog = system.readProgramString(bld.toString(), null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, this.componentHelper.findStronglyConnectedComponents(dg));
		Map<Integer, List<SCComponent>> strata = this.stratificationHelper.calculateStratification(cg);

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
	public void stratifyWithPositiveCycleTest() throws IOException {
		Alpha system = new Alpha();
		StringBuilder bld = new StringBuilder();
		bld.append("ancestor_of(X, Y) :- parent_of(X, Y).");
		bld.append("ancestor_of(X, Z) :- parent_of(X, Y), ancestor_of(Y, Z).");
		InputProgram prog = system.readProgramString(bld.toString(), null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, this.componentHelper.findStronglyConnectedComponents(dg));
		Map<Integer, List<SCComponent>> strata = this.stratificationHelper.calculateStratification(cg);

		Node ancestorOf = dg.getNodeForPredicate(Predicate.getInstance("ancestor_of", 2));
		Node parentOf = dg.getNodeForPredicate(Predicate.getInstance("parent_of", 2));

		Assert.assertEquals(1, strata.size());
		List<SCComponent> stratum0 = strata.get(0);
		assertNodesMatchStratumNodes(stratum0, ancestorOf, parentOf);
	}

	@Test
	public void stratifyLargeGraphTest() {
		Alpha system = new Alpha();
		StringBuilder bld = new StringBuilder();
		bld.append("b :- a.");
		bld.append("c :- b.");
		bld.append("d :- c.");
		bld.append("e :- d.");
		bld.append("f :- not e.");
		bld.append("g :- d, j, not f.");
		bld.append("h :- not c.");
		bld.append("i :- h, not j.");
		bld.append("j :- h, not i.");
		bld.append("k :- g, not l.");
		bld.append("l :- g, not k.");
		bld.append("m :- not k, not l.");
		bld.append("n :- m, not i, not j.");
		bld.append("p :- not m, not n.");

		InputProgram prog = system.readProgramString(bld.toString(), null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, this.componentHelper.findStronglyConnectedComponents(dg));
		Map<Integer, List<SCComponent>> strata = this.stratificationHelper.calculateStratification(cg);

		Node a = dg.getNodeForPredicate(Predicate.getInstance("a", 0));
		Node b = dg.getNodeForPredicate(Predicate.getInstance("b", 0));
		Node c = dg.getNodeForPredicate(Predicate.getInstance("c", 0));
		Node d = dg.getNodeForPredicate(Predicate.getInstance("d", 0));
		Node e = dg.getNodeForPredicate(Predicate.getInstance("e", 0));
		Node f = dg.getNodeForPredicate(Predicate.getInstance("f", 0));
		Node h = dg.getNodeForPredicate(Predicate.getInstance("h", 0));

		Assert.assertEquals(2, strata.size());

		List<SCComponent> stratum0 = strata.get(0);
		assertNodesMatchStratumNodes(stratum0, a, b, c, d, e);

		List<SCComponent> stratum1 = strata.get(1);
		assertNodesMatchStratumNodes(stratum1, h, f);
	}

	@Test
	public void stratifyAvoidDuplicatesTest() {
		Alpha system = new Alpha();
		StringBuilder bld = new StringBuilder();
		bld.append("b :- a.");
		bld.append("c :- b.");
		bld.append("d :- c.");
		bld.append("e :- d.");
		bld.append("f :- not e.");
		bld.append("g :- d, j, not f.");
		bld.append("h :- not c.");
		bld.append("i :- h, not j.");
		bld.append("j :- h, not i.");
		bld.append("k :- g, not l.");
		bld.append("l :- g, not k.");
		bld.append("m :- not k, not l.");
		bld.append("n :- m, not i, not j.");
		bld.append("p :- not m, not n.");

		InputProgram prog = system.readProgramString(bld.toString(), null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, this.componentHelper.findStronglyConnectedComponents(dg));
		Map<Integer, List<SCComponent>> strata = this.stratificationHelper.calculateStratification(cg);

		Node a = dg.getNodeForPredicate(Predicate.getInstance("a", 0));
		Node b = dg.getNodeForPredicate(Predicate.getInstance("b", 0));
		Node c = dg.getNodeForPredicate(Predicate.getInstance("c", 0));
		Node d = dg.getNodeForPredicate(Predicate.getInstance("d", 0));
		Node e = dg.getNodeForPredicate(Predicate.getInstance("e", 0));
		Node f = dg.getNodeForPredicate(Predicate.getInstance("f", 0));
		Node h = dg.getNodeForPredicate(Predicate.getInstance("h", 0));

		Assert.assertEquals(2, strata.size());

		Set<Node> stratum0ExpectedNodes = new HashSet<>();
		stratum0ExpectedNodes.add(a);
		stratum0ExpectedNodes.add(b);
		stratum0ExpectedNodes.add(c);
		stratum0ExpectedNodes.add(d);
		stratum0ExpectedNodes.add(e);
		List<Node> stratum0ActualNodes = extractNodesFromStratum(strata.get(0));
		Assert.assertEquals(stratum0ExpectedNodes.size(), stratum0ActualNodes.size());

		Set<Node> stratum1ExpectedNodes = new HashSet<>();
		stratum1ExpectedNodes.add(f);
		stratum1ExpectedNodes.add(h);
		List<Node> stratum1ActualNodes = extractNodesFromStratum(strata.get(1));
		Assert.assertEquals(stratum1ExpectedNodes.size(), stratum1ActualNodes.size());
	}

	@Test
	public void avoidDuplicatesTest1() {
		Alpha system = new Alpha();
		StringBuilder bld = new StringBuilder();
		bld.append("b :- a.");
		bld.append("c :- b.");
		bld.append("c :- a.");

		InputProgram prog = system.readProgramString(bld.toString(), null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, this.componentHelper.findStronglyConnectedComponents(dg));
		Map<Integer, List<SCComponent>> strata = this.stratificationHelper.calculateStratification(cg);

		Node a = dg.getNodeForPredicate(Predicate.getInstance("a", 0));
		Node b = dg.getNodeForPredicate(Predicate.getInstance("b", 0));
		Node c = dg.getNodeForPredicate(Predicate.getInstance("c", 0));

		Assert.assertEquals(1, strata.size());
		Set<Node> stratum0ExpectedNodes = new HashSet<>();
		stratum0ExpectedNodes.add(a);
		stratum0ExpectedNodes.add(b);
		stratum0ExpectedNodes.add(c);
		List<Node> stratum0ActualNodes = extractNodesFromStratum(strata.get(0));
		LOGGER.debug("Expected nodes: " + StringUtils.join(stratum0ExpectedNodes, ","));
		LOGGER.debug("Actual nodes: " + StringUtils.join(stratum0ActualNodes, ","));
		Assert.assertEquals(stratum0ExpectedNodes.size(), stratum0ActualNodes.size());
	}

}
