package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.grounder.rete.RetePredicate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ReteGrounder extends AbstractGrounder {
	private ParsedProgram programRules;

	private byte[] recentTruthAssignments;

	private RetePredicate predicates;

	public ReteGrounder(ParsedProgram program) {
		super(program);
	}

	@Override
	public AnswerSet assignmentToAnswerSet(java.util.function.Predicate<Predicate> filter, Iterable<Integer> trueAtoms) {
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

	@Override
	public String atomIdToString(int atomId) {
		return null;
	}
}
