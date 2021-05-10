package at.ac.tuwien.kr.alpha.grounder.transformation;

import static at.ac.tuwien.kr.alpha.Util.oops;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.NormalProgram;
import at.ac.tuwien.kr.alpha.common.rule.NormalRule;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

/**
 * Transforms rules such that arithmetic terms only occur in comparison predicates.
 * For example p(X+1) :- q(Y/2), r(f(X*2),Y), X-2 = Y*3, X = 0..9. is transformed into
 * p(_A1) :- q(_A2), r(f(_A3),Y), X-2 = Y*3, _A1 = X+1, _A2 = Y/2, _A3 = X*2, X = 0..9.
 *
 * Copyright (c) 2020-2021, the Alpha Team.
 */
public class ArithmeticTermsRewriting extends ProgramTransformation<NormalProgram, NormalProgram> {
	private static final String ARITHMETIC_VARIABLES_PREFIX = "_A";
	private int numArithmeticVariables;

	@Override
	public NormalProgram apply(NormalProgram inputProgram) {
		List<NormalRule> rewrittenRules = new ArrayList<>();
		boolean didRewrite = false;
		for (NormalRule inputProgramRule : inputProgram.getRules()) {
			if (containsArithmeticTermsToRewrite(inputProgramRule)) {
				rewrittenRules.add(rewriteRule(inputProgramRule));
				didRewrite = true;
			} else {
				// Keep rule as-is if no ArithmeticTerm occurs.
				rewrittenRules.add(inputProgramRule);
			}
		}
		if (!didRewrite) {
			return inputProgram;
		}
		// Create new program with rewritten rules.
		return new NormalProgram(rewrittenRules, inputProgram.getFacts(), inputProgram.getInlineDirectives());
	}

	/**
	 * Takes a normal rule and rewrites it such that {@link ArithmeticTerm}s only appear inside {@link at.ac.tuwien.kr.alpha.common.atoms.ComparisonLiteral}s.
	 *
	 * @param inputProgramRule the rule to rewrite.
	 * @return the rewritten rule. Note that a new {@link NormalRule} is returned for every call of this method.
	 */
	private NormalRule rewriteRule(NormalRule inputProgramRule) {
		numArithmeticVariables = 0;	// Reset number of introduced variables for each rule.
		NormalHead rewrittenHead = null;
		List<Literal> rewrittenBodyLiterals = new ArrayList<>();
		// Rewrite head.
		if (!inputProgramRule.isConstraint()) {
			Atom headAtom = inputProgramRule.getHeadAtom();
			if (containsArithmeticTermsToRewrite(headAtom)) {
				rewrittenHead = new NormalHead(rewriteAtom(headAtom, rewrittenBodyLiterals));
			} else {
				rewrittenHead = inputProgramRule.getHead();
			}
		}
		// Rewrite body.
		for (Literal literal : inputProgramRule.getBody()) {
			if (!containsArithmeticTermsToRewrite(literal.getAtom())) {
				// Keep body literal as-is if no ArithmeticTerm occurs.
				rewrittenBodyLiterals.add(literal);
				continue;
			}
			rewrittenBodyLiterals.add(rewriteAtom(literal.getAtom(), rewrittenBodyLiterals).toLiteral(!literal.isNegated()));
		}
		return new NormalRule(rewrittenHead, rewrittenBodyLiterals);
	}

	/**
	 * Checks whether a normal rule contains an {@link ArithmeticTerm} outside of a {@link at.ac.tuwien.kr.alpha.common.atoms.ComparisonLiteral}.
	 *
	 * @param inputProgramRule the rule to check for presence of arithmetic terms outside comparison literals.
	 * @return true if the inputProgramRule contains an {@link ArithmeticTerm} outside of a {@link at.ac.tuwien.kr.alpha.common.atoms.ComparisonLiteral}.
	 */
	private boolean containsArithmeticTermsToRewrite(NormalRule inputProgramRule) {
		if (!inputProgramRule.isConstraint()) {
			Atom headAtom = inputProgramRule.getHeadAtom();
			if (containsArithmeticTermsToRewrite(headAtom)) {
				return true;
			}
		}
		// Check whether body contains an ArithmeticTerm.
		for (Literal literal : inputProgramRule.getBody()) {
			if (containsArithmeticTermsToRewrite(literal.getAtom())) {
				return true;
			}
		}
		return false;
	}

	private Term rewriteArithmeticSubterms(Term term, List<Literal> bodyLiterals) {
		// Keep term as-is if it contains no ArithmeticTerm.
		if (!containsArithmeticTerm(term)) {
			return term;
		}
		// Switch on term type.
		if (term instanceof ArithmeticTerm) {
			VariableTerm replacementVariable = VariableTerm.getInstance(ARITHMETIC_VARIABLES_PREFIX + numArithmeticVariables++);
			bodyLiterals.add(new ComparisonAtom(replacementVariable, term, ComparisonOperator.EQ).toLiteral());
			return replacementVariable;
		} else if (term instanceof VariableTerm || term instanceof ConstantTerm) {
			return term;
		} else if (term instanceof FunctionTerm) {
			List<Term> termList = ((FunctionTerm) term).getTerms();
			List<Term> rewrittenTermList = new ArrayList<>();
			for (Term subterm : termList) {
				rewrittenTermList.add(rewriteArithmeticSubterms(subterm, bodyLiterals));
			}
			return FunctionTerm.getInstance(((FunctionTerm) term).getSymbol(), rewrittenTermList);
		} else {
			throw oops("Rewriting unknown Term type: " + term.getClass());
		}
	}

	private Atom rewriteAtom(Atom atomToRewrite, List<Literal> bodyLiterals) {
		if (atomToRewrite instanceof ComparisonAtom) {
			throw oops("Trying to rewrite ComparisonAtom.");
		}
		List<Term> rewrittenTerms = new ArrayList<>();
		for (Term atomTerm : atomToRewrite.getTerms()) {
			// Rewrite arithmetic term.
			rewrittenTerms.add(rewriteArithmeticSubterms(atomTerm, bodyLiterals));
		}
		return atomToRewrite.withTerms(rewrittenTerms);
	}

	private boolean containsArithmeticTermsToRewrite(Atom atom) {
		// ComparisonAtom needs no rewriting.
		if (atom instanceof ComparisonAtom) {
			return false;
		}
		for (Term term : atom.getTerms()) {
			if (containsArithmeticTerm(term)) {
				return true;
			}
		}
		return false;
	}

	private boolean containsArithmeticTerm(Term term) {
		// Note: this check probably should be part of the Term interface and done by subtype polymorphism.
		if (term instanceof ArithmeticTerm) {
			return true;
		} else if (term instanceof ConstantTerm || term instanceof VariableTerm) {
			return false;
		} else if (term instanceof FunctionTerm) {
			for (Term subterm : ((FunctionTerm) term).getTerms()) {
				if (containsArithmeticTerm(subterm)) {
					return true;
				}
			}
			return false;
		} else if (term instanceof IntervalTerm) {
			return containsArithmeticTerm(((IntervalTerm) term).getLowerBoundTerm()) || containsArithmeticTerm(((IntervalTerm) term).getUpperBoundTerm());
		} else {
			throw oops("Unexpected term type: " + term.getClass());
		}
	}
}
