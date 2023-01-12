package at.ac.tuwien.kr.alpha.core.solver.optimization;

import at.ac.tuwien.kr.alpha.core.solver.AtomCallbackManager;
import at.ac.tuwien.kr.alpha.core.solver.ThriceTruth;

import java.util.Objects;

/**
 * Atom callbacks for weak constraints.
 *
 * Copyright (c) 2021, the Alpha Team.
 */
public class WeakConstraintAtomCallback implements AtomCallbackManager.AtomCallback {
	private final WeakConstraintsManagerForBoundedOptimality weakConstraintsManagerForBoundedOptimality;
	public final int atom;
	public final int weight;
	public final int level;
	public ThriceTruth lastTruthValue;

	WeakConstraintAtomCallback(WeakConstraintsManagerForBoundedOptimality weakConstraintsManager, int atom, int weight, int level) {
		this.atom = atom;
		this.weight = weight;
		this.level = level;
		this.weakConstraintsManagerForBoundedOptimality = weakConstraintsManager;
	}

	@Override
	public void processCallback() {
		weakConstraintsManagerForBoundedOptimality.processCallback(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		WeakConstraintAtomCallback that = (WeakConstraintAtomCallback) o;
		return atom == that.atom &&
			weight == that.weight &&
			level == that.level;
	}

	@Override
	public int hashCode() {
		return Objects.hash(atom, weight, level);
	}

	@Override
	public String toString() {
		return atom + "[" + weight + "@" + level + "]=" + lastTruthValue;
	}
}