package at.ac.tuwien.kr.alpha.common.depgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;
import at.ac.tuwien.kr.alpha.common.program.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.NormalProgram;


public class StratificationAlgorithmTest {

	private boolean predicateIsBeforePredicateInOrder(Predicate predBefore, Predicate predAfter, List<SCComponent> order) {
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
		Alpha system = new Alpha();
		InputProgram prog = system.readProgramString("a :- b.", null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate a = Predicate.getInstance("a", 0);
		Predicate b = Predicate.getInstance("b", 0);

		assertEquals(2, strata.size());
		assertTrue(predicateIsBeforePredicateInOrder(b, a, strata));
	}

	@Test
	public void stratifyTwoRulesTest() {
		Alpha system = new Alpha();
		StringBuilder bld = new StringBuilder();
		bld.append("b :- a.").append("\n");
		bld.append("c :- b.").append("\n");
		InputProgram prog = system.readProgramString(bld.toString(), null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate a = Predicate.getInstance("a", 0);
		Predicate b = Predicate.getInstance("b", 0);
		Predicate c = Predicate.getInstance("c", 0);

		assertEquals(3, strata.size());
		assertTrue(predicateIsBeforePredicateInOrder(a, b, strata));
		assertTrue(predicateIsBeforePredicateInOrder(b, c, strata));
		assertTrue(predicateIsBeforePredicateInOrder(a, c, strata));
	}

	@Test
	public void stratifyWithNegativeDependencyTest() {
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
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate a = Predicate.getInstance("a", 0);
		Predicate b = Predicate.getInstance("b", 0);
		Predicate c = Predicate.getInstance("c", 0);
		Predicate d = Predicate.getInstance("d", 0);
		Predicate e = Predicate.getInstance("e", 0);

		assertEquals(5, strata.size());
		assertTrue(predicateIsBeforePredicateInOrder(a, b, strata));
		assertTrue(predicateIsBeforePredicateInOrder(b, c, strata));
		assertTrue(predicateIsBeforePredicateInOrder(c, d, strata));
		assertTrue(predicateIsBeforePredicateInOrder(d, e, strata));
	}

	@Test
	public void stratifyWithPositiveCycleTest() {
		Alpha system = new Alpha();
		StringBuilder bld = new StringBuilder();
		bld.append("ancestor_of(X, Y) :- parent_of(X, Y).");
		bld.append("ancestor_of(X, Z) :- parent_of(X, Y), ancestor_of(Y, Z).");
		InputProgram prog = system.readProgramString(bld.toString(), null);
		NormalProgram normalProg = system.normalizeProgram(prog);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normalProg);
		DependencyGraph dg = analyzed.getDependencyGraph();
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate ancestorOf = Predicate.getInstance("ancestor_of", 2);
		Predicate parentOf = Predicate.getInstance("parent_of", 2);

		assertEquals(2, strata.size());
		assertTrue(predicateIsBeforePredicateInOrder(parentOf, ancestorOf, strata));
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
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate a = Predicate.getInstance("a", 0);
		Predicate b = Predicate.getInstance("b", 0);
		Predicate c = Predicate.getInstance("c", 0);
		Predicate d = Predicate.getInstance("d", 0);
		Predicate e = Predicate.getInstance("e", 0);
		Predicate f = Predicate.getInstance("f", 0);
		Predicate h = Predicate.getInstance("h", 0);

		assertTrue(predicateIsBeforePredicateInOrder(a, h, strata));
		assertTrue(predicateIsBeforePredicateInOrder(b, h, strata));
		assertTrue(predicateIsBeforePredicateInOrder(c, h, strata));

		assertTrue(predicateIsBeforePredicateInOrder(a, f, strata));
		assertTrue(predicateIsBeforePredicateInOrder(b, f, strata));
		assertTrue(predicateIsBeforePredicateInOrder(c, f, strata));
		assertTrue(predicateIsBeforePredicateInOrder(d, f, strata));
		assertTrue(predicateIsBeforePredicateInOrder(e, f, strata));
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
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate a = Predicate.getInstance("a", 0);
		Predicate b = Predicate.getInstance("b", 0);
		Predicate c = Predicate.getInstance("c", 0);
		Predicate d = Predicate.getInstance("d", 0);
		Predicate e = Predicate.getInstance("e", 0);
		Predicate f = Predicate.getInstance("f", 0);
		Predicate h = Predicate.getInstance("h", 0);


		assertEquals(7, strata.size());
		assertTrue(predicateIsBeforePredicateInOrder(a, b, strata));
		assertTrue(predicateIsBeforePredicateInOrder(b, c, strata));
		assertTrue(predicateIsBeforePredicateInOrder(c, h, strata));
		assertTrue(predicateIsBeforePredicateInOrder(c, d, strata));
		assertTrue(predicateIsBeforePredicateInOrder(d, e, strata));
		assertTrue(predicateIsBeforePredicateInOrder(e, f, strata));
		assertTrue(predicateIsBeforePredicateInOrder(d, f, strata));
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
		ComponentGraph cg = ComponentGraph.buildComponentGraph(dg, StronglyConnectedComponentsAlgorithm.findStronglyConnectedComponents(dg));
		List<SCComponent> strata = StratificationAlgorithm.calculateStratification(cg);

		Predicate a = Predicate.getInstance("a", 0);
		Predicate b = Predicate.getInstance("b", 0);
		Predicate c = Predicate.getInstance("c", 0);

		assertTrue(predicateIsBeforePredicateInOrder(a, b, strata));
		assertTrue(predicateIsBeforePredicateInOrder(b, c, strata));
		assertTrue(predicateIsBeforePredicateInOrder(a, c, strata));

		assertEquals(3, strata.size());
	}

}
