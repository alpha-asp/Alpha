package at.ac.tuwien.kr.alpha.grounder.structure;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Represents a generic graph structure.
 * Copyright (c) 2017, the Alpha Team.
 */
public class Graph<V> {
	private final Set<V> vertices;
	private final Set<AbstractMap.SimpleEntry<V, V>> edges;
	private final Map<V, Integer> vertexToPosition;
	private int counter;
	private boolean sealed;
	private ArrayList<V> positionToVertex;

	public Graph() {
		vertices = new HashSet<>();
		edges = new HashSet<>();
		vertexToPosition = new LinkedHashMap<>();
		counter = 0;
	}

	private void checkSealed() {
		if (sealed) {
			throw oops("Modifying graph after it was sealed.");
		}
	}

	private void seal() {
		sealed = true;
	}

	public void addVertex(V vertex) {
		checkSealed();
		if (!vertices.contains(vertex)) {
			vertices.add(vertex);
			vertexToPosition.put(vertex, counter++);
		}
	}

	private void checkAddVertex(V vertex) {
		checkSealed();
		if (!vertices.contains(vertex)) {
			addVertex(vertex);
		}
	}

	public void addEdge(V from, V to) {
		checkSealed();
		checkAddVertex(from);
		checkAddVertex(to);
		edges.add(new AbstractMap.SimpleEntry<V, V>(from, to));
	}

	public Set<V> getVertices() {
		seal();
		return Collections.unmodifiableSet(vertices);
	}

	public Map<V, Integer> getVertexToPosition() {
		seal();
		return Collections.unmodifiableMap(vertexToPosition);
	}

	public List<V> getPositionToVertex() {
		seal();
		if (positionToVertex == null) {
			Map<V, Integer> vertexPositions = getVertexToPosition();
			positionToVertex = new ArrayList<>(Collections.nCopies(getNumVertices(), null));
			for (Map.Entry<V, Integer> entry : vertexPositions.entrySet()) {
				positionToVertex.set(entry.getValue(), entry.getKey());
			}
		}
		return Collections.unmodifiableList(positionToVertex);
	}

	public boolean[][] getAdjacencyMatrix() {
		seal();
		boolean[][] matrix = new boolean[vertices.size()][vertices.size()];
		for (AbstractMap.SimpleEntry<V, V> edge : edges) {
			matrix[vertexToPosition.get(edge.getKey())][vertexToPosition.get(edge.getValue())] = true;
		}
		return matrix;
	}

	public LinkedHashMap<V, List<V>> getAdjacencyList() {
		seal();
		LinkedHashMap<V, List<V>> list = new LinkedHashMap<>();
		for (AbstractMap.SimpleEntry<V, V> edge : edges) {
			list.putIfAbsent(edge.getKey(), new LinkedList<>());
			list.get(edge.getKey()).add(edge.getValue());
		}
		for (V vertex : vertices) {
			list.putIfAbsent(vertex, Collections.emptyList());
		}
		return list;
	}

	public int getNumVertices() {
		seal();
		return vertexToPosition.size();
	}
}
