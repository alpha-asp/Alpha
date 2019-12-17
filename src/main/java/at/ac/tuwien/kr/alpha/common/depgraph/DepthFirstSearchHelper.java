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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DepthFirstSearchHelper {

	private Set<Node> discoveredNodes;
	private Deque<Node> finishedNodes;
	private Map<Node, List<Node>> depthFirstForest;

	private void reset() {
		this.discoveredNodes = new HashSet<>();
		this.finishedNodes = new LinkedList<>();
		this.depthFirstForest = new HashMap<>();
		this.depthFirstForest.put(null, new ArrayList<>());
	}

	public DfsResult performDfs(Map<Node, List<Edge>> graph) {
		return this.performDfs(graph.keySet().iterator(), graph);
	}

	/**
	 * Performs a depth-first search on the given graph. The algorithm follows the approach outlined in 
	 * "Introduction to Algortihms, 3rd Edition" by Cormen et al. 
	 * Discovered nodes are "gray" and finished nodes "black", respectively, in the terminology used in the book.
	 * 
	 * @param nodeVisitIt an Iterator defining in which sequence nodes should be visited
	 * @param graph       an adjacency map defining the dependency graph of an ASP program
	 * @return a Set<Node> holding all finished nodes (i.e. all nodes at the end of the DFS run)
	 */
	public DfsResult performDfs(Iterator<Node> nodeVisitIt, Map<Node, List<Edge>> graph) {
		this.reset();
		DfsResult retVal = new DfsResult();
		Node tmp;
		while (nodeVisitIt.hasNext()) {
			tmp = nodeVisitIt.next();
			if (!(this.discoveredNodes.contains(tmp) || this.finishedNodes.contains(tmp))) {
				this.depthFirstForest.get(null).add(tmp);
				this.dfsVisit(tmp, graph);
			}
		}
		retVal.setFinishedNodes(this.finishedNodes);
		retVal.setDepthFirstForest(this.depthFirstForest);
		return retVal;
	}

	private void dfsVisit(Node currNode, Map<Node, List<Edge>> graph) {
		this.discoveredNodes.add(currNode);
		Node tmpNeighbor;
		for (Edge e : graph.get(currNode)) {
			// progress to adjacent nodes
			tmpNeighbor = e.getTarget();
			if (!(this.discoveredNodes.contains(tmpNeighbor) || this.finishedNodes.contains(tmpNeighbor))) {
				if (!this.depthFirstForest.containsKey(currNode)) {
					this.depthFirstForest.put(currNode, new ArrayList<>());
				}
				this.depthFirstForest.get(currNode).add(tmpNeighbor);
				this.dfsVisit(tmpNeighbor, graph);
			}
		}
		this.finishedNodes.add(currNode);
	}

}
