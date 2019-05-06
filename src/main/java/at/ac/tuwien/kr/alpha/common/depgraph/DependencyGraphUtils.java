package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.ArrayList;
import java.util.List;

// TODO internalize into DependencyGraph - most stuff here depends on the dependency graph
// TODO actually only needed by tests -> move there
public final class DependencyGraphUtils {

	private DependencyGraphUtils() {

	}

	public static boolean isReachableFrom(Node dest, Node src, DependencyGraph dg) {
		return DependencyGraphUtils.isReachableFrom(dest, src, dg, new ArrayList<>());
	}

	private static boolean isReachableFrom(Node dest, Node src, DependencyGraph dg, List<Node> discovered) {
		List<Edge> outgoingEdges;
		if (src.equals(dest)) {
			return true;
		}
		if ((outgoingEdges = dg.getNodes().get(src)) == null) {
			return false;
		}
		discovered.add(src);
		// we wanna do BFS here, therefore use 2 loops
		for (Edge edge : outgoingEdges) {
			if (edge.getTarget().equals(dest)) {
				return true;
			}
		}
		for (Edge tmp : outgoingEdges) {
			if (discovered.contains(tmp.getTarget())) {
				// cycle found
				continue;
			}
			if (DependencyGraphUtils.isReachableFrom(dest, tmp.getTarget(), dg, discovered)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the given nodes are strongly connected within the given dependency graph. Strongly connected means every node in the given list is
	 * reachable from every other node in the list and vice versa.
	 * 
	 * @param connectedNodes the nodes to check
	 * @param dg             the dependency graph in which to check
	 * @return true if the given nodes are strongly connected, false otherwise
	 */
	public static boolean areStronglyConnected(List<Node> connectedNodes, DependencyGraph dg) {
		for (Node n1 : connectedNodes) {
			for (Node n2 : connectedNodes) {
				if (!(DependencyGraphUtils.isReachableFrom(n2, n1, dg) && DependencyGraphUtils.isReachableFrom(n1, n2, dg))) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isStronglyConnectedComponent(List<Node> componentNodes, DependencyGraph dg) {
		if (!DependencyGraphUtils.areStronglyConnected(componentNodes, dg)) {
			return false;
		}
		// now check if the given set is maximal
		List<Node> lst = new ArrayList<>();
		for (Node n : componentNodes) {
			lst.add(n);
		}
		for (Node n : dg.getNodes().keySet()) {
			if (lst.contains(n)) {
				continue;
			}
			lst.add(n);
			if (DependencyGraphUtils.areStronglyConnected(lst, dg)) {
				// not a strongly connected component if there is a bigger set which is strongly connected
				return false;
			}
		}
		return true;
	}

}
