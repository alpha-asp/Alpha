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
package at.ac.tuwien.kr.alpha.common.depgraph;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import at.ac.tuwien.kr.alpha.common.program.InternalProgram;

import java.util.Set;

/**
 * Representation of an {@link InternalProgram}'s component graph, i.e. the directed acyclic graph resulting from condensing the program's
 * {@link DependencyGraph} into its strongly connected components. Needed in order to calculate stratifications from which an evaluation order for the
 * {@link at.ac.tuwien.kr.alpha.grounder.transformation.StratifiedEvaluation} transformation can be derived.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public final class ComponentGraph {

	private final Map<Integer, SCComponent> components;
	private final List<SCComponent> entryPoints;

	private ComponentGraph(Map<Integer, SCComponent> components, List<SCComponent> entryPoints) {
		this.components = components;
		this.entryPoints = entryPoints;
	}

	/**
	 * Creates a new {@link ComponentGraph} based on a dependency graph and an {@link SccResult} representing the result of calculating the dependency graph's
	 * strongly connected components (SCCs)
	 * 
	 * @param dg        the dependency graph backing this component graph
	 * @param sccResult the SCC calculation result for the dependency graph in question
	 * @return a new {@link ComponentGraph} representing the strongly connected components of the given dependency graph
	 */
	public static ComponentGraph buildComponentGraph(DependencyGraph dg, SccResult sccResult) {
		return new ComponentGraph.Builder(dg, sccResult).build();
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
		private Map<Node, Integer> nodesByComponentId;
		private Map<Integer, SCComponent.Builder> componentBuilders = new HashMap<>();
		private Map<Integer, SCComponent> components = new HashMap<>();

		private Builder(DependencyGraph dg, SccResult sccResult) {
			this.depGraph = dg;
			this.componentMap = sccResult.getStronglyConnectedComponents();
			this.nodesByComponentId = sccResult.getNodesByComponentId();
		}

		private ComponentGraph build() {
			for (Entry<Integer, List<Node>> entry : this.componentMap.entrySet()) {
				this.componentBuilders.put(entry.getKey(), new SCComponent.Builder(entry.getKey(), entry.getValue()));
			}
			for (Entry<Integer, SCComponent.Builder> entry : this.componentBuilders.entrySet()) {
				for (Node node : entry.getValue().nodes) {
					for (Edge edge : this.depGraph.getAdjancencyMap().get(node)) {
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
			int destComponentId = this.nodesByComponentId.get(edge.getTarget());
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
