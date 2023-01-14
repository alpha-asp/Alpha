package at.ac.tuwien.kr.alpha.commons.programs.rules;

import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.commons.util.Util;

import java.util.List;

/**
 * Represents a weak constraint.
 *
 * Copyright (c) 2020, the Alpha Team.
 */
public class WeakConstraint extends BasicRule {

	private final Term weight;
	private final Term level;
	private final List<Term> termList;

	public WeakConstraint(List<Literal> body, Term weight, Term level, List<Term> termList) {
		super(null, body);
		this.weight = weight;
		this.level = level;
		this.termList = termList;
	}

	public Term getWeight() {
		return weight;
	}

	public Term getLevel() {
		return level;
	}

	public List<Term> getTermList() {
		return termList;
	}

	@Override
	public String toString() {
		String weightInformation = Util.join("[" + weight + "@" + level + (termList.isEmpty() ? "" : ", "), termList, "]");
		return Util.join(":~ ", getBody(), "." + weightInformation);
	}
}
