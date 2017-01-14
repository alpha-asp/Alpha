package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Truth;

import java.util.Iterator;
import java.util.Map;

public interface SimpleReadableAssignment<T extends Truth> extends Iterable<Map.Entry<Integer, T>> {
	boolean isAssigned(int atom);
	T getTruth(int atom);

	Iterator<? extends Entry<T>> getNewAssignmentsIterator2();

	interface Entry<T extends Truth> {
		int getAtom();
		T getTruth();
	}
}
