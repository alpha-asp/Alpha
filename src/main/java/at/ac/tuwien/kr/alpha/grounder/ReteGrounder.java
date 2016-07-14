package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.AnswerSet;
import at.ac.tuwien.kr.alpha.AnswerSetFilter;
import at.ac.tuwien.kr.alpha.NoGood;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.grounder.rete.RetePredicate;

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
	public AnswerSet assignmentToAnswerSet(AnswerSetFilter filter, int[] trueAtoms) {
		return null;
	}

	@Override
	public Map<Integer, NoGood> getNoGoods() {
		return null;
	}

	@Override
	public void updateAssignment(int[] atomIds, boolean[] truthValues) {

	}

	@Override
	public void forgetAssignment(int[] atomIds) {

	}
}
