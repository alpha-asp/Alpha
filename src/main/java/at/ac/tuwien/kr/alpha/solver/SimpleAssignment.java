package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Truth;

import java.util.Iterator;
import java.util.Map;

public interface SimpleAssignment<T extends Truth> extends Iterable<Map.Entry<Integer, T>> {
	boolean isAssigned(int atom);
	Truth getTruth(int atom);
	void assign(int atom, T value);
	void unassign(int atom);
}
