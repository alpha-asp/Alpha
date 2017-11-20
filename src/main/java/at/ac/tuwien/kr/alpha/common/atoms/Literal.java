package at.ac.tuwien.kr.alpha.common.atoms;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public interface Literal extends Atom {

	enum Type {
		BASIC_ATOM, EXTERNAL_ATOM, HEURISTIC_ATOM, INTERVAL_ATOM
	}

	Type getType();
	boolean isNegated();
}
