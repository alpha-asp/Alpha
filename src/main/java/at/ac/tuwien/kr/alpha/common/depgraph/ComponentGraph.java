package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ComponentGraph {

	private Map<Integer, SCComponent> components = new HashMap<>();

	public static ComponentGraph buildFromDependencyGraph(DependencyGraph dg) {
		Map<Integer, List<Node>> componentMap = dg.getStronglyConnectedComponents();
		ComponentGraph retVal = new ComponentGraph();
		for (Entry<Integer, List<Node>> entry : componentMap.entrySet()) {
			retVal.components.put(entry.getKey(), retVal.new SCComponent());
		}
		return null;
	}

	public void addComponent(int id, SCComponent component) {
		this.components.put(id, component);
	}

	private class SCComponent {
		private Map<Node, List<Edge>> nodes;
		private Map<Node, List<Edge>> outboundEdges;
		private Map<Node, List<Edge>> inboundEdges;
		private boolean hasNegativeCycle;

		public Map<Node, List<Edge>> getNodes() {
			return this.nodes;
		}

		public void setNodes(Map<Node, List<Edge>> nodes) {
			this.nodes = nodes;
		}

		public Map<Node, List<Edge>> getOutboundEdges() {
			return this.outboundEdges;
		}

		public void setOutboundEdges(Map<Node, List<Edge>> outboundEdges) {
			this.outboundEdges = outboundEdges;
		}

		public Map<Node, List<Edge>> getInboundEdges() {
			return this.inboundEdges;
		}

		public void setInboundEdges(Map<Node, List<Edge>> inboundEdges) {
			this.inboundEdges = inboundEdges;
		}

		public boolean hasNegativeCycle() {
			return this.hasNegativeCycle;
		}

		public void setHasNegativeCycle(boolean hasNegativeCycle) {
			this.hasNegativeCycle = hasNegativeCycle;
		}
	}

}
