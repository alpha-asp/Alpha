package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentGraph {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComponentGraph.class);

	private Map<Integer, SCComponent> components = new HashMap<>();
	private List<SCComponent> entryPoints = new ArrayList<>();

	public static ComponentGraph fromDependencyGraph(DependencyGraph dg) {
		Map<Integer, List<Node>> componentMap = dg.getStronglyConnectedComponents();
		ComponentGraph retVal = new ComponentGraph();
		for (Entry<Integer, List<Node>> entry : componentMap.entrySet()) {
			retVal.components.put(entry.getKey(), retVal.new SCComponent(entry.getKey()));
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
		SCComponent tmpComponent;
		for (Entry<Integer, SCComponent> componentEntry : retVal.components.entrySet()) {
			tmpComponent = componentEntry.getValue();
			if (tmpComponent.inboundEdges.size() == 0) {
				retVal.entryPoints.add(tmpComponent);
			}
		}
		return retVal;
	}

	public Map<Integer, List<SCComponent>> calculateStratification() {
		Map<Integer, List<SCComponent>> retVal = new HashMap<>();
		LOGGER.debug("Initial call to stratify with entry points!");
		this.stratify(this.entryPoints, retVal);
		return retVal;
	}

	private void stratify(List<SCComponent> currComponents, Map<Integer, List<SCComponent>> strata) {
		List<SCComponent> nextComps = new ArrayList<>();
		LOGGER.debug("Starting stratify run - currComponents = {}", StringUtils.join(currComponents, ","));
		for (SCComponent comp : currComponents) {
			nextComps.addAll(this.stratifyComponent(comp, strata));
		}
		if (!nextComps.isEmpty()) {
			this.stratify(nextComps, strata);
		} else {
			LOGGER.debug("Stratification finished - no more components to work off!");
		}
	}

	private Set<SCComponent> stratifyComponent(SCComponent comp, Map<Integer, List<SCComponent>> strata) {
		Set<SCComponent> retVal = new HashSet<>();
		Map<SCComponent, Boolean> dependencies = comp.getDependencies();
		int maxDepStratum = 0;
		boolean canStratify = true;
		SCComponent dep = null;
		if (comp.hasNegativeCycle()) {
			// no need to check dependencies if we aren't stratifyable
			comp.unstratifyable = true;
			canStratify = false;
		} else {
			for (Entry<SCComponent, Boolean> depEntry : dependencies.entrySet()) {
				dep = depEntry.getKey();
				if (dep.stratum == -1) {
					// NOT breaking out of loop here, need to make sure unstratifyability is propagated
					canStratify = false;
					if (dep.isUnstratifyable()) {
						comp.unstratifyable = true;
					}
				} else {
					maxDepStratum = (dep.stratum > maxDepStratum) ? dep.stratum : maxDepStratum;
					if (depEntry.getValue().equals(false)) {
						maxDepStratum++;
					}
				}
			}
		}
		if (canStratify) {
			strata.putIfAbsent(maxDepStratum, new ArrayList<>());
			strata.get(maxDepStratum).add(comp);
			comp.stratum = maxDepStratum;
		}
		if (canStratify || comp.isUnstratifyable()) {
			// set up dependent compomponents for next step
			// also do this for unstratifyable components since we need to propagate unstratifyability
			// NOTE: this will lead to every node in the graph being explored
			// FIXME repeated addAll(..) calls and Set/List ping-pong is inefficient like hell
			retVal = comp.getDependents();
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
				this.components.get(srcComponentId).unstratifyable = true;
			}
		} else {
			SCComponent srcComponent = this.components.get(srcComponentId);
			SCComponent destComponent = this.components.get(destComponentId);
			srcComponent.addOutboundEdge(srcNode, edge);
			destComponent.addInboundEdge(srcNode, edge);
		}
	}

	public Map<Integer, SCComponent> getComponents() {
		return this.components;
	}

	public List<SCComponent> getEntryPoints() {
		return this.entryPoints;
	}

	public class SCComponent {

		@SuppressWarnings("unused")
		private int id;
		private List<Node> nodes = new ArrayList<>();
		private Map<Node, List<Edge>> internalEdges = new HashMap<>();
		private Map<Node, List<Edge>> outboundEdges = new HashMap<>();
		private Map<Node, List<Edge>> inboundEdges = new HashMap<>();
		private boolean hasNegativeCycle;
		private boolean unstratifyable;
		private int stratum = -1;

		public SCComponent(int id) {
			this.id = id;
		}

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

		public Map<SCComponent, Boolean> getDependencies() {
			Map<SCComponent, Boolean> retVal = new HashMap<>();
			int depComponentId;
			SCComponent dep;
			boolean isPositiveDep = true;
			for (Entry<Node, List<Edge>> entry : this.inboundEdges.entrySet()) {
				depComponentId = entry.getKey().getNodeInfo().getComponentId();
				dep = ComponentGraph.this.components.get(depComponentId);
				isPositiveDep = true;
				for (Edge e : entry.getValue()) {
					if (!e.getSign()) {
						isPositiveDep = false;
					}
				}
				if (retVal.containsKey(dep)) {
					if (!isPositiveDep) {
						retVal.put(dep, false);
					}
				} else {
					retVal.put(dep, isPositiveDep);
				}
			}
			return retVal;
		}

		public Set<SCComponent> getDependents() {
			Set<SCComponent> retVal = new HashSet<>();
			for (List<Edge> edgeList : this.outboundEdges.values()) {
				for (Edge e : edgeList) {
					retVal.add(ComponentGraph.this.components.get(e.getTarget().getNodeInfo().getComponentId()));
				}
			}
			return retVal;
		}

		@Override
		public String toString() {
			return "SCComponent{" + StringUtils.join(this.nodes, ",") + "}";
		}

		public boolean isUnstratifyable() {
			return this.unstratifyable;
		}

		public boolean hasNegativeCycle() {
			return this.hasNegativeCycle;
		}

		public List<Node> getNodes() {
			return this.nodes;
		}

		public void setNodes(List<Node> nodes) {
			this.nodes = nodes;
		}

	}

}
