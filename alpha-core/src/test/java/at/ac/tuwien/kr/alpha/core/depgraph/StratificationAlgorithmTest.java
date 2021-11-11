package at.ac.tuwien.kr.alpha.core.depgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.analysis.ComponentGraph;
import at.ac.tuwien.kr.alpha.api.programs.analysis.ComponentGraph.SCComponent;
import at.ac.tuwien.kr.alpha.api.programs.analysis.DependencyGraph;
import at.ac.tuwien.kr.alpha.api.programs.analysis.DependencyGraph.Node;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.rules.CompiledRule;
import at.ac.tuwien.kr.alpha.core.rules.InternalRule;

public class StratificationAlgorithmTest {

	private static boolean assertPredicateBeforeInOrder(Predicate predBefore, Predicate predAfter, List<SCComponent> order) {
		boolean foundPredBefore = false;
		for (SCComponent component : order) {
			for (Node node : component.getNodes()) {
				if (node.getPredicate() == predBefore) {
					foundPredBefore = true;
				}
				if (node.getPredicate() == predAfter) {
					// Found second predicate, return true if we already found the first predicate.
					return foundPredBefore;
				}
			}
		}
		return false;
	}

	@Test
	public void stratifyOneRuleTest() {
		// r1 := a :- b.
		CompiledRule r1 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("a", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("b", 0)).toLiteral());

		Map<Integer, CompiledRule> rules = new HashMap<>();
		rules.put(0, r1);
		DependencyGraph dg = DependencyGraphImpl.buildDependencyGraph(rules);
		ComponentGraph cg = ComponentGraphImpl.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate a = Predicates.getPredicate("a", 0);
		Predicate b = Predicates.getPredicate("b", 0);

		assertEquals(2, strata.size());
		assertTrue(assertPredicateBeforeInOrder(b, a, strata));
	}

	@Test
	public void stratifyTwoRulesTest() {
		// r1 := b :- a.
		CompiledRule r1 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("b", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("a", 0)).toLiteral());
		// r2 := c :- b.
		CompiledRule r2 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("c", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("b", 0)).toLiteral());

		Map<Integer, CompiledRule> rules = new HashMap<>();
		rules.put(0, r1);
		rules.put(1, r2);
		DependencyGraph dg = DependencyGraphImpl.buildDependencyGraph(rules);
		ComponentGraph cg = ComponentGraphImpl.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate a = Predicates.getPredicate("a", 0);
		Predicate b = Predicates.getPredicate("b", 0);
		Predicate c = Predicates.getPredicate("c", 0);

		assertEquals(3, strata.size());
		assertTrue(assertPredicateBeforeInOrder(a, b, strata));
		assertTrue(assertPredicateBeforeInOrder(b, c, strata));
		assertTrue(assertPredicateBeforeInOrder(a, c, strata));
	}

	@Test
	public void stratifyWithNegativeDependencyTest() {
		// r1 := b :- a.
		CompiledRule r1 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("b", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("a", 0)).toLiteral());
		// r2 := c :- b.
		CompiledRule r2 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("c", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("b", 0)).toLiteral());
		// r3 := d :- not c.
		CompiledRule r3 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("d", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("c", 0)).toLiteral(false));
		// r4 := e :- d.
		CompiledRule r4 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("e", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("d", 0)).toLiteral());

		Map<Integer, CompiledRule> rules = new HashMap<>();
		rules.put(0, r1);
		rules.put(1, r2);
		rules.put(2, r3);
		rules.put(3, r4);
		DependencyGraph dg = DependencyGraphImpl.buildDependencyGraph(rules);
		ComponentGraph cg = ComponentGraphImpl.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate a = Predicates.getPredicate("a", 0);
		Predicate b = Predicates.getPredicate("b", 0);
		Predicate c = Predicates.getPredicate("c", 0);
		Predicate d = Predicates.getPredicate("d", 0);
		Predicate e = Predicates.getPredicate("e", 0);

		assertEquals(5, strata.size());
		assertTrue(assertPredicateBeforeInOrder(a, b, strata));
		assertTrue(assertPredicateBeforeInOrder(b, c, strata));
		assertTrue(assertPredicateBeforeInOrder(c, d, strata));
		assertTrue(assertPredicateBeforeInOrder(d, e, strata));
	}

	@Test
	public void stratifyWithPositiveCycleTest() {
		// r1 := ancestor_of(X, Y) :- parent_of(X, Y).
		CompiledRule r1 = new InternalRule(
				Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("ancestor_of", 2), Terms.newVariable("X"), Terms.newVariable("Y"))),
				Atoms.newBasicAtom(Predicates.getPredicate("parent_of", 2), Terms.newVariable("X"), Terms.newVariable("Y")).toLiteral());
		// r2 := ancestor_of(X, Z) :- parent_of(X, Y), ancestor_of(Y, Z).
		CompiledRule r2 = new InternalRule(
				Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("ancestor_of", 2), Terms.newVariable("X"), Terms.newVariable("Z"))),
				Atoms.newBasicAtom(Predicates.getPredicate("parent_of", 2), Terms.newVariable("X"), Terms.newVariable("Y")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("ancestor_of", 2), Terms.newVariable("Y"), Terms.newVariable("Z")).toLiteral());

		Map<Integer, CompiledRule> rules = new HashMap<>();
		rules.put(0, r1);
		rules.put(1, r2);
		DependencyGraph dg = DependencyGraphImpl.buildDependencyGraph(rules);
		ComponentGraph cg = ComponentGraphImpl.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate ancestorOf = Predicates.getPredicate("ancestor_of", 2);
		Predicate parentOf = Predicates.getPredicate("parent_of", 2);

		assertEquals(2, strata.size());
		assertTrue(assertPredicateBeforeInOrder(parentOf, ancestorOf, strata));
	}

	@Test
	public void stratifyLargeGraphTest() {
		// r1 := b :- a.
		CompiledRule r1 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("b", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("a", 0)).toLiteral());
		// r2 := c :- b.
		CompiledRule r2 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("c", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("b", 0)).toLiteral());
		// r3 := d :- c.
		CompiledRule r3 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("d", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("c", 0)).toLiteral());
		// r4 := e :- d.
		CompiledRule r4 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("e", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("d", 0)).toLiteral());
		// r5 := f :- not e.
		CompiledRule r5 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("f", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("e", 0)).toLiteral(false));
		// r6 := g :- d, j, not f.
		CompiledRule r6 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("g", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("d", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("j", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("f", 0)).toLiteral(false));
		// r7 := h :- not c.
		CompiledRule r7 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("h", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("c", 0)).toLiteral(false));
		// r8 := i :- h, not j.
		CompiledRule r8 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("i", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("h", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("j", 0)).toLiteral(false));
		// r9 := j :- h, not i.
		CompiledRule r9 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("j", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("h", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("i", 0)).toLiteral(false));
		// r10 := k :- g, not l.
		CompiledRule r10 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("k", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("g", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("l", 0)).toLiteral(false));
		// r11 := l :- g, not k.
		CompiledRule r11 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("l", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("g", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("k", 0)).toLiteral(false));
		// r12 := m :- not k, not l.
		CompiledRule r12 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("m", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("k", 0)).toLiteral(false),
				Atoms.newBasicAtom(Predicates.getPredicate("l", 0)).toLiteral(false));
		// r13 := n :- m, not i, not j.
		CompiledRule r13 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("n", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("m", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("i", 0)).toLiteral(false),
				Atoms.newBasicAtom(Predicates.getPredicate("j", 0)).toLiteral(false));
		// r14 := p :- not m, not n.
		CompiledRule r14 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("p", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("m", 0)).toLiteral(false),
				Atoms.newBasicAtom(Predicates.getPredicate("n", 0)).toLiteral(false));

		Map<Integer, CompiledRule> rules = new HashMap<>();
		rules.put(0, r1);
		rules.put(1, r2);
		rules.put(2, r3);
		rules.put(3, r4);
		rules.put(4, r5);
		rules.put(5, r6);
		rules.put(6, r7);
		rules.put(7, r8);
		rules.put(8, r9);
		rules.put(9, r10);
		rules.put(10, r11);
		rules.put(11, r12);
		rules.put(12, r13);
		rules.put(13, r14);
		DependencyGraph dg = DependencyGraphImpl.buildDependencyGraph(rules);
		ComponentGraph cg = ComponentGraphImpl.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate a = Predicates.getPredicate("a", 0);
		Predicate b = Predicates.getPredicate("b", 0);
		Predicate c = Predicates.getPredicate("c", 0);
		Predicate d = Predicates.getPredicate("d", 0);
		Predicate e = Predicates.getPredicate("e", 0);
		Predicate f = Predicates.getPredicate("f", 0);
		Predicate h = Predicates.getPredicate("h", 0);

		assertTrue(assertPredicateBeforeInOrder(a, h, strata));
		assertTrue(assertPredicateBeforeInOrder(b, h, strata));
		assertTrue(assertPredicateBeforeInOrder(c, h, strata));

		assertTrue(assertPredicateBeforeInOrder(a, f, strata));
		assertTrue(assertPredicateBeforeInOrder(b, f, strata));
		assertTrue(assertPredicateBeforeInOrder(c, f, strata));
		assertTrue(assertPredicateBeforeInOrder(d, f, strata));
		assertTrue(assertPredicateBeforeInOrder(e, f, strata));
	}

	@Test
	public void stratifyAvoidDuplicatesTest() {
		// r1 := b :- a.
		CompiledRule r1 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("b", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("a", 0)).toLiteral());
		// r2 := c :- b.
		CompiledRule r2 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("c", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("b", 0)).toLiteral());
		// r3 := d :- c.
		CompiledRule r3 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("d", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("c", 0)).toLiteral());
		// r4 := e :- d.
		CompiledRule r4 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("e", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("d", 0)).toLiteral());
		// r5 := f :- not e.
		CompiledRule r5 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("f", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("e", 0)).toLiteral(false));
		// r6 := g :- d, j, not f.
		CompiledRule r6 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("g", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("d", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("j", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("f", 0)).toLiteral(false));
		// r7 := h :- not c.
		CompiledRule r7 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("h", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("c", 0)).toLiteral(false));
		// r8 := i :- h, not j.
		CompiledRule r8 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("i", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("h", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("j", 0)).toLiteral(false));
		// r9 := j :- h, not i.
		CompiledRule r9 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("j", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("h", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("i", 0)).toLiteral(false));
		// r10 := k :- g, not l.
		CompiledRule r10 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("k", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("g", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("l", 0)).toLiteral(false));
		// r11 := l :- g, not k.
		CompiledRule r11 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("l", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("g", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("k", 0)).toLiteral(false));
		// r12 := m :- not k, not l.
		CompiledRule r12 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("m", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("k", 0)).toLiteral(false),
				Atoms.newBasicAtom(Predicates.getPredicate("l", 0)).toLiteral(false));
		// r13 := n :- m, not i, not j.
		CompiledRule r13 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("n", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("m", 0)).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("i", 0)).toLiteral(false),
				Atoms.newBasicAtom(Predicates.getPredicate("j", 0)).toLiteral(false));
		// r14 := p :- not m, not n.
		CompiledRule r14 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("p", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("m", 0)).toLiteral(false),
				Atoms.newBasicAtom(Predicates.getPredicate("n", 0)).toLiteral(false));

		Map<Integer, CompiledRule> rules = new HashMap<>();
		rules.put(0, r1);
		rules.put(1, r2);
		rules.put(2, r3);
		rules.put(3, r4);
		rules.put(4, r5);
		rules.put(5, r6);
		rules.put(6, r7);
		rules.put(7, r8);
		rules.put(8, r9);
		rules.put(9, r10);
		rules.put(10, r11);
		rules.put(11, r12);
		rules.put(12, r13);
		rules.put(13, r14);
		DependencyGraph dg = DependencyGraphImpl.buildDependencyGraph(rules);
		ComponentGraph cg = ComponentGraphImpl.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate a = Predicates.getPredicate("a", 0);
		Predicate b = Predicates.getPredicate("b", 0);
		Predicate c = Predicates.getPredicate("c", 0);
		Predicate d = Predicates.getPredicate("d", 0);
		Predicate e = Predicates.getPredicate("e", 0);
		Predicate f = Predicates.getPredicate("f", 0);
		Predicate h = Predicates.getPredicate("h", 0);

		assertEquals(7, strata.size());
		assertTrue(assertPredicateBeforeInOrder(a, b, strata));
		assertTrue(assertPredicateBeforeInOrder(b, c, strata));
		assertTrue(assertPredicateBeforeInOrder(c, h, strata));
		assertTrue(assertPredicateBeforeInOrder(c, d, strata));
		assertTrue(assertPredicateBeforeInOrder(d, e, strata));
		assertTrue(assertPredicateBeforeInOrder(e, f, strata));
		assertTrue(assertPredicateBeforeInOrder(d, f, strata));
	}

	@Test
	public void avoidDuplicatesTest1() {
		// r1 := b :- a.
		CompiledRule r1 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("b", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("a", 0)).toLiteral());
		// r2 := c :- b.
		CompiledRule r2 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("c", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("b", 0)).toLiteral());
		// r3 := c :- a.
		CompiledRule r3 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("c", 0))),
				Atoms.newBasicAtom(Predicates.getPredicate("a", 0)).toLiteral());
		Map<Integer, CompiledRule> rules = new HashMap<>();
		rules.put(0, r1);
		rules.put(1, r2);
		rules.put(2, r3);
		DependencyGraph dg = DependencyGraphImpl.buildDependencyGraph(rules);
		ComponentGraph cg = ComponentGraphImpl.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate a = Predicates.getPredicate("a", 0);
		Predicate b = Predicates.getPredicate("b", 0);
		Predicate c = Predicates.getPredicate("c", 0);

		assertTrue(assertPredicateBeforeInOrder(a, b, strata));
		assertTrue(assertPredicateBeforeInOrder(b, c, strata));
		assertTrue(assertPredicateBeforeInOrder(a, c, strata));

		assertEquals(3, strata.size());
	}

}
