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

import at.ac.tuwien.kr.alpha.common.Predicate;

/**
 * A node in a dependency graph. One node references exactly one predicate. This means that all rule heads deriving the same predicate will be condensed into
 * the same graph node. In some cases this results in more "conservative" results in stratification analysis, where some rules will not be evaluated up-front,
 * although that would be possible.
 * 
 * Copyright (c) 2017-2019, the Alpha Team.
 */
public class Node {

	private final Predicate predicate;
	private final String label;
	private final boolean isConstraint;

	public Node(Predicate predicate, boolean isConstraint) {
		this.predicate = predicate;
		this.label = predicate.toString();
		this.isConstraint = isConstraint;
	}

	public Node(Predicate predicate) {
		this(predicate, false);
	}

	/**
	 * Copy-constructor - constructs a new node as a deep-copy of the passed node
	 * 
	 * @param original the node to copy
	 */
	public Node(Node original) {
		this(original.predicate, original.isConstraint);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Node)) {
			return false;
		}
		return this.predicate.equals(((Node) o).predicate);
	}

	@Override
	public int hashCode() {
		return this.predicate.hashCode();
	}

	@Override
	public String toString() {
		return "Node{" + this.predicate.toString() + "}";
	}

	public String getLabel() {
		return this.label;
	}

	public Predicate getPredicate() {
		return this.predicate;
	}

	public boolean isConstraint() {
		return this.isConstraint;
	}

}
