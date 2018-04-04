package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static at.ac.tuwien.kr.alpha.Util.oops;

public class Substitution {
	private TreeMap<VariableTerm, Term> substitution;
	private final TreeMap<VariableTerm, List<VariableTerm>> rightHandVariableOccurrences;

	private Substitution(TreeMap<VariableTerm, Term> substitution, TreeMap<VariableTerm, List<VariableTerm>> rightHandVariableOccurrences) {
		if (substitution == null) {
			throw oops("Substitution is null.");
		}
		this.substitution = substitution;
		this.rightHandVariableOccurrences = rightHandVariableOccurrences;
	}

	public Substitution() {
		this(new TreeMap<>(), new TreeMap<>());
	}

	public Substitution(Substitution clone) {
		this(new TreeMap<>(clone.substitution), new TreeMap<>(clone.rightHandVariableOccurrences));
	}

	public Substitution extendWith(Substitution extension) {
		for (Map.Entry<VariableTerm, Term> extensionVariable : extension.substitution.entrySet()) {
			this.put(extensionVariable.getKey(), extensionVariable.getValue());
		}
		return this;
	}

	/**
	 * Computes the unifier of the atom and the instance and stores it in the variable substitution.
	 * @param atom the body atom to unify
	 * @param instance the ground instance
	 * @param substitution if the atom does not unify, this is left unchanged.
	 * @return true if the atom and the instance unify. False otherwise
	 */
	public static Substitution unify(Atom atom, Instance instance, Substitution substitution) {
		for (int i = 0; i < instance.terms.size(); i++) {
			if (instance.terms.get(i) == atom.getTerms().get(i) ||
				substitution.unifyTerms(atom.getTerms().get(i), instance.terms.get(i))) {
				continue;
			}
			return null;
		}
		return substitution;
	}

	/**
	 * Checks if the left possible non-ground term unifies with the ground term.
	 * @param termNonGround
	 * @param termGround
	 * @return
	 */
	public boolean unifyTerms(Term termNonGround, Term termGround) {
		if (termNonGround == termGround) {
			// Both terms are either the same constant or the same variable term
			return true;
		} else if (termNonGround instanceof ConstantTerm) {
			// Since right term is ground, both terms differ
			return false;
		} else if (termNonGround instanceof VariableTerm) {
			VariableTerm variableTerm = (VariableTerm)termNonGround;
			// Left term is variable, bind it to the right term.
			Term bound = eval(variableTerm);

			if (bound != null) {
				// Variable is already bound, return true if binding is the same as the current ground term.
				return termGround == bound;
			}

			substitution.put(variableTerm, termGround);
			return true;
		} else if (termNonGround instanceof FunctionTerm && termGround instanceof FunctionTerm) {
			// Both terms are function terms
			FunctionTerm ftNonGround = (FunctionTerm) termNonGround;
			FunctionTerm ftGround = (FunctionTerm) termGround;

			if (!(ftNonGround.getSymbol().equals(ftGround.getSymbol()))) {
				return false;
			}

			// Iterate over all subterms of both function terms
			for (int i = 0; i < ftNonGround.getTerms().size(); i++) {
				if (!unifyTerms(ftNonGround.getTerms().get(i), ftGround.getTerms().get(i))) {
					return false;
				}
			}

			return true;
		}
		return false;
	}

	/**
	 * This method should be used to obtain the {@link Term} to be used in place of
	 * a given {@link VariableTerm} under this substitution.
	 *
	 * @param variableTerm the variable term to substitute, if possible
	 * @return a constant term if the substitution contains the given variable, {@code null} otherwise.
	 */
	public Term eval(VariableTerm variableTerm) {
		return this.substitution.get(variableTerm);
	}

	public <T extends Comparable<T>> Term put(VariableTerm variableTerm, Term groundTerm) {
		// TODO: if groundTerm is not ground, we need to store this for right-hand side reverse-lookup
		// TODO: For each variableTerm we must check whether it already occurs at the right hand side of some substitution here and update it.
		if (!groundTerm.isGround()) {
			for (VariableTerm rightHandVariable : groundTerm.getOccurringVariables()) {
				rightHandVariableOccurrences.putIfAbsent(rightHandVariable, new ArrayList<>());
				rightHandVariableOccurrences.get(rightHandVariable).add(variableTerm);
			}
		}
		// Note: We're destroying type information here.
		Term ret = substitution.put(variableTerm, groundTerm);

		// Check if the just-assigned variable occurs somewhere in the right-hand side already.
		List<VariableTerm> rightHandOccurrences = rightHandVariableOccurrences.get(variableTerm);
		if (rightHandOccurrences != null) {
			// Replace all occurrences on the right-hand side with the just-assigned term.
			for (VariableTerm rightHandOccurrence : rightHandOccurrences) {
				// Substitute the right hand where this assigned variable occurs with the new value and store it.
				Term previousRightHand = substitution.get(rightHandOccurrence);
				if (previousRightHand == null) {
					// Variable does not occur on the lef-hand side, skip.
					continue;
				}
				substitution.put(rightHandOccurrence, previousRightHand.substitute(this));
			}
		}

		return ret;
	}

	public boolean isEmpty() {
		return substitution.isEmpty();
	}

	public boolean isVariableSet(VariableTerm variable) {
		return substitution.get(variable) != null;
	}

	/**
	 * Prints the variable substitution in a uniform way (sorted by variable names).
	 *
	 * @return
	 */
	@Override
	public String toString() {
		final StringBuilder ret = new StringBuilder("{");
		boolean isFirst = true;
		for (Map.Entry<VariableTerm, Term> e : substitution.entrySet()) {
			if (isFirst) {
				isFirst = false;
			} else {
				ret.append(",");
			}
			ret.append(e.getKey()).append("->").append(e.getValue());
		}
		ret.append("}");
		return ret.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Substitution that = (Substitution) o;

		return substitution != null ? substitution.equals(that.substitution) : that.substitution == null;
	}

	@Override
	public int hashCode() {
		return substitution != null ? substitution.hashCode() : 0;
	}

	public static Substitution findEqualizingSubstitution(BasicAtom generalAtom, BasicAtom specificAtom) {
		// Some hard examples:
		// p(A,f(A)) with p(X,A) where variable occurs as subterm again and where some variable is shared!
		// First, rename all variables in the specific
		if (!generalAtom.getPredicate().equals(specificAtom.getPredicate())) {
			return null;
		}
		Substitution specializingSubstitution = new Substitution();
		String renamedVariablePrefix = "_Vrenamed_";	// Pick prefix guaranteed to not occur in generalAtom.
		for (int i = 0; i < generalAtom.getPredicate().getArity(); i++) {
			specializingSubstitution = specializeSubstitution(specializingSubstitution,
				generalAtom.getTerms().get(i),
				specificAtom.getTerms().get(i).renameVariables(renamedVariablePrefix));
			if (specializingSubstitution == null) {
				return null;
			}
		}
		return specializingSubstitution;
	}

	private static Substitution specializeSubstitution(Substitution substitution, Term generalTerm, Term specificTerm) {
		if (generalTerm == specificTerm) {
			return substitution;
		}
		// If the general term is a variable, check its current substitution and see whether this matches the specific term.
		if (generalTerm instanceof VariableTerm) {
			Term substitutedGeneralTerm = substitution.eval((VariableTerm) generalTerm);
			// If the variable is not bound already, bind it to the specific term.
			if (substitutedGeneralTerm == null) {
				substitution.put((VariableTerm) generalTerm, specificTerm.substitute(substitution));
				// TODO: check in case we have X -> Y already and are adding Y -> c, if X -> Y must be replaced with X -> c.
				for (Map.Entry<VariableTerm, Term> variableSubstitution : substitution.substitution.entrySet()) {
					if (variableSubstitution.getValue() instanceof VariableTerm
						&& variableSubstitution.getValue() == generalTerm) {
						System.out.println("Problem.");
					}
				}
				return substitution;
			}
			// The variable is bound, check whether its result is exactly the specific term.
			// Note: checking whether the bounded term is more general than the specific one would yield
			//       wrong results, e.g.: p(X,X) and p(f(A),f(g(B))) are incomparable, but f(A) is more general than f(g(B)).
			if (substitutedGeneralTerm == specificTerm) {
				return substitution;
			} else {
				return null;
			}
		}
		if (generalTerm instanceof FunctionTerm) {
			// Check if both given terms are function terms.
			if (!(specificTerm instanceof FunctionTerm)) {
				return null;
			}
			// Check that they are the same function.
			FunctionTerm fgeneralTerm = (FunctionTerm) generalTerm;
			FunctionTerm fspecificTerm = (FunctionTerm) specificTerm;
			if (!fgeneralTerm.getSymbol().equals(fspecificTerm.getSymbol())
				|| fgeneralTerm.getTerms().size() != fspecificTerm.getTerms().size()) {
				return null;
			}
			// Check/specialize their subterms.
			for (int i = 0; i < fgeneralTerm.getTerms().size(); i++) {
				substitution = specializeSubstitution(substitution, fgeneralTerm.getTerms().get(i), fspecificTerm.getTerms().get(i));
				if (substitution == null) {
					return null;
				}
			}
			return substitution;
		}
		if (generalTerm instanceof ConstantTerm) {
			// Equality was already checked above, so terms are different.
			return null;
		}
		throw new RuntimeException("Trying to specialize a term that is neither variable, constant, nor function. Should not happen");
	}

	/**
	 * Returns true if the left substitution is more precise than the right substitution.
	 * @param left
	 * @param right
	 * @return
	 */
	public static boolean isMorePrecise(Substitution left, Substitution right) {
		// TODO: create new atom over  the variables of the left substitution and use the specialize substitution check.
		int atomsize = left.substitution.size();
		List<Term> termListLeft = new ArrayList<>(atomsize);
		for (Map.Entry<VariableTerm, Term> leftVariableEntry : left.substitution.entrySet()) {
			termListLeft.add(leftVariableEntry.getKey());
		}
		String checkFunctionSymbol = "_f";
		FunctionTerm generalFunctionTerm = FunctionTerm.getInstance(checkFunctionSymbol, termListLeft);
		Term leftSubstitutedFunctionTerm = generalFunctionTerm.substitute(left);
		Substitution substitution = specializeSubstitution(new Substitution(right), generalFunctionTerm, leftSubstitutedFunctionTerm);
		return substitution != null;


		/*for (Map.Entry<VariableTerm, Term> variableSubstitution : left.substitution.entrySet()) {
			final VariableTerm variable = variableSubstitution.getKey();
			final Term leftSubstitution = variableSubstitution.getValue();
			Term rightSubstitution = right.substitution.get(variable);
			// Skip variables that are only assigned in left substitution.
			if (rightSubstitution == null) {
				continue;
			}
			// Check if left substituted term is more precise than right.

			if (!isMorePreciseTerm(leftSubstitution, rightSubstitution)) {
				return false;
			}
		}
		return true;*/
	}

	private static boolean isMorePreciseTerm(Term left, Term right) {
		// TODO: this probably fails for f(X,Y) and f(X,X)!
		if (left instanceof ConstantTerm) {
			if (right instanceof VariableTerm || left.equals(right)) {
				return true;
			}
			return false;

		} else if (left instanceof FunctionTerm) {
			if (!(right instanceof FunctionTerm)) {
				return false;
			} else {
				FunctionTerm leftFt = (FunctionTerm) left;
				FunctionTerm rightFt = (FunctionTerm) right;
				if (!leftFt.getSymbol().equals(rightFt.getSymbol())
					|| leftFt.getTerms().size() != rightFt.getTerms().size()) {
					return false;
				}
				for (int i = 0; i < leftFt.getTerms().size(); i++) {
					Term leftTerm = leftFt.getTerms().get(i);
					Term rightTerm = rightFt.getTerms().get(i);
					if (!isMorePreciseTerm(leftTerm, rightTerm)) {
						return false;
					}
				}
				return true;
			}
		} else if (left instanceof VariableTerm) {
			return right instanceof VariableTerm;
		} else {
			throw oops("Unknown Term type.");
		}
	}

	/**
	 * Merge substitution right into left as used in the AnalyzeUnjustified.
	 * Left mappings are seen as equalities, i.e.,
	 * if left has A -> B and right has A -> t then the result will have A -> t and B -> t.
	 * If both substitutions are inconsistent, i.e., A -> t1 in left and A -> t2 in right, then null is returned.
	 *
	 * @param left
	 * @param right
	 * @return
	 */
	public static Substitution mergeIntoLeft(Substitution left, Substitution right) {
		// Note: we assume both substitutions are free of chains, i.e., no A->B, B->C but A->C, B->C.
		Substitution ret = new Substitution(left);
		for (Map.Entry<VariableTerm, Term> mapping : right.substitution.entrySet()) {
			VariableTerm variable = mapping.getKey();
			Term term = mapping.getValue();
			// If variable is unset, simply add.
			if (!ret.isVariableSet(variable)) {
				ret.put(variable, term);
				continue;
			}
			// Variable is already set.
			Term setTerm = ret.eval(variable);
			if (setTerm instanceof VariableTerm) {
				// Variable maps to another variable in left.
				// Add a new mapping of the setTerm variable into our right-assigned term.
				ret.put((VariableTerm) setTerm, term);
				// Note: Substitution.put takes care of resolving the chain variable->setTerm->term.
				continue;
			}
			// Check for inconsistency.
			if (setTerm != term) {
				return null;
			}
			// Now setTerm equals term, no action needed.
		}
		return ret;
	}
}
