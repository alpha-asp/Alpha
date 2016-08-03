package at.ac.tuwien.kr.alpha.common;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class VariableTerm implements Term {
	public String variableName;

	@Override
	public boolean isGround() {
		return false;
	}
}
