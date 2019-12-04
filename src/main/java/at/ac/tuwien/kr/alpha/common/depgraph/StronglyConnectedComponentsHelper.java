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

	private DepthFirstSearchHelper dfsHelper = new DepthFirstSearchHelper();

	private Map<Node, Integer> nodesByComponentId;

	private void reset() {
		this.nodesByComponentId = new HashMap<>();
	}

	/**
	 * Calculates the strongly connected components of the given {@link DependencyGraph} according to the algorithm given in "Introduction to algorithms" by
	 * Cormen, Leiserson, Rivest, and Stein (aka "Kosajaru-algorithm", {@link https://en.wikipedia.org/wiki/Kosaraju%27s_algorithm})
	 * 
	 * @param dg
	 * @return
	 */
	public SCCResult findStronglyConnectedComponents(DependencyGraph dg) {
		this.reset();
		DfsResult intermediateResult = this.dfsHelper.performDfs(dg.getNodes());
		Map<Node, List<Edge>> transposedNodes = this.transposeGraph(dg.getNodes());
		Deque<Node> finishedNodes = intermediateResult.getFinishedNodes();
		DfsResult finalResult = this.dfsHelper.performDfs(finishedNodes.descendingIterator(), transposedNodes);
		Map<Integer, List<Node>> componentMap = this.extractComponents(finalResult);
		return new SCCResult(componentMap, this.nodesByComponentId);
	}

	private Map<Integer, List<Node>> extractComponents(DfsResult dfsResult) {
		int componentCnt = 0;
		Map<Integer, List<Node>> componentMap = new HashMap<>();
		List<Node> tmpComponentMembers;
		for (Node componentRoot : dfsResult.getDepthFirstForest().get(null)) {
			tmpComponentMembers = new ArrayList<>();
			this.addComponentMembers(componentRoot, dfsResult.getDepthFirstForest(), tmpComponentMembers, componentCnt);
			componentMap.put(componentCnt, tmpComponentMembers);
			componentCnt++;
		}
		return componentMap;
	}

	private void addComponentMembers(Node depthFirstTreeNode, Map<Node, List<Node>> depthFirstForest, List<Node> componentMembers, int componentId) {
		componentMembers.add(depthFirstTreeNode);
		this.nodesByComponentId.put(depthFirstTreeNode, componentId);
		List<Node> children;
		if ((children = depthFirstForest.get(depthFirstTreeNode)) == null) {
			return;
		}
		for (Node n : children) {
			this.addComponentMembers(n, depthFirstForest, componentMembers, componentId);
		}
	}

	/**
	 * (Re-)Writes the <code>transposedNodes</code> of this <code>DependencyGraph</code>
	 */
	private Map<Node, List<Edge>> transposeGraph(Map<Node, List<Edge>> nodes) {
		Map<Node, List<Edge>> transposed = new HashMap<>();
		Node srcNode;
		Node targetNode;
		for (Map.Entry<Node, List<Edge>> entry : nodes.entrySet()) {
			srcNode = entry.getKey();
			if (!transposed.containsKey(srcNode)) {
				transposed.put(srcNode, new ArrayList<>());
			}
			for (Edge e : entry.getValue()) {
				targetNode = e.getTarget();
				if (!transposed.containsKey(e.getTarget())) {
					transposed.put(targetNode, new ArrayList<>());
				}
				transposed.get(targetNode).add(new Edge(entry.getKey(), e.getSign(), e.getLabel()));
			}
		}
		return transposed;
	}

	public static class SCCResult {

		private final Map<Integer, List<Node>> stronglyConnectedComponents;
		private final Map<Node, Integer> nodesByComponentId;

		private SCCResult(Map<Integer, List<Node>> components, Map<Node, Integer> nodesByComponentId) {
			this.stronglyConnectedComponents = components;
			this.nodesByComponentId = nodesByComponentId;
		}

		public Map<Integer, List<Node>> getStronglyConnectedComponents() {
			return this.stronglyConnectedComponents;
		}

		public Map<Node, Integer> getNodesByComponentId() {
			return this.nodesByComponentId;
		}

	}

}
