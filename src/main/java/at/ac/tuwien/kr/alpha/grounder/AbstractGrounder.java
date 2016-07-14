package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public abstract class AbstractGrounder implements Grounder {
	protected ParsedProgram program;

	public AbstractGrounder(ParsedProgram program) {
		this.program = program;
	}

	public abstract NoGood[] getMoreNoGoods();

	public abstract void updateAssignments(int[] atomIds, boolean[] truthValues);

	public abstract void forgetAssignments(int[] atomIds);

	public abstract void printAnswerSet(int[] trueAtomIds);
}
