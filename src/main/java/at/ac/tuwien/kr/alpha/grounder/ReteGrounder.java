package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.grounder.rete.RetePredicate;
import at.ac.tuwien.kr.alpha.solver.Assignment;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public abstract class ReteGrounder extends AbstractGrounder {
	private ParsedProgram programRules;

	private byte[] recentTruthAssignments;

	private RetePredicate predicates;

	public ReteGrounder(ParsedProgram program, java.util.function.Predicate<Predicate> filter) {
		super(program, filter);
	}

	@Override
	public AnswerSet assignmentToAnswerSet(Iterable<Integer> trueAtoms) {
		return null;
	}

	@Override
	public Map<Integer, NoGood> getNoGoods(ReadableAssignment assignment) {
		return null;
	}

	@Override
	public Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms() {
		return null;
	}

	@Override
	public void updateAssignment(Iterator<Assignment.Entry> it) {
	}

	@Override
	public void forgetAssignment(int[] atomIds) {

	}

	@Override
	public String atomToString(int atomId) {
		return null;
	}

	@Override
	public List<Integer> getUnassignedAtoms(ReadableAssignment assignment) {
		return null;
	}

	@Override
	public int registerOutsideNoGood(NoGood noGood) {
		return 0;
	}
}
