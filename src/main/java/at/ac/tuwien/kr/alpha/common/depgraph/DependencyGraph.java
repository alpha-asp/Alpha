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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.impl.InternalRule;

/**
 * Internal representation of an {@link at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram}'s dependency graph. The dependency graph tracks dependencies
 * between rules of a program. Each {@link Node} of the graph represents a {@link Predicate} occurring in the program. A node has an incoming {@link Edge} for
 * every {@link Literal} in some rule body that depends on it, i.e. the predicate of the literal in question is the same as that of the node. The "sign" flag of
 * an {@link Edge} indicates whether the dependency is a positive or negative one, i.e. if the atom in question is preceded by a "not".
 * 
 * Note that constraints are represented by one dummy predicate (named "constr_{num}"). Each constraint node has a negative edge to itself to express the
 * notation of a constraint ":- a, b." as "x :- a, b, not x.".
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public final class DependencyGraph {

	private static final Logger LOGGER = LoggerFactory.getLogger(DependencyGraph.class);

	/*
	 * Maps nodes to outgoing edges of that node NOTE: Doing the value as List<Edge> rather than Map<Integer,Boolean> as one node may have positive and negative
	 * edges to another.
	 */
	private final Map<Node, List<Edge>> nodes;

	private final Map<Predicate, Node> nodesByPredicate;

	private DependencyGraph(Map<Node, List<Edge>> nodes, Map<Predicate, Node> nodesByPredicate) {
		this.nodes = nodes;
		this.nodesByPredicate = nodesByPredicate;
	}

	public static DependencyGraph buildDependencyGraph(Map<Integer, InternalRule> nonGroundRules) {
		return new DependencyGraph.Builder(nonGroundRules.values()).build();
	}

	public Node getNodeForPredicate(Predicate p) {
		return this.nodesByPredicate.get(p);
	}

	public Map<Node, List<Edge>> getNodes() {
		return Collections.unmodifiableMap(this.nodes);
	}

	public Map<Predicate, Node> getNodesByPredicate() {
		return Collections.unmodifiableMap(this.nodesByPredicate);
	}

	private static class Builder {

		private static final String CONSTRAINT_PREDICATE_FORMAT = "[constr_%d]";

		private int constraintNumber;
		private Collection<InternalRule> rules;

		private Map<Node, List<Edge>> nodes = new HashMap<>();
		private Map<Predicate, Node> nodesByPredicate = new HashMap<>();

		private Builder(Collection<InternalRule> rules) {
			this.rules = rules;
			this.constraintNumber = 0;
		}

		private DependencyGraph build() {
			Node tmpHeadNode;
			for (InternalRule tmpRule : this.rules) {
				LOGGER.debug("Processing rule: {}", tmpRule);
				tmpHeadNode = this.handleRuleHead(tmpRule);
				for (Literal l : tmpRule.getBody()) {
					LOGGER.trace("Processing rule body literal: {}", l);
					if (l.getAtom().isBuiltin()) {
						LOGGER.trace("Ignoring builtin atom in literal {}", l);
						continue;
					}
					this.handleRuleBodyLiteral(tmpHeadNode, l);
				}
			}
			return new DependencyGraph(this.nodes, this.nodesByPredicate);
		}

		private Node handleRuleHead(InternalRule rule) {
			LOGGER.trace("Processing head of rule: {}", rule);
			Node retVal;
			Predicate pred;
			if (rule.isConstraint()) {
				pred = this.generateConstraintDummyPredicate();
				retVal = new Node(pred, pred.toString(), true);
				List<Edge> dependencies = new ArrayList<>();
				dependencies.add(new Edge(retVal, false, "-"));
				if (this.nodes.containsKey(retVal)) {
					throw new IllegalStateException("Dependency graph already contains node for constraint " + pred.toString() + "!");
				}
				this.nodes.put(retVal, dependencies);
				this.nodesByPredicate.put(pred, retVal);
			} else {
				Atom head = rule.getHeadAtom();
				pred = head.getPredicate();
				retVal = new Node(pred, pred.toString());
				if (!this.nodesByPredicate.containsKey(pred)) {
					this.nodes.put(retVal, new ArrayList<>());
					this.nodesByPredicate.put(pred, retVal);
				} else {
					retVal = this.nodesByPredicate.get(pred);
				}
			}
			return retVal;
		}

		private void handleRuleBodyLiteral(Node headNode, Literal lit) {
			List<Edge> dependants;
			Edge tmpEdge;
			Node bodyNode;
			Predicate p = lit.getPredicate();
			if (!this.nodesByPredicate.containsKey(p)) {
				LOGGER.trace("Creating new node for predicate {}", p);
				dependants = new ArrayList<>();
				bodyNode = new Node(p);
				this.nodesByPredicate.put(p, bodyNode);
				this.nodes.put(bodyNode, dependants);
			} else {
				LOGGER.trace("Node for predicate {} already exists", p);
				bodyNode = this.nodesByPredicate.get(p);
				dependants = this.nodes.get(bodyNode);
			}
			tmpEdge = new Edge(headNode, !lit.isNegated(), lit.isNegated() ? "-" : "+");
			if (!dependants.contains(tmpEdge)) {
				LOGGER.trace("Adding dependency: {} -> {} ({})", bodyNode.getPredicate(), headNode.getPredicate(), tmpEdge.getSign() ? "+" : "-");
				dependants.add(tmpEdge);
			}
			this.nodesByPredicate.put(lit.getPredicate(), bodyNode);
		}

		private Predicate generateConstraintDummyPredicate() {
			Predicate retVal = Predicate.getInstance(String.format(DependencyGraph.Builder.CONSTRAINT_PREDICATE_FORMAT, this.nextConstraintNumber()), 0);
			return retVal;
		}

		private int nextConstraintNumber() {
			return ++this.constraintNumber;
		}

	}

}
