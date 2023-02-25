package at.ac.tuwien.kr.alpha.api.programs.rules;

import java.util.Set;

import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;

/**
 * A rule as defined in the ASP Core 2 Standard.
 * 
 * @param <H> the type of rule head (e.g. choice, normal, disjunctive) supported
 *            by a specific rule.
 *            Copyright (c) 2021, the Alpha Team.
 */
public interface Rule<H extends Head> {

	H getHead();

	Set<Literal> getBody();

	boolean isConstraint();

	Set<Literal> getPositiveBody();

	Set<Literal> getNegativeBody();

}
