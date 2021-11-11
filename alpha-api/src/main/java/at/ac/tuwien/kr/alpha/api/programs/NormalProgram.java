package at.ac.tuwien.kr.alpha.api.programs;

import at.ac.tuwien.kr.alpha.api.rules.NormalRule;

/**
 * A {@link Program} consisting only of facts and {@link NormalRule}s, i.e. no disjunctive- or choice-rules.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface NormalProgram extends Program<NormalRule> {

}
