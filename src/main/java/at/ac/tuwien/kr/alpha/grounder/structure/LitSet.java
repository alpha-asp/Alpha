package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.Unification;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class LitSet {
	private final Atom literal;
	private final Set<Substitution> complementSubstitutions;
	private static int litSetCounter = 1;

	LitSet(Atom literal, Set<Substitution> complementSubstitutions) {
		this.literal = literal.renameVariables("_AS" + litSetCounter++);
		this.complementSubstitutions = new LinkedHashSet<>();
		for (Substitution complementSubstitution : complementSubstitutions) {
			Atom groundComplementAtom = literal.substitute(complementSubstitution);
			if (!groundComplementAtom.isGround()) {
				throw oops("complementSubstitution is not grounding.");
			}
			Substitution unifyRightAtom = Unification.unifyRightAtom(groundComplementAtom, this.literal);
			if (!this.literal.substitute(unifyRightAtom).isGround()) {
				throw oops("Unification result is not grounding, but should be.");
			}
			this.complementSubstitutions.add(unifyRightAtom);
		}
	}

	/**
	 * Returns true if the left {@link LitSet} is a specialization of the right {@link LitSet}.
	 *
	 * @param left
	 * @param right
	 * @return
	 */
	public static boolean isSpecialization(LitSet left, LitSet right) {
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
