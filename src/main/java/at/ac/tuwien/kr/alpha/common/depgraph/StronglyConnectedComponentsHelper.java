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

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to find strongly connected components of a given {@link DependencyGraph}.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class StronglyConnectedComponentsHelper {

	private DepthFirstSearchAlgorithm dfsHelper = new DepthFirstSearchAlgorithm();

	private Map<Node, Integer> nodesByComponentId;

	private void reset() {
		nodesByComponentId = new HashMap<>();
	}

	/**
	 * Calculates the strongly connected components of the given {@link DependencyGraph} according to the algorithm given in "Introduction to algorithms" by
	 * Cormen, Leiserson, Rivest, and Stein (aka <a href="https://en.wikipedia.org/wiki/Kosaraju%27s_algorithm">Kosajaru-algorithm</a>)
	 * 
	 * @param dg
	 * @return
	 */
	public SccResult findStronglyConnectedComponents(DependencyGraph dg) {
		reset();
		DepthFirstSearchAlgorithm.DfsResult intermediateResult = dfsHelper.performDfs(dg.getAdjancencyMap());
		Map<Node, List<Edge>> transposedNodes = transposeGraph(dg.getAdjancencyMap());
		Deque<Node> finishedNodes = intermediateResult.finishedNodes;
		DepthFirstSearchAlgorithm.DfsResult finalResult = dfsHelper.performDfs(finishedNodes.descendingIterator(), transposedNodes);
		List<List<Node>> componentMap = extractComponents(finalResult);
		return new SccResult(componentMap, nodesByComponentId);
	}

	private List<List<Node>> extractComponents(DepthFirstSearchAlgorithm.DfsResult dfsResult) {
		List<List<Node>> componentMap = new ArrayList<>();
		List<Node> tmpComponentMembers;
		for (Node componentRoot : dfsResult.depthFirstReachable.get(null)) {
			tmpComponentMembers = new ArrayList<>();
			addComponentMembers(componentRoot, dfsResult.depthFirstReachable, tmpComponentMembers, componentMap.size());
			componentMap.add(tmpComponentMembers);
		}
		return componentMap;
	}

	private void addComponentMembers(Node depthFirstTreeNode, Map<Node, List<Node>> depthFirstForest, List<Node> componentMembers, int componentId) {
		componentMembers.add(depthFirstTreeNode);
		nodesByComponentId.put(depthFirstTreeNode, componentId);
		List<Node> children;
		if ((children = depthFirstForest.get(depthFirstTreeNode)) == null) {
			return;
		}
		for (Node n : children) {
			addComponentMembers(n, depthFirstForest, componentMembers, componentId);
		}
	}

	/**
	 * Transposes the given dependency graph, i.e. reverses the direction of each edge.
	 */
	private Map<Node, List<Edge>> transposeGraph(Map<Node, List<Edge>> graph) {
		Map<Node, List<Edge>> transposed = new HashMap<>();
		Node srcNode;
		Node targetNode;
		for (Map.Entry<Node, List<Edge>> entry : graph.entrySet()) {
			srcNode = entry.getKey();
			if (!transposed.containsKey(srcNode)) {
				transposed.put(srcNode, new ArrayList<>());
			}
			for (Edge e : entry.getValue()) {
				targetNode = e.getTarget();
				if (!transposed.containsKey(e.getTarget())) {
					transposed.put(targetNode, new ArrayList<>());
				}
				transposed.get(targetNode).add(new Edge(entry.getKey(), e.getSign()));
			}
		}
		return transposed;
	}

}
