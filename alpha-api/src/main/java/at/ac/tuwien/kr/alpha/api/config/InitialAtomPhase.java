package at.ac.tuwien.kr.alpha.api.config;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Determines the phase (truth value) initially assigned to all atoms.
 * The initial phase is only considered for atoms that were never assigned a value during the run of the solver. It may
 * have huge effects on runtime as the initial phase determines the location in the search space where the search
 * process starts.
 *
 * Copyright (c) 2021, the Alpha Team.
 */
public enum InitialAtomPhase {
	ALLTRUE,		// Initially assign all atoms to true.
	ALLFALSE,		// Initially assign all atoms to false.
	RANDOM,			// Initially assign randomly true/false.
	RULESTRUEATOMSFALSE;	// Initially assign atoms representing rules to true, all others to false.

	public static String listAllowedValues() {
		return Arrays.stream(InitialAtomPhase.values()).map(InitialAtomPhase::toString)
			.map(String::toLowerCase).collect(Collectors.joining(", "));
	}
}
