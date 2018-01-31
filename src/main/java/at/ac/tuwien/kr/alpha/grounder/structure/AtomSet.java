package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.Unification;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
class AtomSet {
	private final Atom literal;
	private final Set<Substitution> complementSubstitutions;
	private static int atomSetCounter = 1;

	AtomSet(Atom literal, Substitution unifier, Set<Substitution> complementSubstitutions) {
		if (!complementSubstitutions.isEmpty()) {
			System.out.println("Breakpoint.");
		}
		this.literal = literal.substitute(unifier).renameVariables("_AS" + atomSetCounter++);
		this.complementSubstitutions = new LinkedHashSet<>();
		for (Substitution complementSubstitution : complementSubstitutions) {
			Atom literalComplement = literal.substitute(complementSubstitution);
			Atom literalComplementUnified = literalComplement.substitute(unifier);
			Atom otherWayRound = literal.substitute(unifier).substitute(complementSubstitution);
			Substitution unifyRightAtom = Unification.unifyRightAtom(literalComplementUnified, this.literal);
			if (!this.literal.substitute(unifyRightAtom).isGround()) {
				System.out.println("Problem here?");
			}
			this.complementSubstitutions.add(unifyRightAtom);
		}
		//this.complementSubstitutions = complementSubstitutions;
	}

	/**
	 * Returns true if the left {@link AtomSet} is a specialization of the right {@link AtomSet}.
	 *
	 * @param left
	 * @param right
	 * @return
	 */
	public static boolean isSpecialization(AtomSet left, AtomSet right) {
		if (Unification.unifyRightAtom(left.literal, right.literal) == null) {
			return false;
		}
		rightLoop:
		for (Substitution rightComplementSubstitution : right.complementSubstitutions) {
			Atom rightSubstitution = right.literal.substitute(rightComplementSubstitution).renameVariables("_X");
			for (Substitution leftComplementSubstitution : left.complementSubstitutions) {
				Atom leftSubstitution = left.literal.substitute(leftComplementSubstitution).renameVariables("_Y");
				Substitution specializingSubstitution = Unification.isMoreGeneral(rightSubstitution, leftSubstitution);
				if (specializingSubstitution != null) {
					continue rightLoop;
				}
			}
			// Right substitution has no matching left one
			return false;
		}
		return true;
	}

	public Atom getLiteral() {
		return literal;
	}

	public Set<Substitution> getComplementSubstitutions() {
		return Collections.unmodifiableSet(complementSubstitutions);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("(" + literal + ",{");
		for (Substitution complementSubstitution : complementSubstitutions) {
			sb.append(literal.substitute(complementSubstitution));
			//sb.append(complementSubstitution);
			sb.append(", ");
		}
		sb.append("})");
		return sb.toString();
	}
}
