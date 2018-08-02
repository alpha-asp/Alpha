package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public interface ImplicationReasonProvider {
	NoGood getNoGood(int impliedLiteral);
}
