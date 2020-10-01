package at.ac.tuwien.kr.alpha.common.graphio;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph;
import at.ac.tuwien.kr.alpha.common.program.AnalyzedProgram;

public class ComponentGraphWriterTest {

	@Test
	public void smokeTest() {
		// Note: rather than testing correct implementation of dot file format,
		// (which would be a lot of work), just test correct dot code generation
		// for one component graph that has all possible "special" node constellations,
		// i.e. positive and negative dependencies, cycle through negation, constraints.
		String asp = "p(X) :- q(X), r(X).\n" +
				"s(X) :- p(X), q(X), not r(X).\n" +
				"t(X) :- p(X), not u(X).\n" +
				"u(X) :- p(X), not t(X).\n" +
				":- p(X), not q(X), not r(X).";
		String expectedGraph = "digraph componentGraph\n" +
				"{\n" +
				"splines=false;\n" +
				"ranksep=4.0;\n" +
				"label = <\n" +
				"	<table border = '1' cellborder = '0'>\n" +
				"		<tr><td>Component Id</td><td>0</td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td></tr>\n" +
				"		<tr><td>Predicates</td><td>q/1<br/></td><td>r/1<br/></td><td>p/1<br/></td><td>[constr_1]/0<br/></td><td>t/1<br/>u/1<br/></td><td>s/1<br/></td></tr>\n"
				+
				"	</table>\n" +
				">\n" +
				"\n" +
				"n0 [label = C0]\n" +
				"n1 [label = C1]\n" +
				"n2 [label = C2]\n" +
				"n0 -> n2 [xlabel=\"+\" labeldistance=0.1]\n" +
				"n1 -> n2 [xlabel=\"+\" labeldistance=0.1]\n" +
				"n3 [label = C3]\n" +
				"n0 -> n3 [xlabel=\"-\" labeldistance=0.1]\n" +
				"n1 -> n3 [xlabel=\"-\" labeldistance=0.1]\n" +
				"n2 -> n3 [xlabel=\"+\" labeldistance=0.1]\n" +
				"n4 [label = C4]\n" +
				"n2 -> n4 [xlabel=\"+\" labeldistance=0.1]\n" +
				"n5 [label = C5]\n" +
				"n0 -> n5 [xlabel=\"+\" labeldistance=0.1]\n" +
				"n1 -> n5 [xlabel=\"-\" labeldistance=0.1]\n" +
				"n2 -> n5 [xlabel=\"+\" labeldistance=0.1]\n" +
				"}\n";
		Alpha alpha = new Alpha();
		AnalyzedProgram prog = AnalyzedProgram.analyzeNormalProgram(
				alpha.normalizeProgram(alpha.readProgramString(asp)));
		ComponentGraph compgraph = prog.getComponentGraph();
		ComponentGraphWriter writer = new ComponentGraphWriter();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeAsDot(compgraph, out);
		String actualGraph = out.toString();
		System.out.println(actualGraph);
		Assert.assertEquals(expectedGraph, actualGraph);
	}

}
