package at.ac.tuwien.kr.alpha.solver.nogood;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.Assignment;
import at.ac.tuwien.kr.alpha.solver.Truth;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;

public abstract class AbstractWatchedNoGood {
	private final NoGood noGood;

	public AbstractWatchedNoGood(NoGood noGood) {
		if (noGood == null) {
			throw new IllegalArgumentException("noGood must not be null");
		}

		this.noGood = noGood;
	}


	/**
	 * Unwraps the contained {@link NoGood}.
	 * @return the {@link NoGood} that is being watched by {@code this}, guaranteed to be not {@code null}.
	 */
	public NoGood getNoGood() {
		return noGood;
	}

	public abstract boolean isUnit(Assignment assignment);

	public <V extends Truth> boolean isViolated(Assignment<V> assignment) {
		for (int i = 0; i < noGood.size(); i++) {
			final int literal = noGood.getLiteral(i);
			final V value = assignment.get(atomOf(literal));
			if (value == null || !value.matches(isNegated(literal))) {
				return false;
			}
		}
		return true;
	}
}
