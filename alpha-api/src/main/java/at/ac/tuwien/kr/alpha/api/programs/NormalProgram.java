package at.ac.tuwien.kr.alpha.api.programs;

import at.ac.tuwien.kr.alpha.api.programs.rules.NormalRule;

/**
 * A {@link Program} consisting only of facts and {@link NormalRule}s, i.e. no disjunctive- or choice-rules, and no aggregates in rule bodies.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface NormalProgram extends Program<NormalRule> {

}
