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

import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * A result of a depth first search as used to find strongly connected components of a {@link DependencyGraph}
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class DfsResult {

	private Deque<Node> finishedNodes;
	private Map<Node, List<Node>> depthFirstForest;

	public Deque<Node> getFinishedNodes() {
		return this.finishedNodes;
	}

	public void setFinishedNodes(Deque<Node> finishedNodes) {
		this.finishedNodes = finishedNodes;
	}

	public Map<Node, List<Node>> getDepthFirstForest() {
		return this.depthFirstForest;
	}

	public void setDepthFirstForest(Map<Node, List<Node>> depthFirstForest) {
		this.depthFirstForest = depthFirstForest;
	}

}
