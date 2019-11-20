package at.ac.tuwien.kr.alpha.common.graphio;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;

import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;
import at.ac.tuwien.kr.alpha.common.depgraph.Node;

public class ComponentGraphWriter {

	private static final String GRAPH_HEADING = "digraph componentGraph";

	private static final String NODE_FMT = "n%d [label = C%d]\n";
	private static final String EDGE_FMT = "n%d -> n%d [xlabel=\"%s\" labeldistance=0.1]\n";

	public void writeAsDot(ComponentGraph graph, OutputStream out) {
		PrintStream ps = new PrintStream(out);
		this.startGraph(ps);
		this.writeComponentsTable(ps, graph);
		ps.println();
		this.writeGraph(ps, graph);
		this.finishGraph(ps);
		ps.close();
	}

	private void startGraph(PrintStream ps) {
		ps.println(ComponentGraphWriter.GRAPH_HEADING);
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
		contentBuilder.append("<td>Predicates</td>");
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
		Map<Integer, SCComponent> components = cg.getComponents();
		// write the node descriptors for the components
		for (int componentId : components.keySet()) {
			ps.printf(NODE_FMT, componentId, componentId);
			for (Entry<Integer, Boolean> dependency : components.get(componentId).getDependencyIds().entrySet()) {
				ps.printf(EDGE_FMT, dependency.getKey(), componentId, dependency.getValue().equals(true) ? "+" : "-");
			}
		}
	}

	private void finishGraph(PrintStream ps) {
		ps.println("}");
	}

}
