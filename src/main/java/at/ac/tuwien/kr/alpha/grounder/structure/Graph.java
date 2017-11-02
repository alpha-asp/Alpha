package at.ac.tuwien.kr.alpha.grounder.structure;

import java.util.*;

/**
 * Represents a generic graph structure.
 * Copyright (c) 2017, the Alpha Team.
 */
public class Graph<V> {
	private final HashSet<V> vertices;
	private final HashSet<AbstractMap.SimpleEntry<V, V>> edges;
	private final LinkedHashMap<V, Integer> vertexPositions;
	private int counter;

	public Graph() {
		vertices = new HashSet<>();
		edges = new HashSet<>();
		vertexPositions = new LinkedHashMap<>();
		counter = 0;
	}

	public void addVertex(V vertex) {
		if (!vertices.contains(vertex)) {
			vertices.add(vertex);
			vertexPositions.put(vertex, counter++);
		}
	}

	private void checkAddVertex(V vertex) {
		if (!vertices.contains(vertex)) {
			addVertex(vertex);
		}
	}

	public void addEdge(V from, V to) {
		checkAddVertex(from);
		checkAddVertex(to);
		edges.add(new AbstractMap.SimpleEntry<V, V>(from, to));
	}

	public Map<V, Integer> getVertexPositions() {
		return Collections.unmodifiableMap(vertexPositions);
	}

	public boolean[][] getAdjacencyMatrix() {
		boolean[][] matrix = new boolean[vertices.size()][vertices.size()];
		for (AbstractMap.SimpleEntry<V, V> edge : edges) {
			matrix[vertexPositions.get(edge.getKey())][vertexPositions.get(edge.getValue())] = true;
		}
		return matrix;
	}

	public LinkedHashMap<V, List<V>> getAdjacencyList() {
		LinkedHashMap<V, List<V>> list = new LinkedHashMap<>();
		for (AbstractMap.SimpleEntry<V, V> edge : edges) {
			list.putIfAbsent(edge.getKey(), new LinkedList<>());
			list.get(edge.getKey()).add(edge.getValue());
		}
		return list;
	}

	public int getNumVertices() {
		return vertexPositions.size();
	}
}
