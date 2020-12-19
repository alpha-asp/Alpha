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

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Algorithm to find strongly connected components of a given {@link DependencyGraph}.
 * 
 * Copyright (c) 2019-2020, the Alpha Team.
 */
public class StronglyConnectedComponentsAlgorithm {

	private Map<Node, Integer> nodesByComponentId = new HashMap<>();

	/**
	 * Calculates the strongly connected components of the given {@link DependencyGraph} according to the algorithm
	 * given in "Introduction to algorithms" by Cormen, Leiserson, Rivest, and Stein
	 * (aka <a href="https://en.wikipedia.org/wiki/Kosaraju%27s_algorithm">Kosajaru-algorithm</a>).
	 * 
	 * @param dg the {@link DependencyGraph} for which to compute the strongly-connected components.
	 * @return an {@link SccResult} containing a list of all strongly-connected components and a mapping of each
	 * 	node to the component it belongs to.
	 */
	public static SccResult findStronglyConnectedComponents(DependencyGraph dg) {
		return new StronglyConnectedComponentsAlgorithm().runStronglyConnectedComponentsAlgorithm(dg);
	}

	private SccResult runStronglyConnectedComponentsAlgorithm(DependencyGraph dg) {
		// Find al SCCs with Kosajaru's algorithm.
		DepthFirstSearchAlgorithm.DfsResult intermediateResult = DepthFirstSearchAlgorithm.performDfs(dg.getAdjancencyMap());
		Map<Node, List<Edge>> transposedNodes = transposeGraph(dg.getAdjancencyMap());
		Deque<Node> finishedNodes = intermediateResult.finishedNodes;
		DepthFirstSearchAlgorithm.DfsResult finalResult = DepthFirstSearchAlgorithm.performDfs(finishedNodes.descendingIterator(), transposedNodes);
		// Extract components.
		List<List<Node>> componentMap = extractComponents(finalResult);
		return new SccResult(componentMap, nodesByComponentId);
	}

	private List<List<Node>> extractComponents(DepthFirstSearchAlgorithm.DfsResult dfsResult) {
		// Each node reachable from "null" in the DfsResult points to a different strongly-connected component.
		List<List<Node>> componentMap = new ArrayList<>();
		for (Node componentRoot : dfsResult.depthFirstReachable.get(null)) {
			int componentId = componentMap.size();
			// Note: every node inside a component is reachable from its root, even the root itself.
			List<Node> nodesInComponent = dfsResult.depthFirstReachable.get(componentRoot);
			for (Node node : nodesInComponent) {
				nodesByComponentId.put(node, componentId);
			}
			List<Node> componentMembers = new ArrayList<>(nodesInComponent);
			componentMap.add(componentMembers);
		}
		return componentMap;
	}

	/**
	 * Transposes the given dependency graph, i.e. reverses the direction of each edge.
	 */
	private static Map<Node, List<Edge>> transposeGraph(Map<Node, List<Edge>> graph) {
		Map<Node, List<Edge>> transposed = new HashMap<>();
		for (Map.Entry<Node, List<Edge>> entry : graph.entrySet()) {
			Node srcNode = entry.getKey();
			if (!transposed.containsKey(srcNode)) {
				transposed.put(srcNode, new ArrayList<>());
			}
			for (Edge e : entry.getValue()) {
				Node targetNode = e.getTarget();
				if (!transposed.containsKey(e.getTarget())) {
					transposed.put(targetNode, new ArrayList<>());
				}
				transposed.get(targetNode).add(new Edge(srcNode, e.getSign()));
			}
		}
		return transposed;
	}

	/**
	 * Represents the result of a search for strongly connected components on a {@link DependencyGraph}.
	 */
	public static class SccResult {

		final List<List<Node>> stronglyConnectedComponents;	// List of strongly-connected-components (each component being a list of nodes).
		final Map<Node, Integer> nodesByComponentId;		// Reverse association of stronglyConnectedComponents.

		SccResult(List<List<Node>> components, Map<Node, Integer> nodesByComponentId) {
			this.stronglyConnectedComponents = components;
			this.nodesByComponentId = nodesByComponentId;
		}

	}
}
