package at.ac.tuwien.kr.alpha.common.depgraph;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.Alpha;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;
import at.ac.tuwien.kr.alpha.grounder.structure.ProgramAnalysis;

public class ComponentGraphTest {

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

		Assert.assertEquals(2, strata.size());

		List<SCComponent> stratum0 = strata.get(0);
		Assert.assertEquals(3, stratum0.size());

		List<SCComponent> stratum1 = strata.get(1);
		Assert.assertEquals(2, stratum1.size());
		// TODO check correct components in stratum
	}

}
