/**
 * Copyright (c) 2019, the Alpha Team.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.core.depgraph;

import org.apache.commons.lang3.StringUtils;

import at.ac.tuwien.kr.alpha.core.programs.InternalProgram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representation of an {@link InternalProgram}'s component graph, i.e. the directed acyclic graph resulting from condensing the program's
 * {@link DependencyGraph} into its strongly connected components. Needed in order to calculate stratifications from which an evaluation order for the
 * {@link at.ac.tuwien.kr.alpha.core.programs.transformation.StratifiedEvaluation} transformation can be derived.
 * 
 * Copyright (c) 2019-2020, the Alpha Team.
 */
public final class ComponentGraph {

	private final ArrayList<SCComponent> components;
	private final List<SCComponent> entryPoints;

	private ComponentGraph(ArrayList<SCComponent> components, List<SCComponent> entryPoints) {
		this.components = components;
		this.entryPoints = entryPoints;
	}

	/**
	 * Creates a new {@link ComponentGraph} based on a dependency graph and an {@link StronglyConnectedComponentsAlgorithm.SccResult} representing the
	 * result of calculating the dependency graph's strongly connected components (SCCs).
	 * 
	 * @param dg        the dependency graph backing this component graph.
	 * @param sccResult the SCC calculation result for the dependency graph in question.
	 * @return a new {@link ComponentGraph} representing the strongly connected components of the given dependency graph.
	 */
	public static ComponentGraph buildComponentGraph(DependencyGraph dg, StronglyConnectedComponentsAlgorithm.SccResult sccResult) {
		return new ComponentGraph.Builder(dg, sccResult).build();
	}

	public List<SCComponent> getComponents() {
		return Collections.unmodifiableList(components);
	}

	public List<SCComponent> getEntryPoints() {
		return Collections.unmodifiableList(entryPoints);
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
			return Collections.unmodifiableMap(dependencyIds);
		}

		public Set<Integer> getDependentIds() {
			return Collections.unmodifiableSet(dependentIds);
		}

		@Override
		public String toString() {
			return "SCComponent{" + StringUtils.join(nodes, ",") + "}";
		}

		public boolean hasNegativeCycle() {
			return hasNegativeCycle;
		}

		public List<Node> getNodes() {
			return nodes;
		}

		public int getId() {
			return id;
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
				return new SCComponent(id, nodes, dependencyIds, dependentIds, hasNegativeCycle);
			}

		}

	}

	private static class Builder {

		private DependencyGraph depGraph;
		private List<List<Node>> componentMap;
		private Map<Node, Integer> nodesByComponentId;
		private ArrayList<SCComponent.Builder> componentBuilders = new ArrayList<>();
		private ArrayList<SCComponent> components = new ArrayList<>();

		private Builder(DependencyGraph dg, StronglyConnectedComponentsAlgorithm.SccResult sccResult) {
			this.depGraph = dg;
			this.componentMap = sccResult.stronglyConnectedComponents;
			this.nodesByComponentId = sccResult.nodesByComponentId;
		}

		private ComponentGraph build() {
			// Create a ComponentBuilder for each component.
			for (int i = 0; i < componentMap.size(); i++) {
				componentBuilders.add(new SCComponent.Builder(i, componentMap.get(i)));
			}
			// Iterate and register each edge of every component.
			for (int i = 0; i < componentBuilders.size(); i++) {
				SCComponent.Builder builder = componentBuilders.get(i);
				for (Node node : builder.nodes) {
					for (Edge edge : depGraph.getAdjancencyMap().get(node)) {
						registerEdge(i, edge);
					}
				}
			}
			// Build each component and identify starting components.
			List<SCComponent> startingPoints = new ArrayList<>();
			for (SCComponent.Builder builder : componentBuilders) {
				SCComponent tmpComponent = builder.build();
				components.add(tmpComponent);
				// Component is starting if it has no dependencies.
				if (tmpComponent.getDependencyIds().isEmpty()) {
					startingPoints.add(tmpComponent);
				}
			}
			return new ComponentGraph(components, startingPoints);
		}

		private void registerEdge(int srcComponentId, Edge edge) {
			int destComponentId = nodesByComponentId.get(edge.getTarget());
			if (srcComponentId == destComponentId) {
				// Record negative self-loop.
				if (!edge.getSign()) {
					componentBuilders.get(srcComponentId).hasNegativeCycle = true;
				}
			} else {
				SCComponent.Builder srcComponentBld = componentBuilders.get(srcComponentId);
				srcComponentBld.dependentIds.add(destComponentId);
				SCComponent.Builder destComponentBld = componentBuilders.get(destComponentId);
				boolean isPositiveDep = true;
				if (destComponentBld.dependencyIds.containsKey(srcComponentId)) {
					isPositiveDep = destComponentBld.dependencyIds.get(srcComponentId);
				}
				// If the dependency to srcComponent already is negative, it stays that way, otherwise it is determined by the edge's sign.
				destComponentBld.dependencyIds.put(srcComponentId, isPositiveDep & edge.getSign());
			}
		}

	}

}
