package at.ac.tuwien.kr.alpha.solver.heuristics;

import java.util.Random;

/**
 * Factory returning atom phase initializers, which determine the initial phase given to atoms that were previously
 * unassigned.
 *
 * Copyright (c) 2019, the Alpha Team.
 */
public abstract class PhaseInitializerFactory {

	public abstract static class PhaseInitializer {
		public abstract boolean getNextInitialPhase();
	}

	public static PhaseInitializer getInstance(String phaseInitializerName, Random random) {
		switch (phaseInitializerName.toLowerCase()) {
			case "alltrue":
				return getPhaseInitializerAllTrue();
			case "allfalse":
				return getPhaseInitializerAllFalse();
			case "random":
				return getPhaseInitializerRandom(random);
			default:
				throw new IllegalArgumentException("Unknown phase initializer requested:" + phaseInitializerName);
		}
	}

	public static PhaseInitializer getPhaseInitializerAllTrue() {
		return new PhaseInitializer() {
			@Override
			public boolean getNextInitialPhase() {
				return true;
			}
		};
	}

	private static PhaseInitializer getPhaseInitializerAllFalse() {
		return new PhaseInitializer() {
			@Override
			public boolean getNextInitialPhase() {
				return false;
			}
		};
	}

	private static PhaseInitializer getPhaseInitializerRandom(Random random) {
		return new PhaseInitializer() {
			private final Random rand = random;
			@Override
			public boolean getNextInitialPhase() {
				return rand.nextBoolean();
			}
		};
	}
}
