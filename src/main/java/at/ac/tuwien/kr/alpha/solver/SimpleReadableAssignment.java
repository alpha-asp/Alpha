package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Truth;

import java.util.Iterator;
import java.util.Map;

public interface SimpleReadableAssignment extends Iterable<Map.Entry<Integer, ThriceTruth>> {
	boolean isAssigned(int atom);
	ThriceTruth getTruth(int atom);

	Iterator<? extends Entry> getNewAssignmentsIterator2();

	interface Entry {
		int getAtom();
		ThriceTruth getTruth();
	}
}
