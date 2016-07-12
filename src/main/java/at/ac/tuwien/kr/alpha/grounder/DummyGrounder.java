package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.AnswerSet;
import at.ac.tuwien.kr.alpha.NoGood;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;

import java.util.Collection;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class DummyGrounder extends AbstractGrounder {
	public DummyGrounder(ParsedProgram program) {
		super(program);
	}

	@Override
	public Collection<NoGood> getNoGoods(int[] ids, boolean[] truthValues) {
		return null;
	}

	@Override
	public AnswerSet translate(int[] trueAtoms) {
		return null;
	}
}
