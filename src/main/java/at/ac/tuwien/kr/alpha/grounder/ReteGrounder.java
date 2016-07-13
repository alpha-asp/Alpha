package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.grounder.rete.RetePredicate;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ReteGrounder extends AbstractGrounder {
	private ParsedProgram programRules;

	private byte[] recentTruthAssignments;

	private RetePredicate predicates;

	@Override
	public void initialize(ParsedProgram program) {
		// save program for later use
		programRules = program;

		// construct basic data structures
	}

	@Override
	public NoGood[] getMoreNoGoods() {
		return new NoGood[0];
	}

	@Override
	public void updateAssignments(int[] atomIds, boolean[] truthValues) {

	}

	@Override
	public void forgetAssignments(int[] atomIds) {

	}

	@Override
	public void printAnswerSet(int[] trueAtomIds) {

	}
}
