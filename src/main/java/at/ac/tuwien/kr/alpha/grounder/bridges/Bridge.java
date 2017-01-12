package at.ac.tuwien.kr.alpha.grounder.bridges;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.ReadableAssignment;
import at.ac.tuwien.kr.alpha.grounder.AtomStore;
import at.ac.tuwien.kr.alpha.grounder.IntIdGenerator;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Map;

public interface Bridge {
	Collection<NoGood> getNoGoods(ReadableAssignment assignment, AtomStore atomStore, Pair<Map<Integer, Integer>, Map<Integer, Integer>> newChoiceAtoms, IntIdGenerator choiceAtomsGenerator);
}
