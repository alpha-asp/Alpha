package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Truth;

import java.util.Map;

public interface SimpleReadableAssignment<T extends Truth> extends Iterable<Map.Entry<Integer, T>> {
	boolean isAssigned(int atom);
	T getTruth(int atom);
}
