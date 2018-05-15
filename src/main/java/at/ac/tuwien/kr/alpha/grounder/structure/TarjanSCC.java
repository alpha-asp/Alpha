package at.ac.tuwien.kr.alpha.grounder.structure;

import java.util.*;

/**
 * Compute the strongly connected components following Tarjan's algorithm.
 * Copyright (c) 2017, the Alpha Team.
 */
public class TarjanSCC<V> {
	private final Graph<V> graph;
	private final LinkedHashMap<V, List<V>> adjacencyList;
	private final int graphSize;
	private int[] v_index;
	private int[] v_lowlink;
	private boolean[] v_onStack;
	private int index;
	private Stack<Integer> stack;
	private final Graph<SCC<V>>  sccGraph;

	public Graph<SCC<V>> getSccGraph() {
		return sccGraph;
	}

	public static class SCC<V> {
		public HashSet<V> scc = new HashSet<>();
	}

	public HashSet<SCC<V>> getStronglyConnectedComponents() {
		return stronglyConnectedComponents;
	}

	public HashMap<V, SCC<V>> getVertexInSCC() {
		return vertexInSCC;
	}

	public ArrayList<SCC<V>> getReverseTopoSortSCCs() {
		return reverseTopoSortSCCs;
	}

	private HashSet<SCC<V>> stronglyConnectedComponents = new HashSet<>();
	private HashMap<V, SCC<V>> vertexInSCC = new HashMap<>();
	private ArrayList<SCC<V>> reverseTopoSortSCCs = new ArrayList<>();

	public TarjanSCC(Graph<V> graph) {
		this.graph = graph;
		this.adjacencyList = graph.getAdjacencyList();
		this.graphSize = graph.getNumVertices();
		this.v_index = new int[graphSize];
		Arrays.fill(v_index, -1);
		this.v_lowlink = new int[graphSize];
		this.v_onStack = new boolean[graphSize];
		this.computeSCCs();
		this.sccGraph = constructSCCGraph();
	}

	private Graph<SCC<V>> constructSCCGraph() {
		Graph<SCC<V>> sccGraph = new Graph<>();
		for (Map.Entry<V, List<V>> entry : adjacencyList.entrySet()) {
			SCC<V> fromSCC = vertexInSCC.get(entry.getKey());
			sccGraph.addVertex(fromSCC);
			for (V v : entry.getValue()) {
				SCC<V> toSCC = vertexInSCC.get(v);
				sccGraph.addEdge(fromSCC, toSCC);
			}
		}
		return sccGraph;
	}

	private void computeSCCs() {
		index = 0;
		stack = new Stack<>();
		for (int i = 0; i < graphSize; i++) {
			if (v_index[i] == -1) {
				strongconnect(i);
			}
		}
	}

	private void strongconnect(int vpos) {
		V v = graph.getPositionToVertex().get(vpos);
		v_index[vpos] = index;
		v_lowlink[vpos] = index;
		index++;
		stack.push(vpos);
		v_onStack[vpos] = true;

		for (V w : adjacencyList.get(v)) {
			int wpos = graph.getVertexToPosition().get(w);
			if (v_index[wpos] == -1) {
				strongconnect(wpos);
				v_lowlink[vpos] = Math.min(v_lowlink[vpos], v_lowlink[wpos]);
			} else if (v_onStack[wpos]) {
				v_lowlink[vpos] = Math.min(v_lowlink[vpos], v_index[wpos]);
			}
		}

		if (v_lowlink[vpos] == v_index[vpos]) {
			SCC<V> currentSCC = new SCC<>();
			this.stronglyConnectedComponents.add(currentSCC);
			int wpos;
			do {
				wpos = stack.pop();
				V w = graph.getPositionToVertex().get(wpos);
				v_onStack[wpos] = false;
				currentSCC.scc.add(w);
				this.vertexInSCC.put(w, currentSCC);
			} while (wpos != vpos);
			reverseTopoSortSCCs.add(currentSCC);
		}
	}
}
