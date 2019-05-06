package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

// TODO make final?
public class ComponentGraph {

	private final Map<Integer, SCComponent> components;
	private final List<SCComponent> entryPoints;

	public ComponentGraph(Map<Integer, SCComponent> components, List<SCComponent> entryPoints) {
		this.components = components;
		this.entryPoints = entryPoints;
	}

	public static ComponentGraph buildComponentGraph(DependencyGraph dg, Map<Integer, List<Node>> componentMap) {
		return new ComponentGraph.Builder(dg, componentMap).build();
	}

	public Map<Integer, SCComponent> getComponents() {
		return Collections.unmodifiableMap(this.components);
	}

	public List<SCComponent> getEntryPoints() {
		return Collections.unmodifiableList(this.entryPoints);
	}

	public static class SCComponent {

		private final int id;
		private final List<Node> nodes;
		private final Map<Integer, Boolean> dependencyIds;
		private final Set<Integer> dependentIds;
		private final boolean hasNegativeCycle;

		private SCComponent(int id, List<Node> nodes, Map<Integer, Boolean> dependencyIds, Set<Integer> dependentIds, boolean hasNegativeCycle) {
			this.id = id;
			this.nodes = nodes;
			this.dependencyIds = dependencyIds;
			this.dependentIds = dependentIds;
			this.hasNegativeCycle = hasNegativeCycle;
		}

		public Map<Integer, Boolean> getDependencyIds() {
			return Collections.unmodifiableMap(this.dependencyIds);
		}

		public Set<Integer> getDependentIds() {
			return Collections.unmodifiableSet(this.dependentIds);
		}

		@Override
		public String toString() {
			return "SCComponent{" + StringUtils.join(this.nodes, ",") + "}";
		}

		public boolean hasNegativeCycle() {
			return this.hasNegativeCycle;
		}

		public List<Node> getNodes() {
			return this.nodes;
		}

		public int getId() {
			return this.id;
		}

		private static class Builder {

			private int id;
			private List<Node> nodes;
			private Map<Integer, Boolean> dependencyIds = new HashMap<>();
			private Set<Integer> dependentIds = new HashSet<>();
			private boolean hasNegativeCycle;

			private Builder(int id, List<Node> nodes) {
				this.nodes = nodes;
				this.id = id;
			}

			private SCComponent build() {
				return new SCComponent(this.id, this.nodes, this.dependencyIds, this.dependentIds, this.hasNegativeCycle);
			}

		}

	}

	private static class Builder {

		private DependencyGraph depGraph;
		private Map<Integer, List<Node>> componentMap;
		private Map<Integer, SCComponent.Builder> componentBuilders = new HashMap<>();
		private Map<Integer, SCComponent> components = new HashMap<>();

		private Builder(DependencyGraph dg, Map<Integer, List<Node>> componentMap) {
			this.depGraph = dg;
			this.componentMap = componentMap;
		}

		private ComponentGraph build() {
			for (Entry<Integer, List<Node>> entry : this.componentMap.entrySet()) {
				this.componentBuilders.put(entry.getKey(), new SCComponent.Builder(entry.getKey(), entry.getValue()));
			}
			for (Entry<Integer, SCComponent.Builder> entry : this.componentBuilders.entrySet()) {
				for (Node node : entry.getValue().nodes) {
					for (Edge edge : this.depGraph.getNodes().get(node)) {
						this.registerEdge(entry.getKey(), edge);
					}
				}
			}
			List<SCComponent> startingPoints = new ArrayList<>();
			SCComponent tmpComponent;
			for (Entry<Integer, SCComponent.Builder> entry : this.componentBuilders.entrySet()) {
				tmpComponent = entry.getValue().build();
				this.components.put(entry.getKey(), tmpComponent);
				if (tmpComponent.getDependencyIds().isEmpty()) {
					startingPoints.add(tmpComponent);
				}
			}
			return new ComponentGraph(this.components, startingPoints);
		}

		private void registerEdge(int srcComponentId, Edge edge) {
			int destComponentId = edge.getTarget().getNodeInfo().getComponentId();
			if (srcComponentId == destComponentId) {
				if (!edge.getSign()) {
					this.componentBuilders.get(srcComponentId).hasNegativeCycle = true;
				}
			} else {
				SCComponent.Builder srcComponentBld = this.componentBuilders.get(srcComponentId);
				srcComponentBld.dependentIds.add(destComponentId);
				SCComponent.Builder destComponentBld = this.componentBuilders.get(destComponentId);
				boolean isPositiveDep = true;
				if (destComponentBld.dependencyIds.containsKey(srcComponentId)) {
					isPositiveDep = destComponentBld.dependencyIds.get(srcComponentId);
				}
				// if the dependency to srcComponent already is negative, it stays that way, otherwise determined by edge.sign
				destComponentBld.dependencyIds.put(srcComponentId, isPositiveDep & edge.getSign());
			}
		}

	}

}
