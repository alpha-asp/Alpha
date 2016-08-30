package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.IntIdGenerator;

import java.util.HashMap;

/** Common representation of Terms. Terms are constructed such that each term is represented by a unique object, hence
 * term equality can be checked by object reference comparison. Each concrete subclass of a Term must implement a
 * factory-like method to obtain instances.
 * Copyright (c) 2016, the Alpha Team.
 */
public abstract class Term {
	private static HashMap<Term, Integer> knownTermIds = new HashMap<>();
	private static IntIdGenerator termIdGenerator = new IntIdGenerator();


	public abstract boolean isGround();

	int getTermId() {
		Integer potentialId = knownTermIds.get(this);
		if (potentialId == null) {
			int nextId = termIdGenerator.getNextId();
			knownTermIds.put(this, nextId);
			return nextId;
		}
		return potentialId;
	}
}
