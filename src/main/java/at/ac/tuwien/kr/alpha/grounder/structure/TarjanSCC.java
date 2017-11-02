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
	private ArrayList<V> vertex;
	private int[] v_index;
	private int[] v_lowlink;
	private boolean[] v_onStack;
	private int index;

	public static class SCC<V> {
		public HashSet<V> scc = new HashSet<>();
	}

	public HashSet<SCC> getSCCs() {
		return SCCs;
	}

	public HashMap<V, SCC> getVertexInSCC() {
		return vertexInSCC;
	}

	public ArrayList<SCC> getReverseTopoSortSCCs() {
		return reverseTopoSortSCCs;
	}

	private HashSet<SCC> SCCs = new HashSet<>();
	private HashMap<V, SCC> vertexInSCC = new HashMap<>();
	private ArrayList<SCC> reverseTopoSortSCCs = new ArrayList<>();

	public TarjanSCC(Graph<V> graph) {
		this.graph = graph;
		this.adjacencyList = graph.getAdjacencyList();
		this.graphSize = graph.getNumVertices();
		this.v_index = new int[graphSize];
		Arrays.fill(v_index, -1);
		this.v_lowlink = new int[graphSize];
		this.v_onStack = new boolean[graphSize];
		this.vertex = new ArrayList<>(graphSize);
		int vpos = 0;
		for (Map.Entry<V, List<V>> entry : adjacencyList.entrySet()) {
			this.vertex.add(vpos++, entry.getKey());
		}
	}

	public void computeSCCs() {
		index = 0;
		stack = new Stack<>();
		for (int i = 0; i < graphSize; i++) {
			if (v_index[i] == 0) {
				strongconnect(i);
			}
		}
	}

	private Stack<Integer> stack;

	private void strongconnect(int vpos) {
		V v = vertex.get(vpos);
		v_index[vpos] = index;
		v_lowlink[vpos] = index;
		index++;
		stack.push(vpos);
		v_onStack[vpos] = true;

		for (V w : adjacencyList.get(v)) {
			int wpos = graph.getVertexPositions().get(w);
			if (v_index[wpos] < 0) {
				strongconnect(wpos);
				v_lowlink[vpos] = Math.min(v_lowlink[vpos], v_lowlink[wpos]);
			} else if (v_onStack[wpos]) {
				v_lowlink[vpos] = Math.min(v_lowlink[vpos], v_index[wpos]);
			}
		}

		if (v_lowlink[vpos] == v_index[vpos]) {
			SCC<V> currentSCC = new SCC<>();
			this.SCCs.add(currentSCC);
			int wpos;
			do {
				wpos = stack.pop();
				V w = vertex.get(wpos);
				v_onStack[wpos] = false;
				currentSCC.scc.add(w);
				this.vertexInSCC.put(w, currentSCC);
			} while (wpos != vpos);
			reverseTopoSortSCCs.add(currentSCC);
		}
	}
}
