package at.ac.tuwien.kr.alpha.core.depgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.analysis.DependencyGraph;
import at.ac.tuwien.kr.alpha.api.programs.analysis.DependencyGraph.Node;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.core.rules.CompiledRule;
import at.ac.tuwien.kr.alpha.core.rules.InternalRule;
import at.ac.tuwien.kr.alpha.core.test.util.DependencyGraphUtils;

public class DependencyGraphImplTest {

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
		Predicate testPredicate = Predicates.getPredicate("test", 2, false, false);
		EdgeImpl e1 = new EdgeImpl(new NodeImpl(testPredicate), true);
		EdgeImpl e2 = new EdgeImpl(new NodeImpl(testPredicate), true);
		assertEquals(e1, e2);
	}

	@Test
	public void reachabilityCheckSimpleTest() {
		// rule := b :- a.
		CompiledRule rule = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("b", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("a", 0)).toLiteral());
		Map<Integer, CompiledRule> rules = new HashMap<>();
		rules.put(0, rule);
		DependencyGraph dg = DependencyGraphImpl.buildDependencyGraph(rules);

		Node a = dg.getNodeForPredicate(Predicates.getPredicate("a", 0));
		Node b = dg.getNodeForPredicate(Predicates.getPredicate("b", 0));

		NodeImpl nonExistent = new NodeImpl(Predicates.getPredicate("notHere", 0));

		assertTrue(DependencyGraphUtils.isReachableFrom(a, a, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(b, a, dg));
		assertFalse(DependencyGraphUtils.isReachableFrom(a, b, dg));
		assertFalse(DependencyGraphUtils.isReachableFrom(nonExistent, a, dg));
		assertFalse(DependencyGraphUtils.isReachableFrom(nonExistent, b, dg));
	}

	@Test
	public void reachabilityCheckWithHopsTest() {
		// r1 := b :- a.
		CompiledRule r1 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("b", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("a", 0)).toLiteral());
		// r2 := c :- b.
		CompiledRule r2 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("c", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("b", 0)).toLiteral());
		// r3 := d :- c.
		CompiledRule r3 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("d", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("c", 0)).toLiteral());

		Map<Integer, CompiledRule> rules = new HashMap<>();
		rules.put(0, r1);
		rules.put(1, r2);
		rules.put(2, r3);
		DependencyGraph dg = DependencyGraphImpl.buildDependencyGraph(rules);

		Node a = dg.getNodeForPredicate(Predicates.getPredicate("a", 0));
		Node b = dg.getNodeForPredicate(Predicates.getPredicate("b", 0));
		Node c = dg.getNodeForPredicate(Predicates.getPredicate("c", 0));
		Node d = dg.getNodeForPredicate(Predicates.getPredicate("d", 0));

		assertTrue(DependencyGraphUtils.isReachableFrom(d, a, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(c, a, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(b, a, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(a, a, dg));

		assertTrue(DependencyGraphUtils.isReachableFrom(d, b, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(c, b, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(b, b, dg));

		assertTrue(DependencyGraphUtils.isReachableFrom(d, c, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(c, c, dg));

		assertFalse(DependencyGraphUtils.isReachableFrom(a, d, dg));
		assertFalse(DependencyGraphUtils.isReachableFrom(a, c, dg));
		assertFalse(DependencyGraphUtils.isReachableFrom(a, b, dg));
	}

	@Test
	public void reachabilityWithCyclesTest() {
		// r1 := b :- a, f1.
		CompiledRule r1 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("b", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("a", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("f1", 0)).toLiteral());
		// r2 := c :- b.
		CompiledRule r2 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("c", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("b", 0)).toLiteral());
		// r3 := d :- c.
		CompiledRule r3 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("d", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("c", 0)).toLiteral());
		// r4 := a :- d.
		CompiledRule r4 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("a", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("d", 0)).toLiteral());
		// r5 := x :- d, f1.
		CompiledRule r5 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("x", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("d", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("f1", 0)).toLiteral());

		Map<Integer, CompiledRule> rules = new HashMap<>();
		rules.put(0, r1);
		rules.put(1, r2);
		rules.put(2, r3);
		rules.put(3, r4);
		rules.put(4, r5);
		DependencyGraph dg = DependencyGraphImpl.buildDependencyGraph(rules);
		Node a = dg.getNodeForPredicate(Predicates.getPredicate("a", 0));
		Node b = dg.getNodeForPredicate(Predicates.getPredicate("b", 0));
		Node c = dg.getNodeForPredicate(Predicates.getPredicate("c", 0));
		Node d = dg.getNodeForPredicate(Predicates.getPredicate("d", 0));
		Node f1 = dg.getNodeForPredicate(Predicates.getPredicate("f1", 0));
		Node x = dg.getNodeForPredicate(Predicates.getPredicate("x", 0));
		Node notInGraph = new NodeImpl(Predicates.getPredicate("notInGraph", 0));

		assertTrue(DependencyGraphUtils.isReachableFrom(d, a, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(c, a, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(b, a, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(a, a, dg));

		assertTrue(DependencyGraphUtils.isReachableFrom(d, b, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(c, b, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(b, b, dg));

		assertTrue(DependencyGraphUtils.isReachableFrom(d, c, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(c, c, dg));

		assertTrue(DependencyGraphUtils.isReachableFrom(a, d, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(a, c, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(a, b, dg));

		assertTrue(DependencyGraphUtils.isReachableFrom(x, f1, dg));
		assertTrue(DependencyGraphUtils.isReachableFrom(c, f1, dg));

		assertFalse(DependencyGraphUtils.isReachableFrom(notInGraph, a, dg));
	}

	@Test
	public void stronglyConnectedComponentsSimpleTest() {
		// r1 := b:- a.
		CompiledRule r1 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("b", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("a", 0)).toLiteral());
		// r2 := a :- b.
		CompiledRule r2 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("a", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("b", 0)).toLiteral());

		Map<Integer, CompiledRule> rules = new HashMap<>();
		rules.put(0, r1);
		rules.put(1, r2);
		DependencyGraph dg = DependencyGraphImpl.buildDependencyGraph(rules);
		Node a = dg.getNodeForPredicate(Predicates.getPredicate("a", 0));
		Node b = dg.getNodeForPredicate(Predicates.getPredicate("b", 0));

		List<Node> componentA = new ArrayList<>();
		componentA.add(a);
		assertTrue(DependencyGraphUtils.areStronglyConnected(componentA, dg));
		assertFalse(DependencyGraphUtils.isStronglyConnectedComponent(componentA, dg));

		List<Node> componentB = new ArrayList<>();
		componentB.add(b);
		assertTrue(DependencyGraphUtils.areStronglyConnected(componentB, dg));
		assertFalse(DependencyGraphUtils.isStronglyConnectedComponent(componentB, dg));

		List<Node> componentAll = new ArrayList<>();
		componentAll.add(a);
		componentAll.add(b);
		assertTrue(DependencyGraphUtils.areStronglyConnected(componentAll, dg));
		assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(componentAll, dg));
	}

	@Test
	public void stronglyConnectedComponentsMultipleComponentsTest() {
		// r1 := a :- f0, f1, not b.
		CompiledRule r1 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("a", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("f0", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("f1", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("b", 0)).toLiteral(false));
		// r2 := b :- f0, f1, not a.
		CompiledRule r2 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("b", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("f0", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("f1", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("a", 0)).toLiteral(false));
		// r3 := c :- f2, f3, not d.
		CompiledRule r3 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("c", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("f2", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("f3", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("d", 0)).toLiteral(false));
		// r4 := d :- f2, f3, not c.
		CompiledRule r4 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("d", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("f2", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("f3", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("c", 0)).toLiteral(false));
		// r5 := x :- a, c, y.
		CompiledRule r5 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("x", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("a", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("c", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("y", 0)).toLiteral());
		// r6 := y :- b, d, x.
		CompiledRule r6 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("y", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("b", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("d", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("x", 0)).toLiteral());
		// r7 := z :- x, y, z.
		CompiledRule r7 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("z", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("x", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("y", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("z", 0)).toLiteral());
		
		Map<Integer, CompiledRule> rules = new HashMap<>();
		rules.put(0, r1);
		rules.put(1, r2);
		rules.put(2, r3);
		rules.put(3, r4);
		rules.put(4, r5);
		rules.put(5, r6);
		rules.put(6, r7);
		DependencyGraph dg = DependencyGraphImpl.buildDependencyGraph(rules);

		Node f0 = dg.getNodeForPredicate(Predicates.getPredicate("f0", 0));
		Node f1 = dg.getNodeForPredicate(Predicates.getPredicate("f1", 0));
		Node f2 = dg.getNodeForPredicate(Predicates.getPredicate("f2", 0));
		Node f3 = dg.getNodeForPredicate(Predicates.getPredicate("f3", 0));
		Node a = dg.getNodeForPredicate(Predicates.getPredicate("a", 0));
		Node b = dg.getNodeForPredicate(Predicates.getPredicate("b", 0));
		Node c = dg.getNodeForPredicate(Predicates.getPredicate("c", 0));
		Node d = dg.getNodeForPredicate(Predicates.getPredicate("d", 0));
		Node x = dg.getNodeForPredicate(Predicates.getPredicate("x", 0));
		Node y = dg.getNodeForPredicate(Predicates.getPredicate("y", 0));
		Node z = dg.getNodeForPredicate(Predicates.getPredicate("z", 0));

		StronglyConnectedComponentsAlgorithm.SccResult sccResult = StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg);
		Map<Node, Integer> nodesByComponent = sccResult.nodesByComponentId;
		List<List<Node>> stronglyConnectedComponents = sccResult.stronglyConnectedComponents;
		assertEquals(8, stronglyConnectedComponents.size());

		for (int i = 0; i < stronglyConnectedComponents.size(); i++) {
			List<Node> stronglyConnectedComponent = stronglyConnectedComponents.get(i);
			assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(stronglyConnectedComponent, dg));
			for (Node node : stronglyConnectedComponent) {
				assertEquals(Integer.valueOf(i), nodesByComponent.get(node));
			}
		}

		List<Node> c1 = new ArrayList<>();
		c1.add(a);
		c1.add(b);
		assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c1, dg));
		assertEquals(nodesByComponent.get(a), nodesByComponent.get(b));

		List<Node> c2 = new ArrayList<>();
		c2.add(c);
		c2.add(d);
		assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c2, dg));
		assertEquals(nodesByComponent.get(c), nodesByComponent.get(d));

		List<Node> c3 = new ArrayList<>();
		c3.add(x);
		c3.add(y);
		assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c3, dg));
		assertEquals(nodesByComponent.get(x), nodesByComponent.get(y));

		List<Node> c4 = new ArrayList<>();
		c4.add(z);
		assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c4, dg));

		List<Node> c5 = new ArrayList<>();
		c5.add(f0);
		assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c5, dg));

		List<Node> c6 = new ArrayList<>();
		c6.add(f1);
		assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c6, dg));

		List<Node> c7 = new ArrayList<>();
		c7.add(f2);
		assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c7, dg));

		List<Node> c8 = new ArrayList<>();
		c8.add(f3);
		assertTrue(DependencyGraphUtils.isStronglyConnectedComponent(c8, dg));
	}

}
