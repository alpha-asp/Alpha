package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Factory returning atom phase initializers, which determine the initial phase given to atoms that were previously
 * unassigned.
 *
 * Copyright (c) 2019, the Alpha Team.
 */
public abstract class PhaseInitializerFactory {

	public enum InitialPhase {
		ALLTRUE,
		ALLFALSE,
		RANDOM,
		RULESTRUEATOMSFALSE;

		public static String listAllowedValues() {
			return Arrays.stream(values()).map(InitialPhase::toString).map(String::toLowerCase).collect(Collectors.joining(", "));
		}
	}

	public abstract static class PhaseInitializer {
		public abstract boolean getNextInitialPhase(int atom);
	}

	public static PhaseInitializer getInstance(String phaseInitializerName, Random random, AtomStore atomStore) {
		InitialPhase initialPhase = InitialPhase.valueOf(phaseInitializerName.toUpperCase());
		switch (initialPhase) {
			case ALLTRUE:
				return getPhaseInitializerAllTrue();
			case ALLFALSE:
				return getPhaseInitializerAllFalse();
			case RANDOM:
				return getPhaseInitializerRandom(random);
			case RULESTRUEATOMSFALSE:
				return getPhaseInitializerRulesTrueRestFalse(atomStore);
			default:
				throw new IllegalArgumentException("Unknown phase initializer requested:" + phaseInitializerName);
		}
	}

	public static PhaseInitializer getPhaseInitializerAllTrue() {
		return new PhaseInitializer() {
			@Override
			public boolean getNextInitialPhase(int atom) {
				return true;
			}
		};
	}

	private static PhaseInitializer getPhaseInitializerAllFalse() {
		return new PhaseInitializer() {
			@Override
			public boolean getNextInitialPhase(int atom) {
				return false;
			}
		};
	}

	private static PhaseInitializer getPhaseInitializerRandom(Random random) {
		return new PhaseInitializer() {
			private final Random rand = random;
			@Override
			public boolean getNextInitialPhase(int atom) {
				return rand.nextBoolean();
			}
		};
	}

	private static PhaseInitializer getPhaseInitializerRulesTrueRestFalse(AtomStore atomStore) {
		return new PhaseInitializer() {
			@Override
			public boolean getNextInitialPhase(int atom) {
				return atomStore.get(atom) instanceof RuleAtom;	// Return true for RuleAtoms, otherwise false.
			}
		};
	}
}
