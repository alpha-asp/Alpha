package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

/**
 * This class provides functions for first-order variable substitution.
 * Copyright (c) 2016, the Alpha Team.
 */
public class SubstitutionUtil {

	/**
	 * This method applies a substitution to an atom and returns the AtomId of the ground atom.
	 * @param variableSubstitution the variable substitution to apply.
	 * @return the AtomId of the corresponding substituted ground atom.
	 */
	public static int groundingSubstitute(AtomStore atomStore, Atom nonGroundAtom, VariableSubstitution variableSubstitution) {
		final Term[] terms = nonGroundAtom.getTerms();
		Term[] groundTermList = new Term[terms.length];
		for (int i = 0; i < terms.length; i++) {
			Term nonGroundTerm = terms[i];
			Term groundTerm = groundTerm(nonGroundTerm, variableSubstitution);
			if (!groundTerm.isGround()) {
				throw new RuntimeException("Grounding substitution yields a non-ground term: " + groundTerm + "\nShould not happen, aborting.");
			}
			groundTermList[i] = groundTerm;
		}
		BasicAtom groundAtom = new BasicAtom(nonGroundAtom.getPredicate(), false, groundTermList);
		return atomStore.add(groundAtom);
	}

	/**
	 * Applies a substitution, result may be nonground.
	 * @param nonGroundTerm the (potentially) non-ground term to apply the substitution to.
	 * @param variableSubstitution the variable substitution to apply.
	 * @return the non-ground term where all variable substitutions have been applied.
	 */
	public static Term groundTerm(Term nonGroundTerm, VariableSubstitution variableSubstitution) {
		if (nonGroundTerm instanceof ConstantTerm) {
			return nonGroundTerm;
		} else if (nonGroundTerm instanceof VariableTerm) {
			Term groundTerm = variableSubstitution.substitution.get(nonGroundTerm);
			if (groundTerm == null) {
				return nonGroundTerm;	// If variable is not substituted, keep term as is.
				//throw new RuntimeException("SubstitutionUtil encountered variable without a substitution given: " + nonGroundTerm);
			}
			return  groundTerm;
		} else if (nonGroundTerm instanceof FunctionTerm) {
			ArrayList<Term> groundTermList = new ArrayList<>(((FunctionTerm) nonGroundTerm).termList.size());
			for (Term term : ((FunctionTerm) nonGroundTerm).termList) {
				groundTermList.add(groundTerm(term, variableSubstitution));
			}
			return FunctionTerm.getInstance(((FunctionTerm) nonGroundTerm).functionSymbol, groundTermList);
		} else {
			throw new RuntimeException("SubstitutionUtil: Unknown term type encountered.");
		}
	}

	/**
	 * This method applies a substitution to a potentially non-ground atom. The resulting atom may be non-ground.
	 * @param nonGroundAtom the (non-ground) atom to apply the substitution to (parameter is not  modified).
	 * @param variableSubstitution the variable substitution to apply.
	 * @return a pair <Boolean>isGround</Boolean>, <BasicAtom>substitutedAtom</BasicAtom> where
	 * isGround true if the substitutedAtom is ground and substitutedAtom is the atom resulting from the applying
	 * the substitution.
	 */
	public static Pair<Boolean, BasicAtom> substitute(Atom nonGroundAtom, VariableSubstitution variableSubstitution) {
		final Term[] terms = nonGroundAtom.getTerms();
		Term[] substitutedTerms = new Term[terms.length];
		boolean isGround = true;
		for (int i = 0; i < nonGroundAtom.getTerms().length; i++) {
			substitutedTerms[i] = groundTerm(terms[i], variableSubstitution);
			if (isGround && !substitutedTerms[i].isGround()) {
				isGround = false;
			}
		}
		BasicAtom substitutedAtom = new BasicAtom(nonGroundAtom.getPredicate(), false, substitutedTerms);
		return new ImmutablePair<>(isGround, substitutedAtom);
	}

	public static String groundAndPrintRule(NonGroundRule rule, VariableSubstitution substitution) {
		String ret = "";
		if (!rule.isConstraint()) {
			BasicAtom groundHead = substitute(rule.getHeadAtom(), substitution).getRight();
			ret += groundHead.toString();
		}
		ret += " :- ";
		boolean isFirst = true;
		for (Atom bodyAtom : rule.getBodyAtomsPositive()) {
			ret += groundAtomToString(bodyAtom, false, substitution, isFirst);
			isFirst = false;
		}
		for (Atom bodyAtom : rule.getBodyAtomsNegative()) {
			ret += groundAtomToString(bodyAtom, true, substitution, isFirst);
			isFirst = false;
		}
		ret += ".";
		return ret;
	}

	private static String groundAtomToString(Atom bodyAtom, boolean isNegative, VariableSubstitution substitution, boolean isFirst) {
		if (bodyAtom instanceof BuiltinAtom) {
			return (isFirst ? ", " : "") + bodyAtom.toString();
		} else {
			BasicAtom groundBodyAtom = substitute((BasicAtom) bodyAtom, substitution).getRight();
			return  (isFirst ? ", " : "") + (isNegative ? "not " : "") + groundBodyAtom.toString();
		}
	}
}
