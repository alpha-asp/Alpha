/**
 * Copyright (c) 2017-2018, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

import java.util.Set;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class Unification {

	public static Substitution isMoreGeneral(Atom specificAtom, Atom generalAtom) {
		Substitution specialization = new Substitution();
		if (specificAtom.getTerms().size() != generalAtom.getTerms().size()
			|| !specificAtom.getPredicate().equals(generalAtom.getPredicate())) {
			return null;
		}
		for (int i = 0; i < specificAtom.getTerms().size(); i++) {
			final Term specificTerm = specificAtom.getTerms().get(i);
			final Term generalTerm = generalAtom.getTerms().get(i);
			if (!isMoreGeneral(specificTerm, generalTerm, specialization)) {
				return null;
			}
		}
		return specialization;
	}

	private static boolean isMoreGeneral(Term specificTerm, Term generalTerm, Substitution specialization) {
		final Term generalSubstitutedTerm = generalTerm.substitute(specialization);
		if (specificTerm == generalSubstitutedTerm) {
			return true;
		}
		if (generalSubstitutedTerm instanceof VariableTerm) {
			if (specialization.isVariableSet((VariableTerm) generalTerm)) {
				// The variable in generalSubstitutedTerm is set and different from the specificTerm.
				return false;
			} else {
				specialization.put((VariableTerm) generalSubstitutedTerm, specificTerm);
				return true;
			}
		} else if (generalSubstitutedTerm instanceof FunctionTerm) {
			if (!(specificTerm instanceof FunctionTerm)) {
				return false;
			}
			final FunctionTerm generalFunction = (FunctionTerm) generalSubstitutedTerm;
			final FunctionTerm specificFunction = (FunctionTerm) specificTerm;
			if (!generalFunction.getSymbol().equals(specificFunction.getSymbol())
				|| generalFunction.getTerms().size() != specificFunction.getTerms().size()) {
				return false;
			}
			for (int i = 0; i < generalFunction.getTerms().size(); i++) {
				final Term generalFunctionTerm = generalFunction.getTerms().get(i);
				final Term specificFunctionTerm = specificFunction.getTerms().get(i);
				if (!isMoreGeneral(specificFunctionTerm, generalFunctionTerm, specialization)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static Substitution unifyAtoms(Atom left, Atom right) {
		return unifyAtoms(left, right, false);
	}

	/**
	 * Instantiates the general Atom to match the specific one and returns the corresponding substitution.
	 * @param general the general Atom to instantiate.
	 * @param specific the specific Atom.
	 * @return a Substitution sigma such that specific == general.substitute(sigma), returns null if no such sigma exists.
	 */
	public static Substitution instantiate(Atom general, Atom specific) {
		return unifyAtoms(specific, general, true);
	}

	private static Substitution unifyAtoms(Atom left, Atom right, boolean keepLeftAsIs) {
		Set<VariableTerm> leftOccurringVariables = left.getOccurringVariables();
		Set<VariableTerm> rightOccurringVaribles = right.getOccurringVariables();
		boolean leftSmaller = leftOccurringVariables.size() < rightOccurringVaribles.size();
		Set<VariableTerm> smallerSet = leftSmaller ? leftOccurringVariables : rightOccurringVaribles;
		Set<VariableTerm> largerSet = leftSmaller ? rightOccurringVaribles : leftOccurringVariables;
		for (VariableTerm variableTerm : smallerSet) {
			if (largerSet.contains(variableTerm)) {
				throw oops("Left and right atom share variables.");
			}
		}
		Substitution mgu = new Substitution();
		if (!left.getPredicate().equals(right.getPredicate())) {
			return null;
		}
		for (int i = 0; i < left.getPredicate().getArity(); i++) {
			final Term leftTerm = left.getTerms().get(i);
			final Term rightTerm = right.getTerms().get(i);
			if (!unifyTerms(leftTerm, rightTerm, mgu, keepLeftAsIs)) {
				return null;
			}
		}
		return mgu;
	}

	private static boolean unifyTerms(Term left, Term right, Substitution currentSubstitution, boolean keepLeftAsIs) {
		final Term leftSubs = left.substitute(currentSubstitution);
		final Term rightSubs = right.substitute(currentSubstitution);
		if (leftSubs == rightSubs) {
			return true;
		}
		if (!keepLeftAsIs && leftSubs instanceof VariableTerm && !currentSubstitution.isVariableSet((VariableTerm) leftSubs)) {
			currentSubstitution.put((VariableTerm) leftSubs, rightSubs);
			return true;
		}
		if (rightSubs instanceof VariableTerm && !currentSubstitution.isVariableSet((VariableTerm) rightSubs)) {
			currentSubstitution.put((VariableTerm) rightSubs, leftSubs);
			return true;
		}
		if (leftSubs instanceof FunctionTerm && rightSubs instanceof FunctionTerm) {
			final FunctionTerm leftFunction = (FunctionTerm) leftSubs;
			final FunctionTerm rightFunction = (FunctionTerm) rightSubs;
			if (!leftFunction.getSymbol().equals(rightFunction.getSymbol())
				|| leftFunction.getTerms().size() != rightFunction.getTerms().size()) {
				return false;
			}
			for (int i = 0; i < leftFunction.getTerms().size(); i++) {
				final Term leftTerm = leftFunction.getTerms().get(i);
				final Term rightTerm = rightFunction.getTerms().get(i);
				if (!unifyTerms(leftTerm, rightTerm, currentSubstitution, keepLeftAsIs)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
