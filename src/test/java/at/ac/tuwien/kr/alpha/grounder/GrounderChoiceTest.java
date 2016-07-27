package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.NoGood;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.Util.entriesToMap;
import static at.ac.tuwien.kr.alpha.Util.entry;

/**
 * Represents a small ASP program with guesses {@code { aa :- not bb.  bb :- not aa. }}.
 * Copyright (c) 2016, the Alpha Team.
 */
public class GrounderChoiceTest extends AbstractGrounder {


	private static Map<Integer, String> atomIdToString = Stream.of(
		entry(1, "aa"),
		entry(2, "bb"),
		entry(3, "_br1"),
		entry(4, "_br2")
	).collect(entriesToMap());

	@Override
	public AnswerSet assignmentToAnswerSet(java.util.function.Predicate filter, int[] trueAtoms) {
		return null;
	}

	@Override
	public Map<Integer, NoGood> getNoGoods() {
		return null;
	}

	@Override
	public Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms() {
		return null;
	}

	@Override
	public void updateAssignment(int[] atomIds, boolean[] truthValues) {

	}

	@Override
	public void forgetAssignment(int[] atomIds) {

	}
}
