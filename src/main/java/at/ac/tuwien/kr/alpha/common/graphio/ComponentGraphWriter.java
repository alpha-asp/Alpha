package at.ac.tuwien.kr.alpha.common.graphio;

import java.io.OutputStream;
import java.io.PrintStream;

import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph;
import at.ac.tuwien.kr.alpha.common.depgraph.Node;

public class ComponentGraphWriter {

	private static final String DEFAULT_GRAPH_HEADING = "digraph componentGraph";

	public void writeAsDot(ComponentGraph graph, OutputStream out) {
		PrintStream ps = new PrintStream(out);
		this.startGraph(ps);
		this.writeComponentsTable(ps, graph);
		this.writeGraph(ps, graph);
		this.finishGraph(ps);
		ps.close();
	}

	private void startGraph(PrintStream ps) {
		ps.println(ComponentGraphWriter.DEFAULT_GRAPH_HEADING);
		ps.println("{");
		ps.println("splines=false;");
		ps.println("ranksep=4.0;");
	}

	private void writeComponentsTable(PrintStream ps, ComponentGraph cg) {
		ps.println("label = <");
		ps.println("\t<table border = '1' cellborder = '0'>");
		StringBuilder headerBuilder = new StringBuilder("<tr>");
		headerBuilder.append("<td>Component Id</td>");
		for (int i = 0; i < cg.getComponents().size(); i++) {
			headerBuilder.append("<td>").append(i).append("</td>");
		}
		headerBuilder.append("</tr>");
		ps.println("\t\t" + headerBuilder.toString());
		StringBuilder contentBuilder = new StringBuilder("<tr>");
		for (int i = 0; i < cg.getComponents().size(); i++) {
			contentBuilder.append("<td>");
			for (Node n : cg.getComponents().get(i).getNodes()) {
				contentBuilder.append(n.getLabel()).append("<br/>");
			}
			contentBuilder.append("</td>");
		}
		contentBuilder.append("</tr>");
		ps.println("\t\t" + contentBuilder.toString());
		ps.println("\t</table>");
		ps.println(">");
	}

	private void writeGraph(PrintStream ps, ComponentGraph cg) {

	}

	private void finishGraph(PrintStream ps) {
		ps.println("}");
	}

}
