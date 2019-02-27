package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ComponentGraph {

	private Map<Integer, SCComponent> components = new HashMap<>();
	private List<Integer> entryPointIds = new ArrayList<>();
	private List<Integer> unstratifyableIds = new ArrayList<>();

	public static ComponentGraph fromDependencyGraph(DependencyGraph dg) {
		Map<Integer, List<Node>> componentMap = dg.getStronglyConnectedComponents();
		ComponentGraph retVal = new ComponentGraph();
		for (Entry<Integer, List<Node>> entry : componentMap.entrySet()) {
			retVal.components.put(entry.getKey(), retVal.new SCComponent());
			retVal.components.get(entry.getKey()).nodes = entry.getValue();
		}
		// iterate again to populate components
		// needs all components initialized so we can work with connecting edges properly
		Node tmpSrcNode;
		for (Entry<Node, List<Edge>> nodeEntry : dg.getNodes().entrySet()) {
			tmpSrcNode = nodeEntry.getKey();
			for (Edge tmpEdge : nodeEntry.getValue()) {
				retVal.registerEdge(tmpSrcNode, tmpEdge);
			}
		}
		// iterate components once more to gather entry points and unstratifyable components
		int tmpComponentId;
		SCComponent tmpComponent;
		for (Entry<Integer, SCComponent> componentEntry : retVal.components.entrySet()) {
			tmpComponentId = componentEntry.getKey();
			tmpComponent = componentEntry.getValue();
			if (tmpComponent.hasNegativeCycle()) {
				retVal.unstratifyableIds.add(tmpComponentId);
			} else {
				if (tmpComponent.inboundEdges.size() == 0) {
					retVal.entryPointIds.add(tmpComponentId);
				}
			}
		}
		return retVal;
	}

	private void registerEdge(Node srcNode, Edge edge) {
		int srcComponentId = srcNode.getNodeInfo().getComponentId();
		int destComponentId = edge.getTarget().getNodeInfo().getComponentId();
		if (srcComponentId == destComponentId) {
			this.components.get(srcComponentId).addInternalEdge(srcNode, edge);
			if (!edge.getSign()) {
				this.components.get(srcComponentId).hasNegativeCycle = true;
			}
		} else {
			SCComponent srcComponent = this.components.get(srcComponentId);
			SCComponent destComponent = this.components.get(destComponentId);
			srcComponent.addOutboundEdge(srcNode, edge);
			destComponent.addInboundEdge(srcNode, edge);
		}
	}

	private class SCComponent {
		@SuppressWarnings("unused")
		private List<Node> nodes = new ArrayList<>();
		private Map<Node, List<Edge>> internalEdges = new HashMap<>();
		private Map<Node, List<Edge>> outboundEdges = new HashMap<>();
		private Map<Node, List<Edge>> inboundEdges = new HashMap<>();
		private boolean hasNegativeCycle;

		public void addInternalEdge(Node src, Edge edge) {
			this.internalEdges.putIfAbsent(src, new ArrayList<>());
			this.internalEdges.get(src).add(edge);
		}

		public void addOutboundEdge(Node src, Edge edge) {
			this.outboundEdges.putIfAbsent(src, new ArrayList<>());
			this.outboundEdges.get(src).add(edge);
		}

		public void addInboundEdge(Node src, Edge edge) {
			this.inboundEdges.putIfAbsent(src, new ArrayList<>());
			this.inboundEdges.get(src).add(edge);
		}

		public boolean hasNegativeCycle() {
			return this.hasNegativeCycle;
		}
	}

}
