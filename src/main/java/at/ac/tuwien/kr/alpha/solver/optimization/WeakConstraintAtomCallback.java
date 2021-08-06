package at.ac.tuwien.kr.alpha.solver.optimization;

import at.ac.tuwien.kr.alpha.solver.AtomCallbackManager;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

import java.util.Objects;

/**
 * Atom callbacks for weak constraints.
 *
 * Copyright (c) 2021, the Alpha Team.
 */
public class WeakConstraintAtomCallback implements AtomCallbackManager.AtomCallback {
	private final WeakConstraintsManager weakConstraintsManager;
	public final int atom;
	public final int weight;
	public final int level;
	public ThriceTruth lastTruthValue;

	WeakConstraintAtomCallback(WeakConstraintsManager weakConstraintsManager, int atom, int weight, int level) {
		this.atom = atom;
		this.weight = weight;
		this.level = level;
		this.weakConstraintsManager = weakConstraintsManager;
	}

	@Override
	public void processCallback() {
		weakConstraintsManager.processCallback(this);
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
}
